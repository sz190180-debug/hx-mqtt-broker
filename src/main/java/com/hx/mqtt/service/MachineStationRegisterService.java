package com.hx.mqtt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.MachineStationRegister;
import com.hx.mqtt.domain.rep.machine.MachineStationRegisterRep;
import com.hx.mqtt.domain.req.machine.MachineStationRegisterAddReq;
import com.hx.mqtt.domain.req.machine.MachineStationRegisterQueryReq;
import com.hx.mqtt.domain.req.machine.MachineStationRegisterUpdateReq;

import java.util.List;

/**
 * 机台寄存器配置服务接口
 */
public interface MachineStationRegisterService extends IService<MachineStationRegister> {

    /**
     * 添加机台寄存器配置
     *
     * @param req 添加请求
     * @return 寄存器ID
     */
    Long addMachineStationRegister(MachineStationRegisterAddReq req);

    /**
     * 更新机台寄存器配置
     *
     * @param req 更新请求
     * @return 是否成功
     */
    Boolean updateMachineStationRegister(MachineStationRegisterUpdateReq req);

    /**
     * 删除机台寄存器配置
     *
     * @param registerId 寄存器ID
     * @return 是否成功
     */
    Boolean deleteMachineStationRegister(Long registerId);

    /**
     * 分页查询机台寄存器配置
     *
     * @param req 查询请求
     * @return 分页结果
     */
    IPage<MachineStationRegisterRep> pageMachineStationRegister(MachineStationRegisterQueryReq req);

    /**
     * 根据ID获取机台寄存器配置详情
     *
     * @param registerId 寄存器ID
     * @return 寄存器详情
     */
    MachineStationRegisterRep getMachineStationRegisterDetail(Long registerId);

    /**
     * 根据机台ID获取寄存器配置列表
     *
     * @param stationId 机台ID
     * @return 寄存器配置列表
     */
    List<MachineStationRegisterRep> getRegistersByStationId(Long stationId);

    /**
     * 根据点位编码获取寄存器配置
     *
     * @param vertexCode 点位编码
     * @return 寄存器配置列表
     */
    List<MachineStationRegisterRep> getRegistersByVertexCode(String vertexCode);

    /**
     * 获取所有启用的寄存器
     *
     * @param idList idList
     */
    List<MachineStationRegister> getAllEnableRegister(List<Long> idList);
}
