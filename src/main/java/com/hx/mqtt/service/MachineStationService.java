package com.hx.mqtt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.MachineStation;
import com.hx.mqtt.domain.rep.machine.MachineStationRep;
import com.hx.mqtt.domain.req.machine.MachineStationAddReq;
import com.hx.mqtt.domain.req.machine.MachineStationQueryReq;
import com.hx.mqtt.domain.req.machine.MachineStationUpdateReq;

import java.util.List;

/**
 * 机台管理服务接口
 */
public interface MachineStationService extends IService<MachineStation> {

    /**
     * 添加机台
     *
     * @param req 添加请求
     * @return 机台ID
     */
    Long addMachineStation(MachineStationAddReq req);

    /**
     * 更新机台
     *
     * @param req 更新请求
     * @return 是否成功
     */
    Boolean updateMachineStation(MachineStationUpdateReq req);

    /**
     * 删除机台
     *
     * @param stationId 机台ID
     * @return 是否成功
     */
    Boolean deleteMachineStation(Long stationId);

    /**
     * 分页查询机台
     *
     * @param req 查询请求
     * @return 分页结果
     */
    IPage<MachineStationRep> pageMachineStation(MachineStationQueryReq req);

    /**
     * 根据ID获取机台详情
     *
     * @param stationId 机台ID
     * @return 机台详情
     */
    MachineStationRep getMachineStationDetail(Long stationId);

    /**
     * 获取所有机台
     *
     * @return 机台列表
     */
    List<MachineStationRep> getAllMachineStations();

    /**
     * 根据地图ID获取机台列表
     *
     * @param mapId 地图ID
     * @return 机台列表
     */
    List<MachineStationRep> getMachineStationsByMapId(Long mapId);

    /**
     * 根据区域ID获取机台列表
     *
     * @param areaId 区域ID
     * @return 机台列表
     */
    List<MachineStationRep> getMachineStationsByAreaId(Long areaId);

    /**
     * 获取所有启用的机台
     *
     * @return 启用的机台列表
     */
    List<MachineStation> getAllEnabledStations();
}
