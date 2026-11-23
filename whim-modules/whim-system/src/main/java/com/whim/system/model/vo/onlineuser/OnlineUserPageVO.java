package com.whim.system.model.vo.onlineuser;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.whim.mybatisplus.model.vo.PageDataVO;
import lombok.Getter;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 含筛选后账号数的在线会话分页。
 */
@Getter
public class OnlineUserPageVO extends PageDataVO<OnlineUserVO> {
    private final long accountCount;

    /**
     * 保存当前页及该筛选范围内的去重账号数。
     *
     * @param page 会话分页
     * @param accountCount 可见账号数
     */
    public OnlineUserPageVO(IPage<OnlineUserVO> page, long accountCount) {
        super(page);
        this.accountCount = accountCount;
    }
}
