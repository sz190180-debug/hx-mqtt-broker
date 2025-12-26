package com.hx.mqtt.domain.rep.api;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 任务链信息主类
 */
@Data
public class TaskChain implements Serializable {

    private TaskChainPo taskChainPo;
    /**
     * 子任务列表
     */
    private List<TaskPo> taskPos;

    @Data
    public static class TaskChainPo implements Serializable {
        private Long id;

        private Integer status;
        /**
         * 区域ID
         */
        private Long areaId;

        /**
         * 任务链名称
         */
        private String name;

        /**
         * 任务创建时间 (格式："yyyy-MM-dd HH:mm:ss")
         */
        private Date createTime;

        /**
         * 车辆ID
         */
        private Long amrId;

        /**
         * 上游任务号
         */
        private String taskSeq;

        /**
         * 任务完成时间 (格式："yyyy-MM-dd HH:mm:ss")
         */
        private Date finishTime;
    }

    /**
     * 子任务信息类
     */
    @Data
    public static class TaskPo implements Serializable {
        /**
         * 任务类型
         */
        private String taskType;

        /**
         * 目标点编号
         */
        private String endPointCode;

        /**
         * 任务链ID
         */
        private Long taskChainId;

        /**
         * 地图ID
         */
        private Long mapId;

        /**
         * 子任务状态:
         * 0-未执行;
         * 1-正在执行;
         * 2-结束;
         * 3-取消;
         * 4-执行异常;
         * 5-跳过;
         * 6-暂停
         */
        private Integer status;

        /**
         * 子任务开始时间 (格式："yyyyMMdd HH:mm:ss")
         */
        private Date startTime;

        /**
         * 子任务结束时间 (格式："yyyyMMdd HH:mm:ss")
         */
        private Date finishTime;

        /**
         * 上下料属性: 0-上料, 1-下料
         */
        private Integer loading;
    }
}