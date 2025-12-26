package com.hx.mqtt.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.domain.dto.AmrDataDto;
import com.hx.mqtt.domain.entity.Amr;
import com.hx.mqtt.domain.entity.HxUserAmr;
import com.hx.mqtt.domain.rep.user.UserAmrRep;
import com.hx.mqtt.domain.req.user.UserAmrReq;
import com.hx.mqtt.mapper.AmrMapper;
import com.hx.mqtt.mapper.HxUserAmrMapper;
import com.hx.mqtt.service.HxUserAmrService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HxUserAmrServiceImpl extends ServiceImpl<HxUserAmrMapper, HxUserAmr> implements HxUserAmrService {

    private final AmrMapper amrMapper;

    @Override
    public IPage<UserAmrRep> selectAmr(UserAmrReq req) {
        Page<HxUserAmr> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<HxUserAmr> lq = Wrappers.lambdaQuery();
        lq.eq(HxUserAmr::getHxUserId, req.getHxUserId());
        Page<HxUserAmr> pageResult = this.page(page, lq);
        Set<Long> collect = pageResult.getRecords().stream().map(HxUserAmr::getAmrId).collect(Collectors.toSet());
        Map<Long, Amr> amrMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(collect)) {
            amrMap.putAll(amrMapper.selectBatchIds(collect).stream().collect(Collectors.toMap(Amr::getAmrId,
                    v -> v)));
        }
        return pageResult.convert(entity -> {
            UserAmrRep rep = new UserAmrRep();
            BeanUtils.copyProperties(entity, rep);
            Amr amr = amrMap.get(rep.getAmrId());
            if (amr != null) {
                rep.setStatus(amr.getStatus());
                rep.setAlias(amr.getAlias());
            }
            Map<Long, AmrDataDto> amrTableMap = GlobalCache.AMR_TABLE_MAP;
            if (amrTableMap.containsKey(entity.getAmrId())) {
                AmrDataDto amrData = amrTableMap.get(entity.getAmrId());
                rep.setBatteryPercentile(amrData.getBatteryPercentile());
                rep.setState(amrData.getState());
                rep.setMaterials(amrData.getMaterials());
            }
            return rep;
        });
    }
}