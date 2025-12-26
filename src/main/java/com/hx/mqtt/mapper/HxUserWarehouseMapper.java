package com.hx.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hx.mqtt.domain.entity.HxUserWarehouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户仓库权限关联Mapper接口
 */
@Mapper
public interface HxUserWarehouseMapper extends BaseMapper<HxUserWarehouse> {
}
