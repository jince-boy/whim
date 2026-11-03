package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.post.SysPostAddDTO;
import com.whim.system.model.dto.post.SysPostMemberQueryDTO;
import com.whim.system.model.dto.post.SysPostMembersDTO;
import com.whim.system.model.dto.post.SysPostQueryDTO;
import com.whim.system.model.dto.post.SysPostStatusDTO;
import com.whim.system.model.dto.post.SysPostUpdateDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.vo.post.SysPostMemberVO;
import com.whim.system.model.vo.post.SysPostVO;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位管理及成员分配服务。
 */
public interface ISysPostService extends IService<SysPost> {
    /**
     * 按名称、编码、部门和状态分页查询可见岗位。
     *
     * @param query 岗位筛选及分页条件
     * @return 岗位分页
     */
    PageDataVO<SysPostVO> getPostPage(SysPostQueryDTO query);

    /**
     * 分页查询岗位已分配成员或未分配的正常用户。
     *
     * @param query 岗位ID、成员类型及用户筛选条件
     * @return 岗位和用户均在本次操作范围内的用户分页
     */
    PageDataVO<SysPostMemberVO> getPostMemberPage(SysPostMemberQueryDTO query);

    /**
     * 查询岗位详情。
     *
     * @param id 岗位ID
     * @return 岗位详情
     */
    SysPostVO getPostDetail(Long id);

    /**
     * 新增岗位并校验部门归属。
     *
     * @param parameter 岗位新增参数
     */
    void addPost(SysPostAddDTO parameter);

    /**
     * 增量添加指定用户，已有成员不重复添加。
     *
     * @param parameter 岗位ID和本次选择的用户ID
     */
    void addPostMembers(SysPostMembersDTO parameter);

    /**
     * 修改岗位，提交后使任职信息变化的成员会话失效。
     *
     * @param parameter 岗位ID及完整修改字段
     */
    void updatePost(SysPostUpdateDTO parameter);

    /**
     * 修改岗位状态，保留其他字段并在提交后使成员会话失效。
     *
     * @param parameter 岗位ID和目标状态
     */
    void updatePostStatus(SysPostStatusDTO parameter);

    /**
     * 删除没有成员关联的岗位。
     *
     * @param id 岗位ID
     */
    void deletePost(Long id);

    /**
     * 解除明确指定的成员关联，不影响其他成员。
     *
     * @param parameter 岗位ID和本次选择的用户ID
     */
    void deletePostMembers(SysPostMembersDTO parameter);
}
