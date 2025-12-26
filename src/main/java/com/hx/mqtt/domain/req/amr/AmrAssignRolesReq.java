package com.hx.mqtt.domain.req.amr;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AmrAssignRolesReq extends BasePageReq {

    @NotEmpty(message = "车辆id不能为空")
    private List<Long> amrIds;

    @NotEmpty(message = "用户id不能为空")
    private List<Long> userIds;

    /**
     * 1-追加 2-覆盖
     */
    @NotNull(message = "添加类型不能为空")
    private Integer addType;
}
