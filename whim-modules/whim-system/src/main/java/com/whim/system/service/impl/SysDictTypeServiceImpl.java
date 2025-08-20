package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.constant.CacheKeys;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.ConvertUtils;
import com.whim.core.utils.StringFormatUtils;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.redis.utils.CacheUtils;
import com.whim.system.mapper.SysDictDataMapper;
import com.whim.system.mapper.SysDictTypeMapper;
import com.whim.system.model.dto.sysDictType.SysDictTypeQueryDTO;
import com.whim.system.model.dto.sysDictType.SysDictTypeInsertDTO;
import com.whim.system.model.dto.sysDictType.SysDictTypeUpdateDTO;
import com.whim.system.model.entity.SysDictData;
import com.whim.system.model.entity.SysDictType;
import com.whim.system.model.vo.SysDictDataVO;
import com.whim.system.model.vo.SysDictTypeVO;
import com.whim.system.service.ISysDictDataService;
import com.whim.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 系统字典表(SysDictType)表服务实现类
 *
 * @author Jince
 * @since 2025-06-27 17:23:37
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {
    private final ISysDictDataService sysDictDataService;
    private final SysDictDataMapper sysDictDataMapper;

    /**
     * 字典类型分页查询
     *
     * @param sysDictTypeQueryDTO 字典类型查询参数
     * @param pageQueryDTO        分页参数
     * @return 字典类型分页数据
     */
    @Override
    public PageDataVO<SysDictTypeVO> getDictTypePage(SysDictTypeQueryDTO sysDictTypeQueryDTO, PageQueryDTO pageQueryDTO) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(sysDictTypeQueryDTO.getName()), SysDictType::getName, sysDictTypeQueryDTO.getName())
                .like(StringUtils.isNotBlank(sysDictTypeQueryDTO.getType()), SysDictType::getType, sysDictTypeQueryDTO.getType());
        IPage<SysDictTypeVO> page = this.page(pageQueryDTO.getPage(), wrapper).convert(item -> ConvertUtils.convert(item, SysDictTypeVO.class));
        return new PageDataVO<>(page);
    }

    /**
     * 查询字典类型列表
     *
     * @return 字典类型列表
     */
    @Override
    public List<SysDictTypeVO> getDictTypeList() {
        return ConvertUtils.convert(this.list(), SysDictTypeVO.class);
    }

    /**
     * 根据id查询字典类型
     *
     * @param id 字典类型id
     * @return 字典类型
     */
    @Override
    public SysDictTypeVO getDictTypeById(Long id) {
        SysDictType sysDictType = this.getById(id);
        if (sysDictType == null) {
            throw new ServiceException("查询的字典类型不存在");
        }
        return ConvertUtils.convert(sysDictType, SysDictTypeVO.class);
    }

    /**
     * 新增字典类型
     *
     * @param sysDictTypeInsertDTO 字典类型参数
     * @return 字典数据集合
     */
    @CachePut(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictTypeInsertDTO.type")
    @Override
    public List<SysDictDataVO> insertDictType(SysDictTypeInsertDTO sysDictTypeInsertDTO) {
        boolean exists = this.lambdaQuery().eq(SysDictType::getType, sysDictTypeInsertDTO.getType()).exists();
        if (exists) {
            throw new ServiceException("字典类型已存在");
        }
        if (this.save(ConvertUtils.convert(sysDictTypeInsertDTO, SysDictType.class))) {
            return new ArrayList<>();
        }
        throw new ServiceException("新增字典失败");
    }


    /**
     * 修改字典类型
     *
     * @param sysDictTypeUpdateDTO 字典类型参数
     * @return 字典数据集合
     */
    @CachePut(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictTypeUpdateDTO.type")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictDataVO> updateDictType(SysDictTypeUpdateDTO sysDictTypeUpdateDTO) {
        SysDictType sysDictType = this.getById(sysDictTypeUpdateDTO.getId());
        if (sysDictType == null) {
            throw new ServiceException("修改的字典不存在");
        }
        boolean exists = this.lambdaQuery()
                .eq(SysDictType::getType, sysDictTypeUpdateDTO.getType())
                .ne(SysDictType::getId, sysDictTypeUpdateDTO.getId())  // 排除当前记录
                .exists();
        if (exists) {
            throw new ServiceException("字典类型已存在");
        }
        LambdaUpdateWrapper<SysDictData> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(SysDictData::getDictType, sysDictTypeUpdateDTO.getType());
        wrapper.eq(SysDictData::getDictType, sysDictType.getType());
        sysDictDataService.update(wrapper);

        if (this.updateById(ConvertUtils.convert(sysDictTypeUpdateDTO, SysDictType.class))) {
            CacheUtils.evict(CacheKeys.SYS_DICT, sysDictType.getType());
            return sysDictDataMapper.getDictDataListByDictType(sysDictTypeUpdateDTO.getType());
        }
        throw new ServiceException("修改字典失败");
    }

    /**
     * 删除字典类型
     *
     * @param dictTypeIds 字典类型id集合
     */
    @Override
    public void deleteDictTypeByIds(Long[] dictTypeIds) {
        for (Long dictTypeId : dictTypeIds) {
            SysDictType sysDictType = this.getById(dictTypeId);
            if (sysDictDataService.lambdaQuery().eq(SysDictData::getDictType, sysDictType.getType()).exists()) {
                throw new ServiceException(StringFormatUtils.format("字典'{}'下存在数据，不能删除", sysDictType.getName()));
            }
            CacheUtils.evict(CacheKeys.SYS_DICT, sysDictType.getType());
        }
        this.removeByIds(Arrays.asList(dictTypeIds));
    }

    /**
     * 重置字典缓存
     */
    @Override
    public void resetDictCache() {
        CacheUtils.clear(CacheKeys.SYS_DICT);
    }
}

