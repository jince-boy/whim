package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.json.utils.JsonUtils;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.mapper.SysConfigMapper;
import com.whim.system.model.config.ConfigOption;
import com.whim.system.model.dto.config.SysConfigAddDTO;
import com.whim.system.model.dto.config.SysConfigQueryDTO;
import com.whim.system.model.dto.config.SysConfigUpdateDTO;
import com.whim.system.model.entity.SysConfig;
import com.whim.system.model.enums.ConfigValueType;
import com.whim.system.model.vo.config.SiteSettingsVO;
import com.whim.system.model.vo.config.SysConfigVO;
import com.whim.system.service.ISysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 业务配置读写与内置项保护。
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {
    private static final String LOGIN_CAPTCHA_KEY = "system.captcha.enabled";
    private static final String SITE_TITLE = "system.site.title";
    private static final String SITE_DESCRIPTION = "system.site.description";
    private static final String SITE_COPYRIGHT = "system.site.copyright";

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
     * 查询当前是否需要登录验证码。
     *
     * @return 是否启用验证码校验
     */
    @Override
    public boolean isLoginCaptchaEnabled() {
        // 只有可信内置配置显式关闭时才跳过验证码，缺失或异常的值按开启处理。
        SysConfig config = lambdaQuery().select(SysConfig::getConfigValue)
                .eq(SysConfig::getConfigKey, LOGIN_CAPTCHA_KEY).eq(SysConfig::getBuiltIn, true).one();
        return config == null || !"false".equals(config.getConfigValue());
    }

    /**
     * 获取允许在登录前展示的网站信息。
     *
     * @return 网站标题、介绍和版权信息
     */
    @Override
    public SiteSettingsVO getSiteSettings() {
        // 公共接口只读取明确列举的内置键，新配置和自定义值不会被自动公开。
        List<SysConfig> settings = lambdaQuery().select(SysConfig::getConfigKey, SysConfig::getConfigValue)
                .eq(SysConfig::getBuiltIn, true).in(SysConfig::getConfigKey, SITE_TITLE, SITE_DESCRIPTION, SITE_COPYRIGHT).list();
        SiteSettingsVO settingsView = new SiteSettingsVO();
        for (SysConfig setting : settings) {
            switch (setting.getConfigKey()) {
                case SITE_TITLE -> settingsView.setTitle(setting.getConfigValue());
                case SITE_DESCRIPTION -> settingsView.setDescription(setting.getConfigValue());
                case SITE_COPYRIGHT -> settingsView.setCopyright(setting.getConfigValue());
            }
        }
        return settingsView;
    }

    /**
     * 新增自定义配置。
     *
     * @param parameter 配置名称、键、值类型、选项、配置值与备注
     */
    @Override
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
    }

    /**
     * 修改配置值，自定义配置还可调整名称、类型、选项和备注。
     *
     * @param parameter 配置ID与可修改字段
     */
    @Override
    public void updateConfig(SysConfigUpdateDTO parameter) {
        SysConfig current = lambdaQuery().select(SysConfig::getBuiltIn, SysConfig::getValueType, SysConfig::getValueOptions)
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
    }

    /**
     * 按主键删除自定义配置。
     *
     * @param id 配置主键
     */
    @Override
    public void deleteConfigById(Long id) {
        SysConfig config = lambdaQuery().select(SysConfig::getBuiltIn).eq(SysConfig::getId, id).one();
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
