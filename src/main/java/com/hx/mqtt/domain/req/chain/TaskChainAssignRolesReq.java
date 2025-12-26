package com.hx.mqtt.domain.req.chain;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskChainAssignRolesReq extends BasePageReq {

    @NotEmpty(message = "任务链模板id不能为空")
    private List<Long> taskChainIds;

    @NotEmpty(message = "用户id不能为空")
    private List<Long> userIds;

    /**
     * 1-追加 2-覆盖
     */
    @NotNull(message = "添加类型不能为空")
    private Integer addType;

    /**
     * 仓库ID，根据任务链类型判断是否必填
     * 出库入库任务链必填，普通任务链不需要
     */
    private Long warehouseId;

    /**
     * 库位列ID，非必填
     */
    private Long columnId;
}
