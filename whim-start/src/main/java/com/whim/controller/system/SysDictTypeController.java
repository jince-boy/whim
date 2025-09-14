package com.whim.controller.system;

import com.whim.web.annotation.SystemApiPrefix;
import com.whim.core.utils.ExcelUtils;
import com.whim.core.web.Result;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.sysDictType.SysDictTypeQueryDTO;
import com.whim.system.model.dto.sysDictType.SysDictTypeInsertDTO;
import com.whim.system.model.dto.sysDictType.SysDictTypeUpdateDTO;
import com.whim.system.model.vo.SysDictTypeVO;
import com.whim.system.service.ISysDictTypeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
@Validated
@SystemApiPrefix
@RestController
@RequestMapping("/dictType")
@RequiredArgsConstructor
public class SysDictTypeController {
    private final ISysDictTypeService sysDictTypeService;

    /**
     * 分页查询字典类型列表
     *
     * @param sysDictTypeQueryDTO 查询条件参数
     * @param pageQueryDTO        分页参数
     * @return 分页结果(包含数据列表和分页信息)
     */
    @SystemCheckPermission("system:dictType:query")
    @GetMapping("/page")
    public Result<PageDataVO<SysDictTypeVO>> getDictTypePage(SysDictTypeQueryDTO sysDictTypeQueryDTO, PageQueryDTO pageQueryDTO) {
        return Result.success("查询成功", sysDictTypeService.getDictTypePage(sysDictTypeQueryDTO, pageQueryDTO));
    }

    /**
     * 根据ID获取字典类型详情
     *
     * @param id 字典类型ID
     * @return 字典类型详细信息
     */
    @SystemCheckPermission("system:dictType:detail")
    @GetMapping("/detail")
    public Result<SysDictTypeVO> getDictTypeById(@NotNull(message = "字典类型ID不能为空") Long id) {
        return Result.success("查询成功", sysDictTypeService.getDictTypeById(id));
    }

    /**
     * 新增字典类型
     *
     * @param sysDictTypeInsertDTO 字典类型数据
     * @return 操作结果
     */
    @Log(title = "字典类型", logType = LogType.INSERT)
    @SystemCheckPermission("system:dictType:add")
    @PostMapping("/add")
    public Result<Void> addDictType(@Validated @RequestBody SysDictTypeInsertDTO sysDictTypeInsertDTO) {
        sysDictTypeService.insertDictType(sysDictTypeInsertDTO);
        return Result.success("添加成功");
    }

    /**
     * 修改字典类型
     *
     * @param sysDictTypeUpdateDTO 字典类型数据(必须包含ID)
     * @return 操作结果
     */
    @Log(title = "字典类型", logType = LogType.UPDATE)
    @SystemCheckPermission("system:dictType:edit")
    @PutMapping("/update")
    public Result<Void> editDictType(@Validated @RequestBody SysDictTypeUpdateDTO sysDictTypeUpdateDTO) {
        sysDictTypeService.updateDictType(sysDictTypeUpdateDTO);
        return Result.success("修改成功");
    }

    /**
     * 批量删除字典类型
     *
     * @param dictTypeIds 需要删除的字典类型ID数组
     * @return 操作结果
     */
    @Log(title = "字典类型", logType = LogType.DELETE)
    @SystemCheckPermission("system:dictType:delete")
    @DeleteMapping("/delete")
    public Result<Void> deleteDictTypeByIds(Long[] dictTypeIds) {
        sysDictTypeService.deleteDictTypeByIds(dictTypeIds);
        return Result.success("删除成功");
    }

    /**
     * 导出字典类型数据
     *
     * @param response HTTP响应对象(用于输出Excel文件)
     * @apiNote 导出当前所有字典类型数据(不受分页限制)
     */
    @Log(title = "字典类型", logType = LogType.EXPORT)
    @SystemCheckPermission("system:dictType:export")
    @GetMapping("/export")
    public void exportDictType(HttpServletResponse response) {
        ExcelUtils.exportExcel(sysDictTypeService.getDictTypeList(), SysDictTypeVO.class)
                .autoColumnWidth()
                .fileName("字典类型")
                .toResponse(response);
    }

    /**
     * 重置字典缓存
     *
     * @return 操作结果
     * @apiNote 清空并重新加载系统字典缓存
     */
    @Log(title = "字典类型", logType = LogType.CLEAN)
    @SystemCheckPermission("system:dictType:reset")
    @DeleteMapping("/reset")
    public Result<Void> resetDictCache() {
        sysDictTypeService.resetDictCache();
        return Result.success("重置成功");
    }
}
