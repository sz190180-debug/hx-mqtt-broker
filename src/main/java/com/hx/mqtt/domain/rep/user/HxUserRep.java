package com.hx.mqtt.domain.rep.user;

import lombok.Data;

import java.util.Date;

@Data
public class HxUserRep {

    private Long hxUserId;

    /**
     * 用户类型
     */
    private Integer userType;

    private String username;

    private String password;

    private String clientId;

    private Boolean isOnline;

    private String remark;

    private Date createdTime;

    private String createBy;
}
