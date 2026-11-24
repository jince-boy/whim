package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.homepage.HomeTargetQueryDTO;
import com.whim.system.model.dto.homepage.SysHomePageAddDTO;
import com.whim.system.model.dto.homepage.SysHomePageQueryDTO;
import com.whim.system.model.dto.homepage.SysHomePageUpdateDTO;
import com.whim.system.model.entity.SysHomePage;
import com.whim.system.model.vo.homepage.HomeRouteVO;
import com.whim.system.model.vo.homepage.HomeTargetVO;
import com.whim.system.model.vo.homepage.SysHomePageVO;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 首页配置维护与个人首页解析。
 */
public interface ISysHomePageService extends IService<SysHomePage> {
    /**
     * 分页查询首页配置。
     *
     * @param query 分页和筛选条件
     * @return 首页配置分页
     */
    PageDataVO<SysHomePageVO> getHomePageConfigPage(SysHomePageQueryDTO query);

    /**
     * 搜索可关联的用户、角色或部门。
     *
     * @param query 类型、关键词和分页条件
     * @return 关联对象分页
     */
    PageDataVO<HomeTargetVO> getHomeTargetPage(HomeTargetQueryDTO query);

    /**
     * 按优先顺序查询当前账号可匹配的首页。
     *
     * @return 首页路由候选
     */
    List<HomeRouteVO> getCurrentHomeRoutes();

    /**
     * 新增首页配置。
     *
     * @param parameter 首页配置
     */
    void addHomePage(SysHomePageAddDTO parameter);

    /**
     * 更新首页配置。
     *
     * @param parameter 首页配置和主键
     */
    void updateHomePage(SysHomePageUpdateDTO parameter);

    /**
     * 删除首页配置。
     *
     * @param id 配置主键
     */
    void deleteHomePage(Long id);
}
