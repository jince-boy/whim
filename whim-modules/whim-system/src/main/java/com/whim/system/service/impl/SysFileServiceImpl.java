package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.FileStorageException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.core.utils.RestClientUtils;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.model.PartUploadSignature;
import com.whim.file.model.StoragePart;
import com.whim.file.storage.IFileStorage;
import com.whim.file.storage.MultipartFileStorage;
import com.whim.file.storage.impl.AliyunOssFileStorageImpl;
import com.whim.file.storage.impl.LocalFileStorageImpl;
import com.whim.file.storage.impl.MinioFileStorageImpl;
import com.whim.file.support.ImageThumbnailGenerator;
import com.whim.file.support.FileUploadSecurity;
import com.whim.file.support.StoragePath;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.redis.executor.RedissonReadLockExecutor;
import com.whim.redis.executor.RedissonWriteLockExecutor;
import com.whim.system.config.properties.FileManagementProperties;
import com.whim.system.config.properties.FileManagementProperties.UploadPartSizeRule;
import com.whim.system.mapper.SysFileMapper;
import com.whim.system.model.dto.file.SysFileQueryDTO;
import com.whim.system.model.dto.file.SysFileStorageAddDTO;
import com.whim.system.model.dto.file.SysFileStorageUpdateDTO;
import com.whim.system.model.dto.file.SysFileUploadDTO;
import com.whim.system.model.dto.file.SysFileUploadPartDTO;
import com.whim.system.model.dto.file.SysFileUploadTaskAddDTO;
import com.whim.system.model.entity.SysFile;
import com.whim.system.model.entity.SysFileStorage;
import com.whim.system.model.entity.SysFileUploadTask;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.file.SysFileStorageVO;
import com.whim.system.model.vo.file.SysFileUploadTaskVO;
import com.whim.system.model.vo.file.SysFileVO;
import com.whim.system.service.ISysDictDataService;
import com.whim.system.service.ISysFileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件业务服务，统一处理存储配置、文件上传、上传任务及文件记录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {
    private static final String STORAGE_LOCK = "system:file:storage";
    private static final String UPLOAD_LOCK = "system:file:upload";
    private static final String FILE_LOCK = "system:file:content";
    private static final int MAXIMUM_UPLOAD_PART_COUNT = 10000;
    private final AuthenticationContext authenticationContext;
    private final ISysDictDataService dictDataService;
    private final FileStorageProperties storageProperties;
    private final FileManagementProperties managementProperties;
    // 按需取得本服务的类代理，使内部阶段的锁和事务注解生效，也避免构造器循环依赖。
    private final ObjectProvider<SysFileServiceImpl> fileServiceProvider;

    /**
     * 按文件元数据、存储配置及上传时间分页查询文件，并批量补充上传人名称。
     *
     * @param query 查询条件
     * @return 文件分页
     */
    @Override
    public PageDataVO<SysFileVO> getFilePage(SysFileQueryDTO query) {
        if (query.getStartTime() != null && query.getEndTime() != null
                && query.getStartTime().isAfter(query.getEndTime())) {
            throw new ServiceException("上传开始时间不能晚于结束时间");
        }
        String fileName = StringUtils.trimToNull(query.getFileName());
        String originalName = StringUtils.trimToNull(query.getOriginalName());
        String fileSuffix = Strings.CS.removeStart(StringUtils.trimToNull(query.getFileSuffix()), ".");
        String platform = StringUtils.trimToNull(query.getPlatform());
        String uploadSource = StringUtils.trimToNull(query.getUploadSource());
        // 文件名称和后缀使用独立元数据列筛选，避免目录误匹配及分页后过滤造成总数不准确。
        PageDataVO<SysFileVO> result = new PageDataVO<>(page(query.getPage(), new LambdaQueryWrapper<SysFile>()
                .eq(query.getStorageId() != null, SysFile::getStorageId, query.getStorageId())
                .eq(platform != null, SysFile::getPlatform, platform)
                .eq(uploadSource != null, SysFile::getUploadSource, uploadSource)
                .eq(StringUtils.isNotBlank(fileSuffix), SysFile::getFileSuffix, fileSuffix)
                .like(fileName != null, SysFile::getFileName, fileName)
                .like(originalName != null, SysFile::getOriginalName, originalName)
                .ge(query.getStartTime() != null, SysFile::getCreateTime, query.getStartTime())
                .le(query.getEndTime() != null, SysFile::getCreateTime, query.getEndTime())
                .orderByDesc(SysFile::getId)).convert(row -> BeanConvertUtils.convert(row, SysFileVO.class)));
        Set<Long> uploaderIds = result.getData().stream().map(SysFileVO::getCreateBy)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 仅批量读取当前页上传人的展示字段，避免头像上传时用户服务与文件服务循环注入。
        Map<Long, String> uploaderNames = uploaderIds.isEmpty() ? Map.of()
                : Db.lambdaQuery(SysUser.class)
                .select(SysUser::getId, SysUser::getName, SysUser::getUsername)
                .in(SysUser::getId, uploaderIds).list().stream()
                .collect(Collectors.toMap(SysUser::getId,
                        account -> StringUtils.defaultIfBlank(account.getName(), account.getUsername())));
        result.getData().forEach(file -> file.setCreateByName(
                file.getCreateBy() == null ? null : uploaderNames.get(file.getCreateBy())));
        return result;
    }

    /**
     * 从文件原存储生成96像素缩略图，不修改原文件或创建额外的存储记录。
     *
     * @param id 文件ID
     * @return PNG 缩略图内容，不可预览时为空数组
     */
    @Lock4j(name = FILE_LOCK, keys = "#p0", executor = RedissonReadLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public byte[] getFileThumbnail(Long id) {
        return getFileImage(id, 96);
    }

    /**
     * 生成仅包含重新编码像素的预览图，禁止返回原始HTML、SVG或图片附带内容。
     *
     * @param id 文件ID
     * @return 最大2048像素的PNG预览，不可预览时为空数组
     */
    @Lock4j(name = FILE_LOCK, keys = "#p0", executor = RedissonReadLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public byte[] getFilePreview(Long id) {
        return getFileImage(id, 2048);
    }

    /**
     * 在调用方持有文件读锁时读取原文件并生成安全位图。
     *
     * @param id 文件ID
     * @param size 图片最长边
     * @return PNG图片内容，不可预览时为空数组
     */
    private byte[] getFileImage(Long id, int size) {
        SysFile file = getFile(id);
        if (!Strings.CI.startsWith(file.getContentType(), "image/")
                || file.getFileSize() > ImageThumbnailGenerator.MAXIMUM_SOURCE_BYTES) {
            return new byte[0];
        }
        // 与下载共用文件读锁；已存在的文件记录固定存储位置，预览不占用全局配置锁。
        IFileStorage storage = openStorage(getStorage(file.getStorageId()));
        try (DownloadHandler download = storage.download(file.getObjectKey())) {
            return ImageThumbnailGenerator.generateThumbnail(download.getInputStream(), size, size);
        } finally {
            closeStorage(storage);
        }
    }

    /**
     * 查询当前用户的上传任务及存储端已确认的分片。
     *
     * @param id 上传任务ID
     * @return 可继续上传的任务进度
     */
    @Lock4j(name = UPLOAD_LOCK, keys = "#p0", executor = RedissonLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public SysFileUploadTaskVO getFileUploadTask(Long id) {
        SysFileUploadTask task = getOwnedUploadTask(id);
        if (task.getFileId() != null || task.getCancelled()) return getUploadTaskStatus(task);
        MultipartFileStorage storage = openStorage(getStorage(task.getStorageId()));
        try {
            return getUploadTaskProgress(task, storage);
        } finally {
            closeStorage(storage);
        }
    }

    /**
     * 按创建顺序查询脱敏的存储配置列表，切换默认项不改变顺序。
     *
     * @return 不包含密钥的配置列表
     */
    @Override
    public List<SysFileStorageVO> getStorageList() {
        return BeanConvertUtils.convertList(Db.lambdaQuery(SysFileStorage.class)
                .orderByAsc(SysFileStorage::getId).list(), SysFileStorageVO.class);
    }

    /**
     * 查询上传大小上限。
     *
     * @return 单文件字节上限
     */
    @Override
    public Long getUploadLimit() {
        return storageProperties.getMaxFileSize().toBytes();
    }

    /**
     * 使用默认配置保存文件和业务上传途径，失败时补偿清理。
     *
     * @param parameter 上传参数
     * @return 文件记录
     */
    @Lock4j(name = STORAGE_LOCK, executor = RedissonReadLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public SysFileVO addFile(SysFileUploadDTO parameter) {
        if (parameter.getFile().isEmpty()) {
            throw new ServiceException("不能上传空文件");
        }
        String uploadSource = parameter.getUploadSource().trim();
        // 上传途径标记业务入口，必须使用已维护的字典编码，避免同一来源出现不同拼写。
        if (dictDataService.getDictDataByType("sys_file_upload_source").stream()
                .noneMatch(item -> uploadSource.equals(item.getValue()))) {
            throw new ServiceException("上传途径不存在，请检查文件上传途径字典");
        }
        String originalName = Objects.requireNonNullElse(parameter.getFile().getOriginalFilename(), "未命名文件");
        originalName = originalName.replace('\\', '/');
        originalName = originalName.substring(originalName.lastIndexOf('/') + 1);
        if (originalName.isBlank() || originalName.length() > 255 || originalName.chars().anyMatch(Character::isISOControl)) {
            throw new ServiceException("文件名不能为空、包含控制字符或超过255个字符");
        }
        try {
            // 普通上传尚无任务记录，配置读锁保护从选定位置到文件记录提交的过程。
            SysFileStorage config = getDefaultStorage();
            IFileStorage storage = openStorage(config);
            try {
                String key = null;
                try {
                    MetaData metadata;
                    try (FileSource source = FileSource.open(parameter.getFile(), storageProperties)) {
                        // 对象名由服务端生成，原始文件名只用于展示和下载。
                        key = config.getId() + "/" + LocalDate.now() + "/" + source.getFileName();
                        metadata = storage.upload(source, key, source.getContentType());
                    }
                    SysFile file = new SysFile();
                    file.setStorageId(config.getId());
                    file.setStorageName(config.getName());
                    file.setPlatform(config.getPlatform());
                    file.setUploadSource(uploadSource);
                    file.setFileName(FilenameUtils.getName(key));
                    file.setOriginalName(originalName);
                    file.setFileSuffix(StoragePath.getFileExtension(originalName));
                    file.setObjectKey(key);
                    file.setFileSize(metadata.getFileSize());
                    file.setContentType(metadata.getContentType());
                    if (!save(file)) {
                        throw new ServiceException("保存文件记录失败");
                    }
                    return BeanConvertUtils.convert(file, SysFileVO.class);
                } catch (Exception exception) {
                    // 数据库保存失败或上传结果不确定时，按同一对象标识清理，避免静默遗留文件。
                    if (key != null) {
                        try {
                            storage.deleteFile(key);
                        } catch (Exception cleanupException) {
                            log.error("上传失败后的文件清理失败，存储ID：{}，对象：{}", config.getId(), key, cleanupException);
                            exception.addSuppressed(cleanupException);
                        }
                    }
                    throw exception;
                }
            } finally {
                closeStorage(storage);
            }
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new FileStorageException("上传文件失败", exception);
        }
    }

    /**
     * 根据文件大小匹配配置档位并在默认存储中创建可恢复任务，重复请求复用同一任务。
     *
     * @param parameter 文件信息及客户端请求标识
     * @return 上传任务及分片大小
     */
    @Lock4j(name = "system:file:uploadRequest", keys = "#p0.requestId", acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public SysFileUploadTaskVO addFileUploadTask(SysFileUploadTaskAddDTO parameter) {
        FileUploadSecurity.validateMetadata(parameter.getOriginalName(), parameter.getContentType());
        if (parameter.getFileSize() > storageProperties.getMaxFileSize().toBytes()) {
            throw new ServiceException("文件大小超过允许上限");
        }
        String originalName = FilenameUtils.getName(parameter.getOriginalName().replace('\\', '/'));
        if (originalName.isBlank() || originalName.chars().anyMatch(Character::isISOControl)) {
            throw new ServiceException("文件名不能为空或包含控制字符");
        }
        String uploadSource = parameter.getUploadSource().trim();
        if (dictDataService.getDictDataByType("sys_file_upload_source").stream()
                .noneMatch(item -> uploadSource.equals(item.getValue()))) {
            throw new ServiceException("上传途径不存在，请检查文件上传途径字典");
        }
        String contentType = MediaType.parseMediaType(parameter.getContentType()).toString();
        Long userId = authenticationContext.getUserId();
        SysFileUploadTask task = Db.lambdaQuery(SysFileUploadTask.class)
                .eq(SysFileUploadTask::getCreateBy, userId)
                .eq(SysFileUploadTask::getRequestId, parameter.getRequestId()).one();
        if (task != null) {
            if (!task.getOriginalName().equals(originalName) || !task.getFileSize().equals(parameter.getFileSize())
                    || !task.getUploadSource().equals(uploadSource) || !task.getContentType().equals(contentType)) {
                throw new ServiceException("上传请求标识不能用于不同的文件");
            }
            if (task.getCancelled() || task.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ServiceException("上传任务已取消或过期，请重新选择文件");
            }
            return fileServiceProvider.getObject().initializeFileUpload(task.getId());
        }
        // 规则在启动时校验为从0B开始递增，文件恰好达到下限时进入对应档位。
        List<UploadPartSizeRule> partSizeRules = managementProperties.getUploadPartSizeRules();
        int partSize = Math.toIntExact(partSizeRules.getFirst().getPartSize().toBytes());
        for (UploadPartSizeRule rule : partSizeRules) {
            if (parameter.getFileSize() < rule.getMinFileSize().toBytes()) break;
            partSize = Math.toIntExact(rule.getPartSize().toBytes());
        }
        if (Math.ceilDiv(parameter.getFileSize(), partSize) > MAXIMUM_UPLOAD_PART_COUNT) {
            throw new ServiceException("文件大小超过允许上限");
        }
        task = new SysFileUploadTask();
        task.setRequestId(parameter.getRequestId());
        task.setOriginalName(originalName);
        task.setContentType(contentType);
        task.setFileSize(parameter.getFileSize());
        task.setUploadSource(uploadSource);
        // 分片大小只在创建时选择并持久化，后续续传及初始化重试沿用原任务的切分边界。
        task.setPartSize(partSize);
        task.setCancelled(false);
        task.setExpiresAt(LocalDateTime.now().plusDays(1));
        // 先在配置锁内提交任务引用，再按任务锁初始化存储会话，避免配置被并发删除。
        fileServiceProvider.getObject().addFileUploadTaskRecord(task);
        return fileServiceProvider.getObject().initializeFileUpload(task.getId());
    }

    /**
     * 校验并提交单个分片，同一序号重传覆盖原分片。
     *
     * @param parameter 任务ID、分片序号及内容
     */
    @Lock4j(name = UPLOAD_LOCK, keys = "#p0.id", executor = RedissonLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void addFileUploadPart(SysFileUploadPartDTO parameter) {
        SysFileUploadTask task = getOwnedUploadTask(parameter.getId());
        if (task.getCancelled() || task.getFileId() != null) {
            throw new ServiceException("上传任务已经结束");
        }
        long offset = (long) (parameter.getPartNumber() - 1) * task.getPartSize();
        long expectedSize = Math.min(task.getPartSize(), task.getFileSize() - offset);
        if (expectedSize <= 0 || parameter.getFile().getSize() != expectedSize) {
            throw new ServiceException("分片序号或文件大小不符合任务要求");
        }
        MultipartFileStorage storage = openStorage(getStorage(task.getStorageId()));
        try {
            initializeUploadSession(task, storage);
            // 仅读取任务约定大小的单个分片，服务端计算摘要并绑定云存储签名，客户端不能扩大上传范围。
            byte[] content = parameter.getFile().getBytes();
            String contentMd5 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(content));
            String contentSha256 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(content));
            if (storage.supportsDirectUpload()) {
                PartUploadSignature signature = storage.getPartUploadSignature(task.getObjectKey(),
                        task.getUploadId(), parameter.getPartNumber(), expectedSize,
                        contentMd5, contentSha256, Duration.ofMinutes(5));
                HttpHeaders headers = new HttpHeaders();
                signature.getHeaders().forEach(headers::set);
                // 预签名地址已经编码，直接传 URI，不能再次经过模板编码而破坏签名。
                try (HttpClient client = HttpClient.newBuilder().connectTimeout(storageProperties.getConnectTimeout())
                        .followRedirects(HttpClient.Redirect.NEVER).build()) {
                    RestClientUtils.createRestClient(client, storageProperties.getRequestTimeout())
                            .put().uri(URI.create(signature.getUrl())).headers(target -> target.addAll(headers))
                            .body(content).retrieve().toBodilessEntity();
                }
            } else {
                try (ByteArrayInputStream input = new ByteArrayInputStream(content)) {
                    storage.uploadPart(task.getObjectKey(), task.getUploadId(), parameter.getPartNumber(),
                            input, expectedSize, contentMd5, contentSha256);
                }
            }
            task.setExpiresAt(LocalDateTime.now().plusDays(1));
            if (!Db.updateById(task)) throw new ServiceException("更新上传任务失败");
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new FileStorageException("上传文件分片失败", exception);
        } finally {
            closeStorage(storage);
        }
    }

    /**
     * 新增存储配置，首个配置自动成为默认存储。
     *
     * @param parameter 配置参数
     */
    @Lock4j(name = STORAGE_LOCK, executor = RedissonWriteLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void addStorage(SysFileStorageAddDTO parameter) {
        SysFileStorage config = BeanConvertUtils.convert(parameter, SysFileStorage.class);
        validateStorage(config, parameter.getSecretKey(), null);
        if (!Db.lambdaQuery(SysFileStorage.class).eq(SysFileStorage::getDefaultFlag, 1).exists()) {
            config.setDefaultFlag(1);
        }
        if (!Db.save(config)) {
            throw new ServiceException("保存存储配置失败");
        }
    }

    /**
     * 修改存储配置，已有文件时禁止改变其物理位置。
     *
     * @param parameter 配置参数
     */
    @Lock4j(name = STORAGE_LOCK, executor = RedissonWriteLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void updateStorage(SysFileStorageUpdateDTO parameter) {
        SysFileStorage previous = getStorage(parameter.getId());
        SysFileStorage config = BeanConvertUtils.convert(parameter, SysFileStorage.class);
        validateStorage(config, parameter.getSecretKey(), previous);
        // 先查任务再查文件，避免在上传完成提交的瞬间漏掉从任务转为文件的引用。
        // 下载始终解析上传时的配置；目录、桶、地域及平台变化会令历史对象不可达。
        if ((Db.lambdaQuery(SysFileUploadTask.class)
                .eq(SysFileUploadTask::getStorageId, config.getId()).eq(SysFileUploadTask::getCancelled, false)
                .isNull(SysFileUploadTask::getFileId).exists()
                || exists(new LambdaQueryWrapper<SysFile>().eq(SysFile::getStorageId, config.getId())))
                && (!Objects.equals(previous.getPlatform(), config.getPlatform())
                || !Objects.equals(previous.getBasePath(), config.getBasePath())
                || !Objects.equals(previous.getEndpoint(), config.getEndpoint())
                || !Objects.equals(previous.getBucket(), config.getBucket())
                || !Objects.equals(previous.getRegion(), config.getRegion()))) {
            throw new ServiceException("该配置已有文件或未完成的上传任务，不能更改存储位置，请新增配置后切换默认存储");
        }
        if (!Db.updateById(config)) {
            throw new ServiceException("修改存储配置失败");
        }
    }

    /**
     * 在同一事务中切换默认存储。
     *
     * @param id 配置ID
     */
    @Lock4j(name = STORAGE_LOCK, executor = RedissonWriteLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void updateDefaultStorage(Long id) {
        getStorage(id);
        // Lock4j切面位于事务外层，独立事务提交后才释放配置写锁，避免出现多个默认项。
        Db.update(new SysFileStorage(), new LambdaUpdateWrapper<>(SysFileStorage.class)
                .eq(SysFileStorage::getDefaultFlag, 1).set(SysFileStorage::getDefaultFlag, null));
        SysFileStorage config = new SysFileStorage();
        config.setId(id);
        config.setDefaultFlag(1);
        if (!Db.updateById(config)) {
            throw new ServiceException("切换默认存储失败");
        }
    }

    /**
     * 删除存储对象后逻辑删除记录，失败时保留记录供重试。
     *
     * @param id 文件ID
     */
    @Lock4j(name = FILE_LOCK, keys = "#p0", executor = RedissonWriteLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void deleteFile(Long id) {
        SysFile file = getFile(id);
        IFileStorage storage = openStorage(getStorage(file.getStorageId()));
        try {
            // 先删对象，后删记录；对象删除幂等，数据库失败后仍可再次发起删除。
            storage.deleteFile(file.getObjectKey());
            if (!removeById(id)) {
                throw new ServiceException("删除文件记录失败，请刷新后重试");
            }
        } finally {
            closeStorage(storage);
        }
    }

    /**
     * 删除没有文件引用的非默认配置。
     *
     * @param id 配置ID
     */
    @Lock4j(name = STORAGE_LOCK, executor = RedissonWriteLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public void deleteStorage(Long id) {
        SysFileStorage config = getStorage(id);
        if (Integer.valueOf(1).equals(config.getDefaultFlag())) {
            throw new ServiceException("默认存储不能删除，请先切换默认存储");
        }
        // 先查任务再查文件，覆盖上传完成时的引用转换；暂停或过期任务也须先清理分片。
        if (Db.lambdaQuery(SysFileUploadTask.class)
                .eq(SysFileUploadTask::getStorageId, id).eq(SysFileUploadTask::getCancelled, false)
                .isNull(SysFileUploadTask::getFileId).exists()) {
            throw new ServiceException("存储配置仍有未完成的上传任务，不能删除");
        }
        if (exists(new LambdaQueryWrapper<SysFile>().eq(SysFile::getStorageId, id))) {
            throw new ServiceException("存储配置仍有文件，不能删除");
        }
        if (!Db.removeById(id, SysFileStorage.class)) {
            throw new ServiceException("删除存储配置失败");
        }
    }

    /**
     * 定时清理一天未活动的任务及分片，每次最多处理100项。
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT1H")
    public void deleteExpiredFileUploads() {
        List<SysFileUploadTask> expired = Db.lambdaQuery(SysFileUploadTask.class)
                .lt(SysFileUploadTask::getExpiresAt, LocalDateTime.now())
                .orderByAsc(SysFileUploadTask::getExpiresAt)
                .page(new Page<SysFileUploadTask>(1, 100, false)).getRecords();
        for (SysFileUploadTask candidate : expired) {
            try {
                fileServiceProvider.getObject().deleteExpiredFileUpload(candidate.getId());
            } catch (Exception exception) {
                // 保留清理失败的任务，下次继续；不删除已正式入库的文件。
                log.error("清理过期上传任务失败，任务ID：{}", candidate.getId(), exception);
            }
        }
    }

    /**
     * 通过认证接口流式下载文件，禁止上传内容在应用域直接执行。
     *
     * @param id 文件ID
     * @param response 下载响应
     */
    @Lock4j(name = FILE_LOCK, keys = "#p0", executor = RedissonReadLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void downloadFile(Long id, HttpServletResponse response) {
        try {
            SysFile file = getFile(id);
            IFileStorage storage = openStorage(getStorage(file.getStorageId()));
            try (DownloadHandler download = storage.download(file.getObjectKey())) {
                response.setContentType(FileUploadSecurity.DOWNLOAD_CONTENT_TYPE);
                response.setContentLengthLong(file.getFileSize());
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("Content-Security-Policy", FileUploadSecurity.CONTENT_SECURITY_POLICY);
                response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getOriginalName(), StandardCharsets.UTF_8).build().toString());
                download.writeToOutputStream(response.getOutputStream());
            } finally {
                closeStorage(storage);
            }
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new FileStorageException("下载文件失败", exception);
        }
    }

    /**
     * 合并存储端完整分片并保存文件记录，重复调用返回同一结果。
     *
     * @param id 上传任务ID
     * @return 已完成的任务
     */
    @Lock4j(name = UPLOAD_LOCK, keys = "#p0", executor = RedissonLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public SysFileUploadTaskVO completeFileUpload(Long id) {
        SysFileUploadTask task = getOwnedUploadTask(id);
        if (task.getCancelled()) throw new ServiceException("上传任务已取消");
        if (task.getFileId() != null) return getUploadTaskStatus(task);
        SysFileStorage config = getStorage(task.getStorageId());
        MultipartFileStorage storage = openStorage(config);
        try {
            // 已创建的旧任务同样受新策略限制，不能通过续传绕过文件类型检查。
            FileUploadSecurity.validateMetadata(task.getOriginalName(), task.getContentType());
            MetaData metadata;
            // 合并成功但数据库提交或响应失败时，通过固定对象标识恢复，避免重复生成文件。
            if (!storage.exists(task.getObjectKey())) {
                List<StoragePart> parts = storage.getUploadedParts(task.getObjectKey(), task.getUploadId());
                int partCount = Math.toIntExact((task.getFileSize() - 1) / task.getPartSize() + 1);
                if (parts.size() != partCount) throw new ServiceException("文件分片尚未上传完整");
                for (int index = 0; index < parts.size(); index++) {
                    StoragePart part = parts.get(index);
                    long expectedSize = Math.min(task.getPartSize(), task.getFileSize() - (long) index * task.getPartSize());
                    if (part.getPartNumber() != index + 1 || part.getSize() != expectedSize) {
                        throw new ServiceException("文件分片序号或大小不一致");
                    }
                }
                metadata = storage.completeUpload(task.getObjectKey(), task.getUploadId(), parts,
                        task.getFileSize(), task.getContentType());
            } else {
                // 重试发现对象已存在，也必须重新校验后才能将其登记为业务文件。
                metadata = FileUploadSecurity.getValidatedFileMetaData(storage, task.getObjectKey());
            }
            if (metadata.getFileSize() != task.getFileSize().longValue()) {
                throw new ServiceException("合并后的文件大小不一致");
            }
            // 处理本地合并已发布、但上次清理失败的情况；清理失败保留任务供重试。
            storage.abortUpload(task.getObjectKey(), task.getUploadId());
            SysFile file = new SysFile();
            file.setStorageId(config.getId());
            file.setStorageName(config.getName());
            file.setPlatform(config.getPlatform());
            file.setUploadSource(task.getUploadSource());
            file.setFileName(FilenameUtils.getName(task.getObjectKey()));
            file.setOriginalName(task.getOriginalName());
            file.setFileSuffix(StoragePath.getFileExtension(task.getOriginalName()));
            file.setObjectKey(task.getObjectKey());
            file.setFileSize(task.getFileSize());
            file.setContentType(metadata.getContentType());
            // 存储合并和清理已结束，只把文件记录及完成标识交给短事务。
            fileServiceProvider.getObject().addFileUploadResult(task, file);
            return getUploadTaskStatus(task);
        } catch (IllegalArgumentException exception) {
            // 拒绝的文件不得入库；清理对象和分片后才标记取消，清理失败保留任务供重试。
            try {
                storage.deleteFile(task.getObjectKey());
                if (task.getUploadId() != null) storage.abortUpload(task.getObjectKey(), task.getUploadId());
                task.setCancelled(true);
                if (!Db.updateById(task)) throw new ServiceException("保存安全校验拒绝状态失败");
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
                log.error("清理安全校验拒绝的文件失败，任务ID：{}", task.getId(), cleanupException);
            }
            throw exception;
        } finally {
            closeStorage(storage);
        }
    }

    /**
     * 取消未完成任务并删除其临时内容，已完成文件不受影响。
     *
     * @param id 上传任务ID
     * @return 取消后的任务状态
     */
    @Lock4j(name = UPLOAD_LOCK, keys = "#p0", executor = RedissonLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public SysFileUploadTaskVO cancelFileUpload(Long id) {
        SysFileUploadTask task = getOwnedUploadTask(id);
        if (task.getFileId() != null || task.getCancelled()) return getUploadTaskStatus(task);
        MultipartFileStorage storage = openStorage(getStorage(task.getStorageId()));
        try {
            // 先清理再标记取消；失败保留任务，调用方可以重试取消。
            if (task.getUploadId() != null) storage.abortUpload(task.getObjectKey(), task.getUploadId());
            storage.deleteFile(task.getObjectKey());
            task.setCancelled(true);
            if (!Db.updateById(task)) throw new ServiceException("取消上传任务失败");
            return getUploadTaskStatus(task);
        } finally {
            closeStorage(storage);
        }
    }

    /**
     * 在配置读锁内创建任务记录，提交后由任务引用保护选定的存储位置。
     *
     * @param task 服务内部构造的上传任务
     */
    @Lock4j(name = STORAGE_LOCK, executor = RedissonReadLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class, timeout = 30)
    protected void addFileUploadTaskRecord(SysFileUploadTask task) {
        SysFileStorage config = getDefaultStorage();
        task.setStorageId(config.getId());
        task.setObjectKey(config.getId() + "/" + LocalDate.now()
                + "/" + StoragePath.generateFileName(task.getOriginalName()));
        if (!Db.save(task)) throw new ServiceException("创建上传任务失败");
    }

    /**
     * 在任务锁内初始化分片会话，首次创建及初始化重试复用同一流程。
     *
     * @param id 上传任务ID
     * @return 初始化后的上传任务进度
     */
    @Lock4j(name = UPLOAD_LOCK, keys = "#p0", executor = RedissonLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    protected SysFileUploadTaskVO initializeFileUpload(Long id) {
        SysFileUploadTask task = getOwnedUploadTask(id);
        if (task.getFileId() != null || task.getCancelled()) return getUploadTaskStatus(task);
        MultipartFileStorage storage = openStorage(getStorage(task.getStorageId()));
        try {
            initializeUploadSession(task, storage);
            return getUploadTaskProgress(task, storage);
        } finally {
            closeStorage(storage);
        }
    }

    /**
     * 原子保存服务内部已合并完成的文件及任务状态，仅由持有任务锁的完成流程调用。
     *
     * @param task 已完成存储合并的上传任务
     * @param file 根据任务构造的文件记录
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class, timeout = 30)
    protected void addFileUploadResult(SysFileUploadTask task, SysFile file) {
        // 外层任务锁在本事务提交后才释放，重试不能创建第二条文件记录。
        if (!save(file)) throw new ServiceException("保存文件记录失败");
        task.setFileId(file.getId());
        task.setExpiresAt(LocalDateTime.now().plusDays(1));
        if (!Db.updateById(task)) throw new ServiceException("保存上传完成状态失败");
    }

    /**
     * 在单个任务锁内复核有效期并清理过期分片，避免与续传或完成操作冲突。
     *
     * @param id 上传任务ID
     */
    @Lock4j(name = UPLOAD_LOCK, keys = "#p0", executor = RedissonLockExecutor.class, acquireTimeout = 5000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    protected void deleteExpiredFileUpload(Long id) {
        SysFileUploadTask task = Db.getById(id, SysFileUploadTask.class);
        if (task == null || !task.getExpiresAt().isBefore(LocalDateTime.now())) return;
        // 完成和取消都已处理存储内容，只清理仍未结束任务的分片及未入库对象。
        if (task.getFileId() == null && !task.getCancelled()) {
            MultipartFileStorage storage = openStorage(getStorage(task.getStorageId()));
            try {
                if (task.getUploadId() != null) {
                    storage.abortUpload(task.getObjectKey(), task.getUploadId());
                }
                storage.deleteFile(task.getObjectKey());
            } finally {
                closeStorage(storage);
            }
        }
        if (!Db.removeById(id, SysFileUploadTask.class)) {
            throw new ServiceException("删除过期上传任务失败");
        }
    }

    /**
     * 查询必须存在的文件记录。
     *
     * @param id 文件ID
     * @return 文件记录
     */
    private SysFile getFile(Long id) {
        SysFile file = getById(id);
        if (file == null) {
            throw new ServiceException("文件不存在或已删除");
        }
        return file;
    }

    /**
     * 检查任务存在性、所有权及有效期。
     *
     * @param id 上传任务ID
     * @return 当前用户的有效任务
     */
    private SysFileUploadTask getOwnedUploadTask(Long id) {
        SysFileUploadTask task = Db.getById(id, SysFileUploadTask.class);
        // 任务属于创建者，拥有上传功能权限不代表可以接管他人的任务。
        if (task == null || !Objects.equals(task.getCreateBy(), authenticationContext.getUserId())) {
            throw new ServiceException("上传任务不存在或无权访问");
        }
        if (task.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ServiceException("上传任务已过期，请重新选择文件");
        }
        return task;
    }

    /**
     * 使用稳定的存储任务标识初始化会话，保存失败时补偿清理。
     *
     * @param task 持久化上传任务
     * @param storage 任务存储客户端
     */
    private void initializeUploadSession(SysFileUploadTask task, MultipartFileStorage storage) {
        if (task.getUploadId() != null || task.getFileId() != null || task.getCancelled()) return;
        // 本地会话目录要求32位小写十六进制标识；由全局任务ID确定性转换，重试不会另建目录。
        String storageTaskId = String.format("%032x", task.getId());
        String uploadId = storage.initiateUpload(task.getObjectKey(), task.getContentType(), storageTaskId);
        try {
            task.setUploadId(uploadId);
            if (!Db.updateById(task)) throw new ServiceException("保存分片会话失败");
        } catch (RuntimeException exception) {
            try {
                storage.abortUpload(task.getObjectKey(), uploadId);
            } catch (Exception cleanupException) {
                exception.addSuppressed(cleanupException);
                log.error("清理未保存的上传会话失败，任务ID：{}", task.getId(), cleanupException);
            }
            throw exception;
        }
    }

    /**
     * 读取已确认的分片，正式对象已发布时返回全部完成的分片序号。
     *
     * @param task 上传任务
     * @param storage 任务存储客户端
     * @return 不包含存储凭据的任务进度
     */
    private SysFileUploadTaskVO getUploadTaskProgress(SysFileUploadTask task, MultipartFileStorage storage) {
        SysFileUploadTaskVO result = getUploadTaskStatus(task);
        if (task.getFileId() != null || task.getCancelled() || task.getUploadId() == null) return result;
        if (storage.exists(task.getObjectKey())) {
            result.setUploadedParts(IntStream.rangeClosed(1,
                    Math.toIntExact((task.getFileSize() - 1) / task.getPartSize() + 1)).boxed().toList());
        } else {
            result.setUploadedParts(storage.getUploadedParts(task.getObjectKey(), task.getUploadId()).stream()
                    .map(StoragePart::getPartNumber).toList());
        }
        return result;
    }

    /**
     * 构建任务状态，已结束任务无需连接存储，避免历史配置删除或云端故障影响幂等响应。
     *
     * @param task 上传任务
     * @return 任务状态，未完成任务的分片清单由存储查询补充
     */
    private SysFileUploadTaskVO getUploadTaskStatus(SysFileUploadTask task) {
        SysFileUploadTaskVO result = BeanConvertUtils.convert(task, SysFileUploadTaskVO.class);
        result.setUploadedParts(task.getFileId() == null ? List.of() : IntStream.rangeClosed(1,
                Math.toIntExact((task.getFileSize() - 1) / task.getPartSize() + 1)).boxed().toList());
        return result;
    }

    /**
     * 查询必须存在的存储配置。
     *
     * @param id 配置ID
     * @return 存储配置
     */
    private SysFileStorage getStorage(Long id) {
        SysFileStorage config = Db.getById(id, SysFileStorage.class);
        if (config == null) {
            throw new ServiceException("存储配置不存在");
        }
        return config;
    }

    /**
     * 查询当前默认存储，未配置时明确提示配置入口。
     *
     * @return 默认存储配置
     */
    private SysFileStorage getDefaultStorage() {
        SysFileStorage config = Db.lambdaQuery(SysFileStorage.class)
                .eq(SysFileStorage::getDefaultFlag, 1).one();
        if (config == null) {
            throw new ServiceException("请先在存储配置中设置默认配置");
        }
        return config;
    }

    /**
     * 校验平台字段并对新密钥加密，空密钥保留原值。
     *
     * @param config 待保存配置
     * @param secretKey 新密钥
     * @param previous 原有配置，新增时为空
     */
    private void validateStorage(SysFileStorage config, String secretKey, SysFileStorage previous) {
        config.setName(config.getName().trim());
        config.setBasePath(StringUtils.trimToEmpty(config.getBasePath()));
        config.setEndpoint(StringUtils.trimToEmpty(config.getEndpoint()));
        config.setBucket(StringUtils.trimToEmpty(config.getBucket()));
        config.setRegion(StringUtils.trimToEmpty(config.getRegion()));
        config.setAccessKey(StringUtils.trimToEmpty(config.getAccessKey()));
        if ("local".equals(config.getPlatform())) {
            if (config.getBasePath().isBlank()) {
                throw new ServiceException("本地存储目录不能为空");
            }
            config.setBasePath(StoragePath.resolveLocalRoot(config.getBasePath()).toString());
            config.setEndpoint("");
            config.setBucket("");
            config.setRegion("");
            config.setAccessKey("");
            config.setSecretCipher("");
            return;
        }
        URI endpoint;
        try {
            endpoint = URI.create(config.getEndpoint());
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("对象存储服务地址格式无效");
        }
        if (!("http".equals(endpoint.getScheme()) || "https".equals(endpoint.getScheme())) || endpoint.getHost() == null
                || endpoint.getUserInfo() != null || endpoint.getQuery() != null || endpoint.getFragment() != null
                || (StringUtils.isNotEmpty(endpoint.getPath()) && !"/".equals(endpoint.getPath()))) {
            throw new ServiceException("对象存储地址须为不含账号、路径、查询参数的 HTTP 或 HTTPS 地址");
        }
        if (config.getBucket().isBlank() || config.getAccessKey().isBlank()
                || ("aliyunOss".equals(config.getPlatform()) && config.getRegion().isBlank())) {
            throw new ServiceException("请填写存储桶、访问标识及所需地域");
        }
        StoragePath.validatePrefix(config.getBasePath());
        if (StringUtils.isNotBlank(secretKey)) {
            config.setSecretCipher(encryptSecret(secretKey));
        } else if (previous != null && config.getPlatform().equals(previous.getPlatform())
                && StringUtils.isNotBlank(previous.getSecretCipher())) {
            config.setSecretCipher(previous.getSecretCipher());
        } else {
            throw new ServiceException("对象存储密钥不能为空");
        }
    }

    /**
     * 使用现有文件模块创建本次操作独占的客户端，配置立即生效。
     *
     * @param config 存储配置
     * @return 需要调用方关闭的存储客户端
     */
    private MultipartFileStorage openStorage(SysFileStorage config) {
        if ("local".equals(config.getPlatform())) {
            FileStorageProperties.LocalStorageProperties local = new FileStorageProperties.LocalStorageProperties();
            local.setName(config.getId().toString());
            local.setBasePath(config.getBasePath());
            return new LocalFileStorageImpl(local);
        }
        FileStorageProperties.CloudStorageProperties cloud = new FileStorageProperties.CloudStorageProperties();
        cloud.setName(config.getId().toString());
        cloud.setUrl(config.getEndpoint());
        cloud.setBucket(config.getBucket());
        cloud.setBasePath(config.getBasePath());
        cloud.setRegion(config.getRegion());
        cloud.setAccessKey(config.getAccessKey());
        cloud.setSecretKey(decryptSecret(config.getSecretCipher()));
        return switch (config.getPlatform()) {
            case "minio" -> new MinioFileStorageImpl(cloud, storageProperties);
            case "aliyunOss" -> new AliyunOssFileStorageImpl(cloud, storageProperties);
            default -> throw new ServiceException("不支持的存储类型");
        };
    }

    /**
     * 释放本次操作的客户端，资源回收失败不改变已经提交的业务结果。
     *
     * @param storage 存储客户端
     */
    private void closeStorage(IFileStorage storage) {
        try {
            storage.close();
        } catch (Exception exception) {
            // 文件记录可能已经提交，不能将清理错误报告为上传或删除失败并诱发重复操作。
            log.error("释放文件存储客户端失败，平台：{}，配置：{}", storage.getPlatform(), storage.getConfigName(), exception);
        }
    }


    /**
     * 获取环境中提供的凭据加密密钥，未配置时仅影响云存储。
     *
     * @return 32字节密钥
     */
    private byte[] getSecretKey() {
        try {
            byte[] key = Base64.getDecoder().decode(Objects.requireNonNullElse(managementProperties.getSecretKey(), ""));
            if (key.length == 32) {
                return key;
            }
        } catch (IllegalArgumentException ignored) {
            // 返回配置说明，不在日志中暴露密钥内容。
        }
        throw new ServiceException("请先配置 FILE_STORAGE_SECRET_KEY（Base64编码的32字节随机密钥）");
    }

    /**
     * 使用带认证的随机化加密保存对象存储密钥。
     *
     * @param secret 原始密钥
     * @return JWE密文
     */
    private String encryptSecret(String secret) {
        byte[] key = getSecretKey();
        try {
            JWEObject encrypted = new JWEObject(new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256GCM), new Payload(secret));
            encrypted.encrypt(new DirectEncrypter(key));
            return encrypted.serialize();
        } catch (JOSEException exception) {
            throw new ServiceException("存储密钥加密失败");
        }
    }

    /**
     * 校验加密协议并解密存储密钥。
     *
     * @param cipher 存储密文
     * @return 原始密钥
     */
    private String decryptSecret(String cipher) {
        byte[] key = getSecretKey();
        try {
            JWEObject encrypted = JWEObject.parse(cipher);
            if (!JWEAlgorithm.DIR.equals(encrypted.getHeader().getAlgorithm())
                    || !EncryptionMethod.A256GCM.equals(encrypted.getHeader().getEncryptionMethod())) {
                throw new ServiceException("存储密钥格式无效");
            }
            encrypted.decrypt(new DirectDecrypter(key));
            return encrypted.getPayload().toString();
        } catch (Exception exception) {
            throw new ServiceException("存储密钥解密失败，请检查服务器加密密钥");
        }
    }
}
