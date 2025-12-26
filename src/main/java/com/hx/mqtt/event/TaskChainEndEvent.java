package com.hx.mqtt.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskChainEndEvent extends ApplicationEvent {

    private final Integer status;
    private final Long taskChainId;
    private final Long amrId;
    private final Boolean isAutoDispatch;

    @Builder
    public TaskChainEndEvent(Object source, Integer status, Long taskChainId, Long amrId, Boolean isAutoDispatch) {
        super(source != null ? source : TaskChainEndEvent.class);
        this.status = status;
        this.taskChainId = taskChainId;
        this.amrId = amrId;
        this.isAutoDispatch = isAutoDispatch;
    }
}
