package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whim.system.model.dto.homepage.SysHomePageQueryDTO;
import com.whim.system.model.entity.SysHomePage;
import com.whim.system.model.vo.homepage.HomeRouteVO;
import com.whim.system.model.vo.homepage.SysHomePageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 首页配置持久化。
 */
@Mapper
public interface SysHomePageMapper extends BaseMapper<SysHomePage> {
    /**
     * 按关联类型联查对象名称，避免分页后逐条查询。
     *
     * @param page 分页参数
     * @param query 筛选条件
     * @return 首页配置分页
     */
    Page<SysHomePageVO> getHomePageConfigPage(Page<SysHomePageVO> page, @Param("query") SysHomePageQueryDTO query);

    /**
     * 合并账号匹配规则与当前路由归属，返回已启用的候选页面。
     *
     * @param userId 当前用户ID
     * @param roleIds 登录快照中的有效角色ID
     * @param departmentId 有效主部门ID
     * @return 按匹配顺序排列的首页路由
     */
    List<HomeRouteVO> getCurrentHomeRoutes(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds,
                                           @Param("departmentId") Long departmentId);
}
