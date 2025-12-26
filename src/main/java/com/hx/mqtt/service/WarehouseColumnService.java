package com.hx.mqtt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.WarehouseColumn;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnRep;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnAddReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnUpdateReq;

import java.util.List;

/**
 * 库位列服务接口
 */
public interface WarehouseColumnService extends IService<WarehouseColumn> {

    /**
     * 添加库位列
     *
     * @param req 添加请求
     * @return 库位列ID
     */
    Long addWarehouseColumn(WarehouseColumnAddReq req);

    /**
     * 更新库位列
     *
     * @param req 更新请求
     * @return 是否成功
     */
    Boolean updateWarehouseColumn(WarehouseColumnUpdateReq req);

    /**
     * 删除库位列
     *
     * @param columnId 库位列ID
     * @return 是否成功
     */
    Boolean deleteWarehouseColumn(Long columnId);

    /**
     * 根据仓库ID获取库位列
     *
     * @param warehouseId 仓库ID
     * @return 库位列列表
     */
    List<WarehouseColumnRep> getColumnsByWarehouseId(Long warehouseId);

    /**
     * 根据ID获取库位列详情
     *
     * @param columnId 库位列ID
     * @return 库位列详情
     */
    WarehouseColumnRep getColumnDetail(Long columnId);
}
