-- 文本沿用默认样式，仅给尚未维护过的其余内置字典项设置区分色。
UPDATE sys_dict_data
SET list_class = CASE value
    WHEN 'RADIO' THEN 'info'
    WHEN 'JSON' THEN 'warning'
    WHEN 'IMAGE' THEN 'success'
    ELSE list_class
END
WHERE id IN (20121, 20122, 20123)
  AND type = 'sys_config_value_type'
  AND deleted = 0
  AND list_class = 'default'
  AND update_time IS NULL;
