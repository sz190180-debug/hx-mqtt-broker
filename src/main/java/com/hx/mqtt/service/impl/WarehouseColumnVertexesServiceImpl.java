package com.hx.mqtt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.domain.entity.WarehouseColumnVertexes;
import com.hx.mqtt.domain.rep.warehouse.BatchUpdatePositionStatusRep;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnVertexesRep;
import com.hx.mqtt.domain.req.warehouse.BatchUpdatePositionStatusReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnVertexesAddReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnVertexesUpdateReq;
import com.hx.mqtt.mapper.WarehouseColumnVertexesMapper;
import com.hx.mqtt.service.HxMapVertexesService;
import com.hx.mqtt.service.WarehouseColumnService;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import com.hx.mqtt.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库位列点位关联服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseColumnVertexesServiceImpl extends ServiceImpl<WarehouseColumnVertexesMapper,
        WarehouseColumnVertexes> implements WarehouseColumnVertexesService {

    private final HxMapVertexesService hxMapVertexesService;
    private final WarehouseColumnService warehouseColumnService;
    private final WarehouseService warehouseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addWarehouseColumnVertexes(WarehouseColumnVertexesAddReq req) {
        // 检查该地图点位是否已经被绑定
        checkVertexUniqueness(req.getHxMapVertexesId(), null);

        WarehouseColumnVertexes vertexes = new WarehouseColumnVertexes();
        BeanUtil.copyProperties(req, vertexes);

        // 如果没有设置状态，默认为可用状态
        if (vertexes.getStatus() == null) {
            vertexes.setStatus(1);
        }

        save(vertexes);
        return vertexes.getPositionId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWarehouseColumnVertexes(WarehouseColumnVertexesUpdateReq req) {
        WarehouseColumnVertexes existing = getById(req.getPositionId());
        if (existing == null) {
            throw new RuntimeException("点位关联不存在");
        }

        // 如果修改了地图点位，需要检查新点位的唯一性
        if (!existing.getHxMapVertexesId().equals(req.getHxMapVertexesId())) {
            checkVertexUniqueness(req.getHxMapVertexesId(), req.getPositionId());
        }

        BeanUtil.copyProperties(req, existing);

        return updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWarehouseColumnVertexes(Long positionId) {
        WarehouseColumnVertexes vertexes = getById(positionId);
        if (vertexes == null) {
            throw new RuntimeException("点位关联不存在");
        }

        return removeById(positionId);
    }

    @Override
    public List<WarehouseColumnVertexesRep> getVertexesByColumnId(Long columnId) {
        LambdaQueryWrapper<WarehouseColumnVertexes> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WarehouseColumnVertexes::getColumnId, columnId);
        wrapper.orderByAsc(WarehouseColumnVertexes::getPositionOrder);

        return list(wrapper).stream()
                .map(this::convertToRep)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseColumnVertexesRep getVertexesDetail(Long positionId) {
        WarehouseColumnVertexes vertexes = getById(positionId);
        if (vertexes == null) {
            return null;
        }

        return convertToRep(vertexes);
    }

    /**
     * 转换为响应对象
     */
    private WarehouseColumnVertexesRep convertToRep(WarehouseColumnVertexes vertexes) {
        WarehouseColumnVertexesRep rep = new WarehouseColumnVertexesRep();
        BeanUtil.copyProperties(vertexes, rep);

        // 设置状态描述
        rep.setStatusDesc(getStatusDesc(vertexes.getStatus()));

        // 获取关联的地图点位信息
        HxMapVertexes mapVertex = hxMapVertexesService.getById(vertexes.getHxMapVertexesId());
        rep.setMapVertex(mapVertex);

        return rep;
    }

    /**
     * 检查地图点位的唯一性
     *
     * @param hxMapVertexesId   地图点位ID
     * @param excludePositionId 排除的关联ID（用于更新时排除自己）
     */
    private void checkVertexUniqueness(Long hxMapVertexesId, Long excludePositionId) {
        LambdaQueryWrapper<WarehouseColumnVertexes> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WarehouseColumnVertexes::getHxMapVertexesId, hxMapVertexesId);

        // 如果是更新操作，排除当前记录
        if (excludePositionId != null) {
            wrapper.ne(WarehouseColumnVertexes::getPositionId, excludePositionId);
        }

        WarehouseColumnVertexes existing = getOne(wrapper);
        if (existing != null) {
            // 获取已绑定的仓库和库位列信息
            String errorMessage = buildDuplicateErrorMessage(existing);
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * 构建重复绑定的错误信息
     */
    private String buildDuplicateErrorMessage(WarehouseColumnVertexes existing) {
        try {
            // 获取地图点位信息
            HxMapVertexes mapVertex = hxMapVertexesService.getById(existing.getHxMapVertexesId());
            String vertexInfo = mapVertex != null ?
                    String.format("点位[%s%s]",
                            mapVertex.getCode(),
                            mapVertex.getAlias() != null && !mapVertex.getAlias().trim().isEmpty() ?
                                    "(" + mapVertex.getAlias() + ")" : "") :
                    String.format("点位ID[%d]", existing.getHxMapVertexesId());

            // 获取库位列信息
            String columnInfo = "未知库位列";
            String warehouseInfo = "未知仓库";

            try {
                // 通过getById方法获取库位列信息
                var column = warehouseColumnService.getById(existing.getColumnId());
                if (column != null) {
                    columnInfo = String.format("库位列[%s]", column.getColumnName());

                    // 获取仓库信息
                    var warehouse = warehouseService.getById(column.getWarehouseId());
                    if (warehouse != null) {
                        warehouseInfo = String.format("仓库[%s]", warehouse.getWarehouseName());
                    }
                }
            } catch (Exception e) {
                // 如果获取失败，使用默认信息
                columnInfo = String.format("库位列ID[%d]", existing.getColumnId());
            }

            return String.format("%s已经被绑定到%s的%s，一个点位只能绑定到一个库位列",
                    vertexInfo, warehouseInfo, columnInfo);

        } catch (Exception e) {
            return "该点位已经被绑定到其他库位列，一个点位只能绑定到一个库位列";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatusByMapVertexId(Long hxMapVertexesId, Integer status) {
        if (hxMapVertexesId == null || status == null) {
            return false;
        }

        LambdaQueryWrapper<WarehouseColumnVertexes> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WarehouseColumnVertexes::getHxMapVertexesId, hxMapVertexesId);

        WarehouseColumnVertexes vertexes = getOne(wrapper);
        if (vertexes == null) {
            return false;
        }

        vertexes.setStatus(status);
        return updateById(vertexes);
    }

    @Override
    public HxMapVertexes findFarthestAvailablePosition(Long warehouseId, Long columnId) {
        if (warehouseId == null) {
            return null;
        }

        try {
            if (columnId != null) {
                // 如果指定了库位列ID，只在该库位列中查找
                return findFarthestInColumn(columnId);
            } else {
                // 如果没有指定库位列ID，从第一列开始依次往下找
                return findFarthestInWarehouse(warehouseId);
            }
        } catch (Exception e) {
            log.error("查找最远可用位置时发生异常，warehouseId: {}, columnId: {}", warehouseId, columnId, e);
            return null;
        }
    }

    /**
     * 在指定库位列中查找最远可用位置
     */
    private HxMapVertexes findFarthestInColumn(Long columnId) {
        // 获取该库位列的所有点位，按排序顺序
        LambdaQueryWrapper<WarehouseColumnVertexes> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WarehouseColumnVertexes::getColumnId, columnId);
        wrapper.orderByAsc(WarehouseColumnVertexes::getPositionOrder);

        List<WarehouseColumnVertexes> positions = list(wrapper);
        if (positions.isEmpty()) {
            return null;
        }

        // 从最远的位置开始往回找，找到第一个可用且可达的位置
        for (int i = positions.size() - 1; i >= 0; i--) {
            WarehouseColumnVertexes position = positions.get(i);

            // 检查该位置是否可用
            if (position.getStatus() != null && position.getStatus() == 1) {
                // 检查从起点到该位置的路径是否可达（中途没有占用的点位）
                if (isPathReachable(positions, i)) {
                    // 获取对应的地图点位信息
                    return hxMapVertexesService.getById(position.getHxMapVertexesId());
                }
            }
        }

        return null;
    }

    /**
     * 在整个仓库中查找最远可用位置（从第一列开始依次查找）
     */
    private HxMapVertexes findFarthestInWarehouse(Long warehouseId) {
        // 获取该仓库的所有库位列，按列顺序
        var columns = warehouseColumnService.getColumnsByWarehouseId(warehouseId);
        if (columns.isEmpty()) {
            return null;
        }

        // 从第一列开始依次查找
        for (var column : columns) {
            HxMapVertexes result = findFarthestInColumn(column.getColumnId());
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 检查从起点到指定位置的路径是否可达（中途没有占用的点位）
     */
    private boolean isPathReachable(List<WarehouseColumnVertexes> positions, int targetIndex) {
        // 检查从第一个位置到目标位置之间是否有占用的点位
        for (int i = 0; i < targetIndex; i++) {
            WarehouseColumnVertexes position = positions.get(i);
            if (position.getStatus() != null && position.getStatus() == 2) {
                // 中途有占用的点位，无法到达
                return false;
            }
        }
        return true;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "可用";
            case 2:
                return "占用";
            case 3:
                return "禁用";
            default:
                return "未知";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchUpdatePositionStatusRep batchUpdatePositionStatus(BatchUpdatePositionStatusReq req) {
        BatchUpdatePositionStatusRep result = new BatchUpdatePositionStatusRep();
        result.setTotalCount(req.getPositionIds().size());
        result.setSuccessPositionIds(new ArrayList<>());
        result.setFailPositionIds(new ArrayList<>());
        result.setFailReasons(new ArrayList<>());

        log.info("开始批量更新点位状态，总数量: {}, 目标状态: {}, 更新模式: {}",
                req.getPositionIds().size(), req.getStatus(), req.getUpdateMode());
        log.info("请求的点位ID列表: {}", req.getPositionIds());

        // 批量获取所有点位信息
        List<WarehouseColumnVertexes> allPositions = listByIds(req.getPositionIds());
        log.info("从数据库查询到的点位数量: {}", allPositions.size());

        if (allPositions.isEmpty()) {
            log.warn("没有找到任何点位，请求的ID: {}", req.getPositionIds());
            result.setFailCount(req.getPositionIds().size());
            req.getPositionIds().forEach(id -> {
                result.getFailPositionIds().add(id);
                result.getFailReasons().add("点位不存在");
            });
            result.generateSummary();
            return result;
        }

        // 根据更新模式过滤点位
        List<WarehouseColumnVertexes> positionsToUpdate = filterPositionsByUpdateMode(allPositions, req.getUpdateMode());

        log.info("根据更新模式 {} 过滤后，需要更新的点位数量: {}", req.getUpdateMode(), positionsToUpdate.size());

        // 批量更新点位状态
        for (WarehouseColumnVertexes position : positionsToUpdate) {
            try {
                Integer oldStatus = position.getStatus();
                position.setStatus(req.getStatus());

                boolean updateSuccess = updateById(position);

                if (updateSuccess) {
                    result.getSuccessPositionIds().add(position.getPositionId());
                    result.setSuccessCount(result.getSuccessCount() + 1);

                    log.debug("成功更新点位状态，ID: {}, 旧状态: {}, 新状态: {}",
                            position.getPositionId(), oldStatus, req.getStatus());
                } else {
                    result.getFailPositionIds().add(position.getPositionId());
                    result.getFailReasons().add("数据库更新失败");
                    result.setFailCount(result.getFailCount() + 1);
                }
            } catch (Exception e) {
                result.getFailPositionIds().add(position.getPositionId());
                result.getFailReasons().add("更新异常: " + e.getMessage());
                result.setFailCount(result.getFailCount() + 1);

                log.error("更新点位状态时发生异常，点位ID: {}", position.getPositionId(), e);
            }
        }

        // 处理不符合更新模式的点位
        List<Long> excludedPositionIds = new ArrayList<>();
        for (WarehouseColumnVertexes position : allPositions) {
            if (!positionsToUpdate.contains(position)) {
                excludedPositionIds.add(position.getPositionId());
            }
        }

        if (!excludedPositionIds.isEmpty()) {
            result.getFailPositionIds().addAll(excludedPositionIds);
            for (int i = 0; i < excludedPositionIds.size(); i++) {
                result.getFailReasons().add("不符合更新模式条件");
            }
            result.setFailCount(result.getFailCount() + excludedPositionIds.size());
        }

        // 处理不存在的点位ID
        List<Long> existingIds = allPositions.stream()
                .map(WarehouseColumnVertexes::getPositionId)
                .collect(Collectors.toList());

        for (Long requestId : req.getPositionIds()) {
            if (!existingIds.contains(requestId)) {
                result.getFailPositionIds().add(requestId);
                result.getFailReasons().add("点位不存在");
                result.setFailCount(result.getFailCount() + 1);
            }
        }

        result.generateSummary();

        log.info("批量更新点位状态完成，{}", result.getSummary());

        return result;
    }

    /**
     * 根据更新模式过滤点位
     */
    private List<WarehouseColumnVertexes> filterPositionsByUpdateMode(List<WarehouseColumnVertexes> positions, String updateMode) {
        if ("all".equals(updateMode)) {
            return positions;
        }

        return positions.stream()
                .filter(position -> {
                    Integer currentStatus = position.getStatus();
                    if (currentStatus == null) {
                        return false;
                    }

                    switch (updateMode) {
                        case "available":
                            return currentStatus == 1;
                        case "occupied":
                            return currentStatus == 2;
                        case "disabled":
                            return currentStatus == 3;
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }
}
