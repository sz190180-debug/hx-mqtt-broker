package com.hx.mqtt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.HxMapVertexes;

import java.util.List;

public interface HxMapVertexesService extends IService<HxMapVertexes> {

    /**
     * 根据codeAlias查询地图点位
     *
     * @param codeAlias 点位编码别名
     * @return 地图点位信息
     */
    HxMapVertexes getByCodeAlias(String codeAlias);

    /**
     * 根据codeAlias查询地图点位
     *
     * @param code code
     * @return 地图点位信息
     */
    HxMapVertexes getByCode(String code);

    List<HxMapVertexes> listBroadcast();
}
