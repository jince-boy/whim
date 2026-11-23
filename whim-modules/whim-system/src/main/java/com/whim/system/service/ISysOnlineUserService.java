package com.whim.system.service;

import com.whim.system.model.dto.onlineuser.OnlineUserKickoutDTO;
import com.whim.system.model.dto.onlineuser.OnlineUserQueryDTO;
import com.whim.system.model.vo.onlineuser.OnlineUserPageVO;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 系统账号在线会话管理。
 */
public interface ISysOnlineUserService {
    /**
     * 分页查询当前操作可见的有效会话。
     *
     * @param query 筛选与分页条件
     * @return 会话分页及账号数
     */
    OnlineUserPageVO getOnlineUserPage(OnlineUserQueryDTO query);

    /**
     * 将目标账号的一个会话强制下线。
     *
     * @param parameter 账号和会话标识
     */
    void kickoutOnlineUser(OnlineUserKickoutDTO parameter);
}
