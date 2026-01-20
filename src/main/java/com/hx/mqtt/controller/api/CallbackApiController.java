package com.hx.mqtt.controller.api;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.req.api.TaskChainInfoReq;
import com.hx.mqtt.event.TaskChainEndEvent;
import com.hx.mqtt.event.TaskSubEndEvent;
import com.hx.mqtt.service.MessageDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/push")
public class CallbackApiController {

    private final MessageDispatchService messageDispatchService;
    private final ApplicationContext applicationContext;

    @PostMapping("/taskInfo")
    public String taskInfo(@RequestBody TaskChainInfoReq req) {
        String jsonString = JSONObject.toJSONString(req);
        log.info("taskInfo:{}", jsonString);
        String clientId = GlobalCache.TASK_CLIENT_ID_MAP.get(req.getTaskChainId());
        if (StrUtil.isNotBlank(clientId)) {
            messageDispatchService.sendClientMessage(TopicEnum.PUSH_INFO, clientId, req);
        } else {
            log.info("unknown task chain id:{},data:{}", req.getTaskChainId(), jsonString);
        }
        Integer status = req.getStatus();
        if (req.getAmrId() != null) {
            GlobalCache.TASK_ID_AMR_MAP.put(req.getTaskChainId(), req.getAmrId());
        }
        if (status != null) {
            if (status == 7 || status == 3) {
                applicationContext.publishEvent(TaskChainEndEvent.builder()
                        .source(this)
                        .status(status)
                        .taskChainId(req.getTaskChainId())
                        .amrId(req.getAmrId())
                        .isAutoDispatch(true)
                        .build());
            } else if (status == 2) {
                applicationContext.publishEvent(TaskSubEndEvent.builder()
                        .source(this)
                        .endPointCode(req.getEndPointCode())
                        .taskType(req.getTaskType())
                        .build());
            }
        }
        return JSONObject.of("receive", 1).toJSONString();
    }
}
