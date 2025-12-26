package com.hx.mqtt.domain.req.warehouse;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户仓库权限分配请求
 */
@Data
public class UserWarehouseAssignReq {

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 仓库ID列表
     */
    private List<Long> warehouseIds;

    /**
     * 分配类型：1-追加，2-覆盖
     */
    @NotNull(message = "分配类型不能为空")
    private Integer assignType;
}
