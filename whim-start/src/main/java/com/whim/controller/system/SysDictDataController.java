package com.whim.controller.system;

import com.whim.core.annotation.SystemApiPrefix;
import com.whim.core.utils.ExcelUtils;
import com.whim.core.web.Result;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.SysDictDataDTO;
import com.whim.system.model.vo.SysDictDataVO;
import com.whim.system.service.ISysDictDataService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@SystemApiPrefix
@RestController
@RequestMapping("/dictData")
@RequiredArgsConstructor
public class SysDictDataController {
    private final ISysDictDataService sysDictDataService;

    /**
     * 字典数据分页查询
     *
     * @param sysDictDataDTO 字典数据查询参数
     * @param pageQueryDTO   分页参数
     * @return 字典数据分页数据
     */
    @SystemCheckPermission("system:dictData:query")
    @GetMapping("/page")
    public Result<PageDataVO<SysDictDataVO>> getDictDataPage(SysDictDataDTO sysDictDataDTO, PageQueryDTO pageQueryDTO) {
        return Result.success("查询成功", sysDictDataService.getDictDataPage(sysDictDataDTO, pageQueryDTO));
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
    @SystemCheckPermission("system:dictData:export")
    @GetMapping("/export")
    public void export(String dictType, HttpServletResponse response) {
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
    public Result<SysDictDataVO> getDictDataById(Long id) {
        return Result.success("查询成功", sysDictDataService.getDictDataById(id));
    }

    /**
     * 添加字典数据
     *
     * @param sysDictDataDTO 字典数据
     * @return 添加结果
     */
    @SystemCheckPermission("system:dictData:add")
    @PostMapping("/add")
    public Result<Void> addDictData(@RequestBody SysDictDataDTO sysDictDataDTO) {
        sysDictDataService.insertDictData(sysDictDataDTO);
        return Result.success("添加成功");
    }


    /**
     * 修改字典数据
     *
     * @param sysDictDataDTO 字典数据
     * @return 修改结果
     */
    @SystemCheckPermission("system:dictData:edit")
    @PutMapping("/update")
    public Result<Void> editDictData(@RequestBody SysDictDataDTO sysDictDataDTO) {
        sysDictDataService.updateDictData(sysDictDataDTO);
        return Result.success("修改成功");
    }


    /**
     * 删除字典数据
     *
     * @param dictDataIds 字典数据id集合
     * @return 删除结果
     */
    @SystemCheckPermission("system:dictData:delete")
    @DeleteMapping("/delete")
    public Result<Void> deleteDictDataByIds(Long[] dictDataIds) {
        sysDictDataService.deleteDictDataByIds(dictDataIds);
        return Result.success("删除成功");
    }
}
