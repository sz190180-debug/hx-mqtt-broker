package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("amr_register_config")
public class AmrRegisterConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long amrId;

    private String ip;

    private Integer port;

    private Integer readAddress;

    private Integer writeAddress;

    private Date createTime;
}