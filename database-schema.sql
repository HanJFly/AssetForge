-- ============================================================
-- AssetForge — 实物资产管理系统数据库设计
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_general_ci
-- 存储引擎：InnoDB
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `assetforge` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `assetforge`;

-- ============================================================
-- 一、组织架构
-- ============================================================

-- 部门表
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`            VARCHAR(100) NOT NULL                COMMENT '部门名称',
    `parent_id`       BIGINT       DEFAULT NULL            COMMENT '上级部门ID，顶级为NULL',
    `manager_user_id` BIGINT       DEFAULT NULL            COMMENT '部门管理员用户ID',
    `sort_order`      INT          NOT NULL DEFAULT 0      COMMENT '同级内排序',
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '软删除标记 0-正常 1-已删除',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_manager_user_id` (`manager_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================================
-- 二、权限体系
-- ============================================================

-- 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code`       VARCHAR(30)  NOT NULL                COMMENT '角色编码 EMPLOYEE/STOREKEEPER/ASSET_ADMIN/DEPT_MANAGER',
    `name`       VARCHAR(50)  NOT NULL                COMMENT '角色名称',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 初始化角色数据
INSERT INTO `role` (`code`, `name`) VALUES
('EMPLOYEE',     '普通员工'),
('STOREKEEPER',  '仓管员'),
('ASSET_ADMIN',  '资产管理员'),
('DEPT_MANAGER', '部门管理员');

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`         VARCHAR(50)    NOT NULL                COMMENT '用户名，唯一',
    `password_hash`    VARCHAR(255)   NOT NULL                COMMENT 'bcrypt加密密码',
    `real_name`        VARCHAR(50)    NOT NULL                COMMENT '真实姓名',
    `employee_no`      VARCHAR(50)    DEFAULT NULL            COMMENT '工号',
    `department_id`    BIGINT         DEFAULT NULL            COMMENT '所属部门ID（仓管员为空）',
    `department_name`  VARCHAR(100)   DEFAULT NULL            COMMENT '冗余快照：所属部门名称',
    `phone`            VARCHAR(20)    DEFAULT NULL            COMMENT '联系电话',
    `email`            VARCHAR(100)   DEFAULT NULL            COMMENT '邮箱',
    `is_deleted`       TINYINT        NOT NULL DEFAULT 0      COMMENT '软删除标记 0-正常 1-已删除',
    `status`           VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/DISABLED',
    `last_login_at`    DATETIME       DEFAULT NULL            COMMENT '最后登录时间',
    `created_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_employee_no` (`employee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户角色关联表（支持多角色兼任）
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL                COMMENT '用户ID',
    `role_id`    BIGINT   NOT NULL                COMMENT '角色ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================================
-- 三、系统配置
-- ============================================================

-- 系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
    `id`                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `config_key`              VARCHAR(100) NOT NULL                COMMENT '配置键',
    `config_value`            VARCHAR(500) NOT NULL                COMMENT '配置值',
    `description`             VARCHAR(200) DEFAULT NULL            COMMENT '配置说明',
    `created_at`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 默认配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('default_residual_rate',    '0.05',       '默认残值率'),
('depreciation_day_of_month','1',          '每月几号执行折旧'),
('requisition_timeout_days', '7',          '申领单超时天数'),
('barcode_prefix',           'AST-',       '条码前缀'),
('barcode_sequence_length',  '6',          '条码流水号位数'),
('max_department_level',     '5',          '部门最大层级数');

-- ============================================================
-- 四、资产管理
-- ============================================================

-- 资产分类表
DROP TABLE IF EXISTS `asset_category`;
CREATE TABLE `asset_category` (
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`                   VARCHAR(100) NOT NULL                COMMENT '分类名称',
    `parent_id`              BIGINT       DEFAULT NULL            COMMENT '上级分类ID，一级为NULL',
    `standard_life_months`   INT          DEFAULT NULL            COMMENT '标准使用年限（月），一级分类为NULL',
    `sort_order`             INT          NOT NULL DEFAULT 0      COMMENT '同级内排序',
    `is_deleted`             TINYINT      NOT NULL DEFAULT 0      COMMENT '软删除标记 0-正常 1-已删除',
    `created_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表';

-- 资产表（管理信息）
DROP TABLE IF EXISTS `asset`;
CREATE TABLE `asset` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `asset_code`        VARCHAR(32)    NOT NULL                COMMENT '资产条码，唯一索引 AST-YYYYMMDD-NNNNNN',
    `name`              VARCHAR(200)   NOT NULL                COMMENT '资产名称',
    `category_id`       BIGINT         NOT NULL                COMMENT '资产分类ID',
    `brand_model`       VARCHAR(200)   NOT NULL                COMMENT '品牌/型号',
    `specification`     TEXT           DEFAULT NULL            COMMENT '规格参数',
    `purpose`           VARCHAR(50)    NOT NULL                COMMENT '用途 OFFICE/PRODUCTION/RD/ADMIN',
    `source_type`       VARCHAR(20)    NOT NULL DEFAULT 'PURCHASE' COMMENT '来源 PURCHASE/LEASE',
    `purchase_date`     DATE           NOT NULL                COMMENT '购置日期',
    `purchase_amount`   DECIMAL(12,2)  NOT NULL                COMMENT '购置金额（元）',
    `department_id`     BIGINT         NOT NULL                COMMENT '所属部门ID',
    `department_name`   VARCHAR(100)   NOT NULL                COMMENT '冗余快照：所属部门名称',
    `supplier`          VARCHAR(200)   DEFAULT NULL            COMMENT '供应商',
    `location`          VARCHAR(200)   DEFAULT NULL            COMMENT '存放地点',
    `status`            VARCHAR(20)    NOT NULL DEFAULT 'STOCK' COMMENT '状态 STOCK/ASSIGNED/SCRAPPED/LOST',
    `current_user_id`   BIGINT         DEFAULT NULL            COMMENT '当前使用人ID，库存时NULL',
    `remark`            TEXT           DEFAULT NULL            COMMENT '备注',
    `is_deleted`        TINYINT        NOT NULL DEFAULT 0      COMMENT '软删除标记 0-正常 1-已删除',
    `created_at`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_code` (`asset_code`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_current_user_id` (`current_user_id`),
    INDEX `idx_asset_code_name` (`asset_code`, `name`),
    INDEX `idx_purchase_date` (`purchase_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产表（管理信息）';

-- 资产台账表（财务信息）
DROP TABLE IF EXISTS `asset_ledger`;
CREATE TABLE `asset_ledger` (
    `id`                           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ledger_no`                    VARCHAR(32)    NOT NULL                COMMENT '台账编号 LEDGER-YYYYMMDD-NNNNNN',
    `asset_id`                     BIGINT         NOT NULL                COMMENT '资产ID',
    `department_id`                BIGINT         NOT NULL                COMMENT '所属部门ID',
    `department_name`              VARCHAR(100)   NOT NULL                COMMENT '冗余快照：所属部门名称',
    `entry_date`                   DATE           NOT NULL                COMMENT '入账日期',
    `original_value`               DECIMAL(12,2)  NOT NULL                COMMENT '原值',
    `residual_rate`                DECIMAL(5,4)   NOT NULL DEFAULT 0.0500 COMMENT '残值率',
    `standard_life_months`         INT            NOT NULL                COMMENT '标准使用年限（月）',
    `monthly_depreciation`         DECIMAL(12,2)  NOT NULL                COMMENT '月折旧额',
    `accumulated_depreciation`     DECIMAL(12,2)  NOT NULL DEFAULT 0.00   COMMENT '累计折旧',
    `net_value`                    DECIMAL(12,2)  NOT NULL                COMMENT '现值 = 原值 - 累计折旧',
    `months_used`                  INT            NOT NULL DEFAULT 0       COMMENT '已使用月数',
    `is_deleted`                   TINYINT        NOT NULL DEFAULT 0      COMMENT '软删除标记 0-正常 1-已删除',
    `created_at`                   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ledger_no` (`ledger_no`),
    UNIQUE KEY `uk_asset_id` (`asset_id`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_entry_date` (`entry_date`),
    INDEX `idx_status_net_value` (`net_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产台账表（财务信息）';

-- 资产台账快照表（月度备份）
DROP TABLE IF EXISTS `asset_ledger_snapshot`;
CREATE TABLE `asset_ledger_snapshot` (
    `id`                         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ledger_id`                  BIGINT         NOT NULL                COMMENT '关联 asset_ledger.id',
    `asset_id`                   BIGINT         NOT NULL                COMMENT '关联 asset.id',
    `snapshot_month`             VARCHAR(7)     NOT NULL                COMMENT '快照月份 YYYY-MM',
    `asset_code`                 VARCHAR(32)    NOT NULL                COMMENT '快照时复制',
    `asset_name`                 VARCHAR(200)   NOT NULL                COMMENT '快照时复制',
    `category_name`              VARCHAR(100)   NOT NULL                COMMENT '快照时复制',
    `department_name`            VARCHAR(100)   NOT NULL                COMMENT '快照时复制：所属部门',
    `entry_date`                 DATE           NOT NULL                COMMENT '入账日期',
    `original_value`             DECIMAL(12,2)  NOT NULL                COMMENT '原值',
    `standard_life_months`       INT            NOT NULL                COMMENT '标准使用年限',
    `monthly_depreciation`       DECIMAL(12,2)  NOT NULL                COMMENT '月折旧额',
    `accumulated_depreciation`   DECIMAL(12,2)  NOT NULL                COMMENT '累计折旧（折旧计算后的值）',
    `net_value`                  DECIMAL(12,2)  NOT NULL                COMMENT '现值',
    `months_used`                INT            NOT NULL                COMMENT '已使用月数',
    `current_user_name`          VARCHAR(50)    DEFAULT NULL            COMMENT '当前使用人姓名',
    `current_user_employee_no`   VARCHAR(50)    DEFAULT NULL            COMMENT '当前使用人工号',
    `current_user_department`    VARCHAR(100)   DEFAULT NULL            COMMENT '当前使用人部门',
    `asset_status`               VARCHAR(20)    NOT NULL                COMMENT '资产状态 STOCK/ASSIGNED/SCRAPPED/LOST',
    `created_at`                 DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ledger_month` (`ledger_id`, `snapshot_month`),
    INDEX `idx_ledger_id` (`ledger_id`),
    INDEX `idx_asset_id` (`asset_id`),
    INDEX `idx_snapshot_month` (`snapshot_month`),
    INDEX `idx_department_name` (`department_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产台账快照表（月度备份）';

-- ============================================================
-- 五、业务单据 — 通用审批表
-- ============================================================

-- 审批记录表（统一审批流）
DROP TABLE IF EXISTS `approval_record`;
CREATE TABLE `approval_record` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `approval_type`     VARCHAR(30)    NOT NULL                COMMENT '审批类型 REGISTER/APPLY/TRANSFER/RETURN/SCRAP',
    `target_type`       VARCHAR(30)    NOT NULL                COMMENT '目标类型 requisition_order/transfer_order/return_order/scrap_order/asset',
    `target_id`         BIGINT         NOT NULL                COMMENT '目标记录ID',
    `applicant_id`      BIGINT         NOT NULL                COMMENT '申请人ID',
    `approver_id`       BIGINT         DEFAULT NULL            COMMENT '审批人ID',
    `approval_status`   VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `approval_remark`   TEXT           DEFAULT NULL            COMMENT '审批意见',
    `approved_at`       DATETIME       DEFAULT NULL            COMMENT '审批时间',
    `transferred_to`    BIGINT         DEFAULT NULL            COMMENT '转交到的用户ID',
    `created_at`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_approver` (`approver_id`, `approval_status`),
    INDEX `idx_applicant` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表（统一审批流）';

-- ============================================================
-- 六、业务单据 — 申领
-- ============================================================

-- 申领单主表
DROP TABLE IF EXISTS `requisition_order`;
CREATE TABLE `requisition_order` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`                  VARCHAR(32)    NOT NULL                COMMENT '单号 REQ-YYYYMMDD-NNNNNN',
    `applicant_id`              BIGINT         NOT NULL                COMMENT '申领人ID',
    `applicant_name`            VARCHAR(50)    NOT NULL                COMMENT '冗余快照：申领人姓名',
    `applicant_employee_no`     VARCHAR(50)    NOT NULL                COMMENT '冗余快照：申领人工号',
    `applicant_department_id`   BIGINT         NOT NULL                COMMENT '冗余快照：申领部门ID',
    `applicant_department_name` VARCHAR(100)   NOT NULL                COMMENT '冗余快照：申领部门名称',
    `reason`                    TEXT           NOT NULL                COMMENT '申领原因',
    `expected_date`             DATE           DEFAULT NULL            COMMENT '期望日期',
    `approval_status`           VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `approval_remark`           TEXT           DEFAULT NULL            COMMENT '审批意见',
    `approver_name`             VARCHAR(50)    DEFAULT NULL            COMMENT '冗余快照：审批人姓名',
    `approved_at`               DATETIME       DEFAULT NULL            COMMENT '审批通过时间',
    `outbound_staff_name`       VARCHAR(50)    DEFAULT NULL            COMMENT '冗余快照：出库仓管员姓名',
    `outbound_at`               DATETIME       DEFAULT NULL            COMMENT '出库时间',
    `expired_at`                DATETIME       DEFAULT NULL            COMMENT '超时失效时间',
    `created_at`                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_applicant_id` (`applicant_id`),
    INDEX `idx_approval_status` (`approval_status`),
    INDEX `idx_outbound_at` (`outbound_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申领单主表';

-- 申领单-资产明细表
DROP TABLE IF EXISTS `requisition_order_item`;
CREATE TABLE `requisition_order_item` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`            BIGINT       NOT NULL                COMMENT '申领单ID',
    `category_id`         BIGINT       NOT NULL                COMMENT '申领时选择的分类ID',
    `category_name`       VARCHAR(100) NOT NULL                COMMENT '冗余快照：申领时分类名称',
    `quantity`            INT          NOT NULL DEFAULT 1      COMMENT '申领数量',
    `asset_code`          VARCHAR(32)  DEFAULT NULL            COMMENT '冗余快照：出库时写入',
    `asset_name`          VARCHAR(200) DEFAULT NULL            COMMENT '冗余快照：出库时写入',
    `brand_model`         VARCHAR(200) DEFAULT NULL            COMMENT '冗余快照：出库时写入',
    `ledger_no`           VARCHAR(32)  DEFAULT NULL            COMMENT '冗余快照：出库时写入',
    `net_value_at_apply`  DECIMAL(12,2) DEFAULT NULL           COMMENT '冗余快照：出库时写入的现值',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申领单-资产明细表';

-- ============================================================
-- 七、业务单据 — 转移
-- ============================================================

-- 转移单主表
DROP TABLE IF EXISTS `transfer_order`;
CREATE TABLE `transfer_order` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`                  VARCHAR(32)    NOT NULL                COMMENT '单号 TRF-YYYYMMDD-NNNNNN',
    `from_user_id`              BIGINT         NOT NULL                COMMENT '转出人ID',
    `from_user_name`            VARCHAR(50)    NOT NULL                COMMENT '冗余快照：转出人姓名',
    `from_user_employee_no`     VARCHAR(50)    NOT NULL                COMMENT '冗余快照：转出人工号',
    `from_user_department_id`   BIGINT         NOT NULL                COMMENT '冗余快照：转出部门ID',
    `from_user_department_name` VARCHAR(100)   NOT NULL                COMMENT '冗余快照：转出部门名称',
    `to_user_id`                BIGINT         NOT NULL                COMMENT '转入人ID',
    `to_user_name`              VARCHAR(50)    NOT NULL                COMMENT '冗余快照：转入人姓名',
    `to_user_employee_no`       VARCHAR(50)    NOT NULL                COMMENT '冗余快照：转入人工号',
    `to_user_department_id`     BIGINT         NOT NULL                COMMENT '冗余快照：转入部门ID',
    `to_user_department_name`   VARCHAR(100)   NOT NULL                COMMENT '冗余快照：转入部门名称',
    `reason`                    TEXT           NOT NULL                COMMENT '转移原因',
    `transfer_date`             DATE           NOT NULL                COMMENT '转移日期',
    `approval_status`           VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `approval_remark`           TEXT           DEFAULT NULL            COMMENT '审批意见',
    `approver_name`             VARCHAR(50)    DEFAULT NULL            COMMENT '冗余快照：审批人姓名',
    `approved_at`               DATETIME       DEFAULT NULL            COMMENT '审批通过时间',
    `created_at`                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_from_user_id` (`from_user_id`),
    INDEX `idx_to_user_id` (`to_user_id`),
    INDEX `idx_approval_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转移单主表';

-- 转移单-资产明细表
DROP TABLE IF EXISTS `transfer_order_item`;
CREATE TABLE `transfer_order_item` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`            BIGINT       NOT NULL                COMMENT '转移单ID',
    `asset_id`            BIGINT       NOT NULL                COMMENT '资产ID',
    `asset_code`          VARCHAR(32)  NOT NULL                COMMENT '冗余快照：资产条码',
    `asset_name`          VARCHAR(200) NOT NULL                COMMENT '冗余快照：资产名称',
    `category_name`       VARCHAR(100) NOT NULL                COMMENT '冗余快照：资产分类名称',
    `brand_model`         VARCHAR(200) NOT NULL                COMMENT '冗余快照：品牌/型号',
    `net_value_at_transfer` DECIMAL(12,2) NOT NULL             COMMENT '冗余快照：转移时资产现值',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_asset` (`order_id`, `asset_id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转移单-资产明细表';

-- ============================================================
-- 八、业务单据 — 归还
-- ============================================================

-- 归还单主表
DROP TABLE IF EXISTS `return_order`;
CREATE TABLE `return_order` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`                  VARCHAR(32)    NOT NULL                COMMENT '单号 RET-YYYYMMDD-NNNNNN',
    `return_user_id`            BIGINT         NOT NULL                COMMENT '归还人ID',
    `return_user_name`          VARCHAR(50)    NOT NULL                COMMENT '冗余快照：归还人姓名',
    `return_user_employee_no`   VARCHAR(50)    NOT NULL                COMMENT '冗余快照：归还人工号',
    `return_user_department_id` BIGINT         NOT NULL                COMMENT '冗余快照：归还人部门ID',
    `return_user_department_name` VARCHAR(100) NOT NULL                COMMENT '冗余快照：归还人部门名称',
    `reason`                    TEXT           NOT NULL                COMMENT '归还原因',
    `expected_return_date`      DATE           NOT NULL                COMMENT '预计归还日期',
    `receiver_name`             VARCHAR(50)    DEFAULT NULL            COMMENT '冗余快照：接收仓管员姓名',
    `received_at`               DATETIME       DEFAULT NULL            COMMENT '入库确认时间',
    `approval_status`           VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `approval_remark`           TEXT           DEFAULT NULL            COMMENT '审批意见',
    `approver_name`             VARCHAR(50)    DEFAULT NULL            COMMENT '冗余快照：审批人姓名',
    `approved_at`               DATETIME       DEFAULT NULL            COMMENT '审批通过时间',
    `created_at`                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_return_user_id` (`return_user_id`),
    INDEX `idx_approval_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归还单主表';

-- 归还单-资产明细表
DROP TABLE IF EXISTS `return_order_item`;
CREATE TABLE `return_order_item` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`            BIGINT       NOT NULL                COMMENT '归还单ID',
    `asset_id`            BIGINT       NOT NULL                COMMENT '资产ID',
    `asset_code`          VARCHAR(32)  NOT NULL                COMMENT '冗余快照：资产条码',
    `asset_name`          VARCHAR(200) NOT NULL                COMMENT '冗余快照：资产名称',
    `category_name`       VARCHAR(100) NOT NULL                COMMENT '冗余快照：资产分类名称',
    `asset_condition`     VARCHAR(20)  NOT NULL                COMMENT '归还状况 NORMAL/MINOR_DAMAGE/MAJOR_DAMAGE',
    `condition_remark`    TEXT         DEFAULT NULL            COMMENT '归还时资产状况文字描述',
    `net_value_at_return` DECIMAL(12,2) NOT NULL               COMMENT '冗余快照：归还时资产现值',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_asset` (`order_id`, `asset_id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归还单-资产明细表';

-- ============================================================
-- 九、业务单据 — 报废
-- ============================================================

-- 报废单主表
DROP TABLE IF EXISTS `scrap_order`;
CREATE TABLE `scrap_order` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`                  VARCHAR(32)    NOT NULL                COMMENT '单号 SCR-YYYYMMDD-NNNNNN',
    `applicant_id`              BIGINT         NOT NULL                COMMENT '申请人ID',
    `applicant_name`            VARCHAR(50)    NOT NULL                COMMENT '冗余快照：申请人姓名',
    `applicant_employee_no`     VARCHAR(50)    NOT NULL                COMMENT '冗余快照：申请人工号',
    `applicant_department_id`   BIGINT         NOT NULL                COMMENT '冗余快照：申请人部门ID',
    `applicant_department_name` VARCHAR(100)   NOT NULL                COMMENT '冗余快照：申请人部门名称',
    `reason`                    TEXT           NOT NULL                COMMENT '报废原因',
    `estimated_residual_value`  DECIMAL(12,2)  DEFAULT NULL            COMMENT '估计残值',
    `approval_status`           VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `approval_remark`           TEXT           DEFAULT NULL            COMMENT '审批意见',
    `approver_name`             VARCHAR(50)    DEFAULT NULL            COMMENT '冗余快照：审批人姓名',
    `approved_at`               DATETIME       DEFAULT NULL            COMMENT '审批通过时间',
    `created_at`                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_applicant_id` (`applicant_id`),
    INDEX `idx_approval_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报废单主表';

-- 报废单-资产明细表
DROP TABLE IF EXISTS `scrap_order_item`;
CREATE TABLE `scrap_order_item` (
    `id`                         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`                   BIGINT       NOT NULL                COMMENT '报废单ID',
    `asset_id`                   BIGINT       NOT NULL                COMMENT '资产ID',
    `asset_code`                 VARCHAR(32)  NOT NULL                COMMENT '冗余快照：资产条码',
    `asset_name`                 VARCHAR(200) NOT NULL                COMMENT '冗余快照：资产名称',
    `category_name`              VARCHAR(100) NOT NULL                COMMENT '冗余快照：资产分类名称',
    `ledger_no`                  VARCHAR(32)  NOT NULL                COMMENT '冗余快照：台账编号',
    `original_value_at_scrap`    DECIMAL(12,2) NOT NULL               COMMENT '冗余快照：报废时资产原值',
    `net_value_at_scrap`         DECIMAL(12,2) NOT NULL               COMMENT '冗余快照：报废时资产现值',
    `accumulated_depreciation_at_scrap` DECIMAL(12,2) NOT NULL        COMMENT '冗余快照：报废时累计折旧',
    `created_at`                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_asset` (`order_id`, `asset_id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报废单-资产明细表';

-- ============================================================
-- 十、盘亏处理
-- ============================================================

-- 盘亏单表
DROP TABLE IF EXISTS `loss_order`;
CREATE TABLE `loss_order` (
    `id`                          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`                    VARCHAR(32)    NOT NULL                COMMENT '单号 LOSS-YYYYMMDD-NNNNNN',
    `inventory_task_id`           BIGINT         NOT NULL                COMMENT '关联盘点任务ID',
    `asset_id`                    BIGINT         NOT NULL                COMMENT '盘亏资产ID',
    `asset_code`                  VARCHAR(32)    NOT NULL                COMMENT '冗余快照：资产条码',
    `asset_name`                  VARCHAR(200)   NOT NULL                COMMENT '冗余快照：资产名称',
    `category_name`               VARCHAR(100)   NOT NULL                COMMENT '冗余快照：资产分类名称',
    `ledger_no`                   VARCHAR(32)    NOT NULL                COMMENT '冗余快照：台账编号',
    `responsible_user_id`         BIGINT         NOT NULL                COMMENT '冗余快照：责任人ID（当前使用人）',
    `responsible_user_name`       VARCHAR(50)    NOT NULL                COMMENT '冗余快照：责任人姓名',
    `responsible_user_employee_no`VARCHAR(50)    NOT NULL                COMMENT '冗余快照：责任人工号',
    `responsible_department_id`   BIGINT         NOT NULL                COMMENT '冗余快照：责任部门ID',
    `responsible_department_name` VARCHAR(100)   NOT NULL                COMMENT '冗余快照：责任部门名称',
    `suggested_compensation`      DECIMAL(12,2)  NOT NULL                COMMENT '建议赔偿金额（按资产现值）',
    `actual_compensation`         DECIMAL(12,2)  DEFAULT NULL            COMMENT '实际赔付金额',
    `compensation_status`         VARCHAR(30)    NOT NULL DEFAULT 'PENDING_COMPENSATION' COMMENT 'PENDING_COMPENSATION/COMPENSATED/EXEMPTING/EXEMPTED',
    `exemption_reason`            TEXT           DEFAULT NULL            COMMENT '豁免原因',
    `handler_id`                  BIGINT         DEFAULT NULL            COMMENT '处理人ID',
    `handling_remark`             TEXT           DEFAULT NULL            COMMENT '处理意见',
    `created_at`                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_inventory_task_id` (`inventory_task_id`),
    INDEX `idx_asset_id` (`asset_id`),
    INDEX `idx_responsible_user_id` (`responsible_user_id`),
    INDEX `idx_compensation_status` (`compensation_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘亏单表';

-- ============================================================
-- 十一、盘点管理
-- ============================================================

-- 盘点任务表
DROP TABLE IF EXISTS `inventory_task`;
CREATE TABLE `inventory_task` (
    `id`                    BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_name`             VARCHAR(200)   NOT NULL                COMMENT '盘点名称',
    `scope_type`            VARCHAR(20)    NOT NULL                COMMENT '范围类型 ALL/DEPARTMENT/CATEGORY',
    `scope_value`           JSON           DEFAULT NULL            COMMENT '范围值 JSON数组',
    `asset_status_filter`   JSON           NOT NULL                COMMENT '纳入盘点的资产状态列表 JSON数组',
    `deadline`              DATE           NOT NULL                COMMENT '截止日期',
    `responsible_user_id`   BIGINT         NOT NULL                COMMENT '负责人ID',
    `status`                VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/IN_PROGRESS/COMPLETED',
    `is_deleted`            TINYINT        NOT NULL DEFAULT 0      COMMENT '软删除标记 0-正常 1-已删除',
    `created_at`            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_responsible_user_id` (`responsible_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点任务表';

-- 盘点明细表
DROP TABLE IF EXISTS `inventory_detail`;
CREATE TABLE `inventory_detail` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`             BIGINT       NOT NULL                COMMENT '盘点任务ID',
    `asset_id`            BIGINT       DEFAULT NULL            COMMENT '资产ID（盘盈时为NULL）',
    `result`              VARCHAR(20)  NOT NULL                COMMENT 'LOSS/GAIN/MISMATCH/NORMAL',
    `found_asset_name`    VARCHAR(200) DEFAULT NULL            COMMENT '盘盈：实物资产名称',
    `found_asset_category` VARCHAR(100) DEFAULT NULL           COMMENT '盘盈：实物资产分类',
    `found_asset_location` VARCHAR(200) DEFAULT NULL           COMMENT '盘盈：实物存放地点',
    `found_asset_code`    VARCHAR(32)  DEFAULT NULL            COMMENT '盘盈：实物上贴附的条码',
    `system_user_id`      BIGINT       DEFAULT NULL            COMMENT '系统中记录的使用人（盘盈时NULL）',
    `actual_user_id`      BIGINT       DEFAULT NULL            COMMENT '实际使用人（盘亏时NULL）',
    `actual_location`     VARCHAR(200) DEFAULT NULL            COMMENT '实际存放地点（盘亏时NULL）',
    `checker_id`          BIGINT       NOT NULL                COMMENT '盘点人ID',
    `checked_at`          DATETIME     DEFAULT NULL            COMMENT '盘点时间',
    `remark`              TEXT         DEFAULT NULL            COMMENT '备注',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_asset` (`task_id`, `asset_id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_asset_id` (`asset_id`),
    INDEX `idx_result` (`result`),
    INDEX `idx_checker_id` (`checker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表';

-- ============================================================
-- 十二、操作日志
-- ============================================================

-- 资产流转日志表
DROP TABLE IF EXISTS `asset_log`;
CREATE TABLE `asset_log` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `asset_id`          BIGINT         NOT NULL                COMMENT '资产ID',
    `operation_type`    VARCHAR(30)    NOT NULL                COMMENT 'REGISTER/APPLY/TRANSFER/RETURN/SCRAP/INVENTORY/DEPRECIATION/LOSS',
    `operator_id`       BIGINT         NOT NULL                COMMENT '操作人ID',
    `from_user_id`      BIGINT         DEFAULT NULL            COMMENT '变更前使用人ID',
    `to_user_id`        BIGINT         DEFAULT NULL            COMMENT '变更后使用人ID',
    `detail`            TEXT           DEFAULT NULL            COMMENT '变更详情 JSON',
    `created_at`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_asset_id` (`asset_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产流转日志表';

-- ============================================================
-- 十三、定时任务执行记录
-- ============================================================

-- 月度折旧执行记录表
DROP TABLE IF EXISTS `depreciation_run_log`;
CREATE TABLE `depreciation_run_log` (
    `id`                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `run_month`               VARCHAR(7)   NOT NULL                COMMENT '折旧月份 YYYY-MM',
    `processed_count`         INT          NOT NULL DEFAULT 0      COMMENT '处理条数',
    `skipped_count`           INT          NOT NULL DEFAULT 0      COMMENT '跳过条数（已报废/盘亏）',
    `total_monthly_depreciation` DECIMAL(14,2) DEFAULT 0.00        COMMENT '当月折旧总额',
    `started_at`              DATETIME     NOT NULL                COMMENT '开始时间',
    `completed_at`            DATETIME     DEFAULT NULL            COMMENT '完成时间',
    `status`                  VARCHAR(20)  NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/SUCCESS/FAILED',
    `error_message`           TEXT         DEFAULT NULL            COMMENT '错误信息',
    `created_at`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_run_month` (`run_month`),
    INDEX `idx_started_at` (`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度折旧执行记录表';

-- ============================================================
-- 十四、文件附件
-- ============================================================

-- 文件上传记录表
DROP TABLE IF EXISTS `file_attachment`;
CREATE TABLE `file_attachment` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_type`          VARCHAR(50)    NOT NULL                COMMENT '业务类型 SCRAP_PHOTO 等',
    `biz_id`            BIGINT         DEFAULT NULL            COMMENT '关联业务记录ID',
    `file_url`          VARCHAR(500)   NOT NULL                COMMENT '文件URL',
    `file_name`         VARCHAR(200)   NOT NULL                COMMENT '原始文件名',
    `file_size`         BIGINT         NOT NULL                COMMENT '文件大小（字节）',
    `content_type`      VARCHAR(100)   NOT NULL                COMMENT 'MIME类型',
    `uploaded_by`       BIGINT         NOT NULL                COMMENT '上传人ID',
    `created_at`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    INDEX `idx_biz` (`biz_type`, `biz_id`),
    INDEX `idx_uploaded_by` (`uploaded_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件附件表';

-- ============================================================
-- 恢复外键检查
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;
