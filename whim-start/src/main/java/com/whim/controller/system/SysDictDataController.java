package com.whim.controller.system;

import com.whim.web.annotation.SystemApiPrefix;
import com.whim.core.utils.ExcelUtils;
import com.whim.core.web.Result;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.sysDictData.SysDictDataInsertDTO;
import com.whim.system.model.dto.sysDictData.SysDictDataQueryDTO;
import com.whim.system.model.dto.sysDictData.SysDictDataUpdateDTO;
import com.whim.system.model.vo.SysDictDataVO;
import com.whim.system.service.ISysDictDataService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author jince
 * date: 2025/8/14 19:13
 * description: 字典数据管理
 */
@Validated
@SystemApiPrefix
@RestController
@RequestMapping("/dictData")
@RequiredArgsConstructor
public class SysDictDataController {
    private final ISysDictDataService sysDictDataService;

    /**
     * 字典数据分页查询
     *
     * @param sysDictDataQueryDTO 字典数据查询参数
     * @param pageQueryDTO        分页参数
     * @return 字典数据分页数据
     */
    @SystemCheckPermission("system:dictData:query")
    @GetMapping("/page")
    public Result<PageDataVO<SysDictDataVO>> getDictDataPage(SysDictDataQueryDTO sysDictDataQueryDTO, PageQueryDTO pageQueryDTO) {
        return Result.success("查询成功", sysDictDataService.getDictDataPage(sysDictDataQueryDTO, pageQueryDTO));
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据
     */
    @GetMapping("/type/{dictType}")
    public Result<List<SysDictDataVO>> getDictDataListByDictType(@PathVariable String dictType) {
        return Result.success("查询成功", sysDictDataService.getDictDataListByDictType(dictType));
    }


    /**
     * 导出字典数据
     *
     * @param dictType 字典类型
     * @param response 响应
     */
    @Log(title = "字典数据", logType = LogType.EXPORT)
    @SystemCheckPermission("system:dictData:export")
    @GetMapping("/export")
    public void export(@NotBlank(message = "字典类型不能为空") String dictType, HttpServletResponse response) {
        List<SysDictDataVO> sysUserSex = sysDictDataService.getDictDataListByDictType(dictType);
        ExcelUtils.exportExcel(sysUserSex, SysDictDataVO.class)
                .fileName("字典数据")
                .autoColumnWidth()
                .toResponse(response);
    }

    /**
     * 根据id查询字典数据
     *
     * @param id 字典数据id
     * @return 字典数据
     */
    @SystemCheckPermission("system:dictData:detail")
    @GetMapping("/detail")
    public Result<SysDictDataVO> getDictDataById(@NotNull(message = "字典数据id不能为空") Long id) {
        return Result.success("查询成功", sysDictDataService.getDictDataById(id));
    }

    /**
     * 添加字典数据
     *
     * @param sysDictDataInsertDTO 字典数据
     * @return 添加结果
     */
    @Log(title = "字典数据", logType = LogType.INSERT)
    @SystemCheckPermission("system:dictData:add")
    @PostMapping("/add")
    public Result<Void> addDictData(@Validated @RequestBody SysDictDataInsertDTO sysDictDataInsertDTO) {
        sysDictDataService.insertDictData(sysDictDataInsertDTO);
        return Result.success("添加成功");
    }


    /**
     * 修改字典数据
     *
     * @param sysDictDataUpdateDTO 字典数据
     * @return 修改结果
     */
    @Log(title = "字典数据", logType = LogType.UPDATE)
    @SystemCheckPermission("system:dictData:edit")
    @PutMapping("/update")
    public Result<Void> editDictData(@Validated @RequestBody SysDictDataUpdateDTO sysDictDataUpdateDTO) {
        sysDictDataService.updateDictData(sysDictDataUpdateDTO);
        return Result.success("修改成功");
    }


    /**
     * 删除字典数据
     *
     * @param dictDataIds 字典数据id集合
     * @return 删除结果
     */
    @Log(title = "字典数据", logType = LogType.DELETE)
    @SystemCheckPermission("system:dictData:delete")
    @DeleteMapping("/delete")
    public Result<Void> deleteDictDataByIds(Long[] dictDataIds) {
        sysDictDataService.deleteDictDataByIds(dictDataIds);
        return Result.success("删除成功");
    }
}
