package com.whim.controller;

import com.whim.common.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * date: 2024/10/20 20:54
 * description: 测试控制器
 */
@RestController
@RequestMapping("/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {

    @GetMapping
    public Result<String> test() {
        return Result.success("hello,world");
    }

}
