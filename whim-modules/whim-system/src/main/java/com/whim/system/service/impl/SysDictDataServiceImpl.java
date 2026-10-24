package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.constant.CacheKeys;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.redis.utils.CacheUtils;
import com.whim.system.mapper.SysDictDataMapper;
import com.whim.system.mapper.SysDictTypeMapper;
import com.whim.system.model.dto.dict.SysDictDataAddDTO;
import com.whim.system.model.dto.dict.SysDictDataQueryDTO;
import com.whim.system.model.dto.dict.SysDictDataUpdateDTO;
import com.whim.system.model.entity.SysDictData;
import com.whim.system.model.entity.SysDictType;
import com.whim.system.model.vo.dict.SysDictDataVO;
import com.whim.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统字典数据服务。
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {
    private final SysDictTypeMapper sysDictTypeMapper;

    /**
     * 分页查询字典数据。
     *
     * @param query 查询参数
     * @return 分页数据
     */
    @Override
    public PageDataVO<SysDictDataVO> getDictDataPage(SysDictDataQueryDTO query) {
        LambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(StringUtils.isNotBlank(query.getLabel()), SysDictData::getLabel, query.getLabel());
        lambdaQueryWrapper.orderByAsc(SysDictData::getSort, SysDictData::getId);
        return new PageDataVO<>(this.page(query.getPage(), lambdaQueryWrapper).convert(row -> BeanConvertUtils.convert(row, SysDictDataVO.class)));
    }

    /**
     * 根据id查询字典数据。
     *
     * @param id 字典数据id
     * @return 字典数据详情
     */
    @Override
    public SysDictDataVO getDictDataDetail(Long id) {
        SysDictData sysDictData = this.getById(id);
        if (Objects.isNull(sysDictData)) {
            throw new ServiceException("字典数据不存在");
        }
        return BeanConvertUtils.convert(sysDictData, SysDictDataVO.class);
    }

    /**
     * 根据类型查询字典数据，没有数据时返回空列表。
     *
     * @param type 字典类型编码
     * @return 按排序值和主键升序排列的字典数据列表
     */
    @Cacheable(cacheNames = CacheKeys.SYS_DICT, key = "#type", unless = "#result.isEmpty()")
    @Override
    public List<SysDictDataVO> getDictDataByType(String type) {
        LambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictData::getType, type);
        lambdaQueryWrapper.orderByAsc(SysDictData::getSort, SysDictData::getId);
        return BeanConvertUtils.convertList(this.list(lambdaQueryWrapper), SysDictDataVO.class);
    }

    /**
     * 添加字典数据，校验所属类型并保持写入期间类型不被删除或改名。
     *
     * @param sysDictDataAddDTO 字典数据添加参数
     */
    @CacheEvict(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictDataAddDTO.type")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addDictData(SysDictDataAddDTO sysDictDataAddDTO) {
        LambdaQueryWrapper<SysDictType> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictType::getType, sysDictDataAddDTO.getType());
        SysDictType sysDictType = sysDictTypeMapper.selectOne(lambdaQueryWrapper);
        if (Objects.isNull(sysDictType)) {
            throw new ServiceException("字典类型不存在");
        }
        SysDictData sysDictData = BeanConvertUtils.convert(sysDictDataAddDTO, SysDictData.class);
        if (!this.save(sysDictData)) {
            throw new ServiceException("添加字典数据失败");
        }
    }

    /**
     * 修改字典数据，不允许变更所属类型。
     *
     * @param sysDictDataUpdateDTO 字典数据修改参数
     */
    @CacheEvict(cacheNames = CacheKeys.SYS_DICT, key = "#sysDictDataUpdateDTO.type")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDictData(SysDictDataUpdateDTO sysDictDataUpdateDTO) {
        SysDictData oldSysDictData = this.getById(sysDictDataUpdateDTO.getId());
        if (Objects.isNull(oldSysDictData)) {
            throw new ServiceException("字典数据不存在");
        }
        if (!oldSysDictData.getType().equalsIgnoreCase(sysDictDataUpdateDTO.getType())) {
            throw new ServiceException("字典数据不能变更所属类型");
        }
        SysDictData sysDictData = BeanConvertUtils.convert(sysDictDataUpdateDTO, SysDictData.class);
        if (!this.updateById(sysDictData)) {
            throw new ServiceException("修改字典数据失败");
        }
    }

    /**
     * 批量删除字典数据，成功后清空字典选项缓存。
     *
     * @param ids 字典数据id列表
     */
    @CacheEvict(cacheNames = CacheKeys.SYS_DICT, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteDictData(List<Long> ids) {
        List<SysDictData> sysDictDataList = this.listByIds(ids);
        if (this.removeByIds(ids)) {
            sysDictDataList.forEach(sysDictData -> CacheUtils.evict(CacheKeys.SYS_DICT, sysDictData.getType()));
        }
    }
}
