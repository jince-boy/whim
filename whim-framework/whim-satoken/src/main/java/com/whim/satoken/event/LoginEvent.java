package com.whim.satoken.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * date: 2025/8/17 22:25
 * description: 登录事件
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String username;
    private Integer status;
    private String remark;
}
