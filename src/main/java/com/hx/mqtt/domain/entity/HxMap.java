package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hx_map") // 指定表名
public class HxMap {
    @TableId(type = IdType.INPUT) // 自增主键
    private Long mapId;

    private Date createdTime;

    private String createBy;
}