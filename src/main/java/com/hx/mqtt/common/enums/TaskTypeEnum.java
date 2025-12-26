package com.hx.mqtt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskTypeEnum {
    O0(0, "移动任务"),
    O1(1, "上料任务"),
    O2(2, "下料任务"),
    O3(3, "充电任务"),
    O5(4, "对接任务"),
    O10(5, "高位上料任务"),
    O11(6, "高位下料任务"),
    O12(7, "直接上料任务"),
    O13(8, "直接下料任务"),
    O250(9, "原点任务"),
    E1(10, "用户确认"),
    E2(11, "调度确认"),
    E3(12, "机械臂抓举"),
    E4(13, "机械臂放下"),
    ;

    private final Integer code;
    private final String msg;

    public static TaskTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskTypeEnum taskTypeEnum : TaskTypeEnum.values()) {
            if (taskTypeEnum.getCode().equals(code)) {
                return taskTypeEnum;
            }
        }
        return null;
    }

    public static Integer getTaskType(TaskTypeEnum typeEnum){
        if(typeEnum == null){
            return null;
        }
        if(typeEnum.equals(TaskTypeEnum.O1) || typeEnum.equals(TaskTypeEnum.O10)||typeEnum.equals(TaskTypeEnum.O12)){
            return 0;
        }
        if(typeEnum.equals(TaskTypeEnum.O2) || typeEnum.equals(TaskTypeEnum.O11)||typeEnum.equals(TaskTypeEnum.O13)){
            return 1;
        }
        return null;
    }
}
