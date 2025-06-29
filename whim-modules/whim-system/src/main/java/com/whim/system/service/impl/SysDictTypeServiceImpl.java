package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.utils.ConvertUtils;
import com.whim.mybatis.page.PageData;
import com.whim.system.mapper.SysDictTypeMapper;
import com.whim.system.model.dto.DictTypePageQueryDTO;
import com.whim.system.model.entity.SysDictType;
import com.whim.system.model.vo.SysDictTypeVO;
import com.whim.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 系统字典表(SysDictType)表服务实现类
 *
 * @author Jince
 * @since 2025-06-27 17:23:37
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    /**
     * 字典类型分页查询
     *
     * @param queryDTO 查询参数
     * @return PageData<SysDictTypeVO>
     */
    @Override
    public PageData<SysDictTypeVO> getDictTypePage(DictTypePageQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryDTO.getName()), SysDictType::getName, queryDTO.getName())
                .like(StringUtils.isNotBlank(queryDTO.getType()), SysDictType::getType, queryDTO.getType());
        IPage<SysDictTypeVO> page = this.page(queryDTO.getPage(), wrapper).convert(item -> ConvertUtils.convert(item, SysDictTypeVO.class));
        return new PageData<>(page);
    }



}

