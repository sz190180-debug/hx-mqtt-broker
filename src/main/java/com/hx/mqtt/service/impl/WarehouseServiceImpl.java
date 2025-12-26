package com.hx.mqtt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.Warehouse;
import com.hx.mqtt.domain.rep.warehouse.WarehouseRep;
import com.hx.mqtt.domain.req.warehouse.WarehouseAddReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseQueryReq;
import com.hx.mqtt.domain.req.warehouse.WarehouseUpdateReq;
import com.hx.mqtt.mapper.WarehouseMapper;
import com.hx.mqtt.service.HxUserWarehouseService;
import com.hx.mqtt.service.WarehouseColumnService;
import com.hx.mqtt.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓库服务实现
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Lazy
    @Autowired
    private WarehouseColumnService warehouseColumnService;

    @Lazy
    @Autowired
    private HxUserWarehouseService hxUserWarehouseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addWarehouse(WarehouseAddReq req) {
        Warehouse warehouse = new Warehouse();
        BeanUtil.copyProperties(req, warehouse);

        save(warehouse);
        return warehouse.getWarehouseId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWarehouse(WarehouseUpdateReq req) {
        Warehouse existing = getById(req.getWarehouseId());
        if (existing == null) {
            throw new RuntimeException("仓库不存在");
        }

        BeanUtil.copyProperties(req, existing);

        return updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWarehouse(Long warehouseId) {
        Warehouse warehouse = getById(warehouseId);
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }

        // 检查是否有关联的库位列
        if (!warehouseColumnService.getColumnsByWarehouseId(warehouseId).isEmpty()) {
            throw new RuntimeException("仓库下存在库位列，无法删除");
        }

        return removeById(warehouseId);
    }

    @Override
    public IPage<WarehouseRep> pageWarehouse(WarehouseQueryReq req) {
        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.lambdaQuery();

        if (StrUtil.isNotBlank(req.getWarehouseName())) {
            wrapper.like(Warehouse::getWarehouseName, req.getWarehouseName());
        }

        wrapper.orderByAsc(Warehouse::getWarehouseId);

        Page<Warehouse> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<Warehouse> result = page(page, wrapper);

        return result.convert(this::convertToRep);
    }

    @Override
    public WarehouseRep getWarehouseDetail(Long warehouseId) {
        Warehouse warehouse = getById(warehouseId);
        if (warehouse == null) {
            return null;
        }

        WarehouseRep rep = convertToRep(warehouse);
        rep.setColumns(warehouseColumnService.getColumnsByWarehouseId(warehouseId));

        return rep;
    }

    @Override
    public List<WarehouseRep> getAllWarehouses() {
        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByAsc(Warehouse::getWarehouseId);

        return list(wrapper).stream()
                .map(this::convertToRep)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<WarehouseRep> pageWarehouseByUser(WarehouseQueryReq req, Long userId) {
        // 获取用户有权限的仓库ID列表
        List<Long> userWarehouseIds = hxUserWarehouseService.getUserWarehouseIds(userId);
        if (userWarehouseIds.isEmpty()) {
            // 用户没有任何仓库权限，返回空结果
            return new Page<>(req.getPageNum(), req.getPageSize());
        }

        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.lambdaQuery();
        wrapper.in(Warehouse::getWarehouseId, userWarehouseIds);

        if (StrUtil.isNotBlank(req.getWarehouseName())) {
            wrapper.like(Warehouse::getWarehouseName, req.getWarehouseName());
        }

        wrapper.orderByAsc(Warehouse::getWarehouseId);

        Page<Warehouse> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<Warehouse> result = page(page, wrapper);

        return result.convert(this::convertToRep);
    }

    @Override
    public List<WarehouseRep> getAllWarehousesByUser(Long userId) {
        // 获取用户有权限的仓库ID列表
        List<Long> userWarehouseIds = hxUserWarehouseService.getUserWarehouseIds(userId);
        if (userWarehouseIds.isEmpty()) {
            // 用户没有任何仓库权限，返回空列表
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.lambdaQuery();
        wrapper.in(Warehouse::getWarehouseId, userWarehouseIds);
        wrapper.orderByAsc(Warehouse::getWarehouseId);

        return list(wrapper).stream()
                .map(this::convertToRep)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<WarehouseRep> pageWarehouseByClientId(WarehouseQueryReq req, String clientId) {
        // 获取用户有权限的仓库ID列表
        List<Long> userWarehouseIds = hxUserWarehouseService.getUserWarehouseIdsByClientId(clientId);
        if (userWarehouseIds.isEmpty()) {
            // 用户没有任何仓库权限，返回空结果
            return new Page<>(req.getPageNum(), req.getPageSize());
        }

        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.lambdaQuery();
        wrapper.in(Warehouse::getWarehouseId, userWarehouseIds);

        if (StrUtil.isNotBlank(req.getWarehouseName())) {
            wrapper.like(Warehouse::getWarehouseName, req.getWarehouseName());
        }

        wrapper.orderByAsc(Warehouse::getWarehouseId);

        Page<Warehouse> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<Warehouse> result = page(page, wrapper);

        return result.convert(this::convertToRep);
    }

    @Override
    public List<WarehouseRep> getAllWarehousesByClientId(String clientId) {
        // 获取用户有权限的仓库ID列表
        List<Long> userWarehouseIds = hxUserWarehouseService.getUserWarehouseIdsByClientId(clientId);
        if (userWarehouseIds.isEmpty()) {
            // 用户没有任何仓库权限，返回空列表
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Warehouse> wrapper = Wrappers.lambdaQuery();
        wrapper.in(Warehouse::getWarehouseId, userWarehouseIds);
        wrapper.orderByAsc(Warehouse::getWarehouseId);

        return list(wrapper).stream()
                .map(this::convertToRep)
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应对象
     */
    private WarehouseRep convertToRep(Warehouse warehouse) {
        WarehouseRep rep = new WarehouseRep();
        BeanUtil.copyProperties(warehouse, rep);
        return rep;
    }
}
