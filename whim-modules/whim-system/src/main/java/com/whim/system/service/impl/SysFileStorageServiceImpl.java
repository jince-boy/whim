package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysFileStorageMapper;
import com.whim.system.model.entity.SysFileStorage;
import com.whim.system.service.ISysFileStorageService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 存储配置持久化服务。
 */
@Service
public class SysFileStorageServiceImpl extends ServiceImpl<SysFileStorageMapper, SysFileStorage>
        implements ISysFileStorageService {
}
