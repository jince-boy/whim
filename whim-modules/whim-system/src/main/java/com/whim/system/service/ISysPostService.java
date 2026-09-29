package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.post.PostSaveDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.vo.post.PostVO;

import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统岗位表服务接口
 */
public interface ISysPostService extends IService<SysPost> {
    /** 分页查询本次列表权限可见的岗位。 */
    PageDataVO<PostVO> pageCurrentTenantPosts(PageQueryDTO query);

    /** 查询本次详情权限可见的岗位。 */
    PostVO getCurrentTenantPost(Long postId);

    /** 查询本次成员查看权限可见岗位的成员ID。 */
    Set<Long> getCurrentTenantPostMemberIds(Long postId);

    /** 在当前租户创建岗位。 */
    Long createCurrentTenantPost(PostSaveDTO request);

    /** 修改本次更新权限可操作的岗位。 */
    void updateCurrentTenantPost(Long postId, PostSaveDTO request);

    /** 启用或停用本次状态权限可操作的岗位。 */
    void setCurrentTenantPostStatus(Long postId, Integer status);

    /** 覆盖本次成员分配权限可操作岗位的成员。 */
    void replaceCurrentTenantPostMembers(Long postId, Set<Long> userIds);

    /** 删除本次删除权限可操作且未关联成员的岗位。 */
    void deleteCurrentTenantPost(Long postId);
}

