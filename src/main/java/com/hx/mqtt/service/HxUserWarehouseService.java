package com.hx.mqtt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.HxUserWarehouse;
import com.hx.mqtt.domain.req.warehouse.UserWarehouseAssignReq;

import java.util.List;

/**
 * 用户仓库权限服务接口
 */
public interface HxUserWarehouseService extends IService<HxUserWarehouse> {

    /**
     * 分配用户仓库权限
     *
     * @param req 分配请求
     * @return 是否成功
     */
    Boolean assignUserWarehouse(UserWarehouseAssignReq req);

    /**
     * 获取用户有权限的仓库ID列表
     *
     * @param userId 用户ID
     * @return 仓库ID列表
     */
    List<Long> getUserWarehouseIds(Long userId);

    /**
     * 根据clientId获取用户有权限的仓库ID列表
     *
     * @param clientId 客户端ID
     * @return 仓库ID列表
     */
    List<Long> getUserWarehouseIdsByClientId(String clientId);

    /**
     * 检查用户是否有仓库权限
     *
     * @param userId 用户ID
     * @param warehouseId 仓库ID
     * @return 是否有权限
     */
    Boolean hasWarehousePermission(Long userId, Long warehouseId);

    /**
     * 根据clientId检查用户是否有仓库权限
     *
     * @param clientId 客户端ID
     * @param warehouseId 仓库ID
     * @return 是否有权限
     */
    Boolean hasWarehousePermissionByClientId(String clientId, Long warehouseId);

    /**
     * 清空用户的所有仓库权限
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean clearUserWarehousePermissions(Long userId);
}
