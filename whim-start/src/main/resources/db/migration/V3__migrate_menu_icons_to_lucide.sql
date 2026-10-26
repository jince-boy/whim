-- 将原图标目录中的菜单标识转换为 Lucide 名称，包含旧 icon- 前缀和大小写形式。
-- 仅更新有明确映射且值发生变化的记录；空值和自定义标识保持原值。
-- 历史迁移保持不变，新建数据库同样通过本迁移完成图标名称转换。
UPDATE sys_permission AS permission_record
INNER JOIN JSON_TABLE(
    '[
        ["bangzhu", "circle-question-mark"],
        ["denglurizhi", "log-in"],
        ["guanlidenglurizhi", "logs"],
        ["wj-rz", "file-text"],
        ["rizhi", "scroll-text"],
        ["fanhui", "arrow-left"],
        ["daochu", "download"],
        ["zengjia", "plus"],
        ["shanchu", "trash-2"],
        ["xiugai", "pencil"],
        ["zidianmokuai", "book-open"],
        ["zidong", "monitor"],
        ["zidongtouyun-zidong", "monitor-cog"],
        ["quanpingsuoxiao", "minimize"],
        ["arrow-down", "arrow-down"],
        ["arrow-up", "arrow-up"],
        ["esc", "keyboard"],
        ["c181huiche", "corner-down-left"],
        ["zhuti", "palette"],
        ["yueliang", "moon"],
        ["taiyang1", "sun"],
        ["camera", "camera"],
        ["equalizer", "sliders-horizontal"],
        ["search", "search"],
        ["user1", "user"],
        ["location-info", "map-pin"],
        ["translate", "languages"],
        ["money", "wallet"],
        ["yonghu", "user"],
        ["motaikuang", "app-window"],
        ["motaikuang1", "panel-top"],
        ["guanbiqita", "panels-top-left"],
        ["full-screen", "maximize"],
        ["user-filled", "user-round"],
        ["connection", "link"],
        ["home-2", "house"],
        ["male", "mars"],
        ["switch", "power"],
        ["female", "venus"],
        ["refresh-2", "refresh-cw"],
        ["question", "circle-question-mark"],
        ["notify", "bell"],
        ["preview-s", "eye"],
        ["preview-no", "eye-off"],
        ["picture-circle", "image"],
        ["difference", "thumbs-down"],
        ["map-location", "map-pinned"],
        ["smile", "smile"],
        ["delete", "trash-2"],
        ["praise", "thumbs-up"],
        ["d-left", "chevrons-left"],
        ["d-right", "chevrons-right"],
        ["d-top", "chevrons-up"],
        ["down", "chevron-down"],
        ["left", "chevron-left"],
        ["right-3", "arrow-left-right"],
        ["d-down", "chevrons-down"],
        ["top", "chevron-up"],
        ["right", "chevron-right"],
        ["mine-3", "user-round"],
        ["lower-control-management-outline", "network"],
        ["order-3", "chart-column"],
        ["people-management", "users"],
        ["order-management-outline", "clipboard-list"],
        ["add-2", "check"],
        ["add-3", "minus"],
        ["closure", "x"],
        ["icona02", "layout-dashboard"],
        ["setting", "settings"],
        ["user", "user"],
        ["menu", "menu"],
        ["mima", "lock-keyhole"],
        ["yanzhengma", "shield-check"]
    ]',
    '$[*]' COLUMNS (
        old_name VARCHAR(128) PATH '$[0]',
        new_name VARCHAR(128) PATH '$[1]'
    )
) AS icon_mapping
    ON LOWER(TRIM(CASE
        WHEN LEFT(TRIM(permission_record.icon), 5) = 'icon-'
            THEN SUBSTRING(TRIM(permission_record.icon), 6)
        ELSE permission_record.icon
    END)) = icon_mapping.old_name
SET permission_record.icon = icon_mapping.new_name,
    permission_record.update_time = CURRENT_TIMESTAMP(3)
WHERE CAST(permission_record.icon AS BINARY) <> CAST(icon_mapping.new_name AS BINARY);
