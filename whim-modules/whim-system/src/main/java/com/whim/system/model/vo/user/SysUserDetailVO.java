package com.whim.system.model.vo.user;

import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.post.SysPostVO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户编辑详情及当前岗位，不包含角色授权或密码。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysUser.class, convertGenerate = false)
public class SysUserDetailVO extends SysUserVO {
    /** 当前任职岗位，包含已停用但未删除的岗位，保证编辑回显。 */
    private List<SysPostVO> posts = new ArrayList<>();
}
