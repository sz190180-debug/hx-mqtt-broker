package com.hx.mqtt.domain.rep.mqtt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskAddRep {

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 任务模板id
     */
    private Long taskTemplateId;
}
