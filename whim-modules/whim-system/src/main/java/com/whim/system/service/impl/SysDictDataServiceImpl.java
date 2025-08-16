package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.constant.CacheKeys;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.ConvertUtils;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.redis.utils.CacheUtils;
import com.whim.system.mapper.SysDictDataMapper;
import com.whim.system.mapper.SysDictTypeMapper;
import com.whim.system.model.dto.SysDictDataDTO;
import com.whim.system.model.entity.SysDictData;
import com.whim.system.model.entity.SysDictType;
import com.whim.system.model.vo.SysDictDataVO;
import com.whim.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据表(SysDictData)表服务实现类
 *
 * @author Jince
 * @since 2025-06-27 17:23:37
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {
    private final SysDictTypeMapper sysDictTypeMapper;

    /**
     * 分页查询字典数据
     *
     * @param sysDictDataDTO 字典数据查询参数
     * @param pageQueryDTO   分页参数
     * @return 字典数据列表
     */
    @Override
    public PageDataVO<SysDictDataVO> getDictDataPage(SysDictDataDTO sysDictDataDTO, PageQueryDTO pageQueryDTO) {
        LambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictData::getDictType, sysDictDataDTO.getDictType());
        lambdaQueryWrapper.like(StringUtils.isNotBlank(sysDictDataDTO.getLabel()), SysDictData::getLabel, sysDictDataDTO.getLabel());
        lambdaQueryWrapper.orderByAsc(SysDictData::getSort);
        IPage<SysDictDataVO> convert = this.page(pageQueryDTO.getPage(), lambdaQueryWrapper).convert(item -> ConvertUtils.convert(item, SysDictDataVO.class));
        return new PageDataVO<>(convert);
    }

    /**
     * 新增字典数据
     *
     * @param sysDictDataDTO 字典数据参数
     * @return 字典数据列表
     */
    @CachePut(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictDataDTO.dictType")
    @Override
    public List<SysDictDataVO> insertDictData(SysDictDataDTO sysDictDataDTO) {
        if (sysDictTypeMapper.selectCount(
                Wrappers.<SysDictType>lambdaQuery()
                        .eq(SysDictType::getType, sysDictDataDTO.getDictType())
        ) == 0) {
            throw new ServiceException("新增字典数据失败，字典类型不存在");
        }
        if (this.save(ConvertUtils.convert(sysDictDataDTO, SysDictData.class))) {
            return baseMapper.getDictDataListByDictType(sysDictDataDTO.getDictType());
        }
        throw new ServiceException("新增字典数据失败");
    }

    /**
     * 根据id查询字典数据
     *
     * @param id 字典数据id
     * @return 字典数据
     */
    @Override
    public SysDictDataVO getDictDataById(Long id) {
        return ConvertUtils.convert(this.getById(id), SysDictDataVO.class);
    }

    /**
     * 修改字典数据
     *
     * @param sysDictDataDTO 字典数据参数
     * @return 字典数据列表
     */
    @CachePut(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictDataDTO.dictType")
    @Override
    public List<SysDictDataVO> updateDictData(SysDictDataDTO sysDictDataDTO) {
        boolean exists = this.lambdaQuery().eq(SysDictData::getDictType, sysDictDataDTO.getDictType())
                .eq(SysDictData::getValue, sysDictDataDTO.getValue())
                .ne(SysDictData::getId, sysDictDataDTO.getId())
                .exists();
        if (exists) {
            throw new ServiceException("字典数据键值已存在");
        }
        if (this.updateById(ConvertUtils.convert(sysDictDataDTO, SysDictData.class))) {
            return baseMapper.getDictDataListByDictType(sysDictDataDTO.getDictType());
        }
        throw new ServiceException("修改字典数据失败");
    }

    /**
     * 通过字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @Cacheable(cacheNames = CacheKeys.SYS_DICT, key = "#dictType")
    @Override
    public List<SysDictDataVO> getDictDataListByDictType(String dictType) {
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictData::getDictType, dictType);
        return ConvertUtils.convert(this.list(queryWrapper), SysDictDataVO.class);
    }

    /**
     * 删除字典数据
     *
     * @param dictDataIds 字典数据id集合
     */
    @Override
    public void deleteDictDataByIds(Long[] dictDataIds) {
        for (Long dictDataId : dictDataIds) {
            SysDictData sysDictData = this.getById(dictDataId);
            this.removeById(sysDictData);
            CacheUtils.evict(CacheKeys.SYS_DICT, sysDictData.getDictType());
        }
    }
}

