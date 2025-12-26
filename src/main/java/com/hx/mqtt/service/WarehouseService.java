package com.hx.mqtt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.Warehouse;
import com.hx.mqtt.domain.rep.warehouse.WarehouseRep;
import com.hx.mqtt.domain.req.warehouse.WarehouseAddReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseQueryReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseUpdateReq;

import java.util.List;

/**
 * 仓库服务接口
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 添加仓库
     *
     * @param req 添加请求
     * @return 仓库ID
     */
    Long addWarehouse(WarehouseAddReq req);

    /**
     * 更新仓库
     *
     * @param req 更新请求
     * @return 是否成功
     */
    Boolean updateWarehouse(WarehouseUpdateReq req);

    /**
     * 删除仓库
     *
     * @param warehouseId 仓库ID
     * @return 是否成功
     */
    Boolean deleteWarehouse(Long warehouseId);

    /**
     * 分页查询仓库
     *
     * @param req 查询请求
     * @return 分页结果
     */
    IPage<WarehouseRep> pageWarehouse(WarehouseQueryReq req);

    /**
     * 根据ID获取仓库详情
     *
     * @param warehouseId 仓库ID
     * @return 仓库详情
     */
    WarehouseRep getWarehouseDetail(Long warehouseId);

    /**
     * 获取所有仓库
     *
     * @return 仓库列表
     */
    List<WarehouseRep> getAllWarehouses();

    /**
     * 根据用户权限分页查询仓库
     *
     * @param req 查询请求
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<WarehouseRep> pageWarehouseByUser(WarehouseQueryReq req, Long userId);

    /**
     * 根据用户权限获取所有仓库
     *
     * @param userId 用户ID
     * @return 仓库列表
     */
    List<WarehouseRep> getAllWarehousesByUser(Long userId);

    /**
     * 根据clientId权限分页查询仓库
     *
     * @param req 查询请求
     * @param clientId 客户端ID
     * @return 分页结果
     */
    IPage<WarehouseRep> pageWarehouseByClientId(WarehouseQueryReq req, String clientId);

    /**
     * 根据clientId权限获取所有仓库
     *
     * @param clientId 客户端ID
     * @return 仓库列表
     */
    List<WarehouseRep> getAllWarehousesByClientId(String clientId);
}
