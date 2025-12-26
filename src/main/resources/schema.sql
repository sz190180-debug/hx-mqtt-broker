-- schema.sql
DROP TABLE IF EXISTS `hx_user`;
CREATE TABLE `hx_user`
(
    hx_user_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_type    TINYINT,
    username     VARCHAR(255),
    password     VARCHAR(255),
    client_id    VARCHAR(255),
    remark       VARCHAR(255),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by    VARCHAR(255),
    CONSTRAINT uk_client_id UNIQUE (client_id)
);

DROP TABLE IF EXISTS `amr`;
CREATE TABLE `amr`
(
    amr_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alias  VARCHAR(255),
    status TINYINT
);

DROP TABLE IF EXISTS `hx_user_amr`;
CREATE TABLE `hx_user_amr`
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    hx_user_id BIGINT NOT NULL,
    amr_id     BIGINT NOT NULL,
    group_name VARCHAR(255)
);

DROP TABLE IF EXISTS `hx_user_task_chain_template`;
CREATE TABLE `hx_user_task_chain_template`
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    hx_user_id             BIGINT   NOT NULL,
    task_chain_template_id BIGINT   NOT NULL,
    last_task_chain_id     BIGINT,
    group_name             VARCHAR(255),
    warehouse_id           BIGINT COMMENT '仓库ID，必填',
    column_id              BIGINT COMMENT '库位列ID，非必填',
    sort_order             INT               DEFAULT 0 COMMENT '排序字段，数值越小排序越靠前',
    created_time           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);
CREATE INDEX idx_last_task_chain_id ON `hx_user_task_chain_template` (last_task_chain_id);

DROP TABLE IF EXISTS `task_chain_template`;
CREATE TABLE task_chain_template
(
    id          BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    alias       VARCHAR(255),
    amr_id      BIGINT,
    group_id    BIGINT,
    area_id     BIGINT       NOT NULL,
    priority    INT          NOT NULL DEFAULT 0,
    enable      BOOLEAN      NOT NULL DEFAULT true,
    repeat_flag BOOLEAN      NOT NULL DEFAULT false,
    create_time TIMESTAMP    NOT NULL,
    is_return   INT          NOT NULL DEFAULT 0,
    expend      VARCHAR(255),
    chain_type  TINYINT      NOT NULL DEFAULT 0
);

DROP TABLE IF EXISTS `task_template`;
CREATE TABLE task_template
(
    id                     BIGINT       NOT NULL,
    sequence               DOUBLE       NOT NULL,
    task_chain_template_id BIGINT       NOT NULL,
    end_point_code         VARCHAR(255) NOT NULL,
    map_id                 BIGINT       NOT NULL,
    action                 VARCHAR(50),
    extend                 TEXT,
    docking_direction      INT,
    docking_x              DOUBLE,
    docking_y              DOUBLE,
    loading                INT,
    task_type              VARCHAR(20)  NOT NULL
);

-- MySQL 特有的注释
-- ALTER TABLE task_template COMMENT '任务模板表';

DROP TABLE IF EXISTS `hx_rcs_ip`;
CREATE TABLE `hx_rcs_ip`
(
    rcs_ip_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip           VARCHAR(255) NOT NULL,
    port         VARCHAR(255) NOT NULL,
    token        VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by    VARCHAR(255)
);

DROP TABLE IF EXISTS `hx_map`;
CREATE TABLE `hx_map`
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    map_id       VARCHAR(255) NOT NULL COMMENT '地图标识',
    map_name     VARCHAR(255) NOT NULL COMMENT '地图名称',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by    VARCHAR(255)
);

DROP TABLE IF EXISTS `hx_map_vertexes`;
CREATE TABLE `hx_map_vertexes`
(
    id                BIGINT       NOT NULL,
    code              VARCHAR(255) NOT NULL,
    alias             VARCHAR(255) DEFAULT NULL,
    description       VARCHAR(255) DEFAULT NULL,
    x                 DOUBLE,
    y                 DOUBLE,
    theta             DOUBLE,
    type              INT,
    loading           INT,
    docking_direction INT,
    docking_x         DOUBLE,
    docking_y         DOUBLE,
    docking_theta     DOUBLE,
    outbound_x        DOUBLE,
    outbound_y        DOUBLE,
    outbound_theta    DOUBLE,
    force_load        INT,
    reloc_threshold   DOUBLE,
    forbidden_back    INT,
    allow_park        INT,
    extend            VARCHAR(2550),
    map_id            BIGINT,
    code_alias        VARCHAR(255) DEFAULT NULL,
    area_id           BIGINT,
    is_broadcast      INT          DEFAULT 0 COMMENT '是否广播：0-否，1-是',
    PRIMARY KEY (id),
    CONSTRAINT uk_code UNIQUE (code)
);

CREATE INDEX idx_vertex_map_id ON `hx_map_vertexes` (map_id);

-- 仓库管理相关表
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse`
(
    warehouse_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(255) NOT NULL COMMENT '仓库名称',
    description    VARCHAR(500) COMMENT '仓库描述'
);

DROP TABLE IF EXISTS `warehouse_column`;
CREATE TABLE `warehouse_column`
(
    column_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT       NOT NULL COMMENT '仓库ID',
    column_name  VARCHAR(255) NOT NULL COMMENT '库位列名称',
    column_order INT          NOT NULL DEFAULT 0 COMMENT '库位列排序'
);

DROP TABLE IF EXISTS `warehouse_column_vertexes`;
CREATE TABLE `warehouse_column_vertexes`
(
    position_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    column_id          BIGINT NOT NULL COMMENT '库位列ID',
    hx_map_vertexes_id BIGINT NOT NULL COMMENT '点位id',
    position_order     INT    NOT NULL DEFAULT 0 COMMENT '点位排序',
    status             INT    NOT NULL DEFAULT 1 COMMENT '状态：1-可用，2-占用，3-禁用'
);

-- 用户仓库权限关联表
DROP TABLE IF EXISTS `hx_user_warehouse`;
CREATE TABLE `hx_user_warehouse`
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    hx_user_id   BIGINT NOT NULL COMMENT '用户ID',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by    VARCHAR(255) COMMENT '创建人',
    UNIQUE KEY uk_user_warehouse (hx_user_id, warehouse_id)
);

-- 机台管理相关表
DROP TABLE IF EXISTS `machine_station`;
CREATE TABLE `machine_station`
(
    station_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_name           VARCHAR(255) NOT NULL COMMENT '机台名称',
    device_type            INT          NOT NULL DEFAULT 1 COMMENT '设备类型 1-机械手',
    map_id                 BIGINT COMMENT '所在地图ID',
    area_id                BIGINT COMMENT '所在区域ID',
    description            VARCHAR(500) COMMENT '机台描述',
    task_chain_template_id BIGINT COMMENT '绑定的入库任务链模板ID',
    first_address_id       BIGINT COMMENT '通信首地址',
    ip_addr                VARCHAR(255) COMMENT 'IP地址',
    port                   INT COMMENT '端口',
    status                 INT          NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_time           TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time           TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by              VARCHAR(255) COMMENT '创建人'
);

-- 机台寄存器配置表（包含协议信息）
DROP TABLE IF EXISTS `machine_station_register`;
CREATE TABLE `machine_station_register`
(
    register_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_id       BIGINT       NOT NULL COMMENT '机台ID',
    register_type    INT          NOT NULL COMMENT '寄存器类型：CONTROL, REQUEST_ENTER, ALLOW_ENTER, AGV_EXIT',
    register_address INT          NOT NULL COMMENT '寄存器地址',
    control_type     INT                   DEFAULT 0 COMMENT '机台控制寄存器类型 0-无动作 1-右叫车 2-右叫料 3-左叫车 4-左叫料',
    control_value    INT                   DEFAULT 0 COMMENT '控制值',
    data_type        VARCHAR(20)           DEFAULT 'UINT16' COMMENT '数据类型',
    protocol_type    VARCHAR(50)  NOT NULL DEFAULT 'MODBUS_TCP' COMMENT '协议类型：MODBUS_TCP',
    description      VARCHAR(255) COMMENT '寄存器描述',
    status           INT          NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    vertex_id        BIGINT       NULL COMMENT '点位ID',
    vertex_code      VARCHAR(255) NULL COMMENT '点位编码',
    call_vertex_id   BIGINT       NULL COMMENT '叫料点位ID',
    call_vertex_code VARCHAR(255) NULL COMMENT '叫料点位编码',
    created_time     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);
