package com.hx.mqtt.domain.rep.api;

import lombok.Data;

import java.util.List;

@Data
public class AmrData {

    private AmrException amrException;
    private Long areaId;
    private Double batteryPercentile;
    private Coordinate coordinate;
    private Materials materials;
    private String id;
    private String ip;
    private Long mapId;
    private String mapName;
    private String name;
    private String state;
    private Long stateId;
    private Long taskChainId;
    private Long taskId;

    @Data
    public static class AmrException {
        private Long level;
        private List<String> exception;
    }

    @Data
    public static class Coordinate {
        private Double x;
        private Double y;
        private Double theta;
    }

    @Data
    public static class Materials {
        private List<MaterialItem> materials;
        private boolean present;

        @Data
        public static class MaterialItem {
            private List<String> code;
            private String position;
            private String info;
        }
    }
}