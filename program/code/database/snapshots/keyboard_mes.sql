/*
 Navicat Premium Dump SQL

 Source Server         : lizl
 Source Server Type    : MySQL
 Source Server Version : 80403 (8.4.3)
 Source Host           : localhost:3306
 Source Schema         : keyboard_mes

 Target Server Type    : MySQL
 Target Server Version : 80403 (8.4.3)
 File Encoding         : 65001

 Date: 17/07/2026 15:13:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for inspection_record
-- ----------------------------
DROP TABLE IF EXISTS `inspection_record`;
CREATE TABLE `inspection_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inspection_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '检验单号',
  `inspection_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '检验类型：process/final/recheck/AOI/ICT/FCT',
  `task_id` bigint NULL DEFAULT NULL COMMENT '生产任务ID',
  `order_id` bigint NOT NULL COMMENT '生产工单ID',
  `product_sn` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品条码/SN',
  `process_route_id` bigint NULL DEFAULT NULL COMMENT '工艺路线步骤ID',
  `standard_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '检验标准快照JSON，避免标准变更影响历史记录',
  `inspector_id` bigint NULL DEFAULT NULL COMMENT '检验员ID；自动检测可为空',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual' COMMENT '数据来源：manual/equipment',
  `equipment_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '检测设备编号，AOI/ICT/FCT时填写',
  `inspection_time` datetime NOT NULL COMMENT '检验时间',
  `result` tinyint NOT NULL COMMENT '结果：0=不合格 1=合格 2=让步接收 3=报废',
  `measured_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '实测数据或设备报告JSON',
  `defect_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '不合格原因',
  `handling_method` tinyint NULL DEFAULT NULL COMMENT '处置方式：1=返修 2=报废 3=让步接收',
  `approval_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态：0=无需/待审 1=通过 2=驳回',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inspection_record_no`(`inspection_no` ASC) USING BTREE,
  INDEX `idx_inspection_record_task`(`task_id` ASC) USING BTREE,
  INDEX `idx_inspection_record_order`(`order_id` ASC) USING BTREE,
  INDEX `idx_inspection_record_route`(`process_route_id` ASC) USING BTREE,
  INDEX `idx_inspection_record_inspector`(`inspector_id` ASC) USING BTREE,
  CONSTRAINT `fk_inspection_record_inspector` FOREIGN KEY (`inspector_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inspection_record_order` FOREIGN KEY (`order_id`) REFERENCES `production_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inspection_record_route` FOREIGN KEY (`process_route_id`) REFERENCES `process_route` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inspection_record_task` FOREIGN KEY (`task_id`) REFERENCES `production_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '检验记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inspection_record
-- ----------------------------
INSERT INTO `inspection_record` VALUES (1, 'QC-20260706-001', 'final', 3, 1, 'SN-SPLIT75-0706-001', 23, '{\"item\":\"分体终检\",\"standard\":\"左右通信稳定，腕托贴合\"}', 6, 'manual', NULL, '2026-07-06 15:05:00', 2, '{\"wristRest\":\"minor_gap\",\"link\":\"pass\"}', '腕托贴合偏差', 3, 1, '让步接收');
INSERT INTO `inspection_record` VALUES (2, 'QC-20260707-001', 'FCT', 13, 2, 'SN-GAMETKL-0707-001', 28, '{\"item\":\"电竞功能测试\",\"standard\":\"8K回报率、RGB、全键无冲\"}', 5, 'equipment', 'FCT-GAME-01', '2026-07-07 15:40:00', 0, '{\"rgb\":\"fail\",\"latency\":\"pass\"}', 'RGB灯效异常', 1, 0, '进入返修');
INSERT INTO `inspection_record` VALUES (3, 'RQ-20260707-001', 'recheck', 13, 2, 'SN-GAMETKL-0707-001', 28, '{\"item\":\"返修复检\",\"standard\":\"RGB灯效正常\"}', 6, 'manual', NULL, '2026-07-07 17:20:00', 1, '{\"rgb\":\"pass\"}', NULL, NULL, 0, '复检通过');
INSERT INTO `inspection_record` VALUES (4, 'QC-20260708-001', 'FCT', 20, 3, 'SN-SILENT98-0708-001', 31, '{\"item\":\"噪声测试\",\"standard\":\"按键噪声低于阈值\"}', 6, 'equipment', 'SOUND-01', '2026-07-08 14:20:00', 0, '{\"noise\":\"high\",\"key\":\"enter\"}', '空腔音偏大', 1, 0, '进入返修');
INSERT INTO `inspection_record` VALUES (5, 'RQ-20260708-001', 'recheck', 20, 3, 'SN-SILENT98-0708-001', 31, '{\"item\":\"返修复检\",\"standard\":\"噪声恢复正常\"}', 6, 'manual', NULL, '2026-07-08 16:30:00', 1, '{\"noise\":\"pass\"}', NULL, NULL, 0, '复检通过');
INSERT INTO `inspection_record` VALUES (6, 'QC-20260710-001', 'final', 37, 5, 'SN-PRO104-0710-001', 8, '{\"item\":\"终检\",\"standard\":\"全键触发、外观、附件\"}', 6, 'manual', NULL, '2026-07-10 15:30:00', 0, '{\"space\":\"noise\",\"appearance\":\"pass\"}', '空格键异响', 1, 0, '需要返修');
INSERT INTO `inspection_record` VALUES (7, 'RQ-20260710-001', 'recheck', 37, 5, 'SN-PRO104-0710-001', 8, '{\"item\":\"返修复检\",\"standard\":\"异响消除\"}', 6, 'manual', NULL, '2026-07-10 17:00:00', 1, '{\"space\":\"pass\"}', NULL, NULL, 0, '复检通过');
INSERT INTO `inspection_record` VALUES (8, 'QC-20260712-001', 'FCT', 54, 7, 'SN-MINI68-0712-001', 13, '{\"item\":\"无线连接测试\",\"standard\":\"10分钟不断连\"}', 5, 'equipment', 'FCT-AGING-02', '2026-07-12 14:40:00', 0, '{\"bluetooth\":\"reconnect\",\"duration\":\"10min\"}', '蓝牙连接重连', 1, 0, '进入返修');
INSERT INTO `inspection_record` VALUES (9, 'RQ-20260712-001', 'recheck', 54, 7, 'SN-MINI68-0712-001', 13, '{\"item\":\"返修复检\",\"standard\":\"无线连接稳定\"}', 6, 'manual', NULL, '2026-07-12 16:00:00', 1, '{\"bluetooth\":\"pass\"}', NULL, NULL, 0, '返修复检通过');
INSERT INTO `inspection_record` VALUES (10, 'QC-20260714-001', 'FCT', 68, 9, 'SN-ALPHA87-0714-001', 3, '{\"item\":\"老化测试\",\"standard\":\"无线连接稳定，RGB正常\"}', 5, 'equipment', 'FCT-AGING-01', '2026-07-14 15:00:00', 0, '{\"bluetooth\":\"unstable\",\"rgb\":\"pass\"}', '蓝牙连接不稳定', 1, 0, '待返修处理');
INSERT INTO `inspection_record` VALUES (11, 'QC-20260715-001', 'process', 77, 10, 'SN-ALPHA87-0715-002', 2, '{\"item\":\"装配过程巡检\",\"standard\":\"键帽无划伤，轴体触发稳定\"}', 5, 'manual', NULL, '2026-07-15 14:30:00', 0, '{\"switch\":\"unstable\",\"keycap\":\"pass\"}', '轴体触发不稳定', 1, 0, '新产生返修任务');
INSERT INTO `inspection_record` VALUES (16, 'QC-20260706-B030', 'FCT', 7, 15, 'SN-MINI-68-B-0706', 12, '{\"item\":\"老化测试\",\"standard\":\"按工艺检验规范执行\"}', 5, 'equipment', 'EQ-012', '2026-07-06 14:50:00', 0, '{\"pass\":false,\"level\":\"minor\"}', '无线连接短暂重连', 1, 0, '检验不合格，已进入返修');
INSERT INTO `inspection_record` VALUES (17, 'QC-20260706-B040', 'final', 8, 15, 'SN-MINI-68-B-0706', 13, '{\"item\":\"终检\",\"standard\":\"按工艺检验规范执行\"}', 5, 'manual', NULL, '2026-07-06 15:30:00', 0, '{\"pass\":false,\"level\":\"minor\"}', '外观装配间隙偏大', 1, 0, '检验不合格，已进入返修');
INSERT INTO `inspection_record` VALUES (18, 'QC-20260707-B030', 'final', 17, 16, 'SN-PRO-104-B-0707', 8, '{\"item\":\"终检\",\"standard\":\"按工艺检验规范执行\"}', 5, 'manual', NULL, '2026-07-07 14:50:00', 0, '{\"pass\":false,\"level\":\"minor\"}', '外观装配间隙偏大', 1, 0, '检验不合格，已进入返修');
INSERT INTO `inspection_record` VALUES (19, 'QC-20260708-B010', 'ICT', 22, 17, 'SN-GAME-TKL-B-0708', 25, '{\"item\":\"低延迟PCBA测试\",\"standard\":\"按工艺检验规范执行\"}', 5, 'equipment', 'EQ-025', '2026-07-08 13:00:00', 0, '{\"pass\":false,\"level\":\"minor\"}', '矩阵扫描偶发失败', 1, 0, '检验不合格，已进入返修');
INSERT INTO `inspection_record` VALUES (20, 'QC-20260708-B040', 'FCT', 25, 17, 'SN-GAME-TKL-B-0708', 28, '{\"item\":\"低延迟功能测试\",\"standard\":\"按工艺检验规范执行\"}', 5, 'equipment', 'EQ-028', '2026-07-08 15:00:00', 0, '{\"pass\":false,\"level\":\"minor\"}', '灯效颜色不一致', 1, 0, '检验不合格，已进入返修');
INSERT INTO `inspection_record` VALUES (21, 'QC-20260709-B020', 'FCT', 33, 18, 'SN-SILENT-98-B-0709', 31, '{\"item\":\"噪声测试\",\"standard\":\"按工艺检验规范执行\"}', 5, 'equipment', 'EQ-031', '2026-07-09 14:40:00', 0, '{\"pass\":false,\"level\":\"minor\"}', '空腔音超过标准', 1, 0, '检验不合格，已进入返修');
INSERT INTO `inspection_record` VALUES (22, 'QC-20260710-B030', 'final', 41, 19, 'SN-SPLIT-75-B-0710', 23, '{\"item\":\"人体工学终检\",\"standard\":\"按工艺检验规范执行\"}', 5, 'manual', NULL, '2026-07-10 14:50:00', 2, '{\"pass\":true,\"concession\":true}', NULL, 3, 1, '轻微偏差，让步接收');
INSERT INTO `inspection_record` VALUES (23, 'QC-20260711-B030', 'FCT', 48, 20, 'SN-ALPHA-87-B-0711', 3, '{\"item\":\"老化测试\",\"standard\":\"按工艺检验规范执行\"}', 5, 'equipment', 'EQ-003', '2026-07-11 14:20:00', 1, '{\"pass\":true}', NULL, NULL, 0, '检验通过');
INSERT INTO `inspection_record` VALUES (24, 'QC-20260711-B040', 'final', 49, 20, 'SN-ALPHA-87-B-0711', 4, '{\"item\":\"终检\",\"standard\":\"按工艺检验规范执行\"}', 5, 'manual', NULL, '2026-07-11 15:00:00', 1, '{\"pass\":true}', NULL, NULL, 0, '检验通过');
INSERT INTO `inspection_record` VALUES (25, 'QC-20260712-B020', 'FCT', 57, 21, 'SN-MACRO-12-B-0712', 19, '{\"item\":\"功能测试\",\"standard\":\"按工艺检验规范执行\"}', 5, 'equipment', 'EQ-019', '2026-07-12 14:10:00', 1, '{\"pass\":true}', NULL, NULL, 0, '检验通过');
INSERT INTO `inspection_record` VALUES (26, 'QC-20260713-B020', 'final', 64, 22, 'SN-LOW-84-B-0713', 16, '{\"item\":\"终检\",\"standard\":\"按工艺检验规范执行\"}', 5, 'manual', NULL, '2026-07-13 14:10:00', 1, '{\"pass\":true}', NULL, NULL, 0, '检验通过');

-- ----------------------------
-- Table structure for material
-- ----------------------------
DROP TABLE IF EXISTS `material`;
CREATE TABLE `material`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物料编码',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物料名称',
  `specification` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格型号',
  `material_type` tinyint NOT NULL COMMENT '物料类型：1=零部件 2=辅料 3=半成品 4=成品',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '计量单位',
  `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商/来源',
  `barcode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物料条码或批次条码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_material_material_code`(`material_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物料表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of material
-- ----------------------------
INSERT INTO `material` VALUES (1, 'MAT-PCB-87-RGB', '87键RGB PCB主板', '三模热插拔，ANSI配列', 1, 'pcs', '华南电子', 'MAT-PCB-87-RGB', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (2, 'MAT-PCB-104-WIRED', '104键有线PCB主板', '有线办公版，白光', 1, 'pcs', '华南电子', 'MAT-PCB-104-WIRED', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (3, 'MAT-PCB-68-BT', '68键蓝牙PCB主板', '蓝牙/2.4G/有线三模', 1, 'pcs', '华南电子', 'MAT-PCB-68-BT', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (4, 'MAT-PCB-84-LOW', '84键矮轴PCB主板', '矮轴专用，Type-C', 1, 'pcs', '华南电子', 'MAT-PCB-84-LOW', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (5, 'MAT-PCB-MACRO-12', '12键宏键盘PCB主板', '带旋钮编码器接口', 1, 'pcs', '华南电子', 'MAT-PCB-MACRO-12', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (6, 'MAT-PCB-SPLIT-75', '75键分体PCB套装', '左右分体双主板，Type-C桥接', 1, 'set', '华南电子', 'MAT-PCB-SPLIT-75', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (7, 'MAT-PCB-GAME-TKL', '电竞TKL低延迟PCB', '8K回报率，热插拔', 1, 'pcs', '华南电子', 'MAT-PCB-GAME-TKL', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (8, 'MAT-PCB-SILENT-98', '98键静音办公PCB', '有线/2.4G双模', 1, 'pcs', '华南电子', 'MAT-PCB-SILENT-98', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (9, 'MAT-SWITCH-LINEAR', '线性轴体', '45g，热插拔', 1, 'pcs', '凯华', 'MAT-SWITCH-LINEAR', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (10, 'MAT-SWITCH-TACTILE', '段落轴体', '55g，热插拔', 1, 'pcs', '凯华', 'MAT-SWITCH-TACTILE', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (11, 'MAT-SWITCH-LOW', '矮轴轴体', '低行程，轻触', 1, 'pcs', '佳达隆', 'MAT-SWITCH-LOW', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (12, 'MAT-SWITCH-SILENT', '静音线性轴体', '静音结构，办公低噪', 1, 'pcs', '佳达隆', 'MAT-SWITCH-SILENT', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (13, 'MAT-KEYCAP-87-PBT', '87键PBT键帽套装', '双色注塑，灰白配色', 1, 'set', '键帽工坊', 'MAT-KEYCAP-87-PBT', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (14, 'MAT-KEYCAP-104-PBT', '104键PBT键帽套装', '办公灰白配色', 1, 'set', '键帽工坊', 'MAT-KEYCAP-104-PBT', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (15, 'MAT-KEYCAP-68-PBT', '68键PBT键帽套装', '便携配列', 1, 'set', '键帽工坊', 'MAT-KEYCAP-68-PBT', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (16, 'MAT-KEYCAP-84-LOW', '84键矮轴键帽套装', '低高度键帽', 1, 'set', '键帽工坊', 'MAT-KEYCAP-84-LOW', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (17, 'MAT-KEYCAP-SPLIT-75', '75键分体键帽套装', '人体工学异形增补', 1, 'set', '键帽工坊', 'MAT-KEYCAP-SPLIT-75', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (18, 'MAT-KEYCAP-GAME-TKL', '电竞TKL透光键帽', 'PBT透光，深灰配色', 1, 'set', '键帽工坊', 'MAT-KEYCAP-GAME-TKL', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (19, 'MAT-KEYCAP-SILENT-98', '98键静音办公键帽', '低噪大键位优化', 1, 'set', '键帽工坊', 'MAT-KEYCAP-SILENT-98', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (20, 'MAT-STAB-SET', '卫星轴套装', '2U/6.25U组合', 1, 'set', '五金供应商A', 'MAT-STAB-SET', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (21, 'MAT-FOAM-SILENT', '夹心吸音棉套装', 'PORON夹心棉+底棉', 1, 'set', '声学材料商', 'MAT-FOAM-SILENT', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (22, 'MAT-CASE-87', '87键上盖下壳', 'ABS外壳，白色', 1, 'set', '注塑供应商A', 'MAT-CASE-87', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (23, 'MAT-CASE-104', '104键上盖下壳', 'ABS外壳，黑色', 1, 'set', '注塑供应商A', 'MAT-CASE-104', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (24, 'MAT-CASE-68', '68键上盖下壳', 'ABS外壳，浅灰', 1, 'set', '注塑供应商A', 'MAT-CASE-68', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (25, 'MAT-CASE-84-LOW', '84键矮轴外壳', '铝合金上盖', 1, 'set', '结构供应商B', 'MAT-CASE-84-LOW', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (26, 'MAT-CASE-MACRO', '宏键盘外壳', 'CNC铝壳，黑色', 1, 'set', '结构供应商B', 'MAT-CASE-MACRO', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (27, 'MAT-CASE-SPLIT-75', '75键分体外壳套装', '左右分体注塑壳+腕托', 1, 'set', '结构供应商B', 'MAT-CASE-SPLIT-75', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (28, 'MAT-CASE-GAME-TKL', '电竞TKL磁吸外壳', '上盖磁吸，黑透配色', 1, 'set', '结构供应商B', 'MAT-CASE-GAME-TKL', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (29, 'MAT-CASE-SILENT-98', '98键静音外壳', '带吸音仓结构', 1, 'set', '结构供应商B', 'MAT-CASE-SILENT-98', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (30, 'MAT-BATTERY-2000', '锂电池', '2000mAh，带保护板', 1, 'pcs', '电池供应商C', 'MAT-BATTERY-2000', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (31, 'MAT-RGB-LED', 'RGB灯珠', 'SMD封装', 1, 'pcs', '灯珠供应商D', 'MAT-RGB-LED', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (32, 'MAT-TYPEC-CABLE', 'Type-C数据线', '1.5m，黑色', 2, 'pcs', '线材供应商E', 'MAT-TYPEC-CABLE', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (33, 'MAT-PACK-BOX-87', '87键包装盒', '彩盒+内托', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-87', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (34, 'MAT-PACK-BOX-104', '104键包装盒', '彩盒+内托', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-104', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (35, 'MAT-PACK-BOX-SMALL', '小配列包装盒', '68/84/宏键盘通用', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-SMALL', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (36, 'MAT-PACK-BOX-SPLIT', '分体键盘包装盒', '左右分体加厚内托', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-SPLIT', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (37, 'MAT-PACK-BOX-GAME', '电竞键盘包装盒', '黑金彩盒+防震托', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-GAME', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (38, 'MAT-PACK-BOX-SILENT', '静音办公包装盒', '办公系列彩盒', 2, 'pcs', '包装供应商F', 'MAT-PACK-BOX-SILENT', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (39, 'MAT-LABEL-SN', 'SN条码标签', '40mm x 20mm', 2, 'pcs', '标签供应商G', 'MAT-LABEL-SN', 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `material` VALUES (40, 'MAT-MANUAL', '用户说明书', '中英文折页', 2, 'pcs', '印刷供应商H', 'MAT-MANUAL', 1, '2026-07-16 17:14:17', NULL);

-- ----------------------------
-- Table structure for process_route
-- ----------------------------
DROP TABLE IF EXISTS `process_route`;
CREATE TABLE `process_route`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_model_id` bigint NOT NULL COMMENT '产品型号ID',
  `route_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工艺路线版本',
  `sequence_no` int NOT NULL COMMENT '工序顺序',
  `process_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工序编码',
  `process_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工序名称',
  `process_type` tinyint NOT NULL COMMENT '工序类型：1=组装 2=检测 3=包装 4=返修',
  `station_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认工位编码',
  `station_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认工位名称',
  `standard_hours` decimal(10, 2) NULL DEFAULT NULL COMMENT '标准工时',
  `is_quality_gate` tinyint NOT NULL DEFAULT 0 COMMENT '是否质检节点：0=否 1=是',
  `inspection_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '检验类型：process/final/AOI/ICT/FCT/recheck',
  `inspection_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '检验项目、标准值、公差、方法等JSON配置',
  `sop_file` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作业指导书路径',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_process_route_model`(`product_model_id` ASC) USING BTREE,
  CONSTRAINT `fk_process_route_model` FOREIGN KEY (`product_model_id`) REFERENCES `product_model` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工艺路线表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of process_route
-- ----------------------------
INSERT INTO `process_route` VALUES (1, 1, 'R1.0', 10, 'PCB-TEST', 'PCBA测试', 2, 'ST-PCBA-01', 'PCBA测试工位', 0.50, 1, 'ICT', '{\"items\":[\"短路\",\"按键矩阵\",\"蓝牙模块\"],\"method\":\"治具测试\"}', '/sop/kb87/pcba-test.pdf', 1);
INSERT INTO `process_route` VALUES (2, 1, 'R1.0', 20, 'ASSEMBLY', '总装', 1, 'ST-ASM-01', '总装一工位', 1.60, 0, NULL, NULL, '/sop/kb87/assembly.pdf', 1);
INSERT INTO `process_route` VALUES (3, 1, 'R1.0', 30, 'AGING', '老化测试', 2, 'ST-AGING-01', '老化测试工位', 2.00, 1, 'FCT', '{\"items\":[\"连续输入\",\"灯效\",\"无线连接\"],\"method\":\"自动测试\"}', '/sop/kb87/aging.pdf', 1);
INSERT INTO `process_route` VALUES (4, 1, 'R1.0', 40, 'FINAL-QC', '终检', 2, 'ST-QC-01', '终检工位', 0.70, 1, 'final', '{\"items\":[\"外观\",\"全键触发\",\"包装附件\"],\"method\":\"人工抽检\"}', '/sop/kb87/final-qc.pdf', 1);
INSERT INTO `process_route` VALUES (5, 1, 'R1.0', 50, 'PACK', '包装入库', 3, 'ST-PACK-01', '包装工位', 0.40, 0, NULL, NULL, '/sop/kb87/pack.pdf', 1);
INSERT INTO `process_route` VALUES (6, 2, 'R1.0', 10, 'PCB-TEST', 'PCBA测试', 2, 'ST-PCBA-02', 'PCBA测试工位', 0.45, 1, 'ICT', '{\"items\":[\"短路\",\"矩阵扫描\"],\"method\":\"治具测试\"}', '/sop/kb104/pcba-test.pdf', 1);
INSERT INTO `process_route` VALUES (7, 2, 'R1.0', 20, 'ASSEMBLY', '总装', 1, 'ST-ASM-02', '总装二工位', 1.80, 0, NULL, NULL, '/sop/kb104/assembly.pdf', 1);
INSERT INTO `process_route` VALUES (8, 2, 'R1.0', 30, 'FINAL-QC', '终检', 2, 'ST-QC-02', '终检工位', 0.80, 1, 'final', '{\"items\":[\"外观\",\"有线连接\",\"全键触发\"],\"method\":\"人工抽检\"}', '/sop/kb104/final-qc.pdf', 1);
INSERT INTO `process_route` VALUES (9, 2, 'R1.0', 40, 'PACK', '包装入库', 3, 'ST-PACK-02', '包装工位', 0.45, 0, NULL, NULL, '/sop/kb104/pack.pdf', 1);
INSERT INTO `process_route` VALUES (10, 3, 'R1.0', 10, 'PCB-TEST', 'PCBA测试', 2, 'ST-PCBA-03', 'PCBA测试工位', 0.40, 1, 'ICT', '{\"items\":[\"蓝牙模块\",\"矩阵扫描\"],\"method\":\"治具测试\"}', '/sop/kb68/pcba-test.pdf', 1);
INSERT INTO `process_route` VALUES (11, 3, 'R1.0', 20, 'ASSEMBLY', '总装', 1, 'ST-ASM-03', '总装三工位', 1.20, 0, NULL, NULL, '/sop/kb68/assembly.pdf', 1);
INSERT INTO `process_route` VALUES (12, 3, 'R1.0', 30, 'AGING', '老化测试', 2, 'ST-AGING-02', '老化测试工位', 1.60, 1, 'FCT', '{\"items\":[\"无线连接\",\"电池充放电\"],\"method\":\"自动测试\"}', '/sop/kb68/aging.pdf', 1);
INSERT INTO `process_route` VALUES (13, 3, 'R1.0', 40, 'FINAL-QC', '终检', 2, 'ST-QC-03', '终检工位', 0.55, 1, 'final', '{\"items\":[\"外观\",\"配件\",\"SN标签\"],\"method\":\"人工抽检\"}', '/sop/kb68/final-qc.pdf', 1);
INSERT INTO `process_route` VALUES (14, 3, 'R1.0', 50, 'PACK', '包装入库', 3, 'ST-PACK-03', '包装工位', 0.30, 0, NULL, NULL, '/sop/kb68/pack.pdf', 1);
INSERT INTO `process_route` VALUES (15, 4, 'R1.0', 10, 'ASSEMBLY', '总装', 1, 'ST-ASM-04', '矮轴总装工位', 1.40, 0, NULL, NULL, '/sop/kb84/assembly.pdf', 1);
INSERT INTO `process_route` VALUES (16, 4, 'R1.0', 20, 'FINAL-QC', '终检', 2, 'ST-QC-04', '终检工位', 0.60, 1, 'final', '{\"items\":[\"矮轴手感\",\"外观\",\"Type-C连接\"],\"method\":\"人工抽检\"}', '/sop/kb84/final-qc.pdf', 1);
INSERT INTO `process_route` VALUES (17, 4, 'R1.0', 30, 'PACK', '包装入库', 3, 'ST-PACK-04', '包装工位', 0.30, 0, NULL, NULL, '/sop/kb84/pack.pdf', 1);
INSERT INTO `process_route` VALUES (18, 5, 'R1.0', 10, 'ASSEMBLY', '总装', 1, 'ST-ASM-05', '小键盘总装工位', 0.80, 0, NULL, NULL, '/sop/macro/assembly.pdf', 1);
INSERT INTO `process_route` VALUES (19, 5, 'R1.0', 20, 'FUNCTION', '功能测试', 2, 'ST-QC-05', '功能测试工位', 0.40, 1, 'FCT', '{\"items\":[\"宏定义\",\"旋钮\",\"RGB\"],\"method\":\"软件测试\"}', '/sop/macro/function.pdf', 1);
INSERT INTO `process_route` VALUES (20, 5, 'R1.0', 30, 'PACK', '包装入库', 3, 'ST-PACK-05', '包装工位', 0.20, 0, NULL, NULL, '/sop/macro/pack.pdf', 1);
INSERT INTO `process_route` VALUES (21, 6, 'R1.0', 10, 'PCB-TEST', '左右主板测试', 2, 'ST-PCBA-04', '分体主板测试工位', 0.70, 1, 'ICT', '{\"items\":[\"左右主板\",\"桥接通信\"],\"method\":\"治具测试\"}', '/sop/split75/pcba-test.pdf', 1);
INSERT INTO `process_route` VALUES (22, 6, 'R1.0', 20, 'ASSEMBLY', '分体总装', 1, 'ST-ASM-06', '分体总装工位', 1.90, 0, NULL, NULL, '/sop/split75/assembly.pdf', 1);
INSERT INTO `process_route` VALUES (23, 6, 'R1.0', 30, 'ERGONOMIC-QC', '人体工学终检', 2, 'ST-QC-06', '分体终检工位', 0.80, 1, 'final', '{\"items\":[\"左右通信\",\"腕托贴合\",\"外观\"],\"method\":\"人工抽检\"}', '/sop/split75/final-qc.pdf', 1);
INSERT INTO `process_route` VALUES (24, 6, 'R1.0', 40, 'PACK', '包装入库', 3, 'ST-PACK-06', '分体包装工位', 0.50, 0, NULL, NULL, '/sop/split75/pack.pdf', 1);
INSERT INTO `process_route` VALUES (25, 7, 'R1.0', 10, 'PCB-TEST', '低延迟PCBA测试', 2, 'ST-PCBA-05', '电竞主板测试工位', 0.55, 1, 'ICT', '{\"items\":[\"8K回报率\",\"矩阵扫描\"],\"method\":\"治具测试\"}', '/sop/game-tkl/pcba-test.pdf', 1);
INSERT INTO `process_route` VALUES (26, 7, 'R1.0', 20, 'STAB-TUNE', '大键调校', 1, 'ST-TUNE-01', '手感调校工位', 0.60, 0, NULL, NULL, '/sop/game-tkl/stab-tune.pdf', 1);
INSERT INTO `process_route` VALUES (27, 7, 'R1.0', 30, 'ASSEMBLY', '电竞总装', 1, 'ST-ASM-07', '电竞总装工位', 1.50, 0, NULL, NULL, '/sop/game-tkl/assembly.pdf', 1);
INSERT INTO `process_route` VALUES (28, 7, 'R1.0', 40, 'LATENCY-QC', '低延迟功能测试', 2, 'ST-QC-07', '电竞功能测试工位', 0.70, 1, 'FCT', '{\"items\":[\"延迟\",\"RGB\",\"全键无冲\"],\"method\":\"自动测试\"}', '/sop/game-tkl/latency-qc.pdf', 1);
INSERT INTO `process_route` VALUES (29, 7, 'R1.0', 50, 'PACK', '包装入库', 3, 'ST-PACK-07', '电竞包装工位', 0.35, 0, NULL, NULL, '/sop/game-tkl/pack.pdf', 1);
INSERT INTO `process_route` VALUES (30, 8, 'R1.0', 10, 'ASSEMBLY', '静音总装', 1, 'ST-ASM-08', '静音总装工位', 1.70, 0, NULL, NULL, '/sop/silent98/assembly.pdf', 1);
INSERT INTO `process_route` VALUES (31, 8, 'R1.0', 20, 'SOUND-QC', '噪声测试', 2, 'ST-QC-08', '声学测试工位', 0.60, 1, 'FCT', '{\"items\":[\"按键噪声\",\"空腔音\",\"全键触发\"],\"method\":\"声学测试\"}', '/sop/silent98/sound-qc.pdf', 1);
INSERT INTO `process_route` VALUES (32, 8, 'R1.0', 30, 'PACK', '包装入库', 3, 'ST-PACK-08', '静音包装工位', 0.35, 0, NULL, NULL, '/sop/silent98/pack.pdf', 1);

-- ----------------------------
-- Table structure for product_bom
-- ----------------------------
DROP TABLE IF EXISTS `product_bom`;
CREATE TABLE `product_bom`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_model_id` bigint NOT NULL COMMENT '产品型号ID',
  `bom_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BOM版本',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `quantity` decimal(10, 3) NOT NULL COMMENT '标准用量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '计量单位',
  `sequence_no` int NULL DEFAULT NULL COMMENT 'BOM行序号',
  `effective_date` date NULL DEFAULT NULL COMMENT '生效日期',
  `expire_date` date NULL DEFAULT NULL COMMENT '失效日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_bom_model`(`product_model_id` ASC) USING BTREE,
  INDEX `idx_product_bom_material`(`material_id` ASC) USING BTREE,
  CONSTRAINT `fk_product_bom_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_product_bom_model` FOREIGN KEY (`product_model_id`) REFERENCES `product_model` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '产品BOM表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_bom
-- ----------------------------
INSERT INTO `product_bom` VALUES (1, 1, 'V1.0', 1, 1.000, 'pcs', 10, '2026-07-01', NULL, 1, '主板');
INSERT INTO `product_bom` VALUES (2, 1, 'V1.0', 9, 87.000, 'pcs', 20, '2026-07-01', NULL, 1, '轴体');
INSERT INTO `product_bom` VALUES (3, 1, 'V1.0', 13, 1.000, 'set', 30, '2026-07-01', NULL, 1, '键帽');
INSERT INTO `product_bom` VALUES (4, 1, 'V1.0', 20, 1.000, 'set', 40, '2026-07-01', NULL, 1, '卫星轴');
INSERT INTO `product_bom` VALUES (5, 1, 'V1.0', 22, 1.000, 'set', 50, '2026-07-01', NULL, 1, '外壳');
INSERT INTO `product_bom` VALUES (6, 1, 'V1.0', 30, 1.000, 'pcs', 60, '2026-07-01', NULL, 1, '电池');
INSERT INTO `product_bom` VALUES (7, 1, 'V1.0', 31, 87.000, 'pcs', 70, '2026-07-01', NULL, 1, '灯珠');
INSERT INTO `product_bom` VALUES (8, 1, 'V1.0', 33, 1.000, 'pcs', 80, '2026-07-01', NULL, 1, '包装盒');
INSERT INTO `product_bom` VALUES (9, 2, 'V1.0', 2, 1.000, 'pcs', 10, '2026-07-01', NULL, 1, '主板');
INSERT INTO `product_bom` VALUES (10, 2, 'V1.0', 10, 104.000, 'pcs', 20, '2026-07-01', NULL, 1, '段落轴');
INSERT INTO `product_bom` VALUES (11, 2, 'V1.0', 14, 1.000, 'set', 30, '2026-07-01', NULL, 1, '键帽');
INSERT INTO `product_bom` VALUES (12, 2, 'V1.0', 23, 1.000, 'set', 40, '2026-07-01', NULL, 1, '外壳');
INSERT INTO `product_bom` VALUES (13, 2, 'V1.0', 34, 1.000, 'pcs', 50, '2026-07-01', NULL, 1, '包装盒');
INSERT INTO `product_bom` VALUES (14, 3, 'V1.0', 3, 1.000, 'pcs', 10, '2026-07-01', NULL, 1, '主板');
INSERT INTO `product_bom` VALUES (15, 3, 'V1.0', 9, 68.000, 'pcs', 20, '2026-07-01', NULL, 1, '轴体');
INSERT INTO `product_bom` VALUES (16, 3, 'V1.0', 15, 1.000, 'set', 30, '2026-07-01', NULL, 1, '键帽');
INSERT INTO `product_bom` VALUES (17, 3, 'V1.0', 24, 1.000, 'set', 40, '2026-07-01', NULL, 1, '外壳');
INSERT INTO `product_bom` VALUES (18, 3, 'V1.0', 30, 1.000, 'pcs', 50, '2026-07-01', NULL, 1, '电池');
INSERT INTO `product_bom` VALUES (19, 4, 'V1.0', 4, 1.000, 'pcs', 10, '2026-07-01', NULL, 1, '主板');
INSERT INTO `product_bom` VALUES (20, 4, 'V1.0', 11, 84.000, 'pcs', 20, '2026-07-01', NULL, 1, '矮轴');
INSERT INTO `product_bom` VALUES (21, 4, 'V1.0', 16, 1.000, 'set', 30, '2026-07-01', NULL, 1, '键帽');
INSERT INTO `product_bom` VALUES (22, 4, 'V1.0', 25, 1.000, 'set', 40, '2026-07-01', NULL, 1, '外壳');
INSERT INTO `product_bom` VALUES (23, 5, 'V1.0', 5, 1.000, 'pcs', 10, '2026-07-01', NULL, 1, '宏键盘主板');
INSERT INTO `product_bom` VALUES (24, 5, 'V1.0', 10, 12.000, 'pcs', 20, '2026-07-01', NULL, 1, '轴体');
INSERT INTO `product_bom` VALUES (25, 5, 'V1.0', 26, 1.000, 'set', 30, '2026-07-01', NULL, 1, '外壳');
INSERT INTO `product_bom` VALUES (26, 5, 'V1.0', 35, 1.000, 'pcs', 40, '2026-07-01', NULL, 1, '包装盒');
INSERT INTO `product_bom` VALUES (27, 6, 'V1.0', 6, 1.000, 'set', 10, '2026-07-01', NULL, 1, '分体主板套装');
INSERT INTO `product_bom` VALUES (28, 6, 'V1.0', 10, 75.000, 'pcs', 20, '2026-07-01', NULL, 1, '段落轴');
INSERT INTO `product_bom` VALUES (29, 6, 'V1.0', 17, 1.000, 'set', 30, '2026-07-01', NULL, 1, '人体工学键帽');
INSERT INTO `product_bom` VALUES (30, 6, 'V1.0', 27, 1.000, 'set', 40, '2026-07-01', NULL, 1, '分体外壳');
INSERT INTO `product_bom` VALUES (31, 6, 'V1.0', 36, 1.000, 'pcs', 50, '2026-07-01', NULL, 1, '包装盒');
INSERT INTO `product_bom` VALUES (32, 7, 'V1.0', 7, 1.000, 'pcs', 10, '2026-07-01', NULL, 1, '电竞PCB');
INSERT INTO `product_bom` VALUES (33, 7, 'V1.0', 9, 87.000, 'pcs', 20, '2026-07-01', NULL, 1, '线性轴');
INSERT INTO `product_bom` VALUES (34, 7, 'V1.0', 18, 1.000, 'set', 30, '2026-07-01', NULL, 1, '透光键帽');
INSERT INTO `product_bom` VALUES (35, 7, 'V1.0', 28, 1.000, 'set', 40, '2026-07-01', NULL, 1, '磁吸外壳');
INSERT INTO `product_bom` VALUES (36, 7, 'V1.0', 31, 87.000, 'pcs', 50, '2026-07-01', NULL, 1, 'RGB灯珠');
INSERT INTO `product_bom` VALUES (37, 7, 'V1.0', 37, 1.000, 'pcs', 60, '2026-07-01', NULL, 1, '电竞包装盒');
INSERT INTO `product_bom` VALUES (38, 8, 'V1.0', 8, 1.000, 'pcs', 10, '2026-07-01', NULL, 1, '静音PCB');
INSERT INTO `product_bom` VALUES (39, 8, 'V1.0', 12, 98.000, 'pcs', 20, '2026-07-01', NULL, 1, '静音轴');
INSERT INTO `product_bom` VALUES (40, 8, 'V1.0', 19, 1.000, 'set', 30, '2026-07-01', NULL, 1, '静音键帽');
INSERT INTO `product_bom` VALUES (41, 8, 'V1.0', 21, 1.000, 'set', 40, '2026-07-01', NULL, 1, '吸音棉');
INSERT INTO `product_bom` VALUES (42, 8, 'V1.0', 29, 1.000, 'set', 50, '2026-07-01', NULL, 1, '静音外壳');
INSERT INTO `product_bom` VALUES (43, 8, 'V1.0', 38, 1.000, 'pcs', 60, '2026-07-01', NULL, 1, '办公包装盒');
INSERT INTO `product_bom` VALUES (44, 8, 'V1.0', 39, 1.000, 'pcs', 70, '2026-07-01', NULL, 1, 'SN标签');

-- ----------------------------
-- Table structure for product_model
-- ----------------------------
DROP TABLE IF EXISTS `product_model`;
CREATE TABLE `product_model`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `model_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '型号编码',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '型号名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品类别',
  `configuration_desc` varchar(800) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '外壳、PCB、轴体、键帽、灯效、连接方式等配置摘要',
  `firmware_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '固件/功能版本',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_model_model_code`(`model_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '产品型号表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_model
-- ----------------------------
INSERT INTO `product_model` VALUES (1, 'KB-ALPHA-87', 'Alpha 87 热插拔机械键盘', '机械键盘', '87键、热插拔、RGB、三模连接、PBT键帽', 'v1.2.0', 1, '2026-07-16 17:14:16', NULL, '主推演示型号');
INSERT INTO `product_model` VALUES (2, 'KB-PRO-104', 'Pro 104 办公机械键盘', '机械键盘', '104键、有线连接、白色背光、办公场景', 'v1.0.5', 1, '2026-07-16 17:14:16', NULL, '大批量办公型号');
INSERT INTO `product_model` VALUES (3, 'KB-MINI-68', 'Mini 68 便携机械键盘', '机械键盘', '68键、小配列、蓝牙/2.4G/有线三模', 'v2.0.1', 1, '2026-07-16 17:14:16', NULL, '小配列型号');
INSERT INTO `product_model` VALUES (4, 'KB-LOW-84', 'Low 84 矮轴键盘', '机械键盘', '84键、矮轴、轻薄外壳、Type-C', 'v1.1.3', 1, '2026-07-16 17:14:16', NULL, '轻薄型号');
INSERT INTO `product_model` VALUES (5, 'KB-MACRO-12', 'Macro 12 自定义小键盘', '功能键盘', '12键宏定义、旋钮、RGB灯效', 'v0.9.8', 1, '2026-07-16 17:14:16', NULL, '小批量试产型号');
INSERT INTO `product_model` VALUES (6, 'KB-SPLIT-75', 'Split 75 人体工学分体键盘', '人体工学键盘', '75键、左右分体、热插拔、腕托套件', 'v1.3.2', 1, '2026-07-16 17:14:16', NULL, '人体工学新品');
INSERT INTO `product_model` VALUES (7, 'KB-GAME-TKL', 'Game TKL 电竞低延迟键盘', '电竞键盘', '87键TKL、8K回报率、RGB、磁吸上盖', 'v2.4.0', 1, '2026-07-16 17:14:16', NULL, '电竞系列');
INSERT INTO `product_model` VALUES (8, 'KB-SILENT-98', 'Silent 98 静音办公键盘', '静音键盘', '98键、静音轴、吸音棉、办公低噪', 'v1.0.8', 1, '2026-07-16 17:14:16', NULL, '静音办公系列');

-- ----------------------------
-- Table structure for production_order
-- ----------------------------
DROP TABLE IF EXISTS `production_order`;
CREATE TABLE `production_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '生产工单编号',
  `product_model_id` bigint NOT NULL COMMENT '产品型号ID',
  `bom_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绑定BOM版本',
  `route_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绑定工艺路线版本',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产批次号',
  `quantity` int NOT NULL COMMENT '工单数量',
  `planned_start_time` datetime NULL DEFAULT NULL COMMENT '计划开始时间',
  `planned_end_time` datetime NULL DEFAULT NULL COMMENT '计划结束时间',
  `delivery_date` date NULL DEFAULT NULL COMMENT '交付日期',
  `line_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '计划产线/班组',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源：ERP/manual',
  `priority` tinyint NOT NULL DEFAULT 0 COMMENT '优先级：0=普通 1=加急',
  `planner_id` bigint NULL DEFAULT NULL COMMENT '计划/派工负责人ID',
  `material_ready_status` tinyint NOT NULL DEFAULT 0 COMMENT '物料齐套状态：0=未齐套 1=齐套',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=待配置 1=已接受 2=生产中 3=待终检 4=完成 5=关闭',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_production_order_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_production_order_model`(`product_model_id` ASC) USING BTREE,
  INDEX `idx_production_order_planner`(`planner_id` ASC) USING BTREE,
  CONSTRAINT `fk_production_order_model` FOREIGN KEY (`product_model_id`) REFERENCES `product_model` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_production_order_planner` FOREIGN KEY (`planner_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '生产工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of production_order
-- ----------------------------
INSERT INTO `production_order` VALUES (1, 'MO-20260706-001', 6, 'V1.0', 'R1.0', 'BATCH-SPLIT75-0706', 45, '2026-07-06 08:30:00', '2026-07-06 17:30:00', '2026-07-13', '六号人体工学线', 'ERP', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (2, 'MO-20260707-001', 7, 'V1.0', 'R1.0', 'BATCH-GAMETKL-0707', 80, '2026-07-07 08:30:00', '2026-07-07 18:00:00', '2026-07-14', '七号电竞线', 'ERP', 1, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (3, 'MO-20260708-001', 8, 'V1.0', 'R1.0', 'BATCH-SILENT98-0708', 60, '2026-07-08 08:30:00', '2026-07-08 17:00:00', '2026-07-15', '八号静音线', 'ERP', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (4, 'MO-20260709-001', 1, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0709', 100, '2026-07-09 08:30:00', '2026-07-09 18:00:00', '2026-07-16', '一号装配线', 'ERP', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (5, 'MO-20260710-001', 2, 'V1.0', 'R1.0', 'BATCH-PRO104-0710', 90, '2026-07-10 08:30:00', '2026-07-10 18:00:00', '2026-07-17', '二号装配线', 'ERP', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (6, 'MO-20260711-001', 4, 'V1.0', 'R1.0', 'BATCH-LOW84-0711', 70, '2026-07-11 08:30:00', '2026-07-11 17:30:00', '2026-07-18', '四号矮轴线', 'ERP', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (7, 'MO-20260712-001', 3, 'V1.0', 'R1.0', 'BATCH-MINI68-0712', 50, '2026-07-12 08:30:00', '2026-07-12 17:30:00', '2026-07-19', '三号装配线', 'ERP', 0, 2, 1, 4, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (8, 'MO-20260713-001', 2, 'V1.0', 'R1.0', 'BATCH-PRO104-0713', 90, '2026-07-13 08:30:00', '2026-07-13 18:00:00', '2026-07-20', '二号装配线', 'ERP', 0, 2, 1, 4, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (9, 'MO-20260714-001', 1, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0714', 100, '2026-07-14 08:30:00', '2026-07-14 18:00:00', '2026-07-21', '一号装配线', 'manual', 1, 2, 1, 3, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (10, 'MO-20260715-001', 1, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0715', 120, '2026-07-15 08:30:00', '2026-07-15 18:00:00', '2026-07-22', '一号装配线', 'manual', 1, 2, 1, 2, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (11, 'MO-20260716-001', 5, 'V1.0', 'R1.0', 'BATCH-MACRO12-0716', 40, '2026-07-16 09:00:00', '2026-07-16 15:00:00', '2026-07-23', '试产线', 'manual', 0, 2, 1, 1, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (12, 'MO-20260717-001', 7, 'V1.0', 'R1.0', 'BATCH-GAMETKL-0717', 75, '2026-07-17 08:30:00', '2026-07-17 18:00:00', '2026-07-24', '七号电竞线', 'manual', 1, 2, 1, 2, '2026-07-16 17:14:17', '2026-07-17 11:18:03');
INSERT INTO `production_order` VALUES (13, 'MO-20260718-001', 6, 'V1.0', 'R1.0', 'BATCH-SPLIT75-0718', 55, '2026-07-18 08:30:00', '2026-07-18 17:30:00', '2026-07-25', '六号人体工学线', 'manual', 0, 2, 1, 0, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (14, 'MO-20260718-002', 8, 'V1.0', 'R1.0', 'BATCH-SILENT98-0718', 65, '2026-07-18 13:00:00', '2026-07-18 20:00:00', '2026-07-26', '八号静音线', 'manual', 0, 2, 0, 0, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (15, 'MO-20260706-002', 3, 'V1.0', 'R1.0', 'BATCH-MINI68-0706-B', 36, '2026-07-06 13:00:00', '2026-07-06 20:00:00', '2026-07-13', '三号装配线', 'manual', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (16, 'MO-20260707-002', 2, 'V1.0', 'R1.0', 'BATCH-PRO104-0707-B', 55, '2026-07-07 13:00:00', '2026-07-07 21:00:00', '2026-07-14', '二号装配线', 'ERP', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (17, 'MO-20260708-002', 7, 'V1.0', 'R1.0', 'BATCH-GAMETKL-0708-B', 42, '2026-07-08 12:30:00', '2026-07-08 20:30:00', '2026-07-15', '七号电竞线', 'manual', 1, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (18, 'MO-20260709-002', 8, 'V1.0', 'R1.0', 'BATCH-SILENT98-0709-B', 48, '2026-07-09 13:30:00', '2026-07-09 20:00:00', '2026-07-16', '八号静音线', 'ERP', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (19, 'MO-20260710-002', 6, 'V1.0', 'R1.0', 'BATCH-SPLIT75-0710-B', 35, '2026-07-10 13:00:00', '2026-07-10 20:30:00', '2026-07-17', '六号人体工学线', 'manual', 0, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (20, 'MO-20260711-002', 1, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0711-B', 60, '2026-07-11 12:30:00', '2026-07-11 20:30:00', '2026-07-18', '一号装配线', 'ERP', 1, 2, 1, 5, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (21, 'MO-20260712-002', 5, 'V1.0', 'R1.0', 'BATCH-MACRO12-0712-B', 30, '2026-07-12 13:00:00', '2026-07-12 18:00:00', '2026-07-19', '试产线', 'manual', 0, 2, 1, 4, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (22, 'MO-20260713-002', 4, 'V1.0', 'R1.0', 'BATCH-LOW84-0713-B', 65, '2026-07-13 13:00:00', '2026-07-13 20:00:00', '2026-07-20', '四号矮轴线', 'ERP', 0, 2, 1, 4, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (23, 'MO-20260714-002', 7, 'V1.0', 'R1.0', 'BATCH-GAMETKL-0714-B', 85, '2026-07-14 13:00:00', '2026-07-15 12:00:00', '2026-07-22', '七号电竞线', 'manual', 1, 2, 1, 0, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (24, 'MO-20260715-002', 3, 'V1.0', 'R1.0', 'BATCH-MINI68-0715-B', 72, '2026-07-15 13:30:00', '2026-07-16 13:00:00', '2026-07-23', '三号装配线', 'ERP', 0, 2, 1, 0, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (25, 'MO-20260716-002', 8, 'V1.0', 'R1.0', 'BATCH-SILENT98-0716-B', 68, '2026-07-16 13:00:00', '2026-07-17 10:00:00', '2026-07-24', '八号静音线', 'manual', 0, 2, 1, 0, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (26, 'MO-20260717-002', 2, 'V1.0', 'R1.0', 'BATCH-PRO104-0717-B', 95, '2026-07-17 13:00:00', '2026-07-18 12:00:00', '2026-07-25', '二号装配线', 'ERP', 0, 2, 1, 0, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (27, 'MO-20260718-003', 1, 'V1.0', 'R1.0', 'BATCH-ALPHA87-0718-C', 88, '2026-07-18 08:00:00', '2026-07-18 18:00:00', '2026-07-26', '一号装配线', 'manual', 1, 2, 1, 0, '2026-07-16 17:14:17', NULL);
INSERT INTO `production_order` VALUES (28, 'MO-20260718-004', 5, 'V1.0', 'R1.0', 'BATCH-MACRO12-0718-D', 24, '2026-07-18 14:00:00', '2026-07-18 20:00:00', '2026-07-27', '试产线', 'manual', 0, 2, 0, 0, '2026-07-16 17:14:17', NULL);

-- ----------------------------
-- Table structure for production_task
-- ----------------------------
DROP TABLE IF EXISTS `production_task`;
CREATE TABLE `production_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务单号',
  `order_id` bigint NOT NULL COMMENT '生产工单ID',
  `process_route_id` bigint NOT NULL COMMENT '工艺路线步骤ID',
  `sequence_no` int NOT NULL COMMENT '工序顺序',
  `station_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实际执行工位',
  `assigned_user_id` bigint NULL DEFAULT NULL COMMENT '指派操作员/班组长ID',
  `planned_quantity` int NOT NULL COMMENT '计划数量',
  `completed_quantity` int NOT NULL DEFAULT 0 COMMENT '已完工数量',
  `defect_quantity` int NOT NULL DEFAULT 0 COMMENT '不良数量',
  `abnormal_type` tinyint NULL DEFAULT NULL COMMENT '异常类型：1=缺料 2=设备 3=质量 4=其他',
  `abnormal_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '异常描述',
  `abnormal_solution` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '异常处理措施',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=待开工 1=生产中 2=暂停 3=已完工 4=待质检 5=异常',
  `start_time` datetime NULL DEFAULT NULL COMMENT '实际开始时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '实际完成时间',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_production_task_task_no`(`task_no` ASC) USING BTREE,
  INDEX `idx_production_task_order`(`order_id` ASC) USING BTREE,
  INDEX `idx_production_task_route`(`process_route_id` ASC) USING BTREE,
  INDEX `idx_production_task_assignee`(`assigned_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_production_task_assignee` FOREIGN KEY (`assigned_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_production_task_order` FOREIGN KEY (`order_id`) REFERENCES `production_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_production_task_route` FOREIGN KEY (`process_route_id`) REFERENCES `process_route` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 116 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '生产任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of production_task
-- ----------------------------
INSERT INTO `production_task` VALUES (1, 'TASK-0706-001-010', 1, 21, 10, 'ST-PCBA-04', 5, 45, 45, 1, NULL, NULL, NULL, 3, '2026-07-06 08:40:00', '2026-07-06 09:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (2, 'TASK-0706-001-020', 1, 22, 20, 'ST-ASM-06', 3, 45, 45, 0, NULL, NULL, NULL, 3, '2026-07-06 08:50:00', '2026-07-06 10:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (3, 'TASK-0706-001-030', 1, 23, 30, 'ST-QC-06', 5, 45, 45, 1, NULL, NULL, NULL, 3, '2026-07-06 09:00:00', '2026-07-06 10:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (4, 'TASK-0706-001-040', 1, 24, 40, 'ST-PACK-06', 4, 45, 45, 0, NULL, NULL, NULL, 3, '2026-07-06 09:10:00', '2026-07-06 10:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (5, 'TASK-0706-015-010', 15, 10, 10, 'ST-PCBA-03', 5, 36, 36, 0, NULL, NULL, NULL, 3, '2026-07-06 13:10:00', '2026-07-06 14:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (6, 'TASK-0706-015-020', 15, 11, 20, 'ST-ASM-03', 3, 36, 36, 0, NULL, NULL, NULL, 3, '2026-07-06 13:20:00', '2026-07-06 14:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (7, 'TASK-0706-015-030', 15, 12, 30, 'ST-AGING-02', 5, 36, 36, 0, NULL, NULL, NULL, 3, '2026-07-06 13:30:00', '2026-07-06 14:40:00', '历史完成任务');
INSERT INTO `production_task` VALUES (8, 'TASK-0706-015-040', 15, 13, 40, 'ST-QC-03', 5, 36, 36, 0, NULL, NULL, NULL, 3, '2026-07-06 13:40:00', '2026-07-06 14:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (9, 'TASK-0706-015-050', 15, 14, 50, 'ST-PACK-03', 4, 36, 36, 0, NULL, NULL, NULL, 3, '2026-07-06 13:50:00', '2026-07-06 15:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (10, 'TASK-0707-002-010', 2, 25, 10, 'ST-PCBA-05', 5, 80, 80, 2, NULL, NULL, NULL, 3, '2026-07-07 08:40:00', '2026-07-07 09:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (11, 'TASK-0707-002-020', 2, 26, 20, 'ST-TUNE-01', 3, 80, 80, 0, NULL, NULL, NULL, 3, '2026-07-07 08:50:00', '2026-07-07 10:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (12, 'TASK-0707-002-030', 2, 27, 30, 'ST-ASM-07', 3, 80, 80, 0, NULL, NULL, NULL, 3, '2026-07-07 09:00:00', '2026-07-07 10:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (13, 'TASK-0707-002-040', 2, 28, 40, 'ST-QC-07', 5, 80, 80, 2, NULL, NULL, NULL, 3, '2026-07-07 09:10:00', '2026-07-07 10:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (14, 'TASK-0707-002-050', 2, 29, 50, 'ST-PACK-07', 4, 80, 80, 0, NULL, NULL, NULL, 3, '2026-07-07 09:20:00', '2026-07-07 10:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (15, 'TASK-0707-016-010', 16, 6, 10, 'ST-PCBA-02', 5, 55, 55, 0, NULL, NULL, NULL, 3, '2026-07-07 13:10:00', '2026-07-07 14:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (16, 'TASK-0707-016-020', 16, 7, 20, 'ST-ASM-02', 3, 55, 55, 0, NULL, NULL, NULL, 3, '2026-07-07 13:20:00', '2026-07-07 14:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (17, 'TASK-0707-016-030', 16, 8, 30, 'ST-QC-02', 5, 55, 55, 0, NULL, NULL, NULL, 3, '2026-07-07 13:30:00', '2026-07-07 14:40:00', '历史完成任务');
INSERT INTO `production_task` VALUES (18, 'TASK-0707-016-040', 16, 9, 40, 'ST-PACK-02', 4, 55, 55, 0, NULL, NULL, NULL, 3, '2026-07-07 13:40:00', '2026-07-07 14:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (19, 'TASK-0708-003-010', 3, 30, 10, 'ST-ASM-08', 3, 60, 60, 0, NULL, NULL, NULL, 3, '2026-07-08 08:40:00', '2026-07-08 09:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (20, 'TASK-0708-003-020', 3, 31, 20, 'ST-QC-08', 5, 60, 60, 1, NULL, NULL, NULL, 3, '2026-07-08 08:50:00', '2026-07-08 10:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (21, 'TASK-0708-003-030', 3, 32, 30, 'ST-PACK-08', 4, 60, 60, 0, NULL, NULL, NULL, 3, '2026-07-08 09:00:00', '2026-07-08 10:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (22, 'TASK-0708-017-010', 17, 25, 10, 'ST-PCBA-05', 5, 42, 42, 0, NULL, NULL, NULL, 3, '2026-07-08 12:40:00', '2026-07-08 13:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (23, 'TASK-0708-017-020', 17, 26, 20, 'ST-TUNE-01', 3, 42, 42, 0, NULL, NULL, NULL, 3, '2026-07-08 12:50:00', '2026-07-08 14:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (24, 'TASK-0708-017-030', 17, 27, 30, 'ST-ASM-07', 3, 42, 42, 0, NULL, NULL, NULL, 3, '2026-07-08 13:00:00', '2026-07-08 14:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (25, 'TASK-0708-017-040', 17, 28, 40, 'ST-QC-07', 5, 42, 42, 0, NULL, NULL, NULL, 3, '2026-07-08 13:10:00', '2026-07-08 14:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (26, 'TASK-0708-017-050', 17, 29, 50, 'ST-PACK-07', 4, 42, 42, 0, NULL, NULL, NULL, 3, '2026-07-08 13:20:00', '2026-07-08 14:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (27, 'TASK-0709-004-010', 4, 1, 10, 'ST-PCBA-01', 5, 100, 100, 0, NULL, NULL, NULL, 3, '2026-07-09 08:40:00', '2026-07-09 09:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (28, 'TASK-0709-004-020', 4, 2, 20, 'ST-ASM-01', 3, 100, 100, 0, NULL, NULL, NULL, 3, '2026-07-09 08:50:00', '2026-07-09 10:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (29, 'TASK-0709-004-030', 4, 3, 30, 'ST-AGING-01', 5, 100, 100, 0, NULL, NULL, NULL, 3, '2026-07-09 09:00:00', '2026-07-09 10:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (30, 'TASK-0709-004-040', 4, 4, 40, 'ST-QC-01', 5, 100, 100, 0, NULL, NULL, NULL, 3, '2026-07-09 09:10:00', '2026-07-09 10:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (31, 'TASK-0709-004-050', 4, 5, 50, 'ST-PACK-01', 4, 100, 100, 0, NULL, NULL, NULL, 3, '2026-07-09 09:20:00', '2026-07-09 10:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (32, 'TASK-0709-018-010', 18, 30, 10, 'ST-ASM-08', 3, 48, 48, 0, NULL, NULL, NULL, 3, '2026-07-09 13:40:00', '2026-07-09 14:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (33, 'TASK-0709-018-020', 18, 31, 20, 'ST-QC-08', 5, 48, 48, 0, NULL, NULL, NULL, 3, '2026-07-09 13:50:00', '2026-07-09 15:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (34, 'TASK-0709-018-030', 18, 32, 30, 'ST-PACK-08', 4, 48, 48, 0, NULL, NULL, NULL, 3, '2026-07-09 14:00:00', '2026-07-09 15:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (35, 'TASK-0710-005-010', 5, 6, 10, 'ST-PCBA-02', 5, 90, 90, 2, NULL, NULL, NULL, 3, '2026-07-10 08:40:00', '2026-07-10 09:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (36, 'TASK-0710-005-020', 5, 7, 20, 'ST-ASM-02', 3, 90, 90, 0, NULL, NULL, NULL, 3, '2026-07-10 08:50:00', '2026-07-10 10:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (37, 'TASK-0710-005-030', 5, 8, 30, 'ST-QC-02', 5, 90, 90, 2, NULL, NULL, NULL, 3, '2026-07-10 09:00:00', '2026-07-10 10:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (38, 'TASK-0710-005-040', 5, 9, 40, 'ST-PACK-02', 4, 90, 90, 0, NULL, NULL, NULL, 3, '2026-07-10 09:10:00', '2026-07-10 10:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (39, 'TASK-0710-019-010', 19, 21, 10, 'ST-PCBA-04', 5, 35, 35, 0, NULL, NULL, NULL, 3, '2026-07-10 13:10:00', '2026-07-10 14:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (40, 'TASK-0710-019-020', 19, 22, 20, 'ST-ASM-06', 3, 35, 35, 0, NULL, NULL, NULL, 3, '2026-07-10 13:20:00', '2026-07-10 14:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (41, 'TASK-0710-019-030', 19, 23, 30, 'ST-QC-06', 5, 35, 35, 0, NULL, NULL, NULL, 3, '2026-07-10 13:30:00', '2026-07-10 14:40:00', '历史完成任务');
INSERT INTO `production_task` VALUES (42, 'TASK-0710-019-040', 19, 24, 40, 'ST-PACK-06', 4, 35, 35, 0, NULL, NULL, NULL, 3, '2026-07-10 13:40:00', '2026-07-10 14:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (43, 'TASK-0711-006-010', 6, 15, 10, 'ST-ASM-04', 3, 70, 70, 0, NULL, NULL, NULL, 3, '2026-07-11 08:40:00', '2026-07-11 09:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (44, 'TASK-0711-006-020', 6, 16, 20, 'ST-QC-04', 5, 70, 70, 1, NULL, NULL, NULL, 3, '2026-07-11 08:50:00', '2026-07-11 10:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (45, 'TASK-0711-006-030', 6, 17, 30, 'ST-PACK-04', 4, 70, 70, 0, NULL, NULL, NULL, 3, '2026-07-11 09:00:00', '2026-07-11 10:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (46, 'TASK-0711-020-010', 20, 1, 10, 'ST-PCBA-01', 5, 60, 60, 0, NULL, NULL, NULL, 3, '2026-07-11 12:40:00', '2026-07-11 13:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (47, 'TASK-0711-020-020', 20, 2, 20, 'ST-ASM-01', 3, 60, 60, 0, NULL, NULL, NULL, 3, '2026-07-11 12:50:00', '2026-07-11 14:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (48, 'TASK-0711-020-030', 20, 3, 30, 'ST-AGING-01', 5, 60, 60, 0, NULL, NULL, NULL, 3, '2026-07-11 13:00:00', '2026-07-11 14:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (49, 'TASK-0711-020-040', 20, 4, 40, 'ST-QC-01', 5, 60, 60, 0, NULL, NULL, NULL, 3, '2026-07-11 13:10:00', '2026-07-11 14:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (50, 'TASK-0711-020-050', 20, 5, 50, 'ST-PACK-01', 4, 60, 60, 0, NULL, NULL, NULL, 3, '2026-07-11 13:20:00', '2026-07-11 14:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (51, 'TASK-0712-007-010', 7, 10, 10, 'ST-PCBA-03', 5, 50, 50, 1, NULL, NULL, NULL, 3, '2026-07-12 08:40:00', '2026-07-12 09:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (52, 'TASK-0712-007-020', 7, 11, 20, 'ST-ASM-03', 3, 50, 50, 0, NULL, NULL, NULL, 3, '2026-07-12 08:50:00', '2026-07-12 10:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (53, 'TASK-0712-007-030', 7, 12, 30, 'ST-AGING-02', 5, 50, 50, 1, NULL, NULL, NULL, 3, '2026-07-12 09:00:00', '2026-07-12 10:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (54, 'TASK-0712-007-040', 7, 13, 40, 'ST-QC-03', 5, 50, 50, 1, NULL, NULL, NULL, 3, '2026-07-12 09:10:00', '2026-07-12 10:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (55, 'TASK-0712-007-050', 7, 14, 50, 'ST-PACK-03', 4, 50, 50, 0, NULL, NULL, NULL, 3, '2026-07-12 09:20:00', '2026-07-12 10:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (56, 'TASK-0712-021-010', 21, 18, 10, 'ST-ASM-05', 3, 30, 30, 0, NULL, NULL, NULL, 3, '2026-07-12 13:10:00', '2026-07-12 14:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (57, 'TASK-0712-021-020', 21, 19, 20, 'ST-QC-05', 5, 30, 30, 0, NULL, NULL, NULL, 3, '2026-07-12 13:20:00', '2026-07-12 14:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (58, 'TASK-0712-021-030', 21, 20, 30, 'ST-PACK-05', 4, 30, 30, 0, NULL, NULL, NULL, 3, '2026-07-12 13:30:00', '2026-07-12 14:40:00', '历史完成任务');
INSERT INTO `production_task` VALUES (59, 'TASK-0713-008-010', 8, 6, 10, 'ST-PCBA-02', 5, 90, 90, 2, NULL, NULL, NULL, 3, '2026-07-13 08:40:00', '2026-07-13 09:50:00', '历史完成任务');
INSERT INTO `production_task` VALUES (60, 'TASK-0713-008-020', 8, 7, 20, 'ST-ASM-02', 3, 90, 90, 0, NULL, NULL, NULL, 3, '2026-07-13 08:50:00', '2026-07-13 10:00:00', '历史完成任务');
INSERT INTO `production_task` VALUES (61, 'TASK-0713-008-030', 8, 8, 30, 'ST-QC-02', 5, 90, 90, 2, NULL, NULL, NULL, 3, '2026-07-13 09:00:00', '2026-07-13 10:10:00', '历史完成任务');
INSERT INTO `production_task` VALUES (62, 'TASK-0713-008-040', 8, 9, 40, 'ST-PACK-02', 4, 90, 90, 0, NULL, NULL, NULL, 3, '2026-07-13 09:10:00', '2026-07-13 10:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (63, 'TASK-0713-022-010', 22, 15, 10, 'ST-ASM-04', 3, 65, 65, 0, NULL, NULL, NULL, 3, '2026-07-13 13:10:00', '2026-07-13 14:20:00', '历史完成任务');
INSERT INTO `production_task` VALUES (64, 'TASK-0713-022-020', 22, 16, 20, 'ST-QC-04', 5, 65, 65, 0, NULL, NULL, NULL, 3, '2026-07-13 13:20:00', '2026-07-13 14:30:00', '历史完成任务');
INSERT INTO `production_task` VALUES (65, 'TASK-0713-022-030', 22, 17, 30, 'ST-PACK-04', 4, 65, 65, 0, NULL, NULL, NULL, 3, '2026-07-13 13:30:00', '2026-07-13 14:40:00', '历史完成任务');
INSERT INTO `production_task` VALUES (66, 'TASK-0714-009-010', 9, 1, 10, 'ST-PCBA-01', 5, 100, 100, 0, NULL, NULL, NULL, 3, '2026-07-14 08:40:00', '2026-07-14 09:50:00', '计划待执行');
INSERT INTO `production_task` VALUES (67, 'TASK-0714-009-020', 9, 2, 20, 'ST-ASM-01', 3, 100, 100, 2, NULL, NULL, NULL, 3, '2026-07-14 08:50:00', '2026-07-14 10:00:00', '计划待执行');
INSERT INTO `production_task` VALUES (68, 'TASK-0714-009-030', 9, 3, 30, 'ST-AGING-01', 5, 100, 98, 2, 3, '老化测试发现蓝牙连接不稳定', '已转返修排查无线模块', 5, '2026-07-14 09:00:00', NULL, '存在质量异常，待返修');
INSERT INTO `production_task` VALUES (69, 'TASK-0714-009-040', 9, 4, 40, 'ST-QC-01', 5, 100, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (70, 'TASK-0714-009-050', 9, 5, 50, 'ST-PACK-01', 4, 100, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (71, 'TASK-0714-023-010', 23, 25, 10, 'ST-PCBA-05', 5, 85, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (72, 'TASK-0714-023-020', 23, 26, 20, 'ST-TUNE-01', 3, 85, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (73, 'TASK-0714-023-030', 23, 27, 30, 'ST-ASM-07', 3, 85, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (74, 'TASK-0714-023-040', 23, 28, 40, 'ST-QC-07', 5, 85, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (75, 'TASK-0714-023-050', 23, 29, 50, 'ST-PACK-07', 4, 85, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (76, 'TASK-0715-010-010', 10, 1, 10, 'ST-PCBA-01', 5, 120, 120, 0, NULL, NULL, NULL, 3, '2026-07-15 08:40:00', '2026-07-15 09:50:00', '生产进行中');
INSERT INTO `production_task` VALUES (77, 'TASK-0715-010-020', 10, 2, 20, 'ST-ASM-01', 3, 120, 76, 2, NULL, NULL, NULL, 1, '2026-07-15 08:50:00', NULL, '生产进行中');
INSERT INTO `production_task` VALUES (78, 'TASK-0715-010-030', 10, 3, 30, 'ST-AGING-01', 5, 120, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '生产进行中');
INSERT INTO `production_task` VALUES (79, 'TASK-0715-010-040', 10, 4, 40, 'ST-QC-01', 5, 120, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '生产进行中');
INSERT INTO `production_task` VALUES (80, 'TASK-0715-010-050', 10, 5, 50, 'ST-PACK-01', 4, 120, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '生产进行中');
INSERT INTO `production_task` VALUES (81, 'TASK-0715-024-010', 24, 10, 10, 'ST-PCBA-03', 5, 72, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (82, 'TASK-0715-024-020', 24, 11, 20, 'ST-ASM-03', 3, 72, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (83, 'TASK-0715-024-030', 24, 12, 30, 'ST-AGING-02', 5, 72, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (84, 'TASK-0715-024-040', 24, 13, 40, 'ST-QC-03', 5, 72, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (85, 'TASK-0715-024-050', 24, 14, 50, 'ST-PACK-03', 4, 72, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (86, 'TASK-0716-011-010', 11, 18, 10, 'ST-ASM-05', 3, 40, 18, 0, NULL, NULL, NULL, 1, '2026-07-16 09:10:00', NULL, '生产进行中');
INSERT INTO `production_task` VALUES (87, 'TASK-0716-011-020', 11, 19, 20, 'ST-QC-05', 5, 40, 0, 0, NULL, NULL, NULL, 1, NULL, NULL, '生产进行中');
INSERT INTO `production_task` VALUES (88, 'TASK-0716-011-030', 11, 20, 30, 'ST-PACK-05', 4, 40, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '生产进行中');
INSERT INTO `production_task` VALUES (89, 'TASK-0716-025-010', 25, 30, 10, 'ST-ASM-08', 3, 68, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (90, 'TASK-0716-025-020', 25, 31, 20, 'ST-QC-08', 5, 68, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (91, 'TASK-0716-025-030', 25, 32, 30, 'ST-PACK-08', 4, 68, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (92, 'TASK-0717-012-010', 12, 25, 10, 'ST-PCBA-05', 5, 75, 31, 0, NULL, NULL, NULL, 1, '2026-07-17 08:40:00', NULL, '生产进行中');
INSERT INTO `production_task` VALUES (93, 'TASK-0717-012-020', 12, 26, 20, 'ST-TUNE-01', 3, 75, 30, 1, NULL, NULL, NULL, 1, '2026-07-17 08:50:00', NULL, '生产进行中');
INSERT INTO `production_task` VALUES (94, 'TASK-0717-012-030', 12, 27, 30, 'ST-ASM-07', 3, 75, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '生产进行中');
INSERT INTO `production_task` VALUES (95, 'TASK-0717-012-040', 12, 28, 40, 'ST-QC-07', 5, 75, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '生产进行中');
INSERT INTO `production_task` VALUES (96, 'TASK-0717-012-050', 12, 29, 50, 'ST-PACK-07', 4, 75, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '生产进行中');
INSERT INTO `production_task` VALUES (97, 'TASK-0717-026-010', 26, 6, 10, 'ST-PCBA-02', 5, 95, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (98, 'TASK-0717-026-020', 26, 7, 20, 'ST-ASM-02', 3, 95, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (99, 'TASK-0717-026-030', 26, 8, 30, 'ST-QC-02', 5, 95, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (100, 'TASK-0717-026-040', 26, 9, 40, 'ST-PACK-02', 4, 95, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (101, 'TASK-0718-027-010', 27, 1, 10, 'ST-PCBA-01', 5, 88, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (102, 'TASK-0718-027-020', 27, 2, 20, 'ST-ASM-01', 3, 88, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (103, 'TASK-0718-027-030', 27, 3, 30, 'ST-AGING-01', 5, 88, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (104, 'TASK-0718-027-040', 27, 4, 40, 'ST-QC-01', 5, 88, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (105, 'TASK-0718-027-050', 27, 5, 50, 'ST-PACK-01', 4, 88, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (106, 'TASK-0718-013-010', 13, 21, 10, 'ST-PCBA-04', 5, 55, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (107, 'TASK-0718-013-020', 13, 22, 20, 'ST-ASM-06', 3, 55, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (108, 'TASK-0718-013-030', 13, 23, 30, 'ST-QC-06', 5, 55, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (109, 'TASK-0718-013-040', 13, 24, 40, 'ST-PACK-06', 4, 55, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (110, 'TASK-0718-014-010', 14, 30, 10, 'ST-ASM-08', 3, 65, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (111, 'TASK-0718-014-020', 14, 31, 20, 'ST-QC-08', 5, 65, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (112, 'TASK-0718-014-030', 14, 32, 30, 'ST-PACK-08', 4, 65, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (113, 'TASK-0718-028-010', 28, 18, 10, 'ST-ASM-05', 3, 24, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (114, 'TASK-0718-028-020', 28, 19, 20, 'ST-QC-05', 5, 24, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');
INSERT INTO `production_task` VALUES (115, 'TASK-0718-028-030', 28, 20, 30, 'ST-PACK-05', 4, 24, 0, 0, NULL, NULL, NULL, 0, NULL, NULL, '计划待执行');

-- ----------------------------
-- Table structure for rework_order
-- ----------------------------
DROP TABLE IF EXISTS `rework_order`;
CREATE TABLE `rework_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rework_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '返修工单号',
  `source_inspection_id` bigint NOT NULL COMMENT '来源检验记录ID',
  `order_id` bigint NOT NULL COMMENT '原生产工单ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '来源任务ID',
  `product_sn` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品条码/SN',
  `defect_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '不合格原因',
  `rework_requirement` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '返修要求',
  `assignee_id` bigint NULL DEFAULT NULL COMMENT '维修责任人ID',
  `deadline` datetime NULL DEFAULT NULL COMMENT '返修截止时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=待分发 1=返修中 2=待复检 3=完成 4=关闭',
  `repair_action` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '维修措施',
  `replaced_material` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更换物料摘要',
  `repair_hours` decimal(10, 2) NULL DEFAULT NULL COMMENT '维修工时',
  `repair_result` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '维修结果',
  `repair_time` datetime NULL DEFAULT NULL COMMENT '维修完成时间',
  `recheck_inspection_id` bigint NULL DEFAULT NULL COMMENT '复检记录ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rework_order_no`(`rework_no` ASC) USING BTREE,
  INDEX `idx_rework_order_source_inspection`(`source_inspection_id` ASC) USING BTREE,
  INDEX `idx_rework_order_order`(`order_id` ASC) USING BTREE,
  INDEX `idx_rework_order_task`(`task_id` ASC) USING BTREE,
  INDEX `idx_rework_order_assignee`(`assignee_id` ASC) USING BTREE,
  INDEX `idx_rework_order_recheck`(`recheck_inspection_id` ASC) USING BTREE,
  CONSTRAINT `fk_rework_order_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_rework_order_order` FOREIGN KEY (`order_id`) REFERENCES `production_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_rework_order_recheck` FOREIGN KEY (`recheck_inspection_id`) REFERENCES `inspection_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_rework_order_source_inspection` FOREIGN KEY (`source_inspection_id`) REFERENCES `inspection_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_rework_order_task` FOREIGN KEY (`task_id`) REFERENCES `production_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '返修工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rework_order
-- ----------------------------
INSERT INTO `rework_order` VALUES (1, 'RW-20260707-001', 2, 2, 13, 'SN-GAMETKL-0707-001', 'RGB灯效异常', '检查灯珠焊点并刷新灯效固件', 7, '2026-07-07 18:30:00', 3, '补焊异常灯珠并刷新固件', 'MAT-RGB-LED x2', 0.50, '已修复，复检通过', '2026-07-07 17:00:00', 3, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (2, 'RW-20260708-001', 4, 3, 20, 'SN-SILENT98-0708-001', '空腔音偏大', '补装吸音棉并复测按键噪声', 7, '2026-07-08 18:00:00', 3, '补装底棉并调整定位柱', 'MAT-FOAM-SILENT x1', 0.40, '噪声恢复正常，复检通过', '2026-07-08 16:05:00', 5, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (3, 'RW-20260710-001', 6, 5, 37, 'SN-PRO104-0710-001', '空格键异响', '调整卫星轴润滑和安装位置，复检空格键手感', 7, '2026-07-10 18:00:00', 3, '重新润滑卫星轴并校正钢丝', 'MAT-STAB-SET x1', 0.50, '异响消除，复检通过', '2026-07-10 16:45:00', 7, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (4, 'RW-20260712-001', 8, 7, 54, 'SN-MINI68-0712-001', '蓝牙连接重连', '检查天线焊点并重新刷写固件，复检无线连接稳定性', 7, '2026-07-12 18:00:00', 3, '重焊天线焊点并刷新固件', '天线焊点返工', 0.60, '已修复，复检通过', '2026-07-12 15:45:00', 9, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (5, 'RW-20260714-001', 10, 9, 68, 'SN-ALPHA87-0714-001', '蓝牙连接不稳定', '排查无线模块和电池连接，完成后提交复检', 7, '2026-07-15 12:00:00', 1, '正在检测无线模块焊点', NULL, 0.20, '处理中', NULL, NULL, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (6, 'RW-20260715-001', 11, 10, 77, 'SN-ALPHA87-0715-002', '轴体触发不稳定', '更换异常轴体并做单键触发复测', 7, '2026-07-15 18:00:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (7, 'RW-20260706-B030', 16, 15, 7, 'SN-MINI-68-B-0706', '无线连接短暂重连', '检查天线与无线模块连接', 7, '2026-07-06 18:50:00', 3, '已完成维修并复测', NULL, 0.50, '维修完成，功能恢复正常', '2026-07-06 16:20:00', NULL, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (8, 'RW-20260706-B040', 17, 15, 8, 'SN-MINI-68-B-0706', '外观装配间隙偏大', '调整装配间隙并复检外观', 7, '2026-07-06 19:30:00', 3, '已完成维修并复测', NULL, 0.50, '维修完成，功能恢复正常', '2026-07-06 17:00:00', NULL, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (9, 'RW-20260707-B030', 18, 16, 17, 'SN-PRO-104-B-0707', '外观装配间隙偏大', '调整装配间隙并复检外观', 7, '2026-07-07 18:50:00', 3, '已完成维修并复测', NULL, 0.50, '维修完成，功能恢复正常', '2026-07-07 16:20:00', NULL, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (10, 'RW-20260708-B010', 19, 17, 22, 'SN-GAME-TKL-B-0708', '矩阵扫描偶发失败', '检查PCB焊点并重新测试矩阵', 7, '2026-07-08 17:00:00', 3, '已完成维修并复测', NULL, 0.50, '维修完成，功能恢复正常', '2026-07-08 14:30:00', NULL, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (11, 'RW-20260708-B040', 20, 17, 25, 'SN-GAME-TKL-B-0708', '灯效颜色不一致', '检查灯珠焊点并刷新灯效配置', 7, '2026-07-08 19:00:00', 3, '已完成维修并复测', 'MAT-RGB-LED x1', 0.50, '维修完成，功能恢复正常', '2026-07-08 16:30:00', NULL, '2026-07-16 17:14:18');
INSERT INTO `rework_order` VALUES (12, 'RW-20260709-B020', 21, 18, 33, 'SN-SILENT-98-B-0709', '空腔音超过标准', '补装吸音棉并复测噪声', 7, '2026-07-09 18:40:00', 1, '正在定位缺陷原因', 'MAT-FOAM-SILENT x1', 0.20, '处理中', NULL, NULL, '2026-07-16 17:14:18');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工号/登录账号',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码密文',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码：admin/planner/operator/inspector/repair',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `department` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部门/班组',
  `position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '岗位',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `skill_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '技能等级',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_employee_no`(`employee_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'U001', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'admin', '系统管理员', '信息化部', '系统管理员', '13800000001', 'L3', 1, '2026-07-16 17:14:16', NULL);
INSERT INTO `sys_user` VALUES (2, 'U002', 'e10adc3949ba59abbe56e057f20f883e', '计划员张敏', 'planner', '计划员', '生产计划部', '生产计划', '13800000002', 'L2', 1, '2026-07-16 17:14:16', NULL);
INSERT INTO `sys_user` VALUES (3, 'U003', 'e10adc3949ba59abbe56e057f20f883e', '装配员李强', 'operator', '操作员', '一号装配线', '总装工位', '13800000003', 'L2', 1, '2026-07-16 17:14:16', NULL);
INSERT INTO `sys_user` VALUES (4, 'U004', 'e10adc3949ba59abbe56e057f20f883e', '装配员王磊', 'operator', '操作员', '二号装配线', '包装工位', '13800000004', 'L1', 1, '2026-07-16 17:14:16', NULL);
INSERT INTO `sys_user` VALUES (5, 'U005', 'e10adc3949ba59abbe56e057f20f883e', '质检员赵静', 'inspector', '质检员', '质量部', '过程检验', '13800000005', 'L2', 1, '2026-07-16 17:14:16', NULL);
INSERT INTO `sys_user` VALUES (6, 'U006', 'e10adc3949ba59abbe56e057f20f883e', '质检员陈晨', 'inspector', '质检员', '质量部', '终检', '13800000006', 'L3', 1, '2026-07-16 17:14:16', NULL);
INSERT INTO `sys_user` VALUES (7, 'U007', 'e10adc3949ba59abbe56e057f20f883e', '返修员周洋', 'repair', '返修员', '维修组', '返修工位', '13800000007', 'L2', 1, '2026-07-16 17:14:16', NULL);

-- ----------------------------
-- Table structure for work_report
-- ----------------------------
DROP TABLE IF EXISTS `work_report`;
CREATE TABLE `work_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报工单号',
  `task_id` bigint NOT NULL COMMENT '生产任务ID',
  `order_id` bigint NOT NULL COMMENT '生产工单ID',
  `product_sn` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品条码/SN；批量报工可为空',
  `operator_id` bigint NOT NULL COMMENT '操作员ID',
  `station_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '报工工位',
  `report_quantity` int NOT NULL COMMENT '报工数量',
  `qualified_quantity` int NOT NULL COMMENT '合格数量',
  `defect_quantity` int NOT NULL DEFAULT 0 COMMENT '不良数量',
  `actual_hours` decimal(10, 2) NULL DEFAULT NULL COMMENT '实际工时',
  `defect_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '不良原因',
  `report_time` datetime NOT NULL COMMENT '报工时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=撤销 1=有效 2=待审核',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_work_report_report_no`(`report_no` ASC) USING BTREE,
  INDEX `idx_work_report_task`(`task_id` ASC) USING BTREE,
  INDEX `idx_work_report_order`(`order_id` ASC) USING BTREE,
  INDEX `idx_work_report_operator`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `fk_work_report_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_work_report_order` FOREIGN KEY (`order_id`) REFERENCES `production_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_work_report_task` FOREIGN KEY (`task_id`) REFERENCES `production_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '报工记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_report
-- ----------------------------
INSERT INTO `work_report` VALUES (1, 'WR-20260706-001', 3, 1, 'SN-SPLIT75-0706-001', 6, 'ST-QC-06', 45, 44, 1, 1.20, '腕托贴合偏差', '2026-07-06 15:20:00', 1);
INSERT INTO `work_report` VALUES (2, 'WR-20260707-001', 13, 2, 'SN-GAMETKL-0707-001', 5, 'ST-QC-07', 80, 78, 2, 1.60, 'RGB灯效异常', '2026-07-07 16:00:00', 1);
INSERT INTO `work_report` VALUES (3, 'WR-20260708-001', 20, 3, 'SN-SILENT98-0708-001', 6, 'ST-QC-08', 60, 59, 1, 1.10, '空腔音偏大', '2026-07-08 14:40:00', 1);
INSERT INTO `work_report` VALUES (4, 'WR-20260709-001', 30, 4, 'SN-ALPHA87-0709-001', 5, 'ST-QC-01', 100, 99, 1, 1.50, '键帽轻微划伤', '2026-07-09 16:10:00', 1);
INSERT INTO `work_report` VALUES (5, 'WR-20260710-001', 37, 5, 'SN-PRO104-0710-001', 6, 'ST-QC-02', 90, 88, 2, 1.80, '空格键异响', '2026-07-10 15:50:00', 1);
INSERT INTO `work_report` VALUES (6, 'WR-20260711-001', 44, 6, 'SN-LOW84-0711-001', 6, 'ST-QC-04', 70, 69, 1, 1.20, '外壳划伤', '2026-07-11 15:10:00', 1);
INSERT INTO `work_report` VALUES (7, 'WR-20260712-001', 52, 7, 'SN-MINI68-0712-001', 3, 'ST-ASM-03', 50, 49, 1, 2.00, '电池线束压伤', '2026-07-12 12:00:00', 1);
INSERT INTO `work_report` VALUES (8, 'WR-20260712-002', 54, 7, 'SN-MINI68-0712-001', 5, 'ST-QC-03', 50, 49, 1, 1.80, '蓝牙连接重连', '2026-07-12 15:00:00', 1);
INSERT INTO `work_report` VALUES (9, 'WR-20260713-001', 60, 8, 'SN-PRO104-0713-001', 3, 'ST-ASM-02', 90, 88, 2, 2.40, '空格键异响', '2026-07-13 12:10:00', 1);
INSERT INTO `work_report` VALUES (10, 'WR-20260713-002', 61, 8, 'SN-PRO104-0713-001', 6, 'ST-QC-02', 90, 88, 2, 1.90, '外观轻微划伤', '2026-07-13 15:30:00', 1);
INSERT INTO `work_report` VALUES (11, 'WR-20260714-001', 67, 9, 'SN-ALPHA87-0714-001', 3, 'ST-ASM-01', 100, 97, 3, 2.60, '键帽划伤、卫星轴异响', '2026-07-14 12:30:00', 1);
INSERT INTO `work_report` VALUES (12, 'WR-20260714-002', 68, 9, 'SN-ALPHA87-0714-001', 5, 'ST-AGING-01', 98, 96, 2, 1.70, '蓝牙断连', '2026-07-14 15:10:00', 1);
INSERT INTO `work_report` VALUES (13, 'WR-20260715-001', 76, 10, 'SN-ALPHA87-0715-001', 5, 'ST-PCBA-01', 120, 120, 0, 1.20, NULL, '2026-07-15 09:45:00', 1);
INSERT INTO `work_report` VALUES (14, 'WR-20260715-002', 77, 10, 'SN-ALPHA87-0715-001', 3, 'ST-ASM-01', 40, 39, 1, 1.10, '键帽划伤', '2026-07-15 11:30:00', 1);
INSERT INTO `work_report` VALUES (15, 'WR-20260715-003', 77, 10, 'SN-ALPHA87-0715-002', 3, 'ST-ASM-01', 36, 35, 1, 1.00, '轴体触发不稳定', '2026-07-15 14:20:00', 1);
INSERT INTO `work_report` VALUES (16, 'WR-20260716-001', 86, 11, 'SN-MACRO12-0716-001', 3, 'ST-ASM-05', 18, 18, 0, 0.60, NULL, '2026-07-16 10:40:00', 1);
INSERT INTO `work_report` VALUES (17, 'WR-20260717-001', 93, 12, 'SN-GAMETKL-0717-001', 4, 'ST-TUNE-01', 30, 29, 1, 0.80, '大键回弹不一致', '2026-07-17 10:50:00', 1);
INSERT INTO `work_report` VALUES (18, 'WR-20260706-B020', 6, 15, 'SN-MINI-68-B-0706', 4, 'ST-ASM-03', 36, 36, 0, 1.31, NULL, '2026-07-06 14:20:00', 1);
INSERT INTO `work_report` VALUES (19, 'WR-20260706-B040', 8, 15, 'SN-MINI-68-B-0706', 5, 'ST-QC-03', 36, 36, 0, 0.60, NULL, '2026-07-06 15:40:00', 1);
INSERT INTO `work_report` VALUES (20, 'WR-20260707-B020', 16, 16, 'SN-PRO-104-B-0707', 4, 'ST-ASM-02', 55, 54, 1, 1.53, '键帽安装偏位', '2026-07-07 14:20:00', 1);
INSERT INTO `work_report` VALUES (21, 'WR-20260707-B030', 17, 16, 'SN-PRO-104-B-0707', 5, 'ST-QC-02', 55, 55, 0, 0.81, NULL, '2026-07-07 15:00:00', 1);
INSERT INTO `work_report` VALUES (22, 'WR-20260708-B010', 22, 17, 'SN-GAME-TKL-B-0708', 5, 'ST-PCBA-05', 42, 41, 1, 0.60, '矩阵测试偶发失败', '2026-07-08 13:10:00', 1);
INSERT INTO `work_report` VALUES (23, 'WR-20260708-B030', 24, 17, 'SN-GAME-TKL-B-0708', 4, 'ST-ASM-07', 42, 42, 0, 1.64, NULL, '2026-07-08 14:30:00', 1);
INSERT INTO `work_report` VALUES (24, 'WR-20260708-B040', 25, 17, 'SN-GAME-TKL-B-0708', 5, 'ST-QC-07', 42, 40, 2, 0.65, 'RGB灯效色差', '2026-07-08 15:10:00', 1);
INSERT INTO `work_report` VALUES (25, 'WR-20260709-B010', 32, 18, 'SN-SILENT-98-B-0709', 4, 'ST-ASM-08', 48, 48, 0, 1.45, NULL, '2026-07-09 14:10:00', 1);
INSERT INTO `work_report` VALUES (26, 'WR-20260709-B020', 33, 18, 'SN-SILENT-98-B-0709', 5, 'ST-QC-08', 48, 48, 0, 0.61, NULL, '2026-07-09 14:50:00', 1);
INSERT INTO `work_report` VALUES (27, 'WR-20260710-B020', 40, 19, 'SN-SPLIT-75-B-0710', 4, 'ST-ASM-06', 35, 34, 1, 2.07, '键帽安装偏位', '2026-07-10 14:20:00', 1);
INSERT INTO `work_report` VALUES (28, 'WR-20260710-B030', 41, 19, 'SN-SPLIT-75-B-0710', 5, 'ST-QC-06', 35, 33, 2, 0.74, '左右外壳贴合偏差', '2026-07-10 15:00:00', 1);
INSERT INTO `work_report` VALUES (29, 'WR-20260711-B020', 47, 20, 'SN-ALPHA-87-B-0711', 4, 'ST-ASM-01', 60, 60, 0, 1.36, NULL, '2026-07-11 13:50:00', 1);
INSERT INTO `work_report` VALUES (30, 'WR-20260711-B040', 49, 20, 'SN-ALPHA-87-B-0711', 5, 'ST-QC-01', 60, 59, 1, 0.60, '外观轻微瑕疵', '2026-07-11 15:10:00', 1);
INSERT INTO `work_report` VALUES (31, 'WR-20260712-B010', 56, 21, 'SN-MACRO-12-B-0712', 4, 'ST-ASM-05', 30, 30, 0, 0.87, NULL, '2026-07-12 13:40:00', 1);
INSERT INTO `work_report` VALUES (32, 'WR-20260712-B020', 57, 21, 'SN-MACRO-12-B-0712', 5, 'ST-QC-05', 30, 28, 2, 0.37, '旋钮阻尼不一致', '2026-07-12 14:20:00', 1);
INSERT INTO `work_report` VALUES (33, 'WR-20260713-B010', 63, 22, 'SN-LOW-84-B-0713', 4, 'ST-ASM-04', 65, 65, 0, 1.19, NULL, '2026-07-13 13:40:00', 1);
INSERT INTO `work_report` VALUES (34, 'WR-20260713-B020', 64, 22, 'SN-LOW-84-B-0713', 5, 'ST-QC-04', 65, 64, 1, 0.61, '外观轻微瑕疵', '2026-07-13 14:20:00', 1);
INSERT INTO `work_report` VALUES (35, 'WR-20260717111803', 92, 12, NULL, 3, 'ST-PCBA-05', 1, 1, 0, NULL, NULL, '2026-07-17 11:18:03', 1);

SET FOREIGN_KEY_CHECKS = 1;
