package com.whim.controller.system;

import com.whim.core.annotation.SystemApiPrefix;
import com.whim.core.web.Result;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.SysDictDataDTO;
import com.whim.system.model.vo.SysDictDataVO;
import com.whim.system.service.ISysDictDataService;
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
 * date: 2025/8/14 19:13
 * description: 字典数据管理
 */
@SystemApiPrefix
@RestController
@RequestMapping("/dictData")
@RequiredArgsConstructor
public class SysDictDataController {
    private final ISysDictDataService sysDictDataService;

    @SystemCheckPermission("system:dictData:query")
    @RequestMapping("/page")
    public Result<PageDataVO<SysDictDataVO>> getDictDataPage(SysDictDataDTO sysDictDataDTO, PageQueryDTO pageQueryDTO) {
        return Result.success("查询成功", sysDictDataService.getDictDataPage(sysDictDataDTO, pageQueryDTO));
    }

    @SystemCheckPermission("system:dictData:detail")
    @GetMapping("/detail")
    public Result<SysDictDataVO> getDictDataById(Long id) {
        return Result.success("查询成功", sysDictDataService.getDictDataById(id));
    }

    @SystemCheckPermission("system:dictData:add")
    @PostMapping("/add")
    public Result<Void> addDictData(@RequestBody SysDictDataDTO sysDictDataDTO) {
        sysDictDataService.insertDictData(sysDictDataDTO);
        return Result.success("添加成功");
    }


    @SystemCheckPermission("system:dictData:edit")
    @PutMapping("/update")
    public Result<Void> editDictData(@RequestBody SysDictDataDTO sysDictDataDTO) {
        sysDictDataService.updateDictData(sysDictDataDTO);
        return Result.success("修改成功");
    }


    @SystemCheckPermission("system:dictData:delete")
    @DeleteMapping("/delete")
    public Result<Void> deleteDictDataByIds(Long[] dictDataIds) {
        sysDictDataService.deleteDictDataByIds(dictDataIds);
        return Result.success("删除成功");
    }
}
