package com.whim.controller.system;

import com.whim.core.auth.AuthenticationContext;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.sse.SseConnectionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 当前系统账号的在线事件订阅入口。
 */
@RestController
@RequestMapping("/system/sse")
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(prefix = "whim.sse", name = "enabled", matchIfMissing = true)
public class SseController {

    private final AuthenticationContext authenticationContext;
    private final SseConnectionRegistry registry;

    /**
     * 订阅当前登录用户可接收的在线事件。
     *
     * @return 禁用代理缓冲和缓存的 UTF-8 事件流
     */
    @SystemCheckLogin
    @GetMapping("/subscribe")
    public ResponseEntity<SseEmitter> subscribe() {
        SseEmitter emitter = registry.subscribe(
                authenticationContext.getUserId(), authenticationContext.getSessionValidityCheck());
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .cacheControl(CacheControl.noStore())
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }
}
