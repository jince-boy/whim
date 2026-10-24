package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.dict.SysDictTypeAddDTO;
import com.whim.system.model.dto.dict.SysDictTypeQueryDTO;
import com.whim.system.model.dto.dict.SysDictTypeUpdateDTO;
import com.whim.system.model.entity.SysDictType;
import com.whim.system.model.vo.dict.SysDictDataVO;
import com.whim.system.model.vo.dict.SysDictTypeVO;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统共享字典类型维护，不按部门或创建人隔离。
 */
public interface ISysDictTypeService extends IService<SysDictType> {
    /**
     * 分页查询字典类型。
     *
     * @param query 查询参数
     * @return 分页数据
     */
    PageDataVO<SysDictTypeVO> getDictTypePage(SysDictTypeQueryDTO query);

    /**
     * 根据id查询字典类型。
     *
     * @param id 字典类型id
     * @return 字典类型详情
     */
    SysDictTypeVO getDictTypeDetail(Long id);

    /**
     * 根据类型查询字典类型。
     *
     * @param type 类型
     * @return 字典类型详情，不存在时返回null
     */
    SysDictTypeVO getDictTypeByType(String type);

    /**
     * 获取字典类型列表。
     *
     * @return 字典类型列表
     */
    List<SysDictTypeVO> getDictTypeList();

    /**
     * 添加字典类型。
     *
     * @param sysDictTypeAddDTO 字典类型添加参数
     * @return 空字典数据列表
     */
    List<SysDictDataVO> addDictType(SysDictTypeAddDTO sysDictTypeAddDTO);

    /**
     * 修改字典类型。
     *
     * @param sysDictTypeUpdateDTO 字典类型修改参数
     * @return 按排序值和主键升序排列的字典数据列表
     */
    List<SysDictDataVO> updateDictType(SysDictTypeUpdateDTO sysDictTypeUpdateDTO);

    /**
     * 批量删除不含数据项的字典类型。
     *
     * @param ids 字典类型id列表
     */
    void deleteDictTypes(List<Long> ids);

    /**
     * 清除字典类型和字典数据缓存，查询时重新加载。
     */
    void resetDictCache();
}
