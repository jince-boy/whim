package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whim.system.model.dto.post.SysPostMemberQueryDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.vo.post.SysPostMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位持久化及成员分页查询。
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
    /**
     * 分页查询岗位已分配成员或可选用户，不返回密码和联系方式。
     *
     * @param page 分页参数
     * @param postId 已通过本次操作权限校验的岗位ID
     * @param query 成员筛选参数
     * @return 经过用户数据权限过滤的分页结果
     */
    IPage<SysPostMemberVO> getPostMemberPage(Page<SysPostMemberVO> page, @Param("postId") Long postId,
                                          @Param("query") SysPostMemberQueryDTO query);
}
