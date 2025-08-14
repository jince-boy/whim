package com.whim.controller.system;

import com.whim.core.annotation.SystemApiPrefix;
import com.whim.core.web.Result;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.SysDictTypeDTO;
import com.whim.system.model.vo.SysDictTypeVO;
import com.whim.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2025/6/27 18:04
 * @description 字典类型管理
 */
@SystemApiPrefix
@RestController
@RequestMapping("/dictType")
@RequiredArgsConstructor
public class SysDictTypeController {
    private final ISysDictTypeService sysDictTypeService;

    @SystemCheckPermission("system:dictType:query")
    @GetMapping("/page")
    public Result<PageDataVO<SysDictTypeVO>> getDictTypePage(SysDictTypeDTO sysDictTypeDTO, PageQueryDTO pageQueryDTO) {
        return Result.success("查询成功", sysDictTypeService.getDictTypePage(sysDictTypeDTO, pageQueryDTO));
    }

    @SystemCheckPermission("system:dictType:detail")
    @GetMapping("/detail")
    public Result<SysDictTypeVO> getDictTypeById(Long id) {
        return Result.success("查询成功", sysDictTypeService.getDictTypeById(id));
    }

    @SystemCheckPermission("system:dictType:add")
    @PostMapping("/add")
    public Result<Void> addDictType(@RequestBody SysDictTypeDTO sysDictTypeDTO) {
        sysDictTypeService.insertDictType(sysDictTypeDTO);
        return Result.success("添加成功");
    }

    @SystemCheckPermission("system:dictType:edit")
    @PutMapping("/update")
    public Result<Void> editDictType(@RequestBody SysDictTypeDTO sysDictTypeDTO) {
        sysDictTypeService.updateDictType(sysDictTypeDTO);
        return Result.success("修改成功");
    }

    @SystemCheckPermission("system:dictType:delete")
    @DeleteMapping("/delete")
    public Result<Void> deleteDictTypeByIds(Long[] dictTypeIds) {
        sysDictTypeService.deleteDictTypeByIds(dictTypeIds);
        return Result.success("删除成功");
    }
}
