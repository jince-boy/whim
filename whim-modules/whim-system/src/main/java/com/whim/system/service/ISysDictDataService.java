package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.dict.SysDictDataAddDTO;
import com.whim.system.model.dto.dict.SysDictDataExportDTO;
import com.whim.system.model.dto.dict.SysDictDataQueryDTO;
import com.whim.system.model.dto.dict.SysDictDataUpdateDTO;
import com.whim.system.model.entity.SysDictData;
import com.whim.system.model.vo.dict.SysDictDataExportVO;
import com.whim.system.model.vo.dict.SysDictDataVO;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统共享字典数据维护和已登录用户的字典选项查询。
 */
public interface ISysDictDataService extends IService<SysDictData> {
    /**
     * 分页查询指定字典类型的数据。
     *
     * @param query 查询参数
     * @return 分页数据
     */
    PageDataVO<SysDictDataVO> getDictDataPage(SysDictDataQueryDTO query);

    /**
     * 查询字典数据详情。
     *
     * @param id 字典数据id
     * @return 字典数据详情
     */
    SysDictDataVO getDictDataDetail(Long id);

    /**
     * 查询指定类型的字典项，没有数据时返回空列表。
     *
     * @param type 字典类型编码
     * @return 按排序值和主键升序排列的字典数据列表
     */
    List<SysDictDataVO> getDictDataByType(String type);

    /**
     * 查询指定字典类型的数据导出列表。
     *
     * @param query 查询参数
     * @return 字典数据导出列表
     */
    List<SysDictDataExportVO> getDictDataExportList(SysDictDataExportDTO query);

    /**
     * 新增字典数据。
     *
     * @param sysDictDataAddDTO 字典数据添加参数
     */
    void addDictData(SysDictDataAddDTO sysDictDataAddDTO);

    /**
     * 修改字典数据，不允许变更所属类型。
     *
     * @param sysDictDataUpdateDTO 字典数据修改参数
     */
    void updateDictData(SysDictDataUpdateDTO sysDictDataUpdateDTO);

    /**
     * 批量删除匹配的字典数据，成功后清空字典选项缓存。
     *
     * @param ids 字典数据id列表
     */
    void deleteDictData(List<Long> ids);
}
