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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void sendTaskStatusScheduled() {
        sendTaskStatus();
    }

    private void sendTaskStatus(){
        // 找出所有的点位
        List<WarehouseColumnVertexes> warehouseColumnVertexes = warehouseColumnVertexesService.lambdaQuery()
                .ne(WarehouseColumnVertexes::getStatus, 1)
                .list();

        Set<Long> columSet = warehouseColumnVertexes.stream().map(WarehouseColumnVertexes::getColumnId).collect(Collectors.toSet());

        // 找出所有已经占用的点位
        List<WarehouseColumn> warehouseColumns = warehouseColumnService.lambdaQuery()
                .in(WarehouseColumn::getColumnId, columSet)
                .list();

        Set<Long> warehouseSet = warehouseColumns.stream().map(WarehouseColumn::getWarehouseId).collect(Collectors.toSet());

        // 找出所有占用的点位的仓库的所有点位
        List<WarehouseColumn> warehouseColumnList = warehouseColumnService.lambdaQuery()
                .in(WarehouseColumn::getWarehouseId, warehouseSet)
                .list();



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