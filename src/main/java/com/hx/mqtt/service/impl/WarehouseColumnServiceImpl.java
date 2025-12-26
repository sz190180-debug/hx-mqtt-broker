package com.hx.mqtt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.WarehouseColumn;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnRep;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnAddReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnUpdateReq;
import com.hx.mqtt.mapper.WarehouseColumnMapper;
import com.hx.mqtt.service.WarehouseColumnService;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 库位列服务实现
 */
@Service
@RequiredArgsConstructor
public class WarehouseColumnServiceImpl extends ServiceImpl<WarehouseColumnMapper, WarehouseColumn> implements WarehouseColumnService {

    @Lazy
    @Autowired
    private WarehouseColumnVertexesService warehouseColumnVertexesService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addWarehouseColumn(WarehouseColumnAddReq req) {
        WarehouseColumn column = new WarehouseColumn();
        BeanUtil.copyProperties(req, column);

        save(column);
        return column.getColumnId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWarehouseColumn(WarehouseColumnUpdateReq req) {
        WarehouseColumn existing = getById(req.getColumnId());
        if (existing == null) {
            throw new RuntimeException("库位列不存在");
        }

        BeanUtil.copyProperties(req, existing);

        return updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWarehouseColumn(Long columnId) {
        WarehouseColumn column = getById(columnId);
        if (column == null) {
            throw new RuntimeException("库位列不存在");
        }

        // 检查是否有关联的点位
        if (!warehouseColumnVertexesService.getVertexesByColumnId(columnId).isEmpty()) {
            throw new RuntimeException("库位列下存在关联点位，无法删除");
        }

        return removeById(columnId);
    }

    @Override
    public List<WarehouseColumnRep> getColumnsByWarehouseId(Long warehouseId) {
        LambdaQueryWrapper<WarehouseColumn> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WarehouseColumn::getWarehouseId, warehouseId);
        wrapper.orderByAsc(WarehouseColumn::getColumnOrder);

        return list(wrapper).stream()
                .map(this::convertToRep)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseColumnRep getColumnDetail(Long columnId) {
        WarehouseColumn column = getById(columnId);
        if (column == null) {
            return null;
        }

        WarehouseColumnRep rep = convertToRep(column);
        rep.setVertexes(warehouseColumnVertexesService.getVertexesByColumnId(columnId));

        return rep;
    }


    /**
     * 转换为响应对象
     */
    private WarehouseColumnRep convertToRep(WarehouseColumn column) {
        WarehouseColumnRep rep = new WarehouseColumnRep();
        BeanUtil.copyProperties(column, rep);
        return rep;
    }
}
