package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.system.model.dto.sysDictType.SysDictTypeQueryDTO;
import com.whim.system.model.dto.sysDictType.SysDictTypeInsertDTO;
import com.whim.system.model.dto.sysDictType.SysDictTypeUpdateDTO;
import com.whim.system.model.entity.SysDictType;
import com.whim.system.model.vo.SysDictDataVO;
import com.whim.system.model.vo.SysDictTypeVO;

import java.util.List;

/**
 * 系统字典表(SysDictType)表服务接口
 *
 * @author Jince
 * @since 2025-06-27 17:23:37
 */
public interface ISysDictTypeService extends IService<SysDictType> {
    /**
     * 字典类型分页查询
     *
     * @param sysDictTypeQueryDTO 字典类型查询参数
     * @param pageQueryDTO   分页参数
     * @return 字典类型分页数据
     */
    PageDataVO<SysDictTypeVO> getDictTypePage(SysDictTypeQueryDTO sysDictTypeQueryDTO, PageQueryDTO pageQueryDTO);

    /**
     * 查询字典类型列表
     *
     * @return 字典类型列表
     */
    List<SysDictTypeVO> getDictTypeList();


    /**
     * 根据id查询字典类型
     *
     * @param id 字典类型id
     * @return 字典类型
     */
    SysDictTypeVO getDictTypeById(Long id);

    /**
     * 新增字典类型
     *
     * @param sysDictTypeInsertDTO 字典类型参数
     * @return 字典数据集合
     */
    List<SysDictDataVO> insertDictType(SysDictTypeInsertDTO sysDictTypeInsertDTO);

    /**
     * 修改字典类型
     *
     * @param sysDictTypeUpdateDTO 字典类型参数
     * @return 字典数据集合
     */
    List<SysDictDataVO> updateDictType(SysDictTypeUpdateDTO sysDictTypeUpdateDTO);

    /**
     * 删除字典类型
     *
     * @param dictTypeIds 字典类型id集合
     */
    void deleteDictTypeByIds(Long[] dictTypeIds);

    /**
     * 重置字典缓存
     */
    void resetDictCache();
}
