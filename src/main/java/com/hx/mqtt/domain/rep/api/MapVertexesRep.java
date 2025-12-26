package com.hx.mqtt.domain.rep.api;

import lombok.Data;

import java.util.List;

@Data
public class MapVertexesRep {
    private Integer code;
    private List<Vertex> vertexes;
    private String message;

    @Data
    public static class Vertex {
        private VertexPo vertexPo;
    }

    @Data
    public static class VertexPo {
        /**
         * 主键ID
         */
        private Long id;

        /**
         * 名称
         */
        private String code;

        /**
         * 别名
         */
        private String alias;

        /**
         * 描述信息
         */
        private String description;

        /**
         * X坐标
         */
        private Double x;

        /**
         * Y坐标
         */
        private Double y;

        /**
         * 角度（弧度制）
         */
        private Double theta;

        /**
         * 类型
         */
        private Integer type;

        /**
         * 上下料类型：0-上料，1-下料
         */
        private Integer loading;

        /**
         * 对接方向：
         * 0-左，1-右，2-前，3-后，
         * 4-左调整，5-右调整，6-后调整
         */
        private Integer dockingDirection;

        /**
         * 对接X距离
         */
        private Double dockingX;

        /**
         * 对接Y距离
         */
        private Double dockingY;

        /**
         * 对接角度
         */
        private Double dockingTheta;

        /**
         * 出站X距离
         */
        private Double outboundX;

        /**
         * 出站Y距离
         */
        private Double outboundY;

        /**
         * 出站角度
         */
        private Double outboundTheta;

        /**
         * 是否强制上下料：0-否，1-是
         */
        private Integer forceLoad;

        /**
         * 重定位阈值
         */
        private Double relocThreshold;

        /**
         * 是否禁止掉头：0-否，1-是
         */
        private Integer forbiddenBack;

        /**
         * 是否允许停靠：0-否，1-是
         */
        private Integer allowPark;

        /**
         * 扩展属性（JSON字符串格式）
         */
        private String extend;

        /**
         * 关联的地图ID
         */
        private Long mapId;
    }
}