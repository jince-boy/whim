-- 页面和按钮允许不配置权限标识，保留其他菜单字段约束。
DROP TRIGGER IF EXISTS trg_sys_permission_validate_insert;
DROP TRIGGER IF EXISTS trg_sys_permission_validate_update;

DELIMITER $$

CREATE TRIGGER trg_sys_permission_validate_insert BEFORE INSERT ON sys_permission
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.menu_type BETWEEN 1 AND 4
            AND NEW.built_in IN (0, 1) AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '菜单状态、类型、内置标志或删除标识无效';
    END IF;
    IF NEW.menu_type IN (1, 4) AND NEW.perms IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '目录和外链不能设置权限码';
    END IF;
END$$

CREATE TRIGGER trg_sys_permission_validate_update BEFORE UPDATE ON sys_permission
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.menu_type BETWEEN 1 AND 4
            AND NEW.built_in IN (0, 1) AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '菜单状态、类型、内置标志或删除标识无效';
    END IF;
    IF NEW.menu_type IN (1, 4) AND NEW.perms IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '目录和外链不能设置权限码';
    END IF;
END$$

DELIMITER ;
