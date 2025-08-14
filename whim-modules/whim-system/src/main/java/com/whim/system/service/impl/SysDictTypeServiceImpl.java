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
import com.whim.system.mapper.SysDictTypeMapper;
import com.whim.system.model.dto.SysDictTypeDTO;
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

    /**
     * 字典类型分页查询
     *
     * @param sysDictTypeDTO 字典类型查询参数
     * @param pageQueryDTO   分页参数
     * @return 字典类型分页数据
     */
    @Override
    public PageDataVO<SysDictTypeVO> getDictTypePage(SysDictTypeDTO sysDictTypeDTO, PageQueryDTO pageQueryDTO) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(sysDictTypeDTO.getName()), SysDictType::getName, sysDictTypeDTO.getName())
                .like(StringUtils.isNotBlank(sysDictTypeDTO.getType()), SysDictType::getType, sysDictTypeDTO.getType());
        IPage<SysDictTypeVO> page = this.page(pageQueryDTO.getPage(), wrapper).convert(item -> ConvertUtils.convert(item, SysDictTypeVO.class));
        return new PageDataVO<>(page);
    }

    /**
     * 根据id查询字典类型
     *
     * @param id 字典类型id
     * @return 字典类型
     */
    @Override
    public SysDictTypeVO getDictTypeById(Long id) {
        return ConvertUtils.convert(this.getById(id), SysDictTypeVO.class);
    }

    /**
     * 新增字典类型
     *
     * @param sysDictTypeDTO 字典类型参数
     * @return 字典数据集合
     */
    @CachePut(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictTypeDTO.type")
    @Override
    public List<SysDictDataVO> insertDictType(SysDictTypeDTO sysDictTypeDTO) {
        boolean exists = this.lambdaQuery().eq(SysDictType::getType, sysDictTypeDTO.getType()).exists();
        if (exists) {
            throw new ServiceException("字典类型已存在");
        }
        if (this.save(ConvertUtils.convert(sysDictTypeDTO, SysDictType.class))) {
            return new ArrayList<>();
        }
        throw new ServiceException("新增字典失败");
    }


    /**
     * 修改字典类型
     *
     * @param sysDictTypeDTO 字典类型参数
     * @return 字典数据集合
     */
    @CachePut(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictTypeDTO.type")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictDataVO> updateDictType(SysDictTypeDTO sysDictTypeDTO) {
        SysDictType sysDictType = this.getById(sysDictTypeDTO.getId());
        if (sysDictType == null) {
            throw new ServiceException("修改的字典不存在");
        }
        boolean exists = this.lambdaQuery()
                .eq(SysDictType::getType, sysDictTypeDTO.getType())
                .ne(SysDictType::getId, sysDictTypeDTO.getId())  // 排除当前记录
                .exists();
        if (exists) {
            throw new ServiceException("字典类型已存在");
        }
        LambdaUpdateWrapper<SysDictData> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(SysDictData::getDictType, sysDictTypeDTO.getType());
        wrapper.eq(SysDictData::getDictType, sysDictType.getType());
        sysDictDataService.update(wrapper);

        if (this.updateById(ConvertUtils.convert(sysDictTypeDTO, SysDictType.class))) {
            CacheUtils.evict(CacheKeys.SYS_DICT, sysDictType.getType());
            return sysDictDataService.getDictDataListByDictType(sysDictTypeDTO.getType());
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
}

