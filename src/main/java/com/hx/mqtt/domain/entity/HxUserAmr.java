package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hx_user_amr")
public class HxUserAmr {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hxUserId;
    private Long amrId;
    private String groupName;
}