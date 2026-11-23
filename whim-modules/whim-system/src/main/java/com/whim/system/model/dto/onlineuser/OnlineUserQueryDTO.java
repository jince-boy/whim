package com.whim.system.model.dto.onlineuser;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 在线用户筛选和分页参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OnlineUserQueryDTO extends PageQueryDTO {
    @Size(max = 64, message = "用户名最多64个字符")
    private String username;
    @Size(max = 64, message = "登录IP最多64个字符")
    private String ipAddress;
}
