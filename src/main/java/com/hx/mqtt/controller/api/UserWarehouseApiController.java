package com.hx.mqtt.controller.api;

import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.domain.rep.warehouse.WarehouseRep;
import com.hx.mqtt.domain.req.warehouse.UserWarehouseAssignReq;
import com.hx.mqtt.service.HxUserWarehouseService;
import com.hx.mqtt.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户仓库权限管理API控制器
 */
@RestController
@RequestMapping("/api/user-warehouse")
@RequiredArgsConstructor
public class UserWarehouseApiController {

    private final HxUserWarehouseService hxUserWarehouseService;
    private final WarehouseService warehouseService;

    /**
     * 分配用户仓库权限
     */
    @PostMapping("/assign")
    public HttpResp<Boolean> assignUserWarehouse(@Valid @RequestBody UserWarehouseAssignReq req) {
        Boolean result = hxUserWarehouseService.assignUserWarehouse(req);
        return HttpResp.success(result);
    }

    /**
     * 获取用户有权限的仓库ID列表
     */
    @GetMapping("/user/{userId}/warehouses")
    public HttpResp<List<Long>> getUserWarehouseIds(@PathVariable Long userId) {
        List<Long> result = hxUserWarehouseService.getUserWarehouseIds(userId);
        return HttpResp.success(result);
    }

    /**
     * 检查用户是否有仓库权限
     */
    @GetMapping("/user/{userId}/warehouse/{warehouseId}/permission")
    public HttpResp<Boolean> hasWarehousePermission(@PathVariable Long userId, @PathVariable Long warehouseId) {
        Boolean result = hxUserWarehouseService.hasWarehousePermission(userId, warehouseId);
        return HttpResp.success(result);
    }

    /**
     * 获取用户有权限的仓库详情列表
     */
    @GetMapping("/user/{userId}/warehouses/details")
    public HttpResp<List<WarehouseRep>> getUserWarehouseDetails(@PathVariable Long userId) {
        List<WarehouseRep> result = warehouseService.getAllWarehousesByUser(userId);
        return HttpResp.success(result);
    }

    /**
     * 清空用户的所有仓库权限
     */
    @DeleteMapping("/user/{userId}/warehouses")
    public HttpResp<Boolean> clearUserWarehousePermissions(@PathVariable Long userId) {
        Boolean result = hxUserWarehouseService.clearUserWarehousePermissions(userId);
        return HttpResp.success(result);
    }
}
