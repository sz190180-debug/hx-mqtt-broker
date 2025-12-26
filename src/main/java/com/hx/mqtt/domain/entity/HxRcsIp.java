package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hx_rcs_ip") // 指定表名
public class HxRcsIp {
    @TableId(type = IdType.AUTO) // 自增主键
    private Long rcsIpId;

    private String ip;

    private String port;

    private String token;

    private String name;

    private Date createdTime;

    private String createBy;
}