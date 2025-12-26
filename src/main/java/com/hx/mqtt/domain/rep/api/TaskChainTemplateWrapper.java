package com.hx.mqtt.domain.rep.api;

import lombok.Data;

import java.util.List;

@Data
public class TaskChainTemplateWrapper {
    private TaskChainTemplatePo taskChainTemplatePo;
    private List<TaskTemplatePo> taskTemplatePos;
}