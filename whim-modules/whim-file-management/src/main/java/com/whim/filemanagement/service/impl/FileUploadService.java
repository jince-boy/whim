package com.whim.filemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.FileStorageException;
import com.whim.core.exception.TooManyRequestsException;
import com.whim.core.utils.IdUtils;
import com.whim.file.FileStorageService;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.exception.MissingUploadSessionException;
import com.whim.file.model.MetaData;
import com.whim.file.model.PartUploadSignature;
import com.whim.file.model.StoragePart;
import com.whim.file.storage.MultipartFileStorage;
import com.whim.filemanagement.config.FileUploadProperties;
import com.whim.filemanagement.mapper.FileUploadTaskMapper;
import com.whim.filemanagement.model.dto.AddUploadTaskDTO;
import com.whim.filemanagement.model.entity.FileUploadPart;
import com.whim.filemanagement.model.entity.FileUploadTask;
import com.whim.filemanagement.model.entity.FileUploadOwner;
import com.whim.filemanagement.model.vo.UploadOptionsVO;
import com.whim.filemanagement.model.vo.UploadTaskVO;
import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 个人上传任务状态机；数据库行锁协调多实例，存储事实用于恢复不确定结果。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "file.storage", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileUploadService extends ServiceImpl<FileUploadTaskMapper, FileUploadTask> {
    private static final String CREATED = "CREATED";
    private static final String UPLOADING = "UPLOADING";
    private static final String COMPLETING = "COMPLETING";
    private static final String COMPLETED = "COMPLETED";
    private static final String CANCELING = "CANCELING";
    private static final String CANCELED = "CANCELED";
    private static final String EXPIRED = "EXPIRED";
    private static final String FAILED = "FAILED";
    private final FileStorageService fileStorageService;
    private final FileUploadPartService partService;
    private final FileUploadOwnerService ownerService;
    private final AuthenticationContext authenticationContext;
    private final FileUploadProperties properties;
    private final TransactionTemplate fileUploadTransactionTemplate;

    /** 分页查询当前用户的任务及已完成文件，不扩大到角色的部门数据范围。 */
    public PageDataVO<UploadTaskVO> getUploadTasks(PageQueryDTO query) {
        var page = lambdaQuery().eq(FileUploadTask::getOwnerId, authenticationContext.getUserId())
                .orderByDesc(FileUploadTask::getCreateTime).page(query.<FileUploadTask>getPage());
        return new PageDataVO<>(page.convert(task -> toView(task, List.of())));
    }

    /** 返回前端实际生效的容量和并发约束。 */
    public UploadOptionsVO getUploadOptions() {
        return new UploadOptionsVO(properties.getMaximumFileSize().toBytes(),
                properties.getPartSize().toBytes(), properties.getConcurrency());
    }

    /** 读取任务及真实分片；完成或终止后不再访问已经消失的上传会话。 */
    public UploadTaskVO getUploadTask(String taskId) {
        return inTransaction(ignored -> {
            FileUploadTask task = getLockedTask(taskId, authenticationContext.getUserId(), false);
            List<Integer> uploaded = List.of();
            if (UPLOADING.equals(task.getStatus()) && task.getExpiresAt().isAfter(LocalDateTime.now())) {
                try {
                    uploaded = getVerifiedParts(task, false).stream().map(StoragePart::getPartNumber).toList();
                } catch (MissingUploadSessionException missing) {
                    task.setStatus(FAILED);
                    updateTask(task);
                }
            }
            return toView(task, uploaded);
        });
    }

    /** 幂等创建任务，先提交可恢复记录，再初始化外部存储。 */
    public UploadTaskVO addUploadTask(AddUploadTaskDTO request) {
        validateRequest(request);
        Long ownerId = authenticationContext.getUserId();
        // 在任务事务之外幂等创建锁行；竞争插入不影响后续事务。
        if (ownerService.getById(ownerId) == null) {
            FileUploadOwner owner = new FileUploadOwner();
            owner.setOwnerId(ownerId);
            try {
                inTransaction(ignored -> ownerService.save(owner));
            } catch (DuplicateKeyException ignored) {
                // 另一个请求已经创建同一用户的锁行。
            }
        }
        FileUploadTask task;
        try {
            task = inTransaction(ignored -> {
                ownerService.lambdaQuery().eq(FileUploadOwner::getOwnerId, ownerId)
                        .last("FOR UPDATE NOWAIT").one();
                FileUploadTask existing = lambdaQuery().eq(FileUploadTask::getOwnerId, ownerId)
                        .eq(FileUploadTask::getRequestId, request.getRequestId()).one();
                if (existing != null) {
                    return existing;
                }
                List<FileUploadTask> pending = lambdaQuery().eq(FileUploadTask::getOwnerId, ownerId)
                        .in(FileUploadTask::getStatus, CREATED, UPLOADING, COMPLETING, CANCELING).list();
                long reserved = pending.stream().mapToLong(FileUploadTask::getFileSize).sum();
                if (pending.size() >= properties.getMaximumActiveTasks()
                        || request.getFileSize() > properties.getMaximumPendingBytes().toBytes() - reserved) {
                    throw new TooManyRequestsException("待上传任务或容量已达上限，请先完成或取消已有任务");
                }
                var storage = fileStorageService.getStorage(null, null);
                if (!(storage instanceof MultipartFileStorage)) {
                    throw new IllegalArgumentException("默认存储未实现分片上传");
                }
                FileUploadTask created = new FileUploadTask();
                created.setId(IdUtils.uuid32());
                created.setOwnerId(ownerId);
                created.setRequestId(request.getRequestId());
                created.setFileName(request.getFileName());
                created.setFileSize(request.getFileSize());
                created.setContentType(request.getContentType());
                created.setFingerprint(request.getFingerprint());
                created.setPartSize(properties.getPartSize().toBytes());
                created.setPartCount(Math.toIntExact((request.getFileSize() - 1) / created.getPartSize() + 1));
                created.setPlatform(storage.getPlatform());
                created.setConfigName(storage.getConfigName());
                created.setObjectKey("uploads/" + ownerId + "/" + created.getId());
                created.setStatus(CREATED);
                created.setCleaned(false);
                created.setCreateTime(LocalDateTime.now());
                created.setUpdateTime(created.getCreateTime());
                created.setExpiresAt(created.getCreateTime().plus(properties.getTaskLifetime()));
                created.setSignatureExpiresAt(created.getCreateTime());
                save(created);
                return created;
            });
        } catch (DuplicateKeyException exception) {
            task = lambdaQuery().eq(FileUploadTask::getOwnerId, ownerId)
                    .eq(FileUploadTask::getRequestId, request.getRequestId()).one();
            if (task == null) {
                throw exception;
            }
        }
        if (!Objects.equals(task.getFileName(), request.getFileName())
                || !Objects.equals(task.getFileSize(), request.getFileSize())
                || !Objects.equals(task.getFingerprint(), request.getFingerprint())
                || !Objects.equals(task.getContentType(), request.getContentType())) {
            throw new IllegalArgumentException("请求标识已用于其他文件");
        }
        return initializeUpload(task.getId(), ownerId);
    }

    /** 恢复已登记但尚未初始化成功的任务，不延长原有效期。 */
    public UploadTaskVO resumeUpload(String taskId) {
        return initializeUpload(taskId, authenticationContext.getUserId());
    }

    /** 固定分片内容并生成签名，本地模式只登记约束，返回空 URL。 */
    public PartUploadSignature getPartUploadSignature(String taskId, int partNumber,
                                                       String contentMd5, String contentSha256) {
        byte[] checksum = Base64.getDecoder().decode(contentMd5);
        if (checksum.length != 16 || !Base64.getEncoder().encodeToString(checksum).equals(contentMd5)) {
            throw new IllegalArgumentException("分片校验值格式无效");
        }
        byte[] strongChecksum = Base64.getDecoder().decode(contentSha256);
        if (strongChecksum.length != 32
                || !Base64.getEncoder().encodeToString(strongChecksum).equals(contentSha256)) {
            throw new IllegalArgumentException("分片SHA-256格式无效");
        }
        return inTransaction(ignored -> {
            FileUploadTask task = getLockedTask(taskId, authenticationContext.getUserId(), false);
            checkUploading(task);
            long size = getPartSize(task, partNumber);
            FileUploadPart part = partService.lambdaQuery().eq(FileUploadPart::getTaskId, taskId)
                    .eq(FileUploadPart::getPartNumber, partNumber).one();
            if (part == null) {
                part = new FileUploadPart();
                part.setId(IdUtils.uuid32());
                part.setTaskId(taskId);
                part.setPartNumber(partNumber);
                part.setPartSize(size);
                part.setContentMd5(contentMd5);
                part.setContentSha256(contentSha256);
                partService.save(part);
            } else if (!part.getContentMd5().equals(contentMd5) || !part.getContentSha256().equals(contentSha256)) {
                throw new IllegalArgumentException("该分片已绑定其他内容，请重新创建上传任务");
            }
            MultipartFileStorage storage = getStorage(task);
            LocalDateTime expires = LocalDateTime.now().plus(properties.getSignatureLifetime());
            task.setSignatureExpiresAt(expires.isBefore(task.getSignatureExpiresAt())
                    ? task.getSignatureExpiresAt() : expires);
            updateTask(task);
            return storage.supportsDirectUpload()
                    ? storage.getPartUploadSignature(task.getObjectKey(), task.getUploadId(),
                    partNumber, size, contentMd5, contentSha256, properties.getSignatureLifetime())
                    : new PartUploadSignature(null, Map.of());
        });
    }

    /** 本地分片使用任务共享锁及单片排他锁，允许不同分片并行、阻止与取消或合并交错。 */
    public void uploadPart(String taskId, int partNumber, InputStream input, long size) {
        inTransaction(ignored -> {
            FileUploadTask task = getLockedTask(taskId, authenticationContext.getUserId(), true);
            checkUploading(task);
            if (size != getPartSize(task, partNumber)) {
                throw new IllegalArgumentException("分片大小与任务约束不一致");
            }
            MultipartFileStorage storage = getStorage(task);
            if (storage.supportsDirectUpload()) {
                throw new IllegalArgumentException("当前任务需要使用签名地址直传");
            }
            FileUploadPart part = partService.lambdaQuery().eq(FileUploadPart::getTaskId, taskId)
                    .eq(FileUploadPart::getPartNumber, partNumber).last("FOR UPDATE NOWAIT").one();
            if (part == null) {
                throw new IllegalArgumentException("请先登记分片内容校验值");
            }
            storage.uploadPart(task.getObjectKey(), task.getUploadId(), partNumber, input, size,
                    part.getContentMd5(), part.getContentSha256());
            return null;
        });
    }

    /** 先持久化合并意图，再执行外部合并；超时或宕机后可按相同任务继续确认。 */
    public UploadTaskVO completeUpload(String taskId) {
        Long ownerId = authenticationContext.getUserId();
        inTransaction(ignored -> {
            FileUploadTask task = getLockedTask(taskId, ownerId, false);
            if (COMPLETED.equals(task.getStatus()) || COMPLETING.equals(task.getStatus())) {
                return null;
            }
            checkUploading(task);
            getVerifiedParts(task, true);
            task.setStatus(COMPLETING);
            updateTask(task);
            return null;
        });
        UploadTaskVO result = finishUpload(taskId, ownerId);
        if (!COMPLETED.equals(result.getStatus())) {
            throw new FileStorageException("上传分片已丢失，请重新创建上传任务");
        }
        return result;
    }

    /** 持久化取消意图后清理；合并中的任务必须先恢复结果，不允许猜测后删除。 */
    public void cancelUpload(String taskId) {
        Long ownerId = authenticationContext.getUserId();
        inTransaction(ignored -> {
            FileUploadTask task = getLockedTask(taskId, ownerId, false);
            if (COMPLETED.equals(task.getStatus()) || COMPLETING.equals(task.getStatus())) {
                throw new IllegalArgumentException("文件已完成或正在确认合并结果，不能取消");
            }
            if (!CANCELED.equals(task.getStatus()) && !EXPIRED.equals(task.getStatus()) && !FAILED.equals(task.getStatus())) {
                task.setStatus(CANCELING);
                updateTask(task);
            }
            return null;
        });
        cleanUpload(taskId, ownerId);
    }

    /** 按归属打开已完成文件，调用方必须关闭返回的下载处理器。 */
    public DownloadHandler getFileDownloadHandler(String taskId) {
        FileUploadTask task = lambdaQuery().eq(FileUploadTask::getId, taskId)
                .eq(FileUploadTask::getOwnerId, authenticationContext.getUserId()).one();
        if (task == null || !COMPLETED.equals(task.getStatus())) {
            throw new AccessDeniedException("文件不存在或尚未完成上传");
        }
        return getStorage(task).download(task.getObjectKey());
    }

    /** 每次最多处理二十个任务；行锁冲突跳过，不阻塞正在上传的请求。 */
    @Scheduled(fixedDelayString = "${file.upload.cleanup-delay:60000}", initialDelay = 60000)
    public void recoverUploads() {
        LocalDateTime now = LocalDateTime.now();
        List<FileUploadTask> candidates = lambdaQuery().eq(FileUploadTask::getCleaned, false)
                .lt(FileUploadTask::getUpdateTime, now.minusMinutes(1))
                .and(query -> query.in(FileUploadTask::getStatus, COMPLETING, CANCELING, COMPLETED, CANCELED, EXPIRED, FAILED)
                        .or().le(FileUploadTask::getExpiresAt, now))
                .orderByAsc(FileUploadTask::getUpdateTime).last("LIMIT 20").list();
        for (FileUploadTask task : candidates) {
            try {
                if (COMPLETING.equals(task.getStatus())) {
                    finishUpload(task.getId(), null);
                } else {
                    cleanUpload(task.getId(), null);
                }
            } catch (TooManyRequestsException ignored) {
                // 其他实例或请求正在处理同一任务，下次调度再检查。
            } catch (Exception exception) {
                log.warn("恢复文件上传任务失败，任务标识：{}", task.getId(), exception);
                // 失败任务轮换到队尾，避免一条长期故障任务饿死其他清理任务。
                lambdaUpdate().eq(FileUploadTask::getId, task.getId())
                        .eq(FileUploadTask::getUpdateTime, task.getUpdateTime())
                        .set(FileUploadTask::getUpdateTime, LocalDateTime.now()).update();
            }
        }
    }

    /** 初始化失败保留 CREATED 记录，客户端以同一请求标识重试。 */
    private UploadTaskVO initializeUpload(String taskId, Long ownerId) {
        return inTransaction(ignored -> {
            FileUploadTask task = getLockedTask(taskId, ownerId, false);
            if (CREATED.equals(task.getStatus())) {
                if (!task.getExpiresAt().isAfter(LocalDateTime.now())) {
                    throw new IllegalArgumentException("上传任务已过期");
                }
                task.setUploadId(getStorage(task).initiateUpload(
                        task.getObjectKey(), task.getContentType(), task.getId()));
                task.setStatus(UPLOADING);
                updateTask(task);
            }
            return toView(task, List.of());
        });
    }

    /** 最终对象存在时恢复数据库状态；对象不存在时使用真实清单重试合并。 */
    private UploadTaskVO finishUpload(String taskId, Long ownerId) {
        return inTransaction(ignored -> {
            FileUploadTask task = getLockedTask(taskId, ownerId, false);
            if (COMPLETED.equals(task.getStatus())) {
                return toView(task, List.of());
            }
            if (!COMPLETING.equals(task.getStatus())) {
                throw new IllegalArgumentException("上传任务不处于合并状态");
            }
            MultipartFileStorage storage = getStorage(task);
            if (!storage.exists(task.getObjectKey())) {
                try {
                    storage.completeUpload(task.getObjectKey(), task.getUploadId(), getVerifiedParts(task, true),
                            task.getFileSize(), task.getContentType());
                } catch (MissingUploadSessionException missing) {
                    // 排除先前已发出的合并刚刚完成，再把明确丢失的会话标记失败。
                    if (!storage.exists(task.getObjectKey())) {
                        task.setStatus(FAILED);
                        updateTask(task);
                        return toView(task, List.of());
                    }
                }
            }
            MetaData metadata = storage.getFileMetaData(task.getObjectKey());
            if (!Objects.equals(metadata.getFileSize(), task.getFileSize())) {
                throw new FileStorageException("最终文件大小校验失败，已保留任务供排查");
            }
            task.setStatus(COMPLETED);
            updateTask(task);
            return toView(task, List.of());
        });
    }

    /** 清理失败保留终止意图；签名过期并留出在途请求窗口后再次清理，才标记完成。 */
    private void cleanUpload(String taskId, Long ownerId) {
        inTransaction(ignored -> {
            FileUploadTask task = getLockedTask(taskId, ownerId, false);
            if (task.getCleaned() || COMPLETING.equals(task.getStatus())) {
                return null;
            }
            boolean expired = !task.getExpiresAt().isAfter(LocalDateTime.now());
            if (!List.of(COMPLETED, CANCELING, CANCELED, EXPIRED, FAILED).contains(task.getStatus()) && !expired) {
                return null;
            }
            MultipartFileStorage storage = getStorage(task);
            if (task.getUploadId() != null) {
                storage.abortUpload(task.getObjectKey(), task.getUploadId());
            } else if (!storage.supportsDirectUpload()) {
                storage.abortUpload(task.getObjectKey(), task.getId());
            }
            if (!COMPLETED.equals(task.getStatus()) && !FAILED.equals(task.getStatus())) {
                task.setStatus(CANCELING.equals(task.getStatus()) || CANCELED.equals(task.getStatus()) ? CANCELED : EXPIRED);
            }
            if (LocalDateTime.now().isAfter(task.getSignatureExpiresAt().plusMinutes(10))) {
                task.setCleaned(true);
                partService.lambdaUpdate().eq(FileUploadPart::getTaskId, taskId).remove();
            }
            updateTask(task);
            return null;
        });
    }

    /** 行锁始终使用任务主键及可选的当前用户条件；空用户仅供私有恢复流程调用。 */
    private FileUploadTask getLockedTask(String taskId, Long ownerId, boolean shared) {
        FileUploadTask task = lambdaQuery().eq(FileUploadTask::getId, taskId)
                .eq(ownerId != null, FileUploadTask::getOwnerId, ownerId)
                .last(shared ? "FOR SHARE NOWAIT" : "FOR UPDATE NOWAIT").one();
        if (task == null) {
            throw new AccessDeniedException("上传任务不存在或无权访问");
        }
        return task;
    }

    /** 事务中的锁冲突对外返回可重试的429，异常时阶段内的数据库写入全部回滚。 */
    private <T> T inTransaction(Function<org.springframework.transaction.TransactionStatus, T> operation) {
        try {
            return fileUploadTransactionTemplate.execute(operation::apply);
        } catch (PessimisticLockingFailureException exception) {
            throw new TooManyRequestsException("上传任务正在处理，请稍后重试");
        } catch (DataAccessException exception) {
            Throwable cause = exception;
            for (int depth = 0; cause != null && depth < 10; depth++, cause = cause.getCause()) {
                if (cause instanceof java.sql.SQLException sql && sql.getErrorCode() == 3572) {
                    throw new TooManyRequestsException("上传任务正在处理，请稍后重试");
                }
            }
            throw exception;
        }
    }

    /** 对存储事实逐项核对序号、大小及本地摘要，完成时要求分片连续且齐全。 */
    private List<StoragePart> getVerifiedParts(FileUploadTask task, boolean requireComplete) {
        List<FileUploadPart> declared = partService.lambdaQuery().eq(FileUploadPart::getTaskId, task.getId()).list();
        Map<Integer, FileUploadPart> manifest = declared.stream()
                .collect(Collectors.toMap(FileUploadPart::getPartNumber, Function.identity()));
        MultipartFileStorage storage = getStorage(task);
        List<StoragePart> actual = storage.getUploadedParts(task.getObjectKey(), task.getUploadId()).stream()
                .sorted(Comparator.comparingInt(StoragePart::getPartNumber)).toList();
        int previous = 0;
        for (StoragePart part : actual) {
            FileUploadPart expected = manifest.get(part.getPartNumber());
            if (expected == null || part.getPartNumber() <= previous
                    || part.getSize() != getPartSize(task, part.getPartNumber())) {
                throw new IllegalArgumentException("存储端分片与任务约束不一致，请取消后重新上传");
            }
            if (!storage.supportsDirectUpload()) {
                String digest = Base64.getEncoder().encodeToString(java.util.HexFormat.of().parseHex(part.getEtag()));
                if (!expected.getContentMd5().equals(digest)) {
                    throw new IllegalArgumentException("本地分片内容校验失败");
                }
            }
            if ((!storage.supportsDirectUpload() || "minio".equals(task.getPlatform()))
                    && !expected.getContentSha256().equals(part.getContentSha256())) {
                throw new IllegalArgumentException("分片SHA-256与登记内容不一致");
            }
            previous = part.getPartNumber();
        }
        if (requireComplete && actual.size() != task.getPartCount()) {
            throw new IllegalArgumentException("文件分片尚未全部上传");
        }
        return actual;
    }

    /** 获取任务创建时绑定的实例，配置变更不能偷偷切换已有任务的存储。 */
    private MultipartFileStorage getStorage(FileUploadTask task) {
        var storage = fileStorageService.getStorage(task.getPlatform(), task.getConfigName());
        if (storage instanceof MultipartFileStorage multipart) {
            return multipart;
        }
        throw new FileStorageException("任务绑定的存储已不支持分片上传");
    }

    /** 校验可写状态和固定有效期，重复请求不能无限延长临时资源寿命。 */
    private void checkUploading(FileUploadTask task) {
        if (!UPLOADING.equals(task.getStatus()) || !task.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("上传任务已过期或不处于上传状态");
        }
    }

    /** 计算指定分片的唯一合法长度，最后一片允许不足标准大小。 */
    private long getPartSize(FileUploadTask task, int number) {
        if (number < 1 || number > task.getPartCount()) {
            throw new IllegalArgumentException("分片编号超出范围");
        }
        return Math.min(task.getPartSize(), task.getFileSize() - (number - 1L) * task.getPartSize());
    }

    /** 更新阶段状态及恢复调度时间。 */
    private void updateTask(FileUploadTask task) {
        task.setUpdateTime(LocalDateTime.now());
        if (!updateById(task)) {
            throw new FileStorageException("保存上传任务状态失败");
        }
    }

    /** 校验名字、大小和 MIME，禁止客户端控制对象路径。 */
    private void validateRequest(AddUploadTaskDTO request) {
        if (request.getFileSize() < 1 || request.getFileSize() > properties.getMaximumFileSize().toBytes()) {
            throw new IllegalArgumentException("文件大小超出上传限制");
        }
        if (request.getFileName().contains("/") || request.getFileName().contains("\\")
                || request.getFileName().chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("文件名不能包含目录或控制字符");
        }
        MediaType contentType;
        try {
            contentType = MediaType.parseMediaType(request.getContentType());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("文件内容类型格式无效", exception);
        }
        if (contentType.isWildcardType() || contentType.isWildcardSubtype()
                || request.getContentType().chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("文件内容类型无效");
        }
    }

    /** 构造公开视图，避免将实体及内部存储标识直接序列化。 */
    private UploadTaskVO toView(FileUploadTask task, List<Integer> uploaded) {
        UploadTaskVO view = new UploadTaskVO();
        view.setId(task.getId());
        view.setFileName(task.getFileName());
        view.setFileSize(task.getFileSize());
        view.setContentType(task.getContentType());
        view.setFingerprint(task.getFingerprint());
        view.setPartSize(task.getPartSize());
        view.setPartCount(task.getPartCount());
        view.setStatus(task.getStatus());
        view.setDirectUpload(getStorage(task).supportsDirectUpload());
        view.setExpiresAt(task.getExpiresAt());
        view.setCreateTime(task.getCreateTime());
        view.setUploadedParts(uploaded);
        return view;
    }
}

