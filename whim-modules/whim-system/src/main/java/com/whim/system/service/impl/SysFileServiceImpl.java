package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.whim.core.exception.FileStorageException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import com.whim.file.storage.impl.*;
import com.whim.file.support.StoragePath;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.config.properties.FileManagementProperties;
import com.whim.system.mapper.SysFileMapper;
import com.whim.system.model.dto.file.*;
import com.whim.system.model.entity.SysFile;
import com.whim.system.model.entity.SysFileStorage;
import com.whim.system.model.vo.file.*;
import com.whim.system.service.ISysFileService;
import com.whim.system.service.ISysFileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件及存储配置管理，配置读写锁覆盖存储操作和数据库提交。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {
    private static final String STORAGE_LOCK = "system:file:storage";
    private final ISysFileStorageService storageService;
    private final FileStorageProperties storageProperties;
    private final FileManagementProperties managementProperties;
    private final RedissonClient redissonClient;
    private final PlatformTransactionManager transactionManager;

    /**
     * 分页查询文件。
     *
     * @param query 查询条件
     * @return 文件分页
     */
    @Override
    public PageDataVO<SysFileVO> getFilePage(SysFileQueryDTO query) {
        return new PageDataVO<>(page(query.getPage(), new LambdaQueryWrapper<SysFile>()
                .like(StringUtils.isNotBlank(query.getOriginalName()), SysFile::getOriginalName, query.getOriginalName())
                .eq(query.getStorageId() != null, SysFile::getStorageId, query.getStorageId())
                .orderByDesc(SysFile::getId)).convert(row -> BeanConvertUtils.convert(row, SysFileVO.class)));
    }

    /**
     * 按创建顺序查询脱敏的存储配置列表，切换默认项不改变顺序。
     *
     * @return 不包含密钥的配置列表
     */
    @Override
    public List<SysFileStorageVO> getStorageList() {
        return BeanConvertUtils.convertList(storageService.list(new LambdaQueryWrapper<SysFileStorage>()
                .orderByAsc(SysFileStorage::getId)), SysFileStorageVO.class);
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
     * 使用默认配置保存文件和记录，失败时补偿清理。
     *
     * @param parameter 上传参数
     * @return 文件记录
     */
    @Override
    public SysFileVO addFile(SysFileUploadDTO parameter) {
        if (parameter.getFile().isEmpty()) {
            throw new ServiceException("不能上传空文件");
        }
        String originalName = Objects.requireNonNullElse(parameter.getFile().getOriginalFilename(), "未命名文件");
        originalName = originalName.replace('\\', '/');
        originalName = originalName.substring(originalName.lastIndexOf('/') + 1);
        if (originalName.isBlank() || originalName.length() > 255 || originalName.chars().anyMatch(Character::isISOControl)) {
            throw new ServiceException("文件名不能为空、包含控制字符或超过255个字符");
        }
        RLock lock = redissonClient.getReadWriteLock(STORAGE_LOCK).readLock();
        lock.lock();
        try {
            // 上传方式由系统统一设置，读锁保证本次上传期间默认配置不会被切换。
            SysFileStorage config = storageService.getOne(new LambdaQueryWrapper<SysFileStorage>()
                    .eq(SysFileStorage::getDefaultFlag, 1));
            if (config == null) {
                throw new ServiceException("请先在存储配置中设置默认配置");
            }
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
                    file.setOriginalName(originalName);
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
        } finally {
            lock.unlock();
        }
    }

    /**
     * 新增存储配置，首个配置自动成为默认存储。
     *
     * @param parameter 配置参数
     */
    @Override
    public void addStorage(SysFileStorageAddDTO parameter) {
        RLock lock = redissonClient.getReadWriteLock(STORAGE_LOCK).writeLock();
        lock.lock();
        try {
            SysFileStorage config = BeanConvertUtils.convert(parameter, SysFileStorage.class);
            validateStorage(config, parameter.getSecretKey(), null);
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                if (!storageService.exists(new LambdaQueryWrapper<SysFileStorage>().eq(SysFileStorage::getDefaultFlag, 1))) {
                    config.setDefaultFlag(1);
                }
                if (!storageService.save(config)) {
                    throw new ServiceException("保存存储配置失败");
                }
            });
        } finally {
            lock.unlock();
        }
    }

    /**
     * 修改存储配置，已有文件时禁止改变其物理位置。
     *
     * @param parameter 配置参数
     */
    @Override
    public void updateStorage(SysFileStorageUpdateDTO parameter) {
        RLock lock = redissonClient.getReadWriteLock(STORAGE_LOCK).writeLock();
        lock.lock();
        try {
            SysFileStorage previous = getStorage(parameter.getId());
            SysFileStorage config = BeanConvertUtils.convert(parameter, SysFileStorage.class);
            validateStorage(config, parameter.getSecretKey(), previous);
            // 下载始终解析上传时的配置；目录、桶、地域及平台变化会令历史对象不可达。
            if (exists(new LambdaQueryWrapper<SysFile>().eq(SysFile::getStorageId, config.getId()))
                    && (!Objects.equals(previous.getPlatform(), config.getPlatform())
                    || !Objects.equals(previous.getBasePath(), config.getBasePath())
                    || !Objects.equals(previous.getEndpoint(), config.getEndpoint())
                    || !Objects.equals(previous.getBucket(), config.getBucket())
                    || !Objects.equals(previous.getRegion(), config.getRegion()))) {
                throw new ServiceException("该配置已有文件，不能更改存储位置，请新增配置后切换默认存储");
            }
            if (!storageService.updateById(config)) {
                throw new ServiceException("修改存储配置失败");
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 在同一事务中切换默认存储。
     *
     * @param id 配置ID
     */
    @Override
    public void updateDefaultStorage(Long id) {
        RLock lock = redissonClient.getReadWriteLock(STORAGE_LOCK).writeLock();
        lock.lock();
        try {
            getStorage(id);
            // 使用真实配置表和唯一索引，锁覆盖提交，避免并发切换出现多个默认项。
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                storageService.update(new SysFileStorage(), new LambdaUpdateWrapper<SysFileStorage>()
                        .eq(SysFileStorage::getDefaultFlag, 1).set(SysFileStorage::getDefaultFlag, null));
                SysFileStorage config = new SysFileStorage();
                config.setId(id);
                config.setDefaultFlag(1);
                if (!storageService.updateById(config)) {
                    throw new ServiceException("切换默认存储失败");
                }
            });
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除存储对象后逻辑删除记录，失败时保留记录供重试。
     *
     * @param id 文件ID
     */
    @Override
    public void deleteFile(Long id) {
        RLock lock = redissonClient.getReadWriteLock(STORAGE_LOCK).readLock();
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除没有文件引用的非默认配置。
     *
     * @param id 配置ID
     */
    @Override
    public void deleteStorage(Long id) {
        RLock lock = redissonClient.getReadWriteLock(STORAGE_LOCK).writeLock();
        lock.lock();
        try {
            SysFileStorage config = getStorage(id);
            if (Integer.valueOf(1).equals(config.getDefaultFlag())) {
                throw new ServiceException("默认存储不能删除，请先切换默认存储");
            }
            if (exists(new LambdaQueryWrapper<SysFile>().eq(SysFile::getStorageId, id))) {
                throw new ServiceException("存储配置仍有文件，不能删除");
            }
            if (!storageService.removeById(id)) {
                throw new ServiceException("删除存储配置失败");
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 通过认证接口流式下载文件，禁止上传内容在应用域直接执行。
     *
     * @param id 文件ID
     * @param response 下载响应
     */
    @Override
    public void downloadFile(Long id, HttpServletResponse response) {
        RLock lock = redissonClient.getReadWriteLock(STORAGE_LOCK).readLock();
        lock.lock();
        try {
            SysFile file = getFile(id);
            IFileStorage storage = openStorage(getStorage(file.getStorageId()));
            try (DownloadHandler download = storage.download(file.getObjectKey())) {
                response.setContentType("application/octet-stream");
                response.setContentLengthLong(file.getFileSize());
                response.setHeader("X-Content-Type-Options", "nosniff");
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
        } finally {
            lock.unlock();
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
     * 查询必须存在的存储配置。
     *
     * @param id 配置ID
     * @return 存储配置
     */
    private SysFileStorage getStorage(Long id) {
        SysFileStorage config = storageService.getById(id);
        if (config == null) {
            throw new ServiceException("存储配置不存在");
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
            try {
                config.setBasePath(Path.of(config.getBasePath()).toAbsolutePath().normalize().toString());
            } catch (java.nio.file.InvalidPathException exception) {
                throw new ServiceException("本地存储目录格式无效");
            }
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
    private IFileStorage openStorage(SysFileStorage config) {
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
