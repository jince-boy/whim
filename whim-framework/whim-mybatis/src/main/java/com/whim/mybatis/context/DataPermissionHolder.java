package com.whim.mybatis.context;

import com.whim.mybatis.annotation.DataPermission;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jince
 * date: 2025/7/9 18:03
 * description: 数据权限上下文
 */
public class DataPermissionHolder {
    @Getter
    private final DataPermission permission;
    private final Map<String, Object> params = new HashMap<>();

    public DataPermissionHolder(DataPermission dataPermission) {
        this.permission = dataPermission;
    }

    public void addParam(String key, Object value) {
        params.put(key, value);
    }

    public <T> T getValue(String key, Class<T> type) {
        Object value = params.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public boolean hasPermission() {
        return permission != null & !params.isEmpty();
    }

}
