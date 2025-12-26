package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("amr")
public class Amr {
    @TableId(value = "amr_id", type = IdType.AUTO)
    private Long amrId;
    private String alias;
    private Integer status;
}