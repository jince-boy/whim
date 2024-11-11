package com.whim.core.auth.redis;

import cn.dev33.satoken.session.SaSession;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serial;

/**
 * @author jince
 * date: 2024/11/11 下午4:50
 * description:
 */
@JsonIgnoreProperties({"timeout"})
public class SaSessionForJacksonCustomized extends SaSession {
    @Serial
    private static final long serialVersionUID = 1L;

    public SaSessionForJacksonCustomized() {
        super();
    }
    /**
     * 构建一个Session对象
     * @param id Session的id
     */
    public SaSessionForJacksonCustomized(String id) {
        super(id);
    }
}
