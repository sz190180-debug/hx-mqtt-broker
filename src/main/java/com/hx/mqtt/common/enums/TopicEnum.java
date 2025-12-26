package com.hx.mqtt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TopicEnum {
    ERROR_TOPIC("/error", "全局异常主题"),
    LOGIN("/req/user/login", "链接鉴权"),
    ONLINE_AMR("/rep/amr/onlineAmr/%s", "在线车辆"),
    TASK_ADD("/req/task/add", "任务下发"),
    TASK_TYPE_MODIFY("/req/task/type/modify", "任务完成类型修改"),
    TASK_TYPE_BROADCAST("/rep/task/type/broadcast/%s", "任务完成类型广播"),
    TASK_RESUME("/req/task/resume", "恢复车辆"),
    TASK_PAUSE("/req/task/pause", "暂停车辆"),
    TASK_CANCEL("/req/task/cancel", "取消任务"),
    PUSH_INFO("/rep/task/taskInfo", "回传任务链信息"),
    GET_TASK_CHAIN("/req/task/getTaskChainById", "查询任务链信息"),
    USER_TASK_CHAIN_TEMPLATE("/req/task/userTaskChainTemplate", "查询用户任务链模板"),
    USER_TASK_CHAIN_GROUP("/req/task/userTaskChainGroup", "查询用户任务链分组名"),
    TASK_TEMPLATE_SUBMIT("/req/taskChainTemplate/submit", "任务链模板下发"),
    TASK_TEMPLATE_BROADCAST("/rep/taskChainTemplate/broadcast", "任务链执行广播"),
    GET_TASK_CHAINS("/req/task/getTaskChainByIds", "批量查询任务链信息"),
    USER_AMR("/req/amr/userAmr", "查询用户车辆"),

    // ==================== 仓库管理相关 ====================
    WAREHOUSE_ADD("/req/warehouse/add", "添加仓库"),
    WAREHOUSE_UPDATE("/req/warehouse/update", "更新仓库"),
    WAREHOUSE_DELETE("/req/warehouse/delete", "删除仓库"),
    WAREHOUSE_PAGE("/req/warehouse/page", "分页查询仓库"),
    WAREHOUSE_DETAIL("/req/warehouse/detail", "获取仓库详情"),
    WAREHOUSE_ALL("/req/warehouse/all", "获取所有仓库"),

    // ==================== 库位列管理相关 ====================
    WAREHOUSE_COLUMN_ADD("/req/warehouse/column/add", "添加库位列"),
    WAREHOUSE_COLUMN_UPDATE("/req/warehouse/column/update", "更新库位列"),
    WAREHOUSE_COLUMN_DELETE("/req/warehouse/column/delete", "删除库位列"),
    WAREHOUSE_COLUMN_LIST("/req/warehouse/column/list", "获取仓库的库位列"),
    WAREHOUSE_COLUMN_DETAIL("/req/warehouse/column/detail", "获取库位列详情"),

    // ==================== 库位列点位关联管理相关 ====================
    WAREHOUSE_VERTEXES_ADD("/req/warehouse/vertexes/add", "添加库位列点位关联"),
    WAREHOUSE_VERTEXES_UPDATE("/req/warehouse/vertexes/update", "更新库位列点位关联"),
    WAREHOUSE_VERTEXES_DELETE("/req/warehouse/vertexes/delete", "删除库位列点位关联"),
    WAREHOUSE_VERTEXES_LIST("/req/warehouse/vertexes/list", "获取库位列的点位关联列表"),
    WAREHOUSE_VERTEXES_DETAIL("/req/warehouse/vertexes/detail", "获取点位关联详情"),

    // ==================== 批量操作相关 ====================
    WAREHOUSE_POSITION_BATCH_UPDATE("/req/warehouse/position/batchUpdate", "批量更新点位状态");

    private final String path;
    private final String msg;

    public static TopicEnum match(String topic) {
        String[] parts = topic.split("/"); // 按 "/" 分割成数组
        String suffix = "/" + String.join("/", Arrays.stream(parts).skip(3).toArray(String[]::new));

        for (TopicEnum topicEnum : TopicEnum.values()) {
            if (topicEnum.getPath().equals(suffix)) {
                return topicEnum;
            }
        }
        return null;
    }
}
