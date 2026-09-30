package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.dto.post.PostSaveDTO;
import com.whim.system.model.vo.post.PostVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description SysPost业务服务。
 */
public interface ISysPostService extends IService<SysPost> {
    /** 分页查询可见岗位，计数与列表使用同一范围。 */
    PageDataVO<PostVO> pagePosts(PageQueryDTO query);
    /** 查询本次操作可见岗位详情。 */
    PostVO getPost(Long postId);
    /** 查询岗位中本次操作可见的用户ID。 */
    Set<Long> getPostMemberIds(Long postId);
    /** 校验归属后创建岗位。 */
    Long createPost(PostSaveDTO request);
    /** 校验原记录及新归属后修改岗位。 */
    void updatePost(Long postId, PostSaveDTO request);
    /** 启用或停用可见岗位。 */
    void setPostStatus(Long postId, Integer status);
    /** 全量校验后覆盖岗位用户关联，不改变任何角色授权。 */
    void replacePostMembers(Long postId, Set<Long> userIds);
    /** 删除没有用户关联的岗位。 */
    void deletePost(Long postId);
}

