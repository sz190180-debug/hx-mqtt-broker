package com.hx.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hx.mqtt.domain.entity.HxUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HxUserMapper extends BaseMapper<HxUser> {
}
