package com.whim.controller.system;


import com.whim.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统岗位表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysPost")
public class SysPostController {

    /**
     * 系统岗位表服务对象
     */
    private final ISysPostService sysPostService;
}

