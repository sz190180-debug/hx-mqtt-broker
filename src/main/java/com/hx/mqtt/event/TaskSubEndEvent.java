package com.hx.mqtt.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskSubEndEvent extends ApplicationEvent {

    private final String endPointCode;

    private final String taskType;

    @Builder
    public TaskSubEndEvent(Object source, String endPointCode, String taskType) {
        super(source);
        this.endPointCode = endPointCode;
        this.taskType = taskType;
    }
}
