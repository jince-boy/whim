package com.whim.system.controller;


import com.whim.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/06/30
 * @description 字典数据表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysDictData")
public class SysDictDataController {

    /**
     * 字典数据表服务对象
     */
    private final ISysDictDataService sysDictDataService;
}

