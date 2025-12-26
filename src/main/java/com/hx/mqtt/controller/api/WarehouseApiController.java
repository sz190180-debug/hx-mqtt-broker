package com.hx.mqtt.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.domain.rep.warehouse.BatchUpdatePositionStatusRep;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnRep;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnVertexesRep;
import com.hx.mqtt.domain.rep.warehouse.WarehouseRep;
import com.hx.mqtt.domain.req.warehouse.*;
import com.hx.mqtt.service.WarehouseColumnService;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import com.hx.mqtt.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 仓库管理API控制器
 */
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseApiController {

    private final WarehouseService warehouseService;
    private final WarehouseColumnService warehouseColumnService;
    private final WarehouseColumnVertexesService warehouseColumnVertexesService;

    // ==================== 仓库管理 ====================

    /**
     * 添加仓库
     */
    @PostMapping("/add")
    public HttpResp<Long> addWarehouse(@Valid @RequestBody WarehouseAddReq req) {
        Long warehouseId = warehouseService.addWarehouse(req);
        return HttpResp.success(warehouseId);
    }

    /**
     * 更新仓库
     */
    @PostMapping("/update")
    public HttpResp<Boolean> updateWarehouse(@Valid @RequestBody WarehouseUpdateReq req) {
        Boolean result = warehouseService.updateWarehouse(req);
        return HttpResp.success(result);
    }

    /**
     * 删除仓库
     */
    @PostMapping("/delete/{warehouseId}")
    public HttpResp<Boolean> deleteWarehouse(@PathVariable Long warehouseId) {
        Boolean result = warehouseService.deleteWarehouse(warehouseId);
        return HttpResp.success(result);
    }

    /**
     * 分页查询仓库
     */
    @PostMapping("/page")
    public HttpResp<IPage<WarehouseRep>> pageWarehouse(@Valid @RequestBody WarehouseQueryReq req) {
        IPage<WarehouseRep> result = warehouseService.pageWarehouse(req);
        return HttpResp.success(result);
    }

    /**
     * 获取仓库详情
     */
    @GetMapping("/detail/{warehouseId}")
    public HttpResp<WarehouseRep> getWarehouseDetail(@PathVariable Long warehouseId) {
        WarehouseRep result = warehouseService.getWarehouseDetail(warehouseId);
        return HttpResp.success(result);
    }

    /**
     * 添加库位列
     */
    @PostMapping("/column/add")
    public HttpResp<Long> addWarehouseColumn(@Valid @RequestBody WarehouseColumnAddReq req) {
        Long columnId = warehouseColumnService.addWarehouseColumn(req);
        return HttpResp.success(columnId);
    }

    /**
     * 更新库位列
     */
    @PostMapping("/column/update")
    public HttpResp<Boolean> updateWarehouseColumn(@Valid @RequestBody WarehouseColumnUpdateReq req) {
        Boolean result = warehouseColumnService.updateWarehouseColumn(req);
        return HttpResp.success(result);
    }

    /**
     * 删除库位列
     */
    @PostMapping("/column/delete/{columnId}")
    public HttpResp<Boolean> deleteWarehouseColumn(@PathVariable Long columnId) {
        Boolean result = warehouseColumnService.deleteWarehouseColumn(columnId);
        return HttpResp.success(result);
    }

    /**
     * 获取仓库的库位列
     */
    @GetMapping("/column/list/{warehouseId}")
    public HttpResp<List<WarehouseColumnRep>> getColumnsByWarehouseId(@PathVariable Long warehouseId) {
        List<WarehouseColumnRep> result = warehouseColumnService.getColumnsByWarehouseId(warehouseId);
        return HttpResp.success(result);
    }

    /**
     * 获取库位列详情
     */
    @GetMapping("/column/detail/{columnId}")
    public HttpResp<WarehouseColumnRep> getColumnDetail(@PathVariable Long columnId) {
        WarehouseColumnRep result = warehouseColumnService.getColumnDetail(columnId);
        return HttpResp.success(result);
    }

    /**
     * 获取所有仓库
     */
    @GetMapping("/all")
    public HttpResp<List<WarehouseRep>> getAllWarehouses() {
        List<WarehouseRep> result = warehouseService.getAllWarehouses();
        return HttpResp.success(result);
    }

    // ==================== 库位列点位关联管理 ====================

    /**
     * 添加库位列点位关联
     */
    @PostMapping("/vertexes/add")
    public HttpResp<Long> addWarehouseColumnVertexes(@Valid @RequestBody WarehouseColumnVertexesAddReq req) {
        Long positionId = warehouseColumnVertexesService.addWarehouseColumnVertexes(req);
        return HttpResp.success(positionId);
    }

    /**
     * 更新库位列点位关联
     */
    @PostMapping("/vertexes/update")
    public HttpResp<Boolean> updateWarehouseColumnVertexes(@Valid @RequestBody WarehouseColumnVertexesUpdateReq req) {
        Boolean result = warehouseColumnVertexesService.updateWarehouseColumnVertexes(req);
        return HttpResp.success(result);
    }

    /**
     * 删除库位列点位关联
     */
    @PostMapping("/vertexes/delete/{positionId}")
    public HttpResp<Boolean> deleteWarehouseColumnVertexes(@PathVariable Long positionId) {
        Boolean result = warehouseColumnVertexesService.deleteWarehouseColumnVertexes(positionId);
        return HttpResp.success(result);
    }

    /**
     * 获取库位列的点位关联列表
     */
    @GetMapping("/vertexes/list/{columnId}")
    public HttpResp<List<WarehouseColumnVertexesRep>> getVertexesByColumnId(@PathVariable Long columnId) {
        List<WarehouseColumnVertexesRep> result = warehouseColumnVertexesService.getVertexesByColumnId(columnId);
        return HttpResp.success(result);
    }

    /**
     * 获取点位关联详情
     */
    @GetMapping("/vertexes/detail/{positionId}")
    public HttpResp<WarehouseColumnVertexesRep> getVertexesDetail(@PathVariable Long positionId) {
        WarehouseColumnVertexesRep result = warehouseColumnVertexesService.getVertexesDetail(positionId);
        return HttpResp.success(result);
    }

    // ==================== 批量操作 ====================

    /**
     * 批量更新点位状态
     */
    @PostMapping("/position/batch-update-status")
    public HttpResp<BatchUpdatePositionStatusRep> batchUpdatePositionStatus(@Valid @RequestBody BatchUpdatePositionStatusReq req) {
        BatchUpdatePositionStatusRep result = warehouseColumnVertexesService.batchUpdatePositionStatus(req);
        return HttpResp.success(result);
    }
}
