package com.hx.mqtt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.domain.entity.WarehouseColumnVertexes;
import com.hx.mqtt.domain.rep.warehouse.BatchUpdatePositionStatusRep;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnVertexesRep;
import com.hx.mqtt.domain.req.warehouse.BatchUpdatePositionStatusReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnVertexesAddReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnVertexesUpdateReq;

import java.util.List;

/**
 * 库位列点位关联服务接口
 */
public interface WarehouseColumnVertexesService extends IService<WarehouseColumnVertexes> {

    /**
     * 添加库位列点位关联
     *
     * @param req 添加请求
     * @return 关联ID
     */
    Long addWarehouseColumnVertexes(WarehouseColumnVertexesAddReq req);

    /**
     * 更新库位列点位关联
     *
     * @param req 更新请求
     * @return 是否成功
     */
    Boolean updateWarehouseColumnVertexes(WarehouseColumnVertexesUpdateReq req);

    /**
     * 删除库位列点位关联
     *
     * @param positionId 关联ID
     * @return 是否成功
     */
    Boolean deleteWarehouseColumnVertexes(Long positionId);

    /**
     * 根据库位列ID获取点位关联列表
     *
     * @param columnId 库位列ID
     * @return 点位关联列表（按排序）
     */
    List<WarehouseColumnVertexesRep> getVertexesByColumnId(Long columnId);

    /**
     * 根据ID获取点位关联详情
     *
     * @param positionId 关联ID
     * @return 点位关联详情
     */
    WarehouseColumnVertexesRep getVertexesDetail(Long positionId);

    /**
     * 根据地图点位ID更新状态
     *
     * @param hxMapVertexesId 地图点位ID
     * @param status          新状态：1-可用，2-占用，3-禁用
     * @return 是否成功
     */
    Boolean updateStatusByMapVertexId(Long hxMapVertexesId, Integer status);

    /**
     * 根据仓库和库位列查找最远距离可用的位置
     *
     * @param warehouseId 仓库ID
     * @param columnId    库位列ID，如果为null则从第一列开始依次查找
     * @return 最远可用位置的地图点位信息，如果没有找到返回null
     */
    HxMapVertexes findFarthestAvailablePosition(Long warehouseId, Long columnId);

    /**
     * 批量更新点位状态
     *
     * @param req 批量更新请求
     * @return 批量更新结果
     */
    BatchUpdatePositionStatusRep batchUpdatePositionStatus(BatchUpdatePositionStatusReq req);
}
