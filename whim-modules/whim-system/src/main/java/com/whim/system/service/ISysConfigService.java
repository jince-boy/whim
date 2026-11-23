package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.config.SysConfigAddDTO;
import com.whim.system.model.dto.config.SysConfigQueryDTO;
import com.whim.system.model.dto.config.SysConfigUpdateDTO;
import com.whim.system.model.entity.SysConfig;
import com.whim.system.model.vo.config.SiteSettingsVO;
import com.whim.system.model.vo.config.SysConfigVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 业务配置管理。
 */
public interface ISysConfigService extends IService<SysConfig> {
    /**
     * 分页查询指定分类的配置。
     *
     * @param query 分类、筛选条件和分页参数
     * @return 配置分页数据
     */
    PageDataVO<SysConfigVO> getConfigPage(SysConfigQueryDTO query);

    /**
     * 查询当前是否需要登录验证码。
     *
     * @return 是否启用验证码校验
     */
    boolean isLoginCaptchaEnabled();

    /**
     * 查询可在登录前展示的网站信息。
     *
     * @return 网站标题、介绍、版权和当前 Logo 文件标识
     */
    SiteSettingsVO getSiteSettings();

    /**
     * 获取正在使用的公开系统 Logo 缩略图。
     *
     * @param id 网站配置返回的 Logo 文件ID
     * @return PNG 图片，不可用时为空数组
     */
    byte[] getSiteLogo(Long id);

    /**
     * 根据配置记录读取其保存的图片预览。
     *
     * @param id 配置记录ID
     * @return PNG 图片，不可用时为空数组
     */
    byte[] getConfigImage(Long id);

    /**
     * 将配置图片保存到默认文件存储。
     *
     * @param file 本地图片
     * @return 可写入配置值的文件ID
     */
    Long addConfigImage(MultipartFile file);

    /**
     * 新增自定义配置。
     *
     * @param parameter 配置名称、键、值类型、选项、配置值与备注
     */
    void addConfig(SysConfigAddDTO parameter);

    /**
     * 修改配置值，自定义配置还可调整名称、类型、选项和备注。
     *
     * @param parameter 配置ID与可修改字段
     */
    void updateConfig(SysConfigUpdateDTO parameter);

    /**
     * 按主键删除自定义配置。
     *
     * @param id 配置主键
     */
    void deleteConfigById(Long id);
}
