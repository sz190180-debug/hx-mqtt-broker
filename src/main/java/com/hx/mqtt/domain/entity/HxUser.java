package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hx_user")
public class HxUser {

    @TableId(value = "hx_user_id", type = IdType.AUTO)
    private Long hxUserId;

    /**
     * 用户类型
     */
    private Integer userType;

    private String username;

    private String password;

    private String clientId;

    private String remark;

    private Date createdTime;

    private String createBy;
}