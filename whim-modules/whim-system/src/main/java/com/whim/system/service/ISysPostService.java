package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.post.SysPostMemberDTO;
import com.whim.system.model.dto.post.SysPostMemberQueryDTO;
import com.whim.system.model.dto.post.SysPostQueryDTO;
import com.whim.system.model.dto.post.SysPostSaveDTO;
import com.whim.system.model.dto.post.SysPostUpdateDTO;
import com.whim.system.model.dto.organization.SysOrganizationStatusDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.vo.post.SysPostMemberVO;
import com.whim.system.model.vo.post.SysPostVO;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位及岗位成员管理服务。
 */
public interface ISysPostService extends IService<SysPost> {
    /**
     * 按条件分页查询授权范围内的岗位。
     *
     * @param query 查询参数
     * @return 岗位分页数据
     */
    PageDataVO<SysPostVO> getPostPage(SysPostQueryDTO query);

    /**
     * 分页查询岗位已分配成员或尚未分配的正常用户。
     *
     * @param id 岗位ID
     * @param query 查询参数
     * @return 用户数据权限过滤后的分页数据
     */
    PageDataVO<SysPostMemberVO> getPostMemberPage(Long id, SysPostMemberQueryDTO query);

    /**
     * 查询授权范围内的岗位详情。
     *
     * @param id 岗位ID
     * @return 岗位详情
     */
    SysPostVO getPostDetail(Long id);

    /**
     * 新增岗位，不授予任何角色。
     *
     * @param parameter 岗位新增参数
     * @return 新增岗位ID
     */
    Long addPost(SysPostSaveDTO parameter);

    /**
     * 为岗位增量添加授权范围内的正常用户，重复添加不产生新关联。
     *
     * @param id 岗位ID
     * @param parameter 待添加的用户集合
     */
    void addPostMembers(Long id, SysPostMemberDTO parameter);

    /**
     * 完整修改岗位并校验新的部门归属。
     *
     * @param parameter 岗位修改参数
     */
    void updatePost(SysPostUpdateDTO parameter);

    /**
     * 修改岗位状态，启用时校验所属部门及祖先链。
     *
     * @param parameter 包含岗位ID和状态的修改参数
     */
    void updatePostStatus(SysOrganizationStatusDTO parameter);

    /**
     * 删除不含成员的岗位。
     *
     * @param id 岗位ID
     */
    void deletePost(Long id);

    /**
     * 解除明确指定的用户与岗位的关联，保留其他成员。
     *
     * @param id 岗位ID
     * @param parameter 待解除关联的用户集合
     */
    void deletePostMembers(Long id, SysPostMemberDTO parameter);
}
