package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.system.model.dto.sysDictData.SysDictDataInsertDTO;
import com.whim.system.model.dto.sysDictData.SysDictDataQueryDTO;
import com.whim.system.model.dto.sysDictData.SysDictDataUpdateDTO;
import com.whim.system.model.entity.SysDictData;
import com.whim.system.model.vo.SysDictDataVO;

import java.util.List;

/**
 * 字典数据表(SysDictData)表服务接口
 *
 * @author Jince
 * @since 2025-06-27 17:23:36
 */
public interface ISysDictDataService extends IService<SysDictData> {

    /**
     * 分页查询字典数据
     *
     * @param sysDictDataDTO 字典数据查询参数
     * @param pageQueryDTO   分页参数
     * @return 字典数据列表
     */
    PageDataVO<SysDictDataVO> getDictDataPage(SysDictDataQueryDTO sysDictDataQueryDTO, PageQueryDTO pageQueryDTO);

    /**
     * 根据id查询字典数据
     *
     * @param id 字典数据id
     * @return 字典数据
     */
    SysDictDataVO getDictDataById(Long id);

    /**
     * 新增字典数据
     *
     * @param sysDictDataInsertDTO 字典数据参数
     * @return 字典数据列表
     */
    List<SysDictDataVO> insertDictData(SysDictDataInsertDTO sysDictDataInsertDTO);

    /**
     * 修改字典数据
     *
     * @param sysDictDataUpdateDTO 字典数据参数
     * @return 字典数据列表
     */
    List<SysDictDataVO> updateDictData(SysDictDataUpdateDTO sysDictDataUpdateDTO);

    /**
     * 通过字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<SysDictDataVO> getDictDataListByDictType(String dictType);

    /**
     * 删除字典类型
     *
     * @param dictDataIds 字典类型id集合
     */
    void deleteDictDataByIds(Long[] dictDataIds);
}
