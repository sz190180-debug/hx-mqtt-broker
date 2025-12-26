package com.hx.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hx.mqtt.domain.entity.MachineStationRegister;
import org.apache.ibatis.annotations.Mapper;

/**
 * 机台寄存器配置Mapper接口
 */
@Mapper
public interface MachineStationRegisterMapper extends BaseMapper<MachineStationRegister> {
}
