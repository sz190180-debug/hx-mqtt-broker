package com.hx.mqtt.domain.req.warehouse;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量更新点位状态请求
 */
@Data
public class BatchUpdatePositionStatusReq {

    /**
     * 点位ID列表
     */
    @NotEmpty(message = "点位ID列表不能为空")
    private List<Long> positionIds;

    /**
     * 新状态 1-可用 2-占用 3-禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 修改原因
     */
    private String reason;

    /**
     * 更新模式：all-全部更新，available-仅更新可用，occupied-仅更新占用，disabled-仅更新禁用
     */
    private String updateMode = "all";
}
