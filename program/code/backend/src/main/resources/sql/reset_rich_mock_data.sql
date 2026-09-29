-- Keyboard MES 完整模拟数据重置脚本
-- 用途：清空当前业务库所有数据，并导入覆盖看板、追溯、报工、质检、返修的演示数据。
-- 时间范围：2026-07-06 至 2026-07-18。
-- 默认账号密码：U001 ~ U007 / 123456。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `rework_order`;
TRUNCATE TABLE `inspection_record`;
TRUNCATE TABLE `work_report`;
TRUNCATE TABLE `production_task`;
TRUNCATE TABLE `production_order`;
TRUNCATE TABLE `process_route`;
TRUNCATE TABLE `product_bom`;
TRUNCATE TABLE `material`;
TRUNCATE TABLE `product_model`;
TRUNCATE TABLE `sys_user`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. 用户与角色
INSERT INTO `sys_user`
(`employee_no`, `password_hash`, `name`, `role_code`, `role_name`, `department`, `position`, `phone`, `skill_level`, `status`)
VALUES
('U001', MD5('123456'), '系统管理员', 'admin', '系统管理员', '信息化部', '系统管理员', '13800000001', 'L3', 1),
('U002', MD5('123456'), '计划员张敏', 'planner', '计划员', '生产计划部', '生产计划', '13800000002', 'L2', 1),
('U003', MD5('123456'), '装配员李强', 'operator', '操作员', '一号装配线', '总装工位', '13800000003', 'L2', 1),
('U004', MD5('123456'), '装配员王磊', 'operator', '操作员', '二号装配线', '包装工位', '13800000004', 'L1', 1),
('U005', MD5('123456'), '质检员赵静', 'inspector', '质检员', '质量部', '过程检验', '13800000005', 'L2', 1),
('U006', MD5('123456'), '质检员陈晨', 'inspector', '质检员', '质量部', '终检', '13800000006', 'L3', 1),
('U007', MD5('123456'), '返修员周洋', 'repair', '返修员', '维修组', '返修工位', '13800000007', 'L2', 1);

SET @planner_id := (SELECT `id` FROM `sys_user` WHERE `employee_no` = 'U002');
SET @operator_a_id := (SELECT `id` FROM `sys_user` WHERE `employee_no` = 'U003');
SET @operator_b_id := (SELECT `id` FROM `sys_user` WHERE `employee_no` = 'U004');
SET @inspector_a_id := (SELECT `id` FROM `sys_user` WHERE `employee_no` = 'U005');
SET @inspector_b_id := (SELECT `id` FROM `sys_user` WHERE `employee_no` = 'U006');
SET @repair_id := (SELECT `id` FROM `sys_user` WHERE `employee_no` = 'U007');

-- 2. 产品型号：覆盖全尺寸、TKL、小配列、矮轴、分体人体工学、静音办公和宏键盘等类型。
INSERT INTO `product_model`
(`model_code`, `model_name`, `category`, `configuration_desc`, `firmware_version`, `status`, `remark`)
VALUES
('KB-ALPHA-87', 'Alpha 87 热插拔机械键盘', '机械键盘', '87键、热插拔、RGB、三模连接、PBT键帽', 'v1.2.0', 1, '主推演示型号'),
('KB-PRO-104', 'Pro 104 办公机械键盘', '机械键盘', '104键、有线连接、白色背光、办公场景', 'v1.0.5', 1, '大批量办公型号'),
('KB-MINI-68', 'Mini 68 便携机械键盘', '机械键盘', '68键、小配列、蓝牙/2.4G/有线三模', 'v2.0.1', 1, '小配列型号'),
('KB-LOW-84', 'Low 84 矮轴键盘', '机械键盘', '84键、矮轴、轻薄外壳、Type-C', 'v1.1.3', 1, '轻薄型号'),
('KB-MACRO-12', 'Macro 12 自定义小键盘', '功能键盘', '12键宏定义、旋钮、RGB灯效', 'v0.9.8', 1, '小批量试产型号'),
('KB-SPLIT-75', 'Split 75 人体工学分体键盘', '人体工学键盘', '75键、左右分体、热插拔、腕托套件', 'v1.3.2', 1, '人体工学新品'),
('KB-GAME-TKL', 'Game TKL 电竞低延迟键盘', '电竞键盘', '87键TKL、8K回报率、RGB、磁吸上盖', 'v2.4.0', 1, '电竞系列'),
('KB-SILENT-98', 'Silent 98 静音办公键盘', '静音键盘', '98键、静音轴、吸音棉、办公低噪', 'v1.0.8', 1, '静音办公系列');

SET @model_87_id := (SELECT `id` FROM `product_model` WHERE `model_code` = 'KB-ALPHA-87');
SET @model_104_id := (SELECT `id` FROM `product_model` WHERE `model_code` = 'KB-PRO-104');
SET @model_68_id := (SELECT `id` FROM `product_model` WHERE `model_code` = 'KB-MINI-68');
SET @model_84_id := (SELECT `id` FROM `product_model` WHERE `model_code` = 'KB-LOW-84');
SET @model_macro_id := (SELECT `id` FROM `product_model` WHERE `model_code` = 'KB-MACRO-12');
SET @model_split_id := (SELECT `id` FROM `product_model` WHERE `model_code` = 'KB-SPLIT-75');
SET @model_game_id := (SELECT `id` FROM `product_model` WHERE `model_code` = 'KB-GAME-TKL');
SET @model_silent_id := (SELECT `id` FROM `product_model` WHERE `model_code` = 'KB-SILENT-98');

-- 3. 物料主数据
INSERT INTO `material`
(`material_code`, `material_name`, `specification`, `material_type`, `unit`, `supplier`, `barcode`, `status`)
VALUES
('MAT-PCB-87-RGB', '87键RGB PCB主板', '三模热插拔，ANSI配列', 1, 'pcs', '华南电子', 'MAT-PCB-87-RGB', 1),
('MAT-PCB-104-WIRED', '104键有线PCB主板', '有线办公版，白光', 1, 'pcs', '华南电子', 'MAT-PCB-104-WIRED', 1),
('MAT-PCB-68-BT', '68键蓝牙PCB主板', '蓝牙/2.4G/有线三模', 1, 'pcs', '华南电子', 'MAT-PCB-68-BT', 1),
('MAT-PCB-84-LOW', '84键矮轴PCB主板', '矮轴专用，Type-C', 1, 'pcs', '华南电子', 'MAT-PCB-84-LOW', 1),
('MAT-PCB-MACRO-12', '12键宏键盘PCB主板', '带旋钮编码器接口', 1, 'pcs', '华南电子', 'MAT-PCB-MACRO-12', 1),
('MAT-PCB-SPLIT-75', '75键分体PCB套装', '左右分体双主板，Type-C桥接', 1, 'set', '华南电子', 'MAT-PCB-SPLIT-75', 1),
('MAT-PCB-GAME-TKL', '电竞TKL低延迟PCB', '8K回报率，热插拔', 1, 'pcs', '华南电子', 'MAT-PCB-GAME-TKL', 1),
('MAT-PCB-SILENT-98', '98键静音办公PCB', '有线/2.4G双模', 1, 'pcs', '华南电子', 'MAT-PCB-SILENT-98', 1),
('MAT-SWITCH-LINEAR', '线性轴体', '45g，热插拔', 1, 'pcs', '凯华', 'MAT-SWITCH-LINEAR', 1),
('MAT-SWITCH-TACTILE', '段落轴体', '55g，热插拔', 1, 'pcs', '凯华', 'MAT-SWITCH-TACTILE', 1),
('MAT-SWITCH-LOW', '矮轴轴体', '低行程，轻触', 1, 'pcs', '佳达隆', 'MAT-SWITCH-LOW', 1),
('MAT-SWITCH-SILENT', '静音线性轴体', '静音结构，办公低噪', 1, 'pcs', '佳达隆', 'MAT-SWITCH-SILENT', 1),
('MAT-KEYCAP-87-PBT', '87键PBT键帽套装', '双色注塑，灰白配色', 1, 'set', '键帽工坊', 'MAT-KEYCAP-87-PBT', 1),
('MAT-KEYCAP-104-PBT', '104键PBT键帽套装', '办公灰白配色', 1, 'set', '键帽工坊', 'MAT-KEYCAP-104-PBT', 1),
('MAT-KEYCAP-68-PBT', '68键PBT键帽套装', '便携配列', 1, 'set', '键帽工坊', 'MAT-KEYCAP-68-PBT', 1),
('MAT-KEYCAP-84-LOW', '84键矮轴键帽套装', '低高度键帽', 1, 'set', '键帽工坊', 'MAT-KEYCAP-84-LOW', 1),
('MAT-KEYCAP-SPLIT-75', '75键分体键帽套装', '人体工学异形增补', 1, 'set', '键帽工坊', 'MAT-KEYCAP-SPLIT-75', 1),
('MAT-KEYCAP-GAME-TKL', '电竞TKL透光键帽', 'PBT透光，深灰配色', 1, 'set', '键帽工坊', 'MAT-KEYCAP-GAME-TKL', 1),
('MAT-KEYCAP-SILENT-98', '98键静音办公键帽', '低噪大键位优化', 1, 'set', '键帽工坊', 'MAT-KEYCAP-SILENT-98', 1),
('MAT-STAB-SET', '卫星轴套装', '2U/6.25U组合', 1, 'set', '五金供应商A', 'MAT-STAB-SET', 1),
('MAT-FOAM-SILENT', '夹心吸音棉套装', 'PORON夹心棉+底棉', 1, 'set', '声学材料商', 'MAT-FOAM-SILENT', 1),
('MAT-CASE-87', '87键上盖下壳', 'ABS外壳，白色', 1, 'set', '注塑供应商A', 'MAT-CASE-87', 1),
('MAT-CASE-104', '104键上盖下壳', 'ABS外壳，黑色', 1, 'set', '注塑供应商A', 'MAT-CASE-104', 1),
('MAT-CASE-68', '68键上盖下壳', 'ABS外壳，浅灰', 1, 'set', '注塑供应商A', 'MAT-CASE-68', 1),
('MAT-CASE-84-LOW', '84键矮轴外壳', '铝合金上盖', 1, 'set', '结构供应商B', 'MAT-CASE-84-LOW', 1),
('MAT-CASE-MACRO', '宏键盘外壳', 'CNC铝壳，黑色', 1, 'set', '结构供应商B', 'MAT-CASE-MACRO', 1),
('MAT-CASE-SPLIT-75', '75键分体外壳套装', '左右分体注塑壳+腕托', 1, 'set', '结构供应商B', 'MAT-CASE-SPLIT-75', 1),
('MAT-CASE-GAME-TKL', '电竞TKL磁吸外壳', '上盖磁吸，黑透配色', 1, 'set', '结构供应商B', 'MAT-CASE-GAME-TKL', 1),
('MAT-CASE-SILENT-98', '98键静音外壳', '带吸音仓结构', 1, 'set', '结构供应商B', 'MAT-CASE-SILENT-98', 1),
('MAT-BATTERY-2000', '锂电池', '2000mAh，带保护板', 1, 'pcs', '电池供应商C', 'MAT-BATTERY-2000', 1),
('MAT-RGB-LED', 'RGB灯珠', 'SMD封装', 1, 'pcs', '灯珠供应商D', 'MAT-RGB-LED', 1),
('MAT-TYPEC-CABLE', 'Type-C数据线', '1.5m，黑色', 2, 'pcs', '线材供应商E', 'MAT-TYPEC-CABLE', 1),
('MAT-PACK-BOX-87', '87键包装盒', '彩盒+内托', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-87', 1),
('MAT-PACK-BOX-104', '104键包装盒', '彩盒+内托', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-104', 1),
('MAT-PACK-BOX-SMALL', '小配列包装盒', '68/84/宏键盘通用', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-SMALL', 1),
('MAT-PACK-BOX-SPLIT', '分体键盘包装盒', '左右分体加厚内托', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-SPLIT', 1),
('MAT-PACK-BOX-GAME', '电竞键盘包装盒', '黑金彩盒+防震托', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-GAME', 1),
('MAT-PACK-BOX-SILENT', '静音办公包装盒', '办公系列彩盒', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-SILENT', 1),
('MAT-LABEL-SN', 'SN条码标签', '40mm x 20mm', 2, 'pcs', '标签供应商G', 'MAT-LABEL-SN', 1),
('MAT-MANUAL', '用户说明书', '中英文折页', 2, 'pcs', '印刷供应商H', 'MAT-MANUAL', 1);

SET @mat_pcb_87 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PCB-87-RGB');
SET @mat_pcb_104 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PCB-104-WIRED');
SET @mat_pcb_68 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PCB-68-BT');
SET @mat_pcb_84 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PCB-84-LOW');
SET @mat_pcb_macro := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PCB-MACRO-12');
SET @mat_pcb_split := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PCB-SPLIT-75');
SET @mat_pcb_game := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PCB-GAME-TKL');
SET @mat_pcb_silent := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PCB-SILENT-98');
SET @mat_switch_linear := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-SWITCH-LINEAR');
SET @mat_switch_tactile := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-SWITCH-TACTILE');
SET @mat_switch_low := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-SWITCH-LOW');
SET @mat_switch_silent := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-SWITCH-SILENT');
SET @mat_keycap_87 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-KEYCAP-87-PBT');
SET @mat_keycap_104 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-KEYCAP-104-PBT');
SET @mat_keycap_68 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-KEYCAP-68-PBT');
SET @mat_keycap_84 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-KEYCAP-84-LOW');
SET @mat_keycap_split := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-KEYCAP-SPLIT-75');
SET @mat_keycap_game := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-KEYCAP-GAME-TKL');
SET @mat_keycap_silent := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-KEYCAP-SILENT-98');
SET @mat_stab := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-STAB-SET');
SET @mat_foam := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-FOAM-SILENT');
SET @mat_case_87 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-CASE-87');
SET @mat_case_104 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-CASE-104');
SET @mat_case_68 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-CASE-68');
SET @mat_case_84 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-CASE-84-LOW');
SET @mat_case_macro := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-CASE-MACRO');
SET @mat_case_split := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-CASE-SPLIT-75');
SET @mat_case_game := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-CASE-GAME-TKL');
SET @mat_case_silent := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-CASE-SILENT-98');
SET @mat_battery := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-BATTERY-2000');
SET @mat_rgb := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-RGB-LED');
SET @mat_cable := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-TYPEC-CABLE');
SET @mat_pack_87 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PACK-BOX-87');
SET @mat_pack_104 := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PACK-BOX-104');
SET @mat_pack_small := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PACK-BOX-SMALL');
SET @mat_pack_split := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PACK-BOX-SPLIT');
SET @mat_pack_game := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PACK-BOX-GAME');
SET @mat_pack_silent := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-PACK-BOX-SILENT');
SET @mat_label := (SELECT `id` FROM `material` WHERE `material_code` = 'MAT-LABEL-SN');

-- 4. BOM 数据
INSERT INTO `product_bom`
(`product_model_id`, `bom_version`, `material_id`, `quantity`, `unit`, `sequence_no`, `effective_date`, `status`, `remark`)
VALUES
(@model_87_id, 'V1.0', @mat_pcb_87, 1.000, 'pcs', 10, '2026-07-01', 1, '主板'),
(@model_87_id, 'V1.0', @mat_switch_linear, 87.000, 'pcs', 20, '2026-07-01', 1, '轴体'),
(@model_87_id, 'V1.0', @mat_keycap_87, 1.000, 'set', 30, '2026-07-01', 1, '键帽'),
(@model_87_id, 'V1.0', @mat_stab, 1.000, 'set', 40, '2026-07-01', 1, '卫星轴'),
(@model_87_id, 'V1.0', @mat_case_87, 1.000, 'set', 50, '2026-07-01', 1, '外壳'),
(@model_87_id, 'V1.0', @mat_battery, 1.000, 'pcs', 60, '2026-07-01', 1, '电池'),
(@model_87_id, 'V1.0', @mat_rgb, 87.000, 'pcs', 70, '2026-07-01', 1, '灯珠'),
(@model_87_id, 'V1.0', @mat_pack_87, 1.000, 'pcs', 80, '2026-07-01', 1, '包装盒'),
(@model_104_id, 'V1.0', @mat_pcb_104, 1.000, 'pcs', 10, '2026-07-01', 1, '主板'),
(@model_104_id, 'V1.0', @mat_switch_tactile, 104.000, 'pcs', 20, '2026-07-01', 1, '段落轴'),
(@model_104_id, 'V1.0', @mat_keycap_104, 1.000, 'set', 30, '2026-07-01', 1, '键帽'),
(@model_104_id, 'V1.0', @mat_case_104, 1.000, 'set', 40, '2026-07-01', 1, '外壳'),
(@model_104_id, 'V1.0', @mat_pack_104, 1.000, 'pcs', 50, '2026-07-01', 1, '包装盒'),
(@model_68_id, 'V1.0', @mat_pcb_68, 1.000, 'pcs', 10, '2026-07-01', 1, '主板'),
(@model_68_id, 'V1.0', @mat_switch_linear, 68.000, 'pcs', 20, '2026-07-01', 1, '轴体'),
(@model_68_id, 'V1.0', @mat_keycap_68, 1.000, 'set', 30, '2026-07-01', 1, '键帽'),
(@model_68_id, 'V1.0', @mat_case_68, 1.000, 'set', 40, '2026-07-01', 1, '外壳'),
(@model_68_id, 'V1.0', @mat_battery, 1.000, 'pcs', 50, '2026-07-01', 1, '电池'),
(@model_84_id, 'V1.0', @mat_pcb_84, 1.000, 'pcs', 10, '2026-07-01', 1, '主板'),
(@model_84_id, 'V1.0', @mat_switch_low, 84.000, 'pcs', 20, '2026-07-01', 1, '矮轴'),
(@model_84_id, 'V1.0', @mat_keycap_84, 1.000, 'set', 30, '2026-07-01', 1, '键帽'),
(@model_84_id, 'V1.0', @mat_case_84, 1.000, 'set', 40, '2026-07-01', 1, '外壳'),
(@model_macro_id, 'V1.0', @mat_pcb_macro, 1.000, 'pcs', 10, '2026-07-01', 1, '宏键盘主板'),
(@model_macro_id, 'V1.0', @mat_switch_tactile, 12.000, 'pcs', 20, '2026-07-01', 1, '轴体'),
(@model_macro_id, 'V1.0', @mat_case_macro, 1.000, 'set', 30, '2026-07-01', 1, '外壳'),
(@model_macro_id, 'V1.0', @mat_pack_small, 1.000, 'pcs', 40, '2026-07-01', 1, '包装盒'),
(@model_split_id, 'V1.0', @mat_pcb_split, 1.000, 'set', 10, '2026-07-01', 1, '分体主板套装'),
(@model_split_id, 'V1.0', @mat_switch_tactile, 75.000, 'pcs', 20, '2026-07-01', 1, '段落轴'),
(@model_split_id, 'V1.0', @mat_keycap_split, 1.000, 'set', 30, '2026-07-01', 1, '人体工学键帽'),
(@model_split_id, 'V1.0', @mat_case_split, 1.000, 'set', 40, '2026-07-01', 1, '分体外壳'),
(@model_split_id, 'V1.0', @mat_pack_split, 1.000, 'pcs', 50, '2026-07-01', 1, '包装盒'),
(@model_game_id, 'V1.0', @mat_pcb_game, 1.000, 'pcs', 10, '2026-07-01', 1, '电竞PCB'),
(@model_game_id, 'V1.0', @mat_switch_linear, 87.000, 'pcs', 20, '2026-07-01', 1, '线性轴'),
(@model_game_id, 'V1.0', @mat_keycap_game, 1.000, 'set', 30, '2026-07-01', 1, '透光键帽'),
(@model_game_id, 'V1.0', @mat_case_game, 1.000, 'set', 40, '2026-07-01', 1, '磁吸外壳'),
(@model_game_id, 'V1.0', @mat_rgb, 87.000, 'pcs', 50, '2026-07-01', 1, 'RGB灯珠'),
(@model_game_id, 'V1.0', @mat_pack_game, 1.000, 'pcs', 60, '2026-07-01', 1, '电竞包装盒'),
(@model_silent_id, 'V1.0', @mat_pcb_silent, 1.000, 'pcs', 10, '2026-07-01', 1, '静音PCB'),
(@model_silent_id, 'V1.0', @mat_switch_silent, 98.000, 'pcs', 20, '2026-07-01', 1, '静音轴'),
(@model_silent_id, 'V1.0', @mat_keycap_silent, 1.000, 'set', 30, '2026-07-01', 1, '静音键帽'),
(@model_silent_id, 'V1.0', @mat_foam, 1.000, 'set', 40, '2026-07-01', 1, '吸音棉'),
(@model_silent_id, 'V1.0', @mat_case_silent, 1.000, 'set', 50, '2026-07-01', 1, '静音外壳'),
(@model_silent_id, 'V1.0', @mat_pack_silent, 1.000, 'pcs', 60, '2026-07-01', 1, '办公包装盒'),
(@model_silent_id, 'V1.0', @mat_label, 1.000, 'pcs', 70, '2026-07-01', 1, 'SN标签');

-- 5. 工艺路线
INSERT INTO `process_route`
(`product_model_id`, `route_version`, `sequence_no`, `process_code`, `process_name`, `process_type`,
 `station_code`, `station_name`, `standard_hours`, `is_quality_gate`, `inspection_type`, `inspection_config`, `sop_file`, `status`)
VALUES
(@model_87_id, 'R1.0', 10, 'PCB-TEST', 'PCBA测试', 2, 'ST-PCBA-01', 'PCBA测试工位', 0.50, 1, 'ICT', '{"items":["短路","按键矩阵","蓝牙模块"],"method":"治具测试"}', '/sop/kb87/pcba-test.pdf', 1),
(@model_87_id, 'R1.0', 20, 'ASSEMBLY', '总装', 1, 'ST-ASM-01', '总装一工位', 1.60, 0, NULL, NULL, '/sop/kb87/assembly.pdf', 1),
(@model_87_id, 'R1.0', 30, 'AGING', '老化测试', 2, 'ST-AGING-01', '老化测试工位', 2.00, 1, 'FCT', '{"items":["连续输入","灯效","无线连接"],"method":"自动测试"}', '/sop/kb87/aging.pdf', 1),
(@model_87_id, 'R1.0', 40, 'FINAL-QC', '终检', 2, 'ST-QC-01', '终检工位', 0.70, 1, 'final', '{"items":["外观","全键触发","包装附件"],"method":"人工抽检"}', '/sop/kb87/final-qc.pdf', 1),
(@model_87_id, 'R1.0', 50, 'PACK', '包装入库', 3, 'ST-PACK-01', '包装工位', 0.40, 0, NULL, NULL, '/sop/kb87/pack.pdf', 1),
(@model_104_id, 'R1.0', 10, 'PCB-TEST', 'PCBA测试', 2, 'ST-PCBA-02', 'PCBA测试工位', 0.45, 1, 'ICT', '{"items":["短路","矩阵扫描"],"method":"治具测试"}', '/sop/kb104/pcba-test.pdf', 1),
(@model_104_id, 'R1.0', 20, 'ASSEMBLY', '总装', 1, 'ST-ASM-02', '总装二工位', 1.80, 0, NULL, NULL, '/sop/kb104/assembly.pdf', 1),
(@model_104_id, 'R1.0', 30, 'FINAL-QC', '终检', 2, 'ST-QC-02', '终检工位', 0.80, 1, 'final', '{"items":["外观","有线连接","全键触发"],"method":"人工抽检"}', '/sop/kb104/final-qc.pdf', 1),
(@model_104_id, 'R1.0', 40, 'PACK', '包装入库', 3, 'ST-PACK-02', '包装工位', 0.45, 0, NULL, NULL, '/sop/kb104/pack.pdf', 1),
(@model_68_id, 'R1.0', 10, 'PCB-TEST', 'PCBA测试', 2, 'ST-PCBA-03', 'PCBA测试工位', 0.40, 1, 'ICT', '{"items":["蓝牙模块","矩阵扫描"],"method":"治具测试"}', '/sop/kb68/pcba-test.pdf', 1),
(@model_68_id, 'R1.0', 20, 'ASSEMBLY', '总装', 1, 'ST-ASM-03', '总装三工位', 1.20, 0, NULL, NULL, '/sop/kb68/assembly.pdf', 1),
(@model_68_id, 'R1.0', 30, 'AGING', '老化测试', 2, 'ST-AGING-02', '老化测试工位', 1.60, 1, 'FCT', '{"items":["无线连接","电池充放电"],"method":"自动测试"}', '/sop/kb68/aging.pdf', 1),
(@model_68_id, 'R1.0', 40, 'FINAL-QC', '终检', 2, 'ST-QC-03', '终检工位', 0.55, 1, 'final', '{"items":["外观","配件","SN标签"],"method":"人工抽检"}', '/sop/kb68/final-qc.pdf', 1),
(@model_68_id, 'R1.0', 50, 'PACK', '包装入库', 3, 'ST-PACK-03', '包装工位', 0.30, 0, NULL, NULL, '/sop/kb68/pack.pdf', 1),
(@model_84_id, 'R1.0', 10, 'ASSEMBLY', '总装', 1, 'ST-ASM-04', '矮轴总装工位', 1.40, 0, NULL, NULL, '/sop/kb84/assembly.pdf', 1),
(@model_84_id, 'R1.0', 20, 'FINAL-QC', '终检', 2, 'ST-QC-04', '终检工位', 0.60, 1, 'final', '{"items":["矮轴手感","外观","Type-C连接"],"method":"人工抽检"}', '/sop/kb84/final-qc.pdf', 1),
(@model_84_id, 'R1.0', 30, 'PACK', '包装入库', 3, 'ST-PACK-04', '包装工位', 0.30, 0, NULL, NULL, '/sop/kb84/pack.pdf', 1),
(@model_macro_id, 'R1.0', 10, 'ASSEMBLY', '总装', 1, 'ST-ASM-05', '小键盘总装工位', 0.80, 0, NULL, NULL, '/sop/macro/assembly.pdf', 1),
(@model_macro_id, 'R1.0', 20, 'FUNCTION', '功能测试', 2, 'ST-QC-05', '功能测试工位', 0.40, 1, 'FCT', '{"items":["宏定义","旋钮","RGB"],"method":"软件测试"}', '/sop/macro/function.pdf', 1),
(@model_macro_id, 'R1.0', 30, 'PACK', '包装入库', 3, 'ST-PACK-05', '包装工位', 0.20, 0, NULL, NULL, '/sop/macro/pack.pdf', 1),
(@model_split_id, 'R1.0', 10, 'PCB-TEST', '左右主板测试', 2, 'ST-PCBA-04', '分体主板测试工位', 0.70, 1, 'ICT', '{"items":["左右主板","桥接通信"],"method":"治具测试"}', '/sop/split75/pcba-test.pdf', 1),
(@model_split_id, 'R1.0', 20, 'ASSEMBLY', '分体总装', 1, 'ST-ASM-06', '分体总装工位', 1.90, 0, NULL, NULL, '/sop/split75/assembly.pdf', 1),
(@model_split_id, 'R1.0', 30, 'ERGONOMIC-QC', '人体工学终检', 2, 'ST-QC-06', '分体终检工位', 0.80, 1, 'final', '{"items":["左右通信","腕托贴合","外观"],"method":"人工抽检"}', '/sop/split75/final-qc.pdf', 1),
(@model_split_id, 'R1.0', 40, 'PACK', '包装入库', 3, 'ST-PACK-06', '分体包装工位', 0.50, 0, NULL, NULL, '/sop/split75/pack.pdf', 1),
(@model_game_id, 'R1.0', 10, 'PCB-TEST', '低延迟PCBA测试', 2, 'ST-PCBA-05', '电竞主板测试工位', 0.55, 1, 'ICT', '{"items":["8K回报率","矩阵扫描"],"method":"治具测试"}', '/sop/game-tkl/pcba-test.pdf', 1),
(@model_game_id, 'R1.0', 20, 'STAB-TUNE', '大键调校', 1, 'ST-TUNE-01', '手感调校工位', 0.60, 0, NULL, NULL, '/sop/game-tkl/stab-tune.pdf', 1),
(@model_game_id, 'R1.0', 30, 'ASSEMBLY', '电竞总装', 1, 'ST-ASM-07', '电竞总装工位', 1.50, 0, NULL, NULL, '/sop/game-tkl/assembly.pdf', 1),
(@model_game_id, 'R1.0', 40, 'LATENCY-QC', '低延迟功能测试', 2, 'ST-QC-07', '电竞功能测试工位', 0.70, 1, 'FCT', '{"items":["延迟","RGB","全键无冲"],"method":"自动测试"}', '/sop/game-tkl/latency-qc.pdf', 1),
(@model_game_id, 'R1.0', 50, 'PACK', '包装入库', 3, 'ST-PACK-07', '电竞包装工位', 0.35, 0, NULL, NULL, '/sop/game-tkl/pack.pdf', 1),
(@model_silent_id, 'R1.0', 10, 'ASSEMBLY', '静音总装', 1, 'ST-ASM-08', '静音总装工位', 1.70, 0, NULL, NULL, '/sop/silent98/assembly.pdf', 1),
(@model_silent_id, 'R1.0', 20, 'SOUND-QC', '噪声测试', 2, 'ST-QC-08', '声学测试工位', 0.60, 1, 'FCT', '{"items":["按键噪声","空腔音","全键触发"],"method":"声学测试"}', '/sop/silent98/sound-qc.pdf', 1),
(@model_silent_id, 'R1.0', 30, 'PACK', '包装入库', 3, 'ST-PACK-08', '静音包装工位', 0.35, 0, NULL, NULL, '/sop/silent98/pack.pdf', 1);

-- 6. 生产工单：覆盖 2026-07-06 至 2026-07-18。
INSERT INTO `production_order`
(`order_no`, `product_model_id`, `bom_version`, `route_version`, `batch_no`, `quantity`,
 `planned_start_time`, `planned_end_time`, `delivery_date`, `line_name`, `source`, `priority`,
 `planner_id`, `material_ready_status`, `status`)
VALUES
('MO-20260706-001', @model_split_id, 'V1.0', 'R1.0', 'BATCH-SPLIT75-0706', 45, '2026-07-06 08:30:00', '2026-07-06 17:30:00', '2026-07-13', '六号人体工学线', 'ERP', 0, @planner_id, 1, 5),
('MO-20260707-001', @model_game_id, 'V1.0', 'R1.0', 'BATCH-GAMETKL-0707', 80, '2026-07-07 08:30:00', '2026-07-07 18:00:00', '2026-07-14', '七号电竞线', 'ERP', 1, @planner_id, 1, 5),
('MO-20260708-001', @model_silent_id, 'V1.0', 'R1.0', 'BATCH-SILENT98-0708', 60, '2026-07-08 08:30:00', '2026-07-08 17:00:00', '2026-07-15', '八号静音线', 'ERP', 0, @planner_id, 1, 5),
('MO-20260709-001', @model_87_id, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0709', 100, '2026-07-09 08:30:00', '2026-07-09 18:00:00', '2026-07-16', '一号装配线', 'ERP', 0, @planner_id, 1, 5),
('MO-20260710-001', @model_104_id, 'V1.0', 'R1.0', 'BATCH-PRO104-0710', 90, '2026-07-10 08:30:00', '2026-07-10 18:00:00', '2026-07-17', '二号装配线', 'ERP', 0, @planner_id, 1, 5),
('MO-20260711-001', @model_84_id, 'V1.0', 'R1.0', 'BATCH-LOW84-0711', 70, '2026-07-11 08:30:00', '2026-07-11 17:30:00', '2026-07-18', '四号矮轴线', 'ERP', 0, @planner_id, 1, 5),
('MO-20260712-001', @model_68_id, 'V1.0', 'R1.0', 'BATCH-MINI68-0712', 50, '2026-07-12 08:30:00', '2026-07-12 17:30:00', '2026-07-19', '三号装配线', 'ERP', 0, @planner_id, 1, 4),
('MO-20260713-001', @model_104_id, 'V1.0', 'R1.0', 'BATCH-PRO104-0713', 90, '2026-07-13 08:30:00', '2026-07-13 18:00:00', '2026-07-20', '二号装配线', 'ERP', 0, @planner_id, 1, 4),
('MO-20260714-001', @model_87_id, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0714', 100, '2026-07-14 08:30:00', '2026-07-14 18:00:00', '2026-07-21', '一号装配线', 'manual', 1, @planner_id, 1, 3),
('MO-20260715-001', @model_87_id, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0715', 120, '2026-07-15 08:30:00', '2026-07-15 18:00:00', '2026-07-22', '一号装配线', 'manual', 1, @planner_id, 1, 2),
('MO-20260716-001', @model_macro_id, 'V1.0', 'R1.0', 'BATCH-MACRO12-0716', 40, '2026-07-16 09:00:00', '2026-07-16 15:00:00', '2026-07-23', '试产线', 'manual', 0, @planner_id, 1, 1),
('MO-20260717-001', @model_game_id, 'V1.0', 'R1.0', 'BATCH-GAMETKL-0717', 75, '2026-07-17 08:30:00', '2026-07-17 18:00:00', '2026-07-24', '七号电竞线', 'manual', 1, @planner_id, 1, 1),
('MO-20260718-001', @model_split_id, 'V1.0', 'R1.0', 'BATCH-SPLIT75-0718', 55, '2026-07-18 08:30:00', '2026-07-18 17:30:00', '2026-07-25', '六号人体工学线', 'manual', 0, @planner_id, 1, 0),
('MO-20260718-002', @model_silent_id, 'V1.0', 'R1.0', 'BATCH-SILENT98-0718', 65, '2026-07-18 13:00:00', '2026-07-18 20:00:00', '2026-07-26', '八号静音线', 'manual', 0, @planner_id, 0, 0),
('MO-20260706-002', @model_68_id, 'V1.0', 'R1.0', 'BATCH-MINI68-0706-B', 36, '2026-07-06 13:00:00', '2026-07-06 20:00:00', '2026-07-13', '三号装配线', 'manual', 0, @planner_id, 1, 5),
('MO-20260707-002', @model_104_id, 'V1.0', 'R1.0', 'BATCH-PRO104-0707-B', 55, '2026-07-07 13:00:00', '2026-07-07 21:00:00', '2026-07-14', '二号装配线', 'ERP', 0, @planner_id, 1, 5),
('MO-20260708-002', @model_game_id, 'V1.0', 'R1.0', 'BATCH-GAMETKL-0708-B', 42, '2026-07-08 12:30:00', '2026-07-08 20:30:00', '2026-07-15', '七号电竞线', 'manual', 1, @planner_id, 1, 5),
('MO-20260709-002', @model_silent_id, 'V1.0', 'R1.0', 'BATCH-SILENT98-0709-B', 48, '2026-07-09 13:30:00', '2026-07-09 20:00:00', '2026-07-16', '八号静音线', 'ERP', 0, @planner_id, 1, 5),
('MO-20260710-002', @model_split_id, 'V1.0', 'R1.0', 'BATCH-SPLIT75-0710-B', 35, '2026-07-10 13:00:00', '2026-07-10 20:30:00', '2026-07-17', '六号人体工学线', 'manual', 0, @planner_id, 1, 5),
('MO-20260711-002', @model_87_id, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0711-B', 60, '2026-07-11 12:30:00', '2026-07-11 20:30:00', '2026-07-18', '一号装配线', 'ERP', 1, @planner_id, 1, 5),
('MO-20260712-002', @model_macro_id, 'V1.0', 'R1.0', 'BATCH-MACRO12-0712-B', 30, '2026-07-12 13:00:00', '2026-07-12 18:00:00', '2026-07-19', '试产线', 'manual', 0, @planner_id, 1, 4),
('MO-20260713-002', @model_84_id, 'V1.0', 'R1.0', 'BATCH-LOW84-0713-B', 65, '2026-07-13 13:00:00', '2026-07-13 20:00:00', '2026-07-20', '四号矮轴线', 'ERP', 0, @planner_id, 1, 4),
('MO-20260714-002', @model_game_id, 'V1.0', 'R1.0', 'BATCH-GAMETKL-0714-B', 85, '2026-07-14 13:00:00', '2026-07-15 12:00:00', '2026-07-22', '七号电竞线', 'manual', 1, @planner_id, 1, 0),
('MO-20260715-002', @model_68_id, 'V1.0', 'R1.0', 'BATCH-MINI68-0715-B', 72, '2026-07-15 13:30:00', '2026-07-16 13:00:00', '2026-07-23', '三号装配线', 'ERP', 0, @planner_id, 1, 0),
('MO-20260716-002', @model_silent_id, 'V1.0', 'R1.0', 'BATCH-SILENT98-0716-B', 68, '2026-07-16 13:00:00', '2026-07-17 10:00:00', '2026-07-24', '八号静音线', 'manual', 0, @planner_id, 1, 0),
('MO-20260717-002', @model_104_id, 'V1.0', 'R1.0', 'BATCH-PRO104-0717-B', 95, '2026-07-17 13:00:00', '2026-07-18 12:00:00', '2026-07-25', '二号装配线', 'ERP', 0, @planner_id, 1, 0),
('MO-20260718-003', @model_87_id, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0718-C', 88, '2026-07-18 08:00:00', '2026-07-18 18:00:00', '2026-07-26', '一号装配线', 'manual', 1, @planner_id, 1, 0),
('MO-20260718-004', @model_macro_id, 'V1.0', 'R1.0', 'BATCH-MACRO12-0718-D', 24, '2026-07-18 14:00:00', '2026-07-18 20:00:00', '2026-07-27', '试产线', 'manual', 0, @planner_id, 0, 0);

-- 7. 根据工单和工艺路线批量生成生产任务，减少手写维护成本。
INSERT INTO `production_task`
(`task_no`, `order_id`, `process_route_id`, `sequence_no`, `station_code`, `assigned_user_id`,
 `planned_quantity`, `completed_quantity`, `defect_quantity`, `abnormal_type`, `abnormal_desc`, `abnormal_solution`,
 `status`, `start_time`, `finish_time`, `remark`)
SELECT
    CONCAT('TASK-', DATE_FORMAT(o.`planned_start_time`, '%m%d'), '-', LPAD(o.`id`, 3, '0'), '-', LPAD(pr.`sequence_no`, 3, '0')) AS `task_no`,
    o.`id`,
    pr.`id`,
    pr.`sequence_no`,
    pr.`station_code`,
    CASE
        WHEN pr.`process_type` = 1 THEN @operator_a_id
        WHEN pr.`process_type` = 2 THEN @inspector_a_id
        ELSE @operator_b_id
    END AS `assigned_user_id`,
    o.`quantity`,
    CASE
        WHEN DATE(o.`planned_start_time`) <= '2026-07-13' THEN o.`quantity`
        WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` <= 20 THEN o.`quantity`
        WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` = 30 THEN 98
        WHEN o.`order_no` = 'MO-20260715-001' AND pr.`sequence_no` = 10 THEN o.`quantity`
        WHEN o.`order_no` = 'MO-20260715-001' AND pr.`sequence_no` = 20 THEN 76
        WHEN o.`order_no` = 'MO-20260716-001' AND pr.`sequence_no` = 10 THEN 18
        WHEN o.`order_no` = 'MO-20260717-001' AND pr.`sequence_no` <= 20 THEN 30
        ELSE 0
    END AS `completed_quantity`,
    CASE
        WHEN o.`order_no` IN ('MO-20260707-001', 'MO-20260710-001', 'MO-20260713-001') AND pr.`is_quality_gate` = 1 THEN 2
        WHEN o.`order_no` IN ('MO-20260706-001', 'MO-20260708-001', 'MO-20260711-001', 'MO-20260712-001') AND pr.`is_quality_gate` = 1 THEN 1
        WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` IN (20, 30) THEN 2
        WHEN o.`order_no` = 'MO-20260715-001' AND pr.`sequence_no` = 20 THEN 2
        WHEN o.`order_no` = 'MO-20260717-001' AND pr.`sequence_no` = 20 THEN 1
        ELSE 0
    END AS `defect_quantity`,
    CASE WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` = 30 THEN 3 ELSE NULL END AS `abnormal_type`,
    CASE WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` = 30 THEN '老化测试发现蓝牙连接不稳定' ELSE NULL END AS `abnormal_desc`,
    CASE WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` = 30 THEN '已转返修排查无线模块' ELSE NULL END AS `abnormal_solution`,
    CASE
        WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` = 30 THEN 5
        WHEN DATE(o.`planned_start_time`) <= '2026-07-13' THEN 3
        WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` <= 20 THEN 3
        WHEN o.`order_no` = 'MO-20260715-001' AND pr.`sequence_no` = 10 THEN 3
        WHEN o.`order_no` IN ('MO-20260715-001', 'MO-20260716-001', 'MO-20260717-001') AND pr.`sequence_no` <= 20 THEN 1
        ELSE 0
    END AS `status`,
    CASE
        WHEN DATE(o.`planned_start_time`) <= '2026-07-17' AND (
            DATE(o.`planned_start_time`) <= '2026-07-13'
            OR (o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` <= 30)
            OR (o.`order_no` = 'MO-20260715-001' AND pr.`sequence_no` <= 20)
            OR (o.`order_no` = 'MO-20260716-001' AND pr.`sequence_no` = 10)
            OR (o.`order_no` = 'MO-20260717-001' AND pr.`sequence_no` <= 20)
        ) THEN DATE_ADD(o.`planned_start_time`, INTERVAL pr.`sequence_no` MINUTE)
        ELSE NULL
    END AS `start_time`,
    CASE
        WHEN DATE(o.`planned_start_time`) <= '2026-07-13'
             OR (o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` <= 20)
             OR (o.`order_no` = 'MO-20260715-001' AND pr.`sequence_no` = 10)
        THEN DATE_ADD(o.`planned_start_time`, INTERVAL (pr.`sequence_no` + 70) MINUTE)
        ELSE NULL
    END AS `finish_time`,
    CASE
        WHEN DATE(o.`planned_start_time`) <= '2026-07-13' THEN '历史完成任务'
        WHEN o.`order_no` = 'MO-20260714-001' AND pr.`sequence_no` = 30 THEN '存在质量异常，待返修'
        WHEN o.`order_no` IN ('MO-20260715-001', 'MO-20260716-001', 'MO-20260717-001') THEN '生产进行中'
        ELSE '计划待执行'
    END AS `remark`
FROM `production_order` o
JOIN `process_route` pr
  ON pr.`product_model_id` = o.`product_model_id`
 AND pr.`route_version` = o.`route_version`
ORDER BY o.`planned_start_time`, pr.`sequence_no`;

SET @order_0706 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260706-001');
SET @order_0707 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260707-001');
SET @order_0708 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260708-001');
SET @order_0709 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260709-001');
SET @order_0710 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260710-001');
SET @order_0711 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260711-001');
SET @order_0712 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260712-001');
SET @order_0713 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260713-001');
SET @order_0714 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260714-001');
SET @order_0715 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260715-001');
SET @order_0716 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260716-001');
SET @order_0717 := (SELECT `id` FROM `production_order` WHERE `order_no` = 'MO-20260717-001');

SET @task_0706_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0706 AND pr.`is_quality_gate` = 1 ORDER BY pr.`sequence_no` DESC LIMIT 1);
SET @task_0707_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0707 AND pr.`is_quality_gate` = 1 ORDER BY pr.`sequence_no` DESC LIMIT 1);
SET @task_0708_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0708 AND pr.`is_quality_gate` = 1 ORDER BY pr.`sequence_no` DESC LIMIT 1);
SET @task_0709_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0709 AND pr.`is_quality_gate` = 1 ORDER BY pr.`sequence_no` DESC LIMIT 1);
SET @task_0710_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0710 AND pr.`is_quality_gate` = 1 ORDER BY pr.`sequence_no` DESC LIMIT 1);
SET @task_0711_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0711 AND pr.`is_quality_gate` = 1 ORDER BY pr.`sequence_no` DESC LIMIT 1);
SET @task_0712_asm := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0712 AND pr.`process_code` = 'ASSEMBLY' LIMIT 1);
SET @task_0712_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0712 AND pr.`is_quality_gate` = 1 ORDER BY pr.`sequence_no` DESC LIMIT 1);
SET @task_0713_asm := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0713 AND pr.`process_code` = 'ASSEMBLY' LIMIT 1);
SET @task_0713_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0713 AND pr.`is_quality_gate` = 1 ORDER BY pr.`sequence_no` DESC LIMIT 1);
SET @task_0714_asm := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0714 AND pr.`process_code` = 'ASSEMBLY' LIMIT 1);
SET @task_0714_qc := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0714 AND pr.`process_code` = 'AGING' LIMIT 1);
SET @task_0715_pcba := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0715 AND pr.`process_code` = 'PCB-TEST' LIMIT 1);
SET @task_0715_asm := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0715 AND pr.`process_code` = 'ASSEMBLY' LIMIT 1);
SET @task_0716_asm := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0716 AND pr.`process_code` = 'ASSEMBLY' LIMIT 1);
SET @task_0717_tune := (SELECT t.`id` FROM `production_task` t JOIN `process_route` pr ON pr.`id` = t.`process_route_id` WHERE t.`order_id` = @order_0717 AND pr.`process_code` = 'STAB-TUNE' LIMIT 1);

-- 8. 报工记录：报工日期覆盖 7 月 6 日至 7 月 17 日，7 月 18 日保留计划待执行工单。
INSERT INTO `work_report`
(`report_no`, `task_id`, `order_id`, `product_sn`, `operator_id`, `station_code`,
 `report_quantity`, `qualified_quantity`, `defect_quantity`, `actual_hours`, `defect_reason`, `report_time`, `status`)
VALUES
('WR-20260706-001', @task_0706_qc, @order_0706, 'SN-SPLIT75-0706-001', @inspector_b_id, 'ST-QC-06', 45, 44, 1, 1.20, '腕托贴合偏差', '2026-07-06 15:20:00', 1),
('WR-20260707-001', @task_0707_qc, @order_0707, 'SN-GAMETKL-0707-001', @inspector_a_id, 'ST-QC-07', 80, 78, 2, 1.60, 'RGB灯效异常', '2026-07-07 16:00:00', 1),
('WR-20260708-001', @task_0708_qc, @order_0708, 'SN-SILENT98-0708-001', @inspector_b_id, 'ST-QC-08', 60, 59, 1, 1.10, '空腔音偏大', '2026-07-08 14:40:00', 1),
('WR-20260709-001', @task_0709_qc, @order_0709, 'SN-ALPHA87-0709-001', @inspector_a_id, 'ST-QC-01', 100, 99, 1, 1.50, '键帽轻微划伤', '2026-07-09 16:10:00', 1),
('WR-20260710-001', @task_0710_qc, @order_0710, 'SN-PRO104-0710-001', @inspector_b_id, 'ST-QC-02', 90, 88, 2, 1.80, '空格键异响', '2026-07-10 15:50:00', 1),
('WR-20260711-001', @task_0711_qc, @order_0711, 'SN-LOW84-0711-001', @inspector_b_id, 'ST-QC-04', 70, 69, 1, 1.20, '外壳划伤', '2026-07-11 15:10:00', 1),
('WR-20260712-001', @task_0712_asm, @order_0712, 'SN-MINI68-0712-001', @operator_a_id, 'ST-ASM-03', 50, 49, 1, 2.00, '电池线束压伤', '2026-07-12 12:00:00', 1),
('WR-20260712-002', @task_0712_qc, @order_0712, 'SN-MINI68-0712-001', @inspector_a_id, 'ST-QC-03', 50, 49, 1, 1.80, '蓝牙连接重连', '2026-07-12 15:00:00', 1),
('WR-20260713-001', @task_0713_asm, @order_0713, 'SN-PRO104-0713-001', @operator_a_id, 'ST-ASM-02', 90, 88, 2, 2.40, '空格键异响', '2026-07-13 12:10:00', 1),
('WR-20260713-002', @task_0713_qc, @order_0713, 'SN-PRO104-0713-001', @inspector_b_id, 'ST-QC-02', 90, 88, 2, 1.90, '外观轻微划伤', '2026-07-13 15:30:00', 1),
('WR-20260714-001', @task_0714_asm, @order_0714, 'SN-ALPHA87-0714-001', @operator_a_id, 'ST-ASM-01', 100, 97, 3, 2.60, '键帽划伤、卫星轴异响', '2026-07-14 12:30:00', 1),
('WR-20260714-002', @task_0714_qc, @order_0714, 'SN-ALPHA87-0714-001', @inspector_a_id, 'ST-AGING-01', 98, 96, 2, 1.70, '蓝牙断连', '2026-07-14 15:10:00', 1),
('WR-20260715-001', @task_0715_pcba, @order_0715, 'SN-ALPHA87-0715-001', @inspector_a_id, 'ST-PCBA-01', 120, 120, 0, 1.20, NULL, '2026-07-15 09:45:00', 1),
('WR-20260715-002', @task_0715_asm, @order_0715, 'SN-ALPHA87-0715-001', @operator_a_id, 'ST-ASM-01', 40, 39, 1, 1.10, '键帽划伤', '2026-07-15 11:30:00', 1),
('WR-20260715-003', @task_0715_asm, @order_0715, 'SN-ALPHA87-0715-002', @operator_a_id, 'ST-ASM-01', 36, 35, 1, 1.00, '轴体触发不稳定', '2026-07-15 14:20:00', 1),
('WR-20260716-001', @task_0716_asm, @order_0716, 'SN-MACRO12-0716-001', @operator_a_id, 'ST-ASM-05', 18, 18, 0, 0.60, NULL, '2026-07-16 10:40:00', 1),
('WR-20260717-001', @task_0717_tune, @order_0717, 'SN-GAMETKL-0717-001', @operator_b_id, 'ST-TUNE-01', 30, 29, 1, 0.80, '大键回弹不一致', '2026-07-17 10:50:00', 1);

-- 第二批历史工单报工：同一天、不同型号和工序形成更分散的统计数据。
INSERT INTO `work_report`
(`report_no`, `task_id`, `order_id`, `product_sn`, `operator_id`, `station_code`,
 `report_quantity`, `qualified_quantity`, `defect_quantity`, `actual_hours`, `defect_reason`, `report_time`, `status`)
SELECT
    CONCAT('WR-', DATE_FORMAT(o.`planned_start_time`, '%Y%m%d'), '-B', LPAD(pr.`sequence_no`, 3, '0')),
    t.`id`,
    o.`id`,
    CONCAT('SN-', REPLACE(pm.`model_code`, 'KB-', ''), '-B-', DATE_FORMAT(o.`planned_start_time`, '%m%d')),
    CASE WHEN pr.`process_type` = 2 THEN @inspector_a_id ELSE @operator_b_id END,
    t.`station_code`,
    o.`quantity`,
    o.`quantity` - CASE
        WHEN MOD(pr.`sequence_no` + DAY(o.`planned_start_time`), 4) = 0 THEN 2
        WHEN MOD(pr.`sequence_no` + DAY(o.`planned_start_time`), 3) = 0 THEN 1
        ELSE 0
    END,
    CASE
        WHEN MOD(pr.`sequence_no` + DAY(o.`planned_start_time`), 4) = 0 THEN 2
        WHEN MOD(pr.`sequence_no` + DAY(o.`planned_start_time`), 3) = 0 THEN 1
        ELSE 0
    END,
    ROUND(pr.`standard_hours` * (0.85 + MOD(o.`id` + pr.`sequence_no`, 4) * 0.08), 2),
    CASE
        WHEN MOD(pr.`sequence_no` + DAY(o.`planned_start_time`), 4) <> 0
         AND MOD(pr.`sequence_no` + DAY(o.`planned_start_time`), 3) <> 0 THEN NULL
        WHEN pr.`process_code` = 'ASSEMBLY' THEN '键帽安装偏位'
        WHEN pr.`process_code` = 'LATENCY-QC' THEN 'RGB灯效色差'
        WHEN pr.`process_code` = 'SOUND-QC' THEN '按键噪声偏高'
        WHEN pr.`process_code` = 'ERGONOMIC-QC' THEN '左右外壳贴合偏差'
        WHEN pr.`process_code` = 'FUNCTION' THEN '旋钮阻尼不一致'
        WHEN pr.`process_code` = 'PCB-TEST' THEN '矩阵测试偶发失败'
        ELSE '外观轻微瑕疵'
    END,
    DATE_ADD(o.`planned_start_time`, INTERVAL (pr.`sequence_no` * 4) MINUTE),
    1
FROM `production_order` o
JOIN `product_model` pm ON pm.`id` = o.`product_model_id`
JOIN `production_task` t ON t.`order_id` = o.`id`
JOIN `process_route` pr ON pr.`id` = t.`process_route_id`
WHERE o.`order_no` IN (
    'MO-20260706-002', 'MO-20260707-002', 'MO-20260708-002', 'MO-20260709-002',
    'MO-20260710-002', 'MO-20260711-002', 'MO-20260712-002', 'MO-20260713-002'
)
  AND (
      pr.`process_code` IN ('ASSEMBLY', 'FINAL-QC', 'LATENCY-QC', 'SOUND-QC', 'ERGONOMIC-QC', 'FUNCTION')
      OR (o.`order_no` = 'MO-20260708-002' AND pr.`process_code` = 'PCB-TEST')
  )
ORDER BY o.`planned_start_time`, pr.`sequence_no`;

-- 9. 质检与复检记录
INSERT INTO `inspection_record`
(`inspection_no`, `inspection_type`, `task_id`, `order_id`, `product_sn`, `process_route_id`,
 `standard_snapshot`, `inspector_id`, `source_type`, `equipment_no`, `inspection_time`, `result`, `measured_data`,
 `defect_reason`, `handling_method`, `approval_status`, `remark`)
SELECT 'QC-20260706-001', 'final', @task_0706_qc, @order_0706, 'SN-SPLIT75-0706-001', t.`process_route_id`,
       '{"item":"分体终检","standard":"左右通信稳定，腕托贴合"}', @inspector_b_id, 'manual', NULL, '2026-07-06 15:05:00', 2,
       '{"wristRest":"minor_gap","link":"pass"}', '腕托贴合偏差', 3, 1, '让步接收'
FROM `production_task` t WHERE t.`id` = @task_0706_qc
UNION ALL
SELECT 'QC-20260707-001', 'FCT', @task_0707_qc, @order_0707, 'SN-GAMETKL-0707-001', t.`process_route_id`,
       '{"item":"电竞功能测试","standard":"8K回报率、RGB、全键无冲"}', @inspector_a_id, 'equipment', 'FCT-GAME-01', '2026-07-07 15:40:00', 0,
       '{"rgb":"fail","latency":"pass"}', 'RGB灯效异常', 1, 0, '进入返修'
FROM `production_task` t WHERE t.`id` = @task_0707_qc
UNION ALL
SELECT 'RQ-20260707-001', 'recheck', @task_0707_qc, @order_0707, 'SN-GAMETKL-0707-001', t.`process_route_id`,
       '{"item":"返修复检","standard":"RGB灯效正常"}', @inspector_b_id, 'manual', NULL, '2026-07-07 17:20:00', 1,
       '{"rgb":"pass"}', NULL, NULL, 0, '复检通过'
FROM `production_task` t WHERE t.`id` = @task_0707_qc
UNION ALL
SELECT 'QC-20260708-001', 'FCT', @task_0708_qc, @order_0708, 'SN-SILENT98-0708-001', t.`process_route_id`,
       '{"item":"噪声测试","standard":"按键噪声低于阈值"}', @inspector_b_id, 'equipment', 'SOUND-01', '2026-07-08 14:20:00', 0,
       '{"noise":"high","key":"enter"}', '空腔音偏大', 1, 0, '进入返修'
FROM `production_task` t WHERE t.`id` = @task_0708_qc
UNION ALL
SELECT 'RQ-20260708-001', 'recheck', @task_0708_qc, @order_0708, 'SN-SILENT98-0708-001', t.`process_route_id`,
       '{"item":"返修复检","standard":"噪声恢复正常"}', @inspector_b_id, 'manual', NULL, '2026-07-08 16:30:00', 1,
       '{"noise":"pass"}', NULL, NULL, 0, '复检通过'
FROM `production_task` t WHERE t.`id` = @task_0708_qc
UNION ALL
SELECT 'QC-20260710-001', 'final', @task_0710_qc, @order_0710, 'SN-PRO104-0710-001', t.`process_route_id`,
       '{"item":"终检","standard":"全键触发、外观、附件"}', @inspector_b_id, 'manual', NULL, '2026-07-10 15:30:00', 0,
       '{"space":"noise","appearance":"pass"}', '空格键异响', 1, 0, '需要返修'
FROM `production_task` t WHERE t.`id` = @task_0710_qc
UNION ALL
SELECT 'RQ-20260710-001', 'recheck', @task_0710_qc, @order_0710, 'SN-PRO104-0710-001', t.`process_route_id`,
       '{"item":"返修复检","standard":"异响消除"}', @inspector_b_id, 'manual', NULL, '2026-07-10 17:00:00', 1,
       '{"space":"pass"}', NULL, NULL, 0, '复检通过'
FROM `production_task` t WHERE t.`id` = @task_0710_qc
UNION ALL
SELECT 'QC-20260712-001', 'FCT', @task_0712_qc, @order_0712, 'SN-MINI68-0712-001', t.`process_route_id`,
       '{"item":"无线连接测试","standard":"10分钟不断连"}', @inspector_a_id, 'equipment', 'FCT-AGING-02', '2026-07-12 14:40:00', 0,
       '{"bluetooth":"reconnect","duration":"10min"}', '蓝牙连接重连', 1, 0, '进入返修'
FROM `production_task` t WHERE t.`id` = @task_0712_qc
UNION ALL
SELECT 'RQ-20260712-001', 'recheck', @task_0712_qc, @order_0712, 'SN-MINI68-0712-001', t.`process_route_id`,
       '{"item":"返修复检","standard":"无线连接稳定"}', @inspector_b_id, 'manual', NULL, '2026-07-12 16:00:00', 1,
       '{"bluetooth":"pass"}', NULL, NULL, 0, '返修复检通过'
FROM `production_task` t WHERE t.`id` = @task_0712_qc
UNION ALL
SELECT 'QC-20260714-001', 'FCT', @task_0714_qc, @order_0714, 'SN-ALPHA87-0714-001', t.`process_route_id`,
       '{"item":"老化测试","standard":"无线连接稳定，RGB正常"}', @inspector_a_id, 'equipment', 'FCT-AGING-01', '2026-07-14 15:00:00', 0,
       '{"bluetooth":"unstable","rgb":"pass"}', '蓝牙连接不稳定', 1, 0, '待返修处理'
FROM `production_task` t WHERE t.`id` = @task_0714_qc
UNION ALL
SELECT 'QC-20260715-001', 'process', @task_0715_asm, @order_0715, 'SN-ALPHA87-0715-002', t.`process_route_id`,
       '{"item":"装配过程巡检","standard":"键帽无划伤，轴体触发稳定"}', @inspector_a_id, 'manual', NULL, '2026-07-15 14:30:00', 0,
       '{"switch":"unstable","keycap":"pass"}', '轴体触发不稳定', 1, 0, '新产生返修任务'
FROM `production_task` t WHERE t.`id` = @task_0715_asm;

-- 第二批质检数据：覆盖设备检测、人工终检、合格、不合格和让步接收。
INSERT INTO `inspection_record`
(`inspection_no`, `inspection_type`, `task_id`, `order_id`, `product_sn`, `process_route_id`,
 `standard_snapshot`, `inspector_id`, `source_type`, `equipment_no`, `inspection_time`, `result`, `measured_data`,
 `defect_reason`, `handling_method`, `approval_status`, `remark`)
SELECT
    CONCAT('QC-', DATE_FORMAT(o.`planned_start_time`, '%Y%m%d'), '-B', LPAD(pr.`sequence_no`, 3, '0')),
    COALESCE(pr.`inspection_type`, 'process'),
    t.`id`,
    o.`id`,
    CONCAT('SN-', REPLACE(pm.`model_code`, 'KB-', ''), '-B-', DATE_FORMAT(o.`planned_start_time`, '%m%d')),
    pr.`id`,
    CONCAT('{"item":"', pr.`process_name`, '","standard":"按工艺检验规范执行"}'),
    CASE WHEN MOD(pr.`sequence_no`, 2) = 0 THEN @inspector_a_id ELSE @inspector_b_id END,
    CASE WHEN pr.`inspection_type` IN ('ICT', 'FCT') THEN 'equipment' ELSE 'manual' END,
    CASE WHEN pr.`inspection_type` IN ('ICT', 'FCT') THEN CONCAT('EQ-', LPAD(pr.`id`, 3, '0')) ELSE NULL END,
    DATE_ADD(o.`planned_start_time`, INTERVAL (pr.`sequence_no` * 4 - 10) MINUTE),
    CASE
        WHEN DATE(o.`planned_start_time`) <= '2026-07-09' THEN 0
        WHEN DATE(o.`planned_start_time`) = '2026-07-10' THEN 2
        ELSE 1
    END,
    CASE
        WHEN DATE(o.`planned_start_time`) <= '2026-07-09' THEN '{"pass":false,"level":"minor"}'
        WHEN DATE(o.`planned_start_time`) = '2026-07-10' THEN '{"pass":true,"concession":true}'
        ELSE '{"pass":true}'
    END,
    CASE
        WHEN DATE(o.`planned_start_time`) > '2026-07-09' THEN NULL
        WHEN pr.`process_code` = 'LATENCY-QC' THEN '灯效颜色不一致'
        WHEN pr.`process_code` = 'SOUND-QC' THEN '空腔音超过标准'
        WHEN pr.`process_code` = 'PCB-TEST' THEN '矩阵扫描偶发失败'
        WHEN pr.`process_code` = 'AGING' THEN '无线连接短暂重连'
        ELSE '外观装配间隙偏大'
    END,
    CASE
        WHEN DATE(o.`planned_start_time`) <= '2026-07-09' THEN 1
        WHEN DATE(o.`planned_start_time`) = '2026-07-10' THEN 3
        ELSE NULL
    END,
    CASE WHEN DATE(o.`planned_start_time`) = '2026-07-10' THEN 1 ELSE 0 END,
    CASE
        WHEN DATE(o.`planned_start_time`) <= '2026-07-09' THEN '检验不合格，已进入返修'
        WHEN DATE(o.`planned_start_time`) = '2026-07-10' THEN '轻微偏差，让步接收'
        ELSE '检验通过'
    END
FROM `production_order` o
JOIN `product_model` pm ON pm.`id` = o.`product_model_id`
JOIN `production_task` t ON t.`order_id` = o.`id`
JOIN `process_route` pr ON pr.`id` = t.`process_route_id`
WHERE o.`order_no` IN (
    'MO-20260706-002', 'MO-20260707-002', 'MO-20260708-002', 'MO-20260709-002',
    'MO-20260710-002', 'MO-20260711-002', 'MO-20260712-002', 'MO-20260713-002'
)
  AND (
      pr.`process_code` IN ('FINAL-QC', 'LATENCY-QC', 'SOUND-QC', 'ERGONOMIC-QC', 'FUNCTION', 'AGING')
      OR (o.`order_no` = 'MO-20260708-002' AND pr.`process_code` = 'PCB-TEST')
  )
ORDER BY o.`planned_start_time`, pr.`sequence_no`;

SET @qc_0707 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'QC-20260707-001');
SET @rq_0707 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'RQ-20260707-001');
SET @qc_0708 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'QC-20260708-001');
SET @rq_0708 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'RQ-20260708-001');
SET @qc_0710 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'QC-20260710-001');
SET @rq_0710 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'RQ-20260710-001');
SET @qc_0712 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'QC-20260712-001');
SET @rq_0712 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'RQ-20260712-001');
SET @qc_0714 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'QC-20260714-001');
SET @qc_0715 := (SELECT `id` FROM `inspection_record` WHERE `inspection_no` = 'QC-20260715-001');

-- 10. 返修工单
INSERT INTO `rework_order`
(`rework_no`, `source_inspection_id`, `order_id`, `task_id`, `product_sn`, `defect_reason`,
 `rework_requirement`, `assignee_id`, `deadline`, `status`, `repair_action`, `replaced_material`,
 `repair_hours`, `repair_result`, `repair_time`, `recheck_inspection_id`)
VALUES
('RW-20260707-001', @qc_0707, @order_0707, @task_0707_qc, 'SN-GAMETKL-0707-001', 'RGB灯效异常',
 '检查灯珠焊点并刷新灯效固件', @repair_id, '2026-07-07 18:30:00', 3,
 '补焊异常灯珠并刷新固件', 'MAT-RGB-LED x2', 0.50, '已修复，复检通过', '2026-07-07 17:00:00', @rq_0707),
('RW-20260708-001', @qc_0708, @order_0708, @task_0708_qc, 'SN-SILENT98-0708-001', '空腔音偏大',
 '补装吸音棉并复测按键噪声', @repair_id, '2026-07-08 18:00:00', 3,
 '补装底棉并调整定位柱', 'MAT-FOAM-SILENT x1', 0.40, '噪声恢复正常，复检通过', '2026-07-08 16:05:00', @rq_0708),
('RW-20260710-001', @qc_0710, @order_0710, @task_0710_qc, 'SN-PRO104-0710-001', '空格键异响',
 '调整卫星轴润滑和安装位置，复检空格键手感', @repair_id, '2026-07-10 18:00:00', 3,
 '重新润滑卫星轴并校正钢丝', 'MAT-STAB-SET x1', 0.50, '异响消除，复检通过', '2026-07-10 16:45:00', @rq_0710),
('RW-20260712-001', @qc_0712, @order_0712, @task_0712_qc, 'SN-MINI68-0712-001', '蓝牙连接重连',
 '检查天线焊点并重新刷写固件，复检无线连接稳定性', @repair_id, '2026-07-12 18:00:00', 3,
 '重焊天线焊点并刷新固件', '天线焊点返工', 0.60, '已修复，复检通过', '2026-07-12 15:45:00', @rq_0712),
('RW-20260714-001', @qc_0714, @order_0714, @task_0714_qc, 'SN-ALPHA87-0714-001', '蓝牙连接不稳定',
 '排查无线模块和电池连接，完成后提交复检', @repair_id, '2026-07-15 12:00:00', 1,
 '正在检测无线模块焊点', NULL, 0.20, '处理中', NULL, NULL),
('RW-20260715-001', @qc_0715, @order_0715, @task_0715_asm, 'SN-ALPHA87-0715-002', '轴体触发不稳定',
 '更换异常轴体并做单键触发复测', @repair_id, '2026-07-15 18:00:00', 0,
 NULL, NULL, NULL, NULL, NULL, NULL);

-- 第二批返修：早期记录已完成，较新的记录保留处理中状态，便于展示待办分布。
INSERT INTO `rework_order`
(`rework_no`, `source_inspection_id`, `order_id`, `task_id`, `product_sn`, `defect_reason`,
 `rework_requirement`, `assignee_id`, `deadline`, `status`, `repair_action`, `replaced_material`,
 `repair_hours`, `repair_result`, `repair_time`, `recheck_inspection_id`)
SELECT
    REPLACE(ir.`inspection_no`, 'QC-', 'RW-'),
    ir.`id`,
    ir.`order_id`,
    ir.`task_id`,
    ir.`product_sn`,
    ir.`defect_reason`,
    CASE
        WHEN ir.`defect_reason` LIKE '%灯效%' THEN '检查灯珠焊点并刷新灯效配置'
        WHEN ir.`defect_reason` LIKE '%空腔音%' THEN '补装吸音棉并复测噪声'
        WHEN ir.`defect_reason` LIKE '%矩阵%' THEN '检查PCB焊点并重新测试矩阵'
        WHEN ir.`defect_reason` LIKE '%无线%' THEN '检查天线与无线模块连接'
        ELSE '调整装配间隙并复检外观'
    END,
    @repair_id,
    DATE_ADD(ir.`inspection_time`, INTERVAL 4 HOUR),
    CASE WHEN DATE(ir.`inspection_time`) <= '2026-07-08' THEN 3 ELSE 1 END,
    CASE WHEN DATE(ir.`inspection_time`) <= '2026-07-08' THEN '已完成维修并复测' ELSE '正在定位缺陷原因' END,
    CASE
        WHEN ir.`defect_reason` LIKE '%灯效%' THEN 'MAT-RGB-LED x1'
        WHEN ir.`defect_reason` LIKE '%空腔音%' THEN 'MAT-FOAM-SILENT x1'
        ELSE NULL
    END,
    CASE WHEN DATE(ir.`inspection_time`) <= '2026-07-08' THEN 0.50 ELSE 0.20 END,
    CASE WHEN DATE(ir.`inspection_time`) <= '2026-07-08' THEN '维修完成，功能恢复正常' ELSE '处理中' END,
    CASE WHEN DATE(ir.`inspection_time`) <= '2026-07-08' THEN DATE_ADD(ir.`inspection_time`, INTERVAL 90 MINUTE) ELSE NULL END,
    NULL
FROM `inspection_record` ir
WHERE ir.`inspection_no` LIKE 'QC-%-B%'
  AND ir.`result` = 0
ORDER BY ir.`inspection_time`;

-- 11. 导入后统计
SELECT 'sys_user' AS table_name, COUNT(*) AS row_count FROM `sys_user`
UNION ALL SELECT 'product_model', COUNT(*) FROM `product_model`
UNION ALL SELECT 'material', COUNT(*) FROM `material`
UNION ALL SELECT 'product_bom', COUNT(*) FROM `product_bom`
UNION ALL SELECT 'process_route', COUNT(*) FROM `process_route`
UNION ALL SELECT 'production_order', COUNT(*) FROM `production_order`
UNION ALL SELECT 'production_task', COUNT(*) FROM `production_task`
UNION ALL SELECT 'work_report', COUNT(*) FROM `work_report`
UNION ALL SELECT 'inspection_record', COUNT(*) FROM `inspection_record`
UNION ALL SELECT 'rework_order', COUNT(*) FROM `rework_order`;
