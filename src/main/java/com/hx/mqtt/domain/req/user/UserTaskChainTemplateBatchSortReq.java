package com.hx.mqtt.domain.req.user;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 用户任务链模板批量排序请求
 */
@Data
public class UserTaskChainTemplateBatchSortReq {

    /**
     * 排序项列表
     */
    @NotEmpty(message = "排序项列表不能为空")
    @Valid
    private List<SortItem> sortItems;

    @Data
    public static class SortItem {
        /**
         * 用户任务链模板ID
         */
        private Long id;

        /**
         * 排序值，数值越小排序越靠前
         */
        private Integer sortOrder;
    }
}
