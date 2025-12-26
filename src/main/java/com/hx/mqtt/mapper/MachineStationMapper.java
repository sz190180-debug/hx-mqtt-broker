package com.hx.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hx.mqtt.domain.entity.MachineStation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 机台管理Mapper接口
 */
@Mapper
public interface MachineStationMapper extends BaseMapper<MachineStation> {
}
