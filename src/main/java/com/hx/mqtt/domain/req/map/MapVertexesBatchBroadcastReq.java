package com.hx.mqtt.domain.req.map;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量设置点位广播请求
 */
@Data
public class MapVertexesBatchBroadcastReq {

    /**
     * 点位ID列表
     */
    @NotEmpty(message = "点位ID列表不能为空")
    private List<Long> vertexIds;

    /**
     * 是否广播：0-否，1-是
     */
    @NotNull(message = "广播设置不能为空")
    private Integer isBroadcast;
}
