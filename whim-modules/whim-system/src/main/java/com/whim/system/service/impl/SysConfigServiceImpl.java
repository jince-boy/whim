package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.constant.CacheKeys;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.file.support.FileUploadSecurity;
import com.whim.file.support.ImageThumbnailGenerator;
import com.whim.json.utils.JsonUtils;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.mapper.SysConfigMapper;
import com.whim.system.model.dto.config.SysConfigAddDTO;
import com.whim.system.model.dto.config.SysConfigQueryDTO;
import com.whim.system.model.dto.config.SysConfigUpdateDTO;
import com.whim.system.model.dto.file.SysFileUploadDTO;
import com.whim.system.model.entity.SysConfig;
import com.whim.system.model.entity.SysConfig.ConfigOption;
import com.whim.system.model.entity.SysFile;
import com.whim.system.model.enums.ConfigValueType;
import com.whim.system.model.vo.config.SiteSettingsVO;
import com.whim.system.model.vo.config.SysConfigVO;
import com.whim.system.service.ISysConfigService;
import com.whim.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 业务配置读写与内置项保护。
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {
    private static final String CONFIG_CACHE_LOCK = CacheKeys.SYS_CONFIG + ":lock";
    private static final String LOGIN_CAPTCHA_KEY = "system.captcha.enabled";
    private static final String SITE_TITLE = "system.site.title";
    private static final String SITE_DESCRIPTION = "system.site.description";
    private static final String SITE_COPYRIGHT = "system.site.copyright";
    private static final String SITE_LOGO = "system.site.logo";
    private static final String CONFIG_IMAGE_SOURCE = "configImage";
    private static final Set<String> IMAGE_TYPES = Set.of("image/png", "image/jpeg", "image/gif");
    private final ISysFileService fileService;
    private final RedissonClient redissonClient;
    private final PlatformTransactionManager transactionManager;

    /**
     * 分页查询指定分类的配置。
     *
     * @param query 分类、筛选条件和分页参数
     * @return 配置分页数据
     */
    @Override
    public PageDataVO<SysConfigVO> getConfigPage(SysConfigQueryDTO query) {
        LambdaQueryWrapper<SysConfig> conditions = new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getBuiltIn, query.getBuiltIn())
                .like(StringUtils.isNotBlank(query.getConfigName()), SysConfig::getConfigName, query.getConfigName())
                .like(StringUtils.isNotBlank(query.getConfigKey()), SysConfig::getConfigKey, query.getConfigKey())
                .like(StringUtils.isNotBlank(query.getRemark()), SysConfig::getRemark, query.getRemark())
                .orderByAsc(SysConfig::getId);
        return new PageDataVO<>(page(query.getPage(), conditions)
                .convert(config -> BeanConvertUtils.convert(config, SysConfigVO.class)));
    }

    /**
     * 按保存时的配置键查询业务值。
     *
     * @param configKey 内置或自定义配置键
     * @return 配置值，键不存在或未设置值时为 null
     */
    @Override
    public String getConfigValueByKey(String configKey) {
        if (StringUtils.isBlank(configKey)) {
            return null;
        }
        return getCachedConfigValues(false, configKey).get(configKey);
    }

    /**
     * 查询当前是否需要登录验证码。
     *
     * @return 是否启用验证码校验
     */
    @Override
    public boolean isLoginCaptchaEnabled() {
        // 只有可信内置配置显式关闭时才跳过验证码，缺失或异常的值按开启处理。
        return !"false".equals(getCachedConfigValues(true, LOGIN_CAPTCHA_KEY).get(LOGIN_CAPTCHA_KEY));
    }

    /**
     * 获取允许在登录前展示的网站信息。
     *
     * @return 网站标题、介绍、版权和当前 Logo 文件标识
     */
    @Override
    public SiteSettingsVO getSiteSettings() {
        // 公共接口只读取明确列举的内置键，新配置和自定义值不会被自动公开。
        Map<String, String> settings = getCachedConfigValues(true, SITE_TITLE, SITE_DESCRIPTION, SITE_COPYRIGHT, SITE_LOGO);
        SiteSettingsVO settingsView = new SiteSettingsVO();
        settingsView.setTitle(settings.get(SITE_TITLE));
        settingsView.setDescription(settings.get(SITE_DESCRIPTION));
        settingsView.setCopyright(settings.get(SITE_COPYRIGHT));
        settingsView.setLogoFileId(StringUtils.trimToNull(settings.get(SITE_LOGO)));
        return settingsView;
    }

    /**
     * 获取登录前可见的当前系统 Logo。
     *
     * @param id 网站配置返回的 Logo 文件ID
     * @return PNG 缩略图，不可用时为空数组
     */
    @Override
    public byte[] getSiteLogo(Long id) {
        // 公开读取仅匹配当前内置 Logo，旧 Logo 和自定义配置图片不能凭文件ID匿名访问。
        if (!id.toString().equals(getCachedConfigValues(true, SITE_LOGO).get(SITE_LOGO))) {
            return new byte[0];
        }
        return fileService.getFileImageByUploadSource(id, CONFIG_IMAGE_SOURCE, 96);
    }

    /**
     * 读取指定配置已保存的图片。
     *
     * @param id 配置记录ID
     * @return PNG 预览，不可用时为空数组
     */
    @Override
    public byte[] getConfigImage(Long id) {
        SysConfig config = lambdaQuery().select(SysConfig::getConfigValue).eq(SysConfig::getId, id)
                .eq(SysConfig::getValueType, ConfigValueType.IMAGE).one();
        if (config == null || StringUtils.isBlank(config.getConfigValue())) {
            return new byte[0];
        }
        return fileService.getFileImageByUploadSource(Long.valueOf(config.getConfigValue()), CONFIG_IMAGE_SOURCE, 96);
    }

    /**
     * 校验本地图片并通过默认文件存储上传。
     *
     * @param file 待保存的图片
     * @return 可写入配置值的文件ID
     */
    @Override
    public Long addConfigImage(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > ImageThumbnailGenerator.MAXIMUM_SOURCE_BYTES) {
            throw new IllegalArgumentException("请选择不超过20 MiB的图片");
        }
        // 公开 Logo 只展示可安全解码的位图，文件名和浏览器声明的类型均不能代替内容检查。
        try (InputStream header = file.getInputStream()) {
            if (!IMAGE_TYPES.contains(FileUploadSecurity.getContentType(header))
                    || ImageThumbnailGenerator.generateThumbnail(file.getInputStream(), 96, 96).length == 0) {
                throw new IllegalArgumentException("请选择可正常解码的 PNG、JPG 或 GIF 图片");
            }
        } catch (IOException exception) {
            throw new ServiceException("读取配置图片失败", exception);
        }
        SysFileUploadDTO upload = new SysFileUploadDTO();
        upload.setFile(file);
        upload.setUploadSource(CONFIG_IMAGE_SOURCE);
        return fileService.addFile(upload).getId();
    }

    /**
     * 新增自定义配置。
     *
     * @param parameter 配置名称、键、值类型、选项、配置值与备注
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConfig(SysConfigAddDTO parameter) {
        SysConfig config = BeanConvertUtils.convert(parameter, SysConfig.class);
        // 内置身份只由迁移脚本指定，业务入口始终创建自定义配置。
        config.setBuiltIn(false);
        if (config.getValueType() == null) {
            config.setValueType(ConfigValueType.TEXT);
        }
        validateConfigValue(config);
        if (!save(config)) {
            throw new ServiceException("新增配置失败");
        }
        invalidateConfigCacheAfterCommit(config.getConfigKey());
    }

    /**
     * 修改配置值，自定义配置还可调整名称、类型、选项和备注。
     *
     * @param parameter 配置ID与可修改字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(SysConfigUpdateDTO parameter) {
        SysConfig current = lambdaQuery()
                .select(SysConfig::getBuiltIn, SysConfig::getConfigKey, SysConfig::getValueType, SysConfig::getValueOptions)
                .eq(SysConfig::getId, parameter.getId()).one();
        if (current == null) {
            throw new IllegalArgumentException("配置不存在或已删除");
        }
        // 内置身份以数据库为准，内置项只接受配置值变更。
        if (current.getBuiltIn() && (parameter.getRemark() != null || parameter.getConfigName() != null
                || parameter.getValueType() != null || parameter.getValueOptions() != null)) {
            throw new IllegalArgumentException("系统内置配置仅允许修改配置值");
        }
        if (parameter.getConfigName() != null && parameter.getConfigName().isBlank()) {
            throw new IllegalArgumentException("配置名称不能为空");
        }
        // 值、类型和选项按同一份定义校验并一次写入，旧客户端可以只提交值。
        SysConfig update = BeanConvertUtils.convert(parameter, SysConfig.class);
        if (update.getValueType() == null) {
            update.setValueType(current.getValueType());
        }
        if (update.getValueOptions() == null && update.getValueType() == current.getValueType()) {
            update.setValueOptions(current.getValueOptions());
        }
        validateConfigValue(update);
        if (!updateById(update)) {
            throw new IllegalArgumentException("配置不存在或已删除");
        }
        invalidateConfigCacheAfterCommit(current.getConfigKey());
    }

    /**
     * 按主键删除自定义配置。
     *
     * @param id 配置主键
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigById(Long id) {
        SysConfig config = lambdaQuery().select(SysConfig::getBuiltIn, SysConfig::getConfigKey).eq(SysConfig::getId, id).one();
        if (config == null) {
            throw new IllegalArgumentException("配置不存在或已删除");
        }
        // 即使操作者具有删除权限，系统内置配置仍受保护。
        if (config.getBuiltIn()) {
            throw new AccessDeniedException("禁止删除系统内置配置");
        }
        if (!removeById(id)) {
            throw new IllegalArgumentException("配置不存在或已删除");
        }
        invalidateConfigCacheAfterCommit(config.getConfigKey());
    }

    /**
     * 管理员手动刷新时，从数据库重建两类配置的缓存。
     */
    @Override
    public void refreshConfigCache() {
        loadConfigValues(Set.of(), true);
    }

    /**
     * 批量读取指定键，首次读取时只查询未命中的配置。
     *
     * @param builtInOnly 是否仅接受内置配置
     * @param configKeys 本次读取的配置键
     * @return 符合类别要求的配置值
     */
    private Map<String, String> getCachedConfigValues(boolean builtInOnly, String... configKeys) {
        Set<String> keys = Set.of(configKeys);
        RMapCache<String, SysConfig> cache = redissonClient.getMapCache(CacheKeys.SYS_CONFIG);
        Map<String, SysConfig> configs = cache.getAll(keys);
        if (configs.size() < keys.size()) {
            configs = loadConfigValues(keys, false);
        }
        Map<String, String> values = new HashMap<>();
        for (String key : keys) {
            SysConfig config = configs.get(key);
            if (config != null && (!builtInOnly || Boolean.TRUE.equals(config.getBuiltIn()))) {
                values.put(key, config.getConfigValue());
            }
        }
        return values;
    }

    /**
     * 加载未命中的配置，手动刷新时替换全部缓存项。
     *
     * @param keys 本次请求的配置键
     * @param refreshAll 是否手动刷新全部配置
     * @return 已加载的配置
     */
    private Map<String, SysConfig> loadConfigValues(Set<String> keys, boolean refreshAll) {
        // 先取得独立读事务的连接，再持有配置锁，避免等待连接时阻塞其他节点提交。
        TransactionTemplate committedRead = new TransactionTemplate(transactionManager);
        committedRead.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        committedRead.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        committedRead.setReadOnly(true);
        return committedRead.execute(status -> {
            RLock lock = redissonClient.getLock(CONFIG_CACHE_LOCK);
            lock.lock();
            try {
                RMapCache<String, SysConfig> cache = redissonClient.getMapCache(CacheKeys.SYS_CONFIG);
                Set<String> missing = new HashSet<>(keys);
                Map<String, SysConfig> result = new HashMap<>();
                if (!refreshAll) {
                    // 等待锁期间其他节点可能已加载这些键，只查询仍缺少的配置。
                    result.putAll(cache.getAll(keys));
                    missing.removeAll(result.keySet());
                    if (missing.isEmpty()) {
                        return result;
                    }
                }
                List<SysConfig> configs = lambdaQuery()
                        .select(SysConfig::getConfigKey, SysConfig::getConfigValue, SysConfig::getBuiltIn)
                        .in(!refreshAll, SysConfig::getConfigKey, missing).list();
                Map<String, SysConfig> loaded = new HashMap<>();
                for (SysConfig config : configs) {
                    loaded.put(config.getConfigKey(), config);
                }
                if (refreshAll) {
                    // 手动刷新也要清除数据库中已经删除的配置。
                    cache.clear();
                } else {
                    // 未配置的键同样限时缓存；随后新增该键会在提交后清除占位。
                    missing.forEach(key -> loaded.computeIfAbsent(key, unused -> new SysConfig()));
                }
                cache.putAll(loaded, 10, TimeUnit.MINUTES);
                result.putAll(loaded);
                return result;
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * 写入提交后使对应键失效，下次读取时重新加载。
     *
     * @param configKey 发生变更的配置键
     */
    private void invalidateConfigCacheAfterCommit(String configKey) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 与回源共用配置锁，防止并发查询在失效后写回旧值。
             */
            @Override
            public void afterCommit() {
                RLock lock = redissonClient.getLock(CONFIG_CACHE_LOCK);
                lock.lock();
                try {
                    redissonClient.getMapCache(CacheKeys.SYS_CONFIG).fastRemove(configKey);
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    /**
     * 校验配置值和选项定义，使保存后的值能用对应控件重新打开。
     *
     * @param config 本次保存的完整值定义
     */
    private void validateConfigValue(SysConfig config) {
        List<ConfigOption> options = config.getValueOptions();
        if (config.getValueType() == ConfigValueType.RADIO) {
            if (options == null || options.isEmpty()) {
                throw new IllegalArgumentException("单选配置至少需要一个选项");
            }
            Set<String> labels = new HashSet<>();
            Set<String> values = new HashSet<>();
            for (ConfigOption option : options) {
                if (!labels.add(option.getLabel().strip())) {
                    throw new IllegalArgumentException("单选选项名称不能重复");
                }
                if (!values.add(option.getValue())) {
                    throw new IllegalArgumentException("单选选项值不能重复");
                }
            }
            if (!values.contains(config.getConfigValue())) {
                throw new IllegalArgumentException("配置值必须是已定义的单选选项");
            }
            return;
        }
        if (options != null && !options.isEmpty()) {
            throw new IllegalArgumentException("只有单选类型可以设置选项");
        }
        // 转换为其他类型时，必须同时清除旧选项。
        config.setValueOptions(List.of());
        if (config.getValueType() == ConfigValueType.IMAGE && !config.getConfigValue().isEmpty()) {
            String value = config.getConfigValue();
            long fileId = NumberUtils.toLong(value);
            // 配置只能引用配置图片；拒绝任意地址和其他业务的文件，空字符串用于清空。
            if (fileId <= 0 || !Long.toString(fileId).equals(value)
                    || !fileService.lambdaQuery().eq(SysFile::getId, fileId)
                    .eq(SysFile::getUploadSource, CONFIG_IMAGE_SOURCE).in(SysFile::getContentType, IMAGE_TYPES).exists()) {
                throw new IllegalArgumentException("请选择有效的配置图片");
            }
        }
        if (config.getValueType() == ConfigValueType.JSON) {
            try {
                JsonNode value = JsonUtils.getJsonMapper().readerFor(JsonNode.class)
                        .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(config.getConfigValue());
                if (value == null || value.isMissingNode()) {
                    throw new IllegalArgumentException("请输入有效的 JSON 配置值");
                }
            } catch (JacksonException exception) {
                throw new IllegalArgumentException("请输入有效的 JSON 配置值");
            }
        }
    }
}
