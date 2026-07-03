package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysConfig;

import java.util.List;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 系统配置表服务接口
 */
public interface ISysConfigService extends IService<SysConfig> {
    /**
     * 查询系统配置列表。
     *
     * @return 系统配置列表
     */
    List<SysConfig> listConfig();

    /**
     * 查询系统配置详情。
     *
     * @param id 配置ID
     * @return 系统配置详情
     */
    SysConfig getConfigDetail(Long id);

    /**
     * 根据配置键获取配置值。
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 新增系统配置。
     *
     * @param sysConfig 系统配置
     * @return 最新系统配置
     */
    SysConfig createConfig(SysConfig sysConfig);

    /**
     * 修改系统配置。
     *
     * @param sysConfig 系统配置
     * @return 最新系统配置
     */
    SysConfig updateConfig(SysConfig sysConfig);

    /**
     * 删除系统配置。
     *
     * @param id 配置ID
     * @return 是否删除成功
     */
    boolean deleteConfig(Long id);
}

