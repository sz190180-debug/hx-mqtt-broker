package com.hx.mqtt.domain.req.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户任务链模板排序请求
 */
@Data
public class UserTaskChainTemplateSortReq {

    /**
     * 用户任务链模板ID
     */
    @NotNull(message = "用户任务链模板ID不能为空")
    private Long id;

    /**
     * 排序值，数值越小排序越靠前
     */
    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;
}
