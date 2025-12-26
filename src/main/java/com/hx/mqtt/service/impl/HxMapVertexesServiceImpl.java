package com.hx.mqtt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.mapper.HxMapVertexesMapper;
import com.hx.mqtt.service.HxMapVertexesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HxMapVertexesServiceImpl extends ServiceImpl<HxMapVertexesMapper, HxMapVertexes> implements HxMapVertexesService {

    @Override
    public HxMapVertexes getByCodeAlias(String codeAlias) {
        if (codeAlias == null) {
            return null;
        }

        LambdaQueryWrapper<HxMapVertexes> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(HxMapVertexes::getCodeAlias, codeAlias);

        return getOne(wrapper);
    }

    @Override
    public HxMapVertexes getByCode(String code) {
        if (code == null) {
            return null;
        }

        LambdaQueryWrapper<HxMapVertexes> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(HxMapVertexes::getCode, code);

        return getOne(wrapper);
    }

    @Override
    public List<HxMapVertexes> listBroadcast() {
        LambdaQueryWrapper<HxMapVertexes> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(HxMapVertexes::getIsBroadcast, 1);

        return list(wrapper);
    }
}
