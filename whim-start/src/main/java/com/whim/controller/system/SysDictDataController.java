package com.whim.controller.system;

import com.whim.excel.utils.ExcelBuilder;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.dict.SysDictDataAddDTO;
import com.whim.system.model.dto.dict.SysDictDataExportDTO;
import com.whim.system.model.dto.dict.SysDictDataQueryDTO;
import com.whim.system.model.dto.dict.SysDictDataUpdateDTO;
import com.whim.system.model.vo.dict.SysDictDataExportVO;
import com.whim.system.model.vo.dict.SysDictDataVO;
import com.whim.system.service.ISysDictDataService;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统字典数据接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/dictData")
public class SysDictDataController {
    private final ISysDictDataService dictService;

    /**
     * 分页查询字典数据。
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:dictData:page")
    public Result<PageDataVO<SysDictDataVO>> getDictDataPage(@Valid SysDictDataQueryDTO query) {
        return Result.success("字典数据查询成功", dictService.getDictDataPage(query));
    }

    /**
     * 按主键查询字典数据详情。
     */
    @GetMapping("/detail")
    @SystemCheckPermission("system:dictData:detail")
    public Result<SysDictDataVO> getDictDataDetail(@RequestParam Long id) {
        return Result.success("字典数据查询成功", dictService.getDictDataDetail(id));
    }

    /**
     * 已登录用户按编码读取字典选项。
     */
    @GetMapping("/type/{type}")
    public Result<List<SysDictDataVO>> getDictDataByType(@PathVariable String type) {
        return Result.success("字典选项查询成功", dictService.getDictDataByType(type));
    }

    /**
     * 新增字典数据。
     */
    @PostMapping("/add")
    @SystemCheckPermission("system:dictData:add")
    public Result<Void> addDictData(@Valid @RequestBody SysDictDataAddDTO parameter) {
        dictService.addDictData(parameter);
        return Result.success("字典数据新增成功");
    }

    /**
     * 修改字典数据。
     */
    @PutMapping("/update")
    @SystemCheckPermission("system:dictData:update")
    public Result<Void> updateDictData(@Valid @RequestBody SysDictDataUpdateDTO parameter) {
        dictService.updateDictData(parameter);
        return Result.success("字典数据修改成功");
    }

    /**
     * 批量删除字典数据，支持逗号分隔或重复查询参数。
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:dictData:delete")
    public Result<Void> deleteDictData(@RequestParam List<Long> dictDataIds) {
        dictService.deleteDictData(dictDataIds);
        return Result.success("字典数据删除成功");
    }

    /**
     * 导出指定字典类型的数据。
     */
    @PostMapping("/export")
    @SystemCheckPermission("system:dictData:export")
    public void exportDictData(@Valid @RequestBody SysDictDataExportDTO query, HttpServletResponse response) {
        List<SysDictDataExportVO> list = dictService.getDictDataExportList(query);
        ExcelBuilder.of(list, SysDictDataExportVO.class).sheetName("字典数据").toResponse(response);
    }
}
