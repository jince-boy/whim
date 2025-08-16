package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDictData;
import com.whim.system.model.vo.SysDictDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据表(SysDictData)表数据库访问层
 *
 * @author Jince
 * @since 2025-06-27 17:23:36
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据
     */
    List<SysDictDataVO> getDictDataListByDictType(@Param("dictType") String dictType);
}

