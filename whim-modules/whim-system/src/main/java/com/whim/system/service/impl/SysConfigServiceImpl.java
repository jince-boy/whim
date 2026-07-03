package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.constant.CacheKeys;
import com.whim.core.exception.ServiceException;
import com.whim.redis.utils.CacheUtils;
import com.whim.system.mapper.SysConfigMapper;
import com.whim.system.model.entity.SysConfig;
import com.whim.system.service.ISysConfigService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 系统配置表服务实现类
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {
    /**
     * 查询系统配置列表。
     *
     * @return 系统配置列表
     */
    @Override
    public List<SysConfig> listConfig() {
        return list();
    }

    /**
     * 查询系统配置详情。
     *
     * @param id 配置ID
     * @return 系统配置详情
     */
    @Override
    public SysConfig getConfigDetail(Long id) {
        return getById(id);
    }

    /**
     * 根据配置键获取配置值，并优先读取二级缓存。
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @Override
    @Cacheable(cacheNames = CacheKeys.SYS_CONFIG, key = "#configKey", unless = "#result == null")
    public String getConfigValue(String configKey) {
        SysConfig sysConfig = lambdaQuery()
                .select(SysConfig::getConfigValue)
                .eq(SysConfig::getConfigKey, configKey)
                .one();
        return sysConfig == null ? null : sysConfig.getConfigValue();
    }

    /**
     * 新增系统配置，并清理同名配置值缓存。
     *
     * @param sysConfig 系统配置
     * @return 最新系统配置
     */
    @Override
    public SysConfig createConfig(SysConfig sysConfig) {
        boolean exists = lambdaQuery()
                .eq(SysConfig::getConfigKey, sysConfig.getConfigKey())
                .exists();
        if (exists) {
            throw new ServiceException("系统配置键已存在");
        }
        if (!save(sysConfig)) {
            throw new ServiceException("新增系统配置失败");
        }
        SysConfig latestConfig = getById(sysConfig.getId());
        CacheUtils.evict(CacheKeys.SYS_CONFIG, latestConfig.getConfigKey());
        return latestConfig;
    }

    /**
     * 修改系统配置，并清理同名配置值缓存。
     *
     * @param sysConfig 系统配置
     * @return 最新系统配置
     */
    @Override
    public SysConfig updateConfig(SysConfig sysConfig) {
        SysConfig oldConfig = getById(sysConfig.getId());
        if (oldConfig == null) {
            throw new ServiceException("系统配置不存在");
        }
        if (sysConfig.getConfigKey() == null || sysConfig.getConfigKey().isBlank()) {
            sysConfig.setConfigKey(oldConfig.getConfigKey());
        }
        if (!oldConfig.getConfigKey().equals(sysConfig.getConfigKey())) {
            throw new ServiceException("系统配置键不允许修改");
        }
        if (!updateById(sysConfig)) {
            throw new ServiceException("修改系统配置失败");
        }
        SysConfig latestConfig = getById(sysConfig.getId());
        CacheUtils.evict(CacheKeys.SYS_CONFIG, latestConfig.getConfigKey());
        return latestConfig;
    }

    /**
     * 删除系统配置，并清理同名配置值缓存。
     *
     * @param id 配置ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteConfig(Long id) {
        SysConfig oldConfig = getById(id);
        boolean removed = removeById(id);
        if (removed && oldConfig != null) {
            CacheUtils.evict(CacheKeys.SYS_CONFIG, oldConfig.getConfigKey());
        }
        return removed;
    }
}
