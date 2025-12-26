package com.hx.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hx.mqtt.domain.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库Mapper接口
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
}
