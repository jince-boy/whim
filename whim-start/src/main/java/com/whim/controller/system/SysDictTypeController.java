package com.whim.controller.system;

import com.whim.core.exception.ServiceException;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.dict.SysDictTypeAddDTO;
import com.whim.system.model.dto.dict.SysDictTypeQueryDTO;
import com.whim.system.model.dto.dict.SysDictTypeUpdateDTO;
import com.whim.system.model.vo.dict.SysDictTypeVO;
import com.whim.system.service.ISysDictTypeService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统字典类型接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/dictType")
public class SysDictTypeController {
    private final ISysDictTypeService dictService;

    /**
     * 分页查询字典类型。
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:dictType:page")
    public Result<PageDataVO<SysDictTypeVO>> getDictTypePage(@Valid SysDictTypeQueryDTO query) {
        return Result.success("字典类型查询成功", dictService.getDictTypePage(query));
    }

    /**
     * 按主键查询字典类型详情。
     */
    @GetMapping("/detail")
    @SystemCheckPermission("system:dictType:detail")
    public Result<SysDictTypeVO> getDictTypeDetail(@RequestParam Long id) {
        return Result.success("字典类型查询成功", dictService.getDictTypeDetail(id));
    }

    /**
     * 按编码查询字典类型详情。
     */
    @GetMapping("/type/{type}")
    @SystemCheckPermission("system:dictType:detail")
    public Result<SysDictTypeVO> getDictTypeByType(@PathVariable String type) {
        SysDictTypeVO sysDictType = dictService.getDictTypeByType(type);
        if (sysDictType == null) {
            throw new ServiceException("字典类型不存在");
        }
        return Result.success("字典类型查询成功", sysDictType);
    }

    /**
     * 获取全部未删除的字典类型。
     */
    @GetMapping("/list")
    @SystemCheckPermission("system:dictType:page")
    public Result<List<SysDictTypeVO>> getDictTypeList() {
        return Result.success("字典类型查询成功", dictService.getDictTypeList());
    }

    /**
     * 新增字典类型。
     */
    @PostMapping("/add")
    @SystemCheckPermission("system:dictType:add")
    public Result<Void> addDictType(@Valid @RequestBody SysDictTypeAddDTO parameter) {
        dictService.addDictType(parameter);
        return Result.success("字典类型新增成功");
    }

    /**
     * 修改字典类型。
     */
    @PutMapping("/update")
    @SystemCheckPermission("system:dictType:update")
    public Result<Void> updateDictType(@Valid @RequestBody SysDictTypeUpdateDTO parameter) {
        dictService.updateDictType(parameter);
        return Result.success("字典类型修改成功");
    }

    /**
     * 批量删除字典类型，支持逗号分隔或重复查询参数。
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:dictType:delete")
    public Result<Void> deleteDictTypes(List<Long> dictTypeIds) {
        dictService.deleteDictTypes(dictTypeIds);
        return Result.success("字典类型删除成功");
    }

    /**
     * 清除字典类型和字典数据缓存。
     */
    @DeleteMapping("/reset")
    @SystemCheckPermission("system:dictType:reset")
    public Result<Void> resetDictCache() {
        dictService.resetDictCache();
        return Result.success("字典缓存重置成功");
    }

}
