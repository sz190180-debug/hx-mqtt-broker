package com.hx.mqtt.controller.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.domain.entity.HxMap;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.domain.rep.api.MapVertexesRep;
import com.hx.mqtt.domain.req.map.*;
import com.hx.mqtt.service.HxMapService;
import com.hx.mqtt.service.HxMapVertexesService;
import com.hx.mqtt.service.RcsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapApiController {

    private final HxMapService hxMapService;
    private final HxMapVertexesService hxMapVertexesService;
    private final RcsApiService rcsApiService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public HttpResp<Void> add(@Valid @RequestBody MapAddReq req) {
        HxMap hxMap = new HxMap();
        BeanUtils.copyProperties(req, hxMap);
        hxMap.setCreatedTime(new Date());
        hxMapService.saveOrUpdate(hxMap);
        MapVertexesRep mapVertexes = rcsApiService.getMapVertexes(hxMap.getMapId());
        if (mapVertexes == null) {
            throw new RuntimeException("未能找到地图点位相关信息");
        }
        List<MapVertexesRep.Vertex> vertexes = mapVertexes.getVertexes();
        if (CollectionUtil.isNotEmpty(vertexes)) {
            LambdaQueryWrapper<HxMapVertexes> lq = Wrappers.lambdaQuery();
            hxMapVertexesService.remove(lq.eq(HxMapVertexes::getMapId, hxMap.getMapId()));
            List<HxMapVertexes> collect = vertexes.stream().map(v -> {
                HxMapVertexes hxMapVertexes = new HxMapVertexes();
                BeanUtils.copyProperties(v.getVertexPo(), hxMapVertexes);
                return hxMapVertexes;
            }).collect(Collectors.toList());
            hxMapVertexesService.saveBatch(collect);
        }
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    public HttpResp<Void> update(@Valid @RequestBody MapAddReq req) {
        MapVertexesRep mapVertexes = rcsApiService.getMapVertexes(req.getMapId());
        if (mapVertexes == null) {
            throw new RuntimeException("未能找到地图点位相关信息");
        }
        List<MapVertexesRep.Vertex> vertexes = mapVertexes.getVertexes();
        if (CollectionUtil.isNotEmpty(vertexes)) {
            LambdaQueryWrapper<HxMapVertexes> lq = Wrappers.lambdaQuery();
            lq.eq(HxMapVertexes::getMapId, req.getMapId());
            lq.notIn(HxMapVertexes::getId,
                    vertexes.stream()
                            .map(MapVertexesRep.Vertex::getVertexPo)
                            .map(MapVertexesRep.VertexPo::getId)
                            .toArray());
            hxMapVertexesService.remove(lq);
            List<HxMapVertexes> collect = vertexes.stream().map(v -> {
                HxMapVertexes hxMapVertexes = new HxMapVertexes();
                BeanUtils.copyProperties(v.getVertexPo(), hxMapVertexes);
                return hxMapVertexes;
            }).collect(Collectors.toList());
            hxMapVertexesService.saveOrUpdateBatch(collect);
        }
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delete")
    public HttpResp<Void> delete(@Valid @RequestBody MapDeleteReq req) {
        hxMapService.removeById(req.getMapId());
        LambdaQueryWrapper<HxMapVertexes> lq = Wrappers.lambdaQuery();
        hxMapVertexesService.remove(lq.eq(HxMapVertexes::getMapId, req.getMapId()));
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/updateAreaId")
    public HttpResp<?> updateCodeAlias(@Valid @RequestBody MapVertexesUpdateAreaReq req) {
        LambdaQueryWrapper<HxMapVertexes> lq = Wrappers.lambdaQuery();
        lq.eq(HxMapVertexes::getMapId, req.getMapId());
        List<HxMapVertexes> list = hxMapVertexesService.list(lq);
        if (CollectionUtil.isEmpty(list)) {
            return HttpResp.success();
        }
        list.forEach(v -> v.setAreaId(req.getAreaId()));
        hxMapVertexesService.saveOrUpdateBatch(list);
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/updateCodeAlias")
    public HttpResp<?> updateCodeAlias(@Valid @RequestBody MapVertexesUpdateAliasReq req) {
        LambdaQueryWrapper<HxMapVertexes> lq = Wrappers.lambdaQuery();
        lq.eq(HxMapVertexes::getCodeAlias, req.getCodeAlias());
        lq.ne(HxMapVertexes::getId, req.getId());
        List<HxMapVertexes> list = hxMapVertexesService.list(lq);
        if (CollectionUtil.isNotEmpty(list)) {
            return HttpResp.fail("点位编码别名重复");
        }

        HxMapVertexes hxMapVertexes = new HxMapVertexes();
        BeanUtils.copyProperties(req, hxMapVertexes);
        hxMapVertexesService.updateById(hxMapVertexes);
        return HttpResp.success();
    }

    @GetMapping("/select")
    public HttpResp<List<Long>> select() {
        List<Long> mapIds = hxMapService.list(Wrappers.lambdaQuery())
                .stream()
                .map(HxMap::getMapId)
                .collect(Collectors.toList());
        return HttpResp.success(mapIds);
    }

    @PostMapping("/selectVertexes")
    public HttpResp<Page<HxMapVertexes>> select(@Valid @RequestBody MapVertexesPageReq req) {
        // 构建分页参数（页码从1开始）
        Page<HxMapVertexes> page = new Page<>(req.getPageNum(), req.getPageSize());

        LambdaQueryWrapper<HxMapVertexes> lq = Wrappers.lambdaQuery();
        lq.eq(HxMapVertexes::getMapId, req.getMapId());
        lq.like(StrUtil.isNotBlank(req.getCode()), HxMapVertexes::getCode, req.getCode());
        return HttpResp.success(hxMapVertexesService.page(page, lq));
    }

    @GetMapping("/vertexes/all")
    public HttpResp<List<HxMapVertexes>> getAllVertexes() {
        List<HxMapVertexes> vertexes = hxMapVertexesService.list();
        return HttpResp.success(vertexes);
    }

    @PostMapping("/vertexes/batchBroadcast")
    public HttpResp<Void> batchUpdateBroadcast(@Valid @RequestBody MapVertexesBatchBroadcastReq req) {
        // 批量更新点位的广播设置
        LambdaUpdateWrapper<HxMapVertexes> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.in(HxMapVertexes::getId, req.getVertexIds());
        updateWrapper.set(HxMapVertexes::getIsBroadcast, req.getIsBroadcast());

        boolean success = hxMapVertexesService.update(updateWrapper);
        if (success) {
            return HttpResp.success();
        } else {
            throw new RuntimeException("批量更新广播设置失败");
        }
    }
}
