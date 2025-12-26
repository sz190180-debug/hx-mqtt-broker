package com.hx.mqtt.task;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.hx.mqtt.common.enums.StateEnum;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.dto.AmrDataDto;
import com.hx.mqtt.domain.dto.TaskTemplateBroadcastDto;
import com.hx.mqtt.domain.entity.*;
import com.hx.mqtt.domain.rep.api.AmrData;
import com.hx.mqtt.domain.rep.warehouse.WarehouseRep;
import com.hx.mqtt.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.hx.mqtt.common.GlobalCache.AMR_TABLE_MAP;
import static com.hx.mqtt.common.GlobalCache.TASK_ID_AMR_MAP;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalTask {

    private final RcsApiService rcsApiService;
    private final MessageDispatchService messageDispatchService;
    private final AmrService amrService;
    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;
    private final TaskChainTemplateService taskChainTemplateService;
    private final WarehouseColumnVertexesService warehouseColumnVertexesService;
    private final WarehouseColumnService warehouseColumnService;
    private final HxMapVertexesService hxMapVertexesService;

    // 每500毫秒执行一次（不受任务执行时间影响）
    @Scheduled(fixedRate = 500)
    public void getOnlineAmr() {
        try {
            List<AmrData> amrData = JSONArray.parseArray(JSONObject.toJSONString(rcsApiService.onlineAmr()),
                    AmrData.class);
            if (CollUtil.isEmpty(amrData)) {
                return;
            }
            Set<Long> onlineAmrIds =
                    amrData.stream().map(temp -> Long.valueOf(temp.getId())).collect(Collectors.toSet());
            AMR_TABLE_MAP.keySet().removeIf(id -> !onlineAmrIds.contains(id));
            Map<Long, AmrDataDto> result = amrData.stream().map(data -> {
                AmrDataDto dto = new AmrDataDto();
                AmrData.Coordinate coordinate = data.getCoordinate();
                dto.setId(Long.valueOf(data.getId()));
                if (coordinate != null) {
                    dto.setX(coordinate.getX());
                    dto.setY(coordinate.getY());
                    dto.setTheta(coordinate.getTheta());
                }
                StateEnum stateEnum = StateEnum.fromMsg(data.getState());
                if (stateEnum != null) {
                    dto.setState(stateEnum.getCode());
                } else {
                    dto.setState(0);
                }
                dto.setBatteryPercentile(data.getBatteryPercentile());
                if (data.getMaterials() == null) {
                    dto.setMaterials(false);
                } else {
                    dto.setMaterials(CollUtil.isNotEmpty(data.getMaterials().getMaterials()));
                }
                dto.setTaskChainId(data.getTaskChainId());
                dto.setName(data.getName());
                AMR_TABLE_MAP.put(Long.valueOf(data.getId()), dto);
                return dto;
            }).collect(Collectors.toMap(AmrDataDto::getId, v -> v));
            result.forEach((id, data) -> messageDispatchService.broadcastMessage(TopicEnum.ONLINE_AMR, data, id));
        } catch (Exception e) {
            log.error("getOnlineAmr error", e);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void updateAmrTable() {
        try {
            if (CollUtil.isEmpty(AMR_TABLE_MAP)) {
                return;
            }
            Map<Long, Amr> map = amrService.list().stream().collect(Collectors.toMap(Amr::getAmrId, v -> v));
            AMR_TABLE_MAP.keySet().forEach(id -> {
                AmrDataDto dto = AMR_TABLE_MAP.get(id);
                Amr amr = map.computeIfAbsent(id, k -> {
                    Amr newAmr = new Amr();
                    newAmr.setAmrId(id);
                    newAmr.setAlias(dto.getName());
                    return newAmr;
                });
                amr.setStatus(1);
            });
            map.forEach((id, amr) -> {
                if (!AMR_TABLE_MAP.containsKey(id)) {
                    amr.setStatus(0);
                }
            });
            amrService.saveOrUpdateBatch(map.values());
        } catch (Exception e) {
            log.error("updateAmrTable error", e);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void getFullOccupiedWarehouseIdsTask() {
        // 1. 获取所有满仓的ID (逻辑：总仓库 - 有空闲点位的仓库)
        // 这里的逻辑正是“不使用占用符号”，而是“排除空闲符号”
        Set<Long> fullWarehouseIds = getFullOccupiedWarehouseIds();

        // 2. 发送广播
        // Topic: GLOBAL_PATH + "rep/task/full/warehouse"
        // Payload: 满仓的ID集合
        if (fullWarehouseIds != null) {
            // 这里不需要传入 mapId 等 path 参数，因为这个 Topic 是全局的仓库状态
            messageDispatchService.broadcastMessage(TopicEnum.WAREHOUSE_FULL_BROADCAST, fullWarehouseIds);

            // 日志方便调试
            // log.debug("已广播满仓集合, 数量: {}", fullWarehouseIds.size());
        }
    }

    /**
     * 获取所有点位都被占用（即没有可用点位）的仓库ID集合
     *
     * @return 全满的仓库ID集合
     */
    public Set<Long> getFullOccupiedWarehouseIds() {
        // ---------------------------------------------------------------------
        // 步骤 1: 找出所有“空闲”的点位 (Status = 1: 可用)
        // ---------------------------------------------------------------------
        // 我们只关心 columnId，使用 listObjs 减少内存消耗
        List<Long> freeColumnIds = warehouseColumnVertexesService.listObjs(
                Wrappers.<WarehouseColumnVertexes>lambdaQuery()
                        .eq(WarehouseColumnVertexes::getStatus, 1) // 1-可用
                        .select(WarehouseColumnVertexes::getColumnId),
                obj -> (Long) obj
        );

        // ---------------------------------------------------------------------
        // 步骤 2: 找出这些“空闲点位”所属的仓库ID (即：未满的仓库)
        // ---------------------------------------------------------------------
        Set<Long> notFullWarehouseIds = new HashSet<>();
        if (!freeColumnIds.isEmpty()) {
            List<Long> warehouseIds = warehouseColumnService.listObjs(
                    Wrappers.<WarehouseColumn>lambdaQuery()
                            .in(WarehouseColumn::getColumnId, freeColumnIds)
                            .select(WarehouseColumn::getWarehouseId),
                    obj -> (Long) obj
            );
            notFullWarehouseIds.addAll(warehouseIds);
        }

        // ---------------------------------------------------------------------
        // 步骤 3: 找出所有配置了点位的仓库ID (总范围)
        // ---------------------------------------------------------------------
        // 这一步是为了确保我们只统计那些实际拥有列/点位的仓库，避免统计空仓库
        // 使用 groupBy 去重
        List<Long> allWarehouseIdsWithColumns = warehouseColumnService.listObjs(
                Wrappers.<WarehouseColumn>lambdaQuery()
                        .select(WarehouseColumn::getWarehouseId)
                        .groupBy(WarehouseColumn::getWarehouseId),
                obj -> (Long) obj
        );

        // ---------------------------------------------------------------------
        // 步骤 4: 排除法计算 (总仓库 - 未满仓库 = 全满仓库)
        // ---------------------------------------------------------------------
        Set<Long> fullWarehouseIds = new HashSet<>(allWarehouseIdsWithColumns);
        fullWarehouseIds.removeAll(notFullWarehouseIds);

        return fullWarehouseIds;
    }

    @Scheduled(fixedRate = 1000)
    public void sendBroadcast() {
        sendChain();
        sendVertexes();
    }

    private void sendVertexes() {
        try {
            // 查找库位管理表，获取所有库位点位关联信息
            List<WarehouseColumnVertexes> warehouseVertexes = warehouseColumnVertexesService.list();
            if (CollUtil.isEmpty(warehouseVertexes)) {
                log.info("库位管理表为空，跳过广播");
                return;
            }

            // 获取所有关联的地图点位ID
            Set<Long> mapVertexIds = warehouseVertexes.stream()
                    .map(WarehouseColumnVertexes::getHxMapVertexesId)
                    .collect(Collectors.toSet());

            // 查询对应的地图点位信息
            List<HxMapVertexes> mapVertexes =
                    hxMapVertexesService.listByIds(mapVertexIds).stream()
                            .filter(v -> Objects.equals(v.getIsBroadcast(), 1))
                            .collect(Collectors.toList());
            if (CollUtil.isEmpty(mapVertexes)) {
                log.info("点位表为空，跳过广播");
                return;
            }

            // 创建地图点位ID到点位信息的映射
            Map<Long, HxMapVertexes> mapVertexMap = mapVertexes.stream()
                    .collect(Collectors.toMap(HxMapVertexes::getId, v -> v));

            // 按地图ID分组处理广播数据
            Map<Long, Map<String, Integer>> mapGroupedBroadcastData = warehouseVertexes.stream()
                    .filter(wv -> {
                        HxMapVertexes mapVertex = mapVertexMap.get(wv.getHxMapVertexesId());
                        return mapVertex != null && mapVertex.getCodeAlias() != null && mapVertex.getMapId() != null;
                    })
                    .collect(Collectors.groupingBy(
                            wv -> mapVertexMap.get(wv.getHxMapVertexesId()).getMapId(),
                            Collectors.toMap(
                                    wv -> mapVertexMap.get(wv.getHxMapVertexesId()).getCodeAlias(),
                                    wv -> convertWarehouseStatus(wv.getStatus()),
                                    (existing, replacement) -> replacement // 如果有重复的codeAlias，使用后面的值
                            )
                    ));

            // 为每个地图ID单独发送广播消息
            mapGroupedBroadcastData.forEach((mapId, broadcastData) -> {
                if (!broadcastData.isEmpty()) {
                    log.debug("发送地图ID {} 的点位状态广播，包含 {} 个点位", mapId, broadcastData.size());
                    messageDispatchService.broadcastMessage(TopicEnum.TASK_TYPE_BROADCAST, broadcastData, mapId);
                }
            });
        } catch (Exception e) {
            log.warn("sendVertexes error", e);
        }
    }

    /**
     * 转换库位状态：状态1发0，状态2发1，其他状态发1
     *
     * @param warehouseStatus 库位状态：1-可用，2-占用，3-禁用
     * @return 转换后的状态：0或1
     */
    private Integer convertWarehouseStatus(Integer warehouseStatus) {
        if (warehouseStatus == null) {
            return 0;
        }
        switch (warehouseStatus) {
            case 1: // 可用 -> 发0
                return 0;
            case 2: // 占用 -> 发1
                return 1;
            default: // 禁用或其他 -> 发1
                return 1;
        }
    }

    private void sendChain() {
        try {
            if (CollUtil.isEmpty(TASK_ID_AMR_MAP)) {
                return;
            }
            LambdaQueryWrapper<HxUserTaskChainTemplate> lq = Wrappers.lambdaQuery();
            lq.in(HxUserTaskChainTemplate::getLastTaskChainId, TASK_ID_AMR_MAP.keySet());
            List<HxUserTaskChainTemplate> list = hxUserTaskChainTemplateService.list(lq);
            if (CollUtil.isEmpty(list)) {
                return;
            }
            LambdaQueryWrapper<TaskChainTemplate> tcLq = Wrappers.lambdaQuery();
            tcLq.in(TaskChainTemplate::getId,
                    list.stream().map(HxUserTaskChainTemplate::getTaskChainTemplateId).collect(Collectors.toSet()));
            List<TaskChainTemplate> taskChainTemplates = taskChainTemplateService.list(tcLq);
            if (CollUtil.isEmpty(taskChainTemplates)) {
                return;
            }
            messageDispatchService.broadcastMessage(TopicEnum.TASK_TEMPLATE_BROADCAST,
                    taskChainTemplates.stream().map(template -> {
                        TaskTemplateBroadcastDto dto = new TaskTemplateBroadcastDto();
                        dto.setChainType(template.getChainType());
                        Long l = TASK_ID_AMR_MAP.get(template.getId());
                        if (l != null && l != 0) {
                            dto.setAmrId(l);
                        }
                        dto.setTaskChainId(template.getId());
                        return dto;
                    }).collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("sendChain error", e);
        }
    }
}