package com.hx.mqtt.domain.req.user;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserTaskChainTemplateReq extends BasePageReq {

    @NotNull(message = "用户id不能为空")
    private Long hxUserId;

    private String groupName;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 别名
     */
    private String alias;

    /**
     * 排序字段，asc-升序，desc-降序，默认按sortOrder升序
     */
    private String sortOrder = "asc";
}
