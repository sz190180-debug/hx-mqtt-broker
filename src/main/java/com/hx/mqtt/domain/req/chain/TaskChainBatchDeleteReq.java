package com.hx.mqtt.domain.req.chain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 任务链模板批量删除请求
 */
@Data
public class TaskChainBatchDeleteReq {

    /**
     * 用户任务链模板ID列表
     */
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
