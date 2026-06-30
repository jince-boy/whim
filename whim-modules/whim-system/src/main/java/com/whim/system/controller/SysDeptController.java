package com.whim.system.controller;


import com.whim.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/06/30
 * @description 部门表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysDept")
public class SysDeptController {

    /**
     * 部门表服务对象
     */
    private final ISysDeptService sysDeptService;
}

