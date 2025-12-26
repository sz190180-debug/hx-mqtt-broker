package com.hx.mqtt.domain.req.chain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class UpdateGroupNameReq {

    @NotEmpty(message = "id不能为空")
    private List<Long> idList;

    @NotBlank(message = "分组名不能为空")
    private String groupName;
}
