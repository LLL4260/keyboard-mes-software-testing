-- 仅在自己新建的课程隔离库中，完成 schema.sql 建表后执行。
-- 使用同一连接运行整份文件。已有用户时跳过初始化。
START TRANSACTION;
SELECT COUNT(*) INTO @lab_existing_users FROM sys_user FOR UPDATE;
SELECT COUNT(*) INTO @lab_existing_models
FROM product_model WHERE model_code = 'LAB-KEYBOARD' FOR UPDATE;
SET @lab_employee = CONCAT('lab-', LEFT(REPLACE(UUID(), '-', ''), 12));
SET @lab_pass = HEX(RANDOM_BYTES(20));
INSERT INTO sys_user (employee_no, password_hash, name, role_code, role_name, status)
SELECT @lab_employee, MD5(@lab_pass), '实验管理员', 'admin', '管理员', 1
WHERE @lab_existing_users = 0;
SET @lab_created = ROW_COUNT();
INSERT INTO product_model (model_code, model_name, status)
SELECT 'LAB-KEYBOARD', '实验键盘', 1
WHERE @lab_created = 1 AND @lab_existing_models = 0;
COMMIT;
SELECT IF(@lab_created = 1, @lab_employee, NULL) AS local_employee_no,
       IF(@lab_created = 1, @lab_pass, NULL) AS local_password;
SET @lab_employee = NULL, @lab_pass = NULL, @lab_created = NULL;
SET @lab_existing_users = NULL, @lab_existing_models = NULL;
