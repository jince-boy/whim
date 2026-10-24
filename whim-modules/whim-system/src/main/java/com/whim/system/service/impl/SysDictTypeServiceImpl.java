package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.constant.CacheKeys;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.redis.utils.CacheUtils;
import com.whim.system.mapper.SysDictTypeMapper;
import com.whim.system.model.dto.dict.SysDictTypeAddDTO;
import com.whim.system.model.dto.dict.SysDictTypeQueryDTO;
import com.whim.system.model.dto.dict.SysDictTypeUpdateDTO;
import com.whim.system.model.entity.SysDictData;
import com.whim.system.model.entity.SysDictType;
import com.whim.system.model.vo.dict.SysDictDataVO;
import com.whim.system.model.vo.dict.SysDictTypeVO;
import com.whim.system.service.ISysDictDataService;
import com.whim.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统字典类型服务。
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {
    private final ISysDictDataService sysDictDataService;

    /**
     * 分页查询字典类型。
     *
     * @param query 查询参数
     * @return 分页数据
     */
    @Override
    public PageDataVO<SysDictTypeVO> getDictTypePage(SysDictTypeQueryDTO query) {
        LambdaQueryWrapper<SysDictType> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(StringUtils.isNotBlank(query.getName()), SysDictType::getName, query.getName());
        lambdaQueryWrapper.eq(StringUtils.isNotBlank(query.getType()), SysDictType::getType, query.getType());
        return new PageDataVO<>(this.page(query.getPage(), lambdaQueryWrapper).convert(row -> BeanConvertUtils.convert(row, SysDictTypeVO.class)));
    }

    /**
     * 根据id查询字典类型。
     *
     * @param id 字典类型id
     * @return 字典类型详情
     */
    @Override
    public SysDictTypeVO getDictTypeDetail(Long id) {
        SysDictType sysDictType = this.getById(id);
        if (Objects.isNull(sysDictType)) {
            throw new ServiceException("字典类型不存在");
        }
        return BeanConvertUtils.convert(sysDictType, SysDictTypeVO.class);
    }

    /**
     * 根据类型查询字典类型。
     *
     * @param type 类型
     * @return 字典类型详情
     */
    @Cacheable(cacheNames = CacheKeys.SYS_DICT_TYPE, key = "#type", unless = "#result==null")
    @Override
    public SysDictTypeVO getDictTypeByType(String type) {
        return BeanConvertUtils.convert(this.getOne(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getType, type)), SysDictTypeVO.class);
    }

    /**
     * 获取字典类型列表。
     *
     * @return 字典类型列表
     */
    @Override
    public List<SysDictTypeVO> getDictTypeList() {
        return BeanConvertUtils.convertList(this.list(), SysDictTypeVO.class);
    }

    /**
     * 添加字典类型。
     *
     * @param sysDictTypeAddDTO 字典类型添加参数
     * @return 字典数据列表
     */
    @CachePut(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictTypeAddDTO.type")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<SysDictDataVO> addDictType(SysDictTypeAddDTO sysDictTypeAddDTO) {
        SysDictType sysDictType = BeanConvertUtils.convert(sysDictTypeAddDTO, SysDictType.class);
        if (this.save(sysDictType)) {
            return new ArrayList<>();
        }
        throw new ServiceException("添加字典类型失败");
    }

    /**
     * 修改字典类型并同步数据项编码。
     *
     * @param sysDictTypeUpdateDTO 字典类型修改参数
     * @return 按排序值和主键升序排列的字典数据列表
     */
    @CachePut(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictTypeUpdateDTO.type")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<SysDictDataVO> updateDictType(SysDictTypeUpdateDTO sysDictTypeUpdateDTO) {
        SysDictType oldSysDictType = this.getById(sysDictTypeUpdateDTO.getId());
        if (Objects.isNull(oldSysDictType)) {
            throw new ServiceException("字典类型不存在");
        }
        SysDictType sysDictType = BeanConvertUtils.convert(sysDictTypeUpdateDTO, SysDictType.class);
        if (!this.updateById(sysDictType)) {
            throw new ServiceException("修改字典类型失败");
        }
        if (!oldSysDictType.getType().equals(sysDictType.getType())) {
            LambdaUpdateWrapper<SysDictData> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(SysDictData::getType, oldSysDictType.getType());
            lambdaUpdateWrapper.set(SysDictData::getType, sysDictType.getType());
            // 传入实体，使框架填充数据项的更新人和更新时间。
            SysDictData sysDictData = new SysDictData();
            sysDictDataService.update(sysDictData, lambdaUpdateWrapper);
        }
        CacheUtils.evict(CacheKeys.SYS_DICT, oldSysDictType.getType());
        CacheUtils.evict(CacheKeys.SYS_DICT_TYPE, oldSysDictType.getType());
        LambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictData::getType, sysDictType.getType());
        lambdaQueryWrapper.orderByAsc(SysDictData::getSort, SysDictData::getId);
        return BeanConvertUtils.convertList(sysDictDataService.list(lambdaQueryWrapper), SysDictDataVO.class);
    }

    /**
     * 批量删除字典类型，存在未删除数据项时不允许删除。
     *
     * @param ids 字典类型id列表
     */
    @Override
    public void deleteDictTypes(List<Long> ids) {
        List<SysDictType> sysDictTypes = this.listByIds(ids);
        sysDictTypes.forEach(sysDictType -> {
            if (sysDictDataService.exists(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getType, sysDictType.getType()))) {
                throw new ServiceException("字典类型仍包含字典数据，请先删除数据项");
            }
        });
        if (this.removeByIds(ids)) {
            sysDictTypes.forEach(sysDictType -> {
                CacheUtils.evict(CacheKeys.SYS_DICT, sysDictType.getType());
                CacheUtils.evict(CacheKeys.SYS_DICT_TYPE, sysDictType.getType());
            });
        }
    }

    /**
     * 清除字典类型和字典数据缓存，查询时重新加载。
     */
    @Override
    public void resetDictCache() {
        CacheUtils.clear(CacheKeys.SYS_DICT_TYPE);
        CacheUtils.clear(CacheKeys.SYS_DICT);
    }
}
