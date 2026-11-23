package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.OnlineSessionInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.core.utils.IPUtils;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.system.model.dto.onlineuser.OnlineUserKickoutDTO;
import com.whim.system.model.dto.onlineuser.OnlineUserQueryDTO;
import com.whim.system.model.vo.onlineuser.OnlineUserPageVO;
import com.whim.system.model.vo.onlineuser.OnlineUserVO;
import com.whim.system.service.ISysOnlineUserService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 按各操作的授权范围管理系统账号在线会话。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysOnlineUserServiceImpl implements ISysOnlineUserService {
    private static final Comparator<OnlineSessionInfo> RECENT_FIRST = Comparator
            .comparing(OnlineSessionInfo::getLastActiveTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(OnlineSessionInfo::getSessionId);
    private final AuthenticationSession authenticationSession;
    private final AuthenticationContext authenticationContext;
    private final ISysUserService userService;

    /**
     * 先过滤可见会话，再按最近活跃时间排序、计数并分页。
     *
     * @param query 筛选与分页条件
     * @return 当前页会话及筛选后的账号总数
     */
    @Override
    @DataPermission(permission = "monitor:onlineUser:page", tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public OnlineUserPageVO getOnlineUserPage(OnlineUserQueryDTO query) {
        String username = query.getUsername() == null ? "" : query.getUsername().strip();
        String ipAddress = query.getIpAddress() == null ? "" : query.getIpAddress().strip();
        Page<OnlineUserVO> page = query.getPage();
        long offset = page.offset();
        long windowSize = offset + page.getSize();
        Set<Long> accounts = new HashSet<>();
        PriorityQueue<OnlineSessionInfo> recentSessions = new PriorityQueue<>(RECENT_FIRST.reversed());
        long total = 0;
        try (Stream<OnlineSessionInfo> sessions = authenticationSession.getOnlineSessions(AuthUserType.SYSTEM)) {
            // 在线用户的“本人”指账号本人，未分配部门的用户同样可由本人或全范围角色查看。
            Iterator<OnlineSessionInfo> matches = sessions.filter(row -> DataPermissionContext.allowsOwnership(
                            "sys_user", row.getUserId(), row.getDeptId()))
                    .filter(row -> username.isEmpty() || Strings.CI.contains(row.getUsername(), username))
                    .filter(row -> ipAddress.isEmpty() || Strings.CI.contains(row.getIpAddress(), ipAddress))
                    .iterator();
            while (matches.hasNext()) {
                OnlineSessionInfo session = matches.next();
                total++;
                accounts.add(session.getUserId());
                // 统计覆盖全部命中记录，仅保留排在本页末尾及之前的会话；堆顶始终是其中最旧的一条。
                if (recentSessions.size() < windowSize) {
                    recentSessions.add(session);
                } else if (RECENT_FIRST.compare(session, recentSessions.element()) < 0) {
                    recentSessions.remove();
                    recentSessions.add(session);
                }
            }
        }
        page.setTotal(total);
        List<OnlineSessionInfo> selected = recentSessions.stream().sorted(RECENT_FIRST).skip(offset).toList();
        List<OnlineUserVO> rows = BeanConvertUtils.convertList(selected, OnlineUserVO.class);
        Set<Long> candidates = rows.stream().filter(row -> !row.isCurrent()).map(OnlineUserVO::getUserId).collect(Collectors.toSet());
        UserInfo operator = authenticationContext.getCurrentUserInfo();
        // 同一账号的查看和强退范围可能来自不同角色，仅对本页候选用户批量计算按钮权限。
        Set<Long> allowed = !candidates.isEmpty() && (operator.isSuperAdministrator()
                || operator.getPermissionCodeSet().contains("monitor:onlineUser:kickout"))
                ? userService.getKickoutUserIds(candidates) : Set.of();
        for (OnlineUserVO row : rows) {
            row.setCanKickout(!row.isCurrent() && allowed.contains(row.getUserId()));
            try {
                row.setLoginLocation(IPUtils.getCityInfo(row.getIpAddress()));
            } catch (ServiceException exception) {
                log.warn("在线会话缺少IP归属地，客户端地址={}", row.getIpAddress());
            }
        }
        page.setRecords(rows);
        return new OnlineUserPageVO(page, accounts.size());
    }

    /**
     * 按强退操作的实时授权范围校验目标账号，并撤销选中的独立会话。
     *
     * @param parameter 账号和会话标识
     */
    @Override
    @DataPermission(permission = "monitor:onlineUser:kickout", tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void kickoutOnlineUser(OnlineUserKickoutDTO parameter) {
        // 使用当前用户表中的归属和受控身份校验，防止列表加载后账号变更导致越权。
        if (!userService.getKickoutUserIds(Set.of(parameter.getUserId())).contains(parameter.getUserId())) {
            throw new AccessDeniedException("无权强制下线该账号的会话");
        }
        authenticationSession.kickoutSession(AuthUserType.SYSTEM, parameter.getUserId(), parameter.getSessionId());
    }
}
