package com.hx.mqtt.domain.req.api;

import lombok.Data;

import java.util.List;

/**
 * 任务链请求参数
 */
@Data
public class TaskChainAddReq {

    private TaskChain taskChain;

    /**
     * 任务列表（必填）
     */
    private List<TaskPo> tasks;

    @Data
    public static class TaskChain {
        /**
         * 是否回传（0否1是）
         * 长度：2
         */
        private Integer isReturn;

        /**
         * 区域ID（必填）
         * 长度：20
         */
        private Long areaId;

        /**
         * 任务优先级（0-99）
         * 长度：2
         */
        private Integer priority = 0;

        /**
         * 车辆ID
         * 长度：20
         */
        private Long amrId;

        /**
         * 分组ID
         * 长度：20
         */
        private Long groupId;

        /**
         * 上游任务号
         * 长度：40
         */
        private String taskSeq;
    }

    @Data
    public static class TaskPo {
        /**
         * 动作类型（必填）
         * 长度：10
         */
        private String taskType;

        /**
         * 地图ID（必填）
         * 长度：20
         */
        private Long mapId;

        /**
         * 目标点编号（必填）
         * 长度：255
         */
        private String endPointCode;

        /**
         * 扩展字段（JSON字符串）
         * 长度：255
         */
        private String extend;
    }
}