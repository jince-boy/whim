# 1全部数据权限
-- select * from sys_user where ( 1=1);

# 2自定义数据权限
-- select * from sys_user where dept_id in(1,2);

# 3本部门数据权限
-- select * from sys_user where dept_id=1;

# 4本部门及以下数据权限
-- select * from sys_dept where id=1 or id in(SELECT id FROM sys_dept
-- WHERE find_in_set('1', ancestors) <> 0)

# 5只本人数据权限
-- select * from sys_user where id=1

# 6本部门及以下数据权限或本人数据权限
-- select * from sys_user as u where dept_id=1 or dept_id in (select id from sys_dept where find_in_set('1',ancestors)<>0) or u.id=1


SELECT id, dept_id, username, password, avatar, name, email, phone, gender, status, remark, deleted, create_by, create_time, update_by, update_time FROM sys_user WHERE (username = 'test') 
 and dept_id=1 and dept_id in (2)