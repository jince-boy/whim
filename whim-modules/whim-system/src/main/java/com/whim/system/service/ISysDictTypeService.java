package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatis.model.vo.PageDataVO;
import com.whim.system.model.dto.DictTypePageQueryDTO;
import com.whim.system.model.entity.SysDictType;
import com.whim.system.model.vo.SysDictTypeVO;

/**
 * 系统字典表(SysDictType)表服务接口
 *
 * @author Jince
 * @since 2025-06-27 17:23:37
 */
public interface ISysDictTypeService extends IService<SysDictType> {
    /**
     * 分页查询
     *
     * @param dictTypePageQueryDTO 查询参数
     * @return PageData<SysDictType>
     */
    PageDataVO<SysDictTypeVO> getDictTypePage(DictTypePageQueryDTO dictTypePageQueryDTO);
}
