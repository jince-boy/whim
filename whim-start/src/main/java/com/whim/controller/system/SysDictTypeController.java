package com.whim.controller.system;

import com.whim.core.annotation.SystemApiPrefix;
import com.whim.core.web.Result;
import com.whim.mybatis.model.vo.PageDataVO;
import com.whim.system.model.dto.DictTypePageQueryDTO;
import com.whim.system.model.vo.SysDictTypeVO;
import com.whim.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * date: 2025/6/27 18:04
 * description: 字典类型管理
 */
@SystemApiPrefix
@RestController
@RequestMapping("/dictType")
@RequiredArgsConstructor
public class SysDictTypeController {
    private final ISysDictTypeService sysDictTypeService;

    @RequestMapping("/page")
    public Result<PageDataVO<SysDictTypeVO>> getDictTypePage(DictTypePageQueryDTO dictTypePageQueryDTO) {
        return Result.success("查询成功", sysDictTypeService.getDictTypePage(dictTypePageQueryDTO));
    }
}
