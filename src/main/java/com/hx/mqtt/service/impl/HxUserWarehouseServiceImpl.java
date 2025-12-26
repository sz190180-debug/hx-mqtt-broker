package com.hx.mqtt.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.common.enums.RoleAddTypeEnum;
import com.hx.mqtt.domain.entity.HxUser;
import com.hx.mqtt.domain.entity.HxUserWarehouse;
import com.hx.mqtt.domain.req.warehouse.UserWarehouseAssignReq;
import com.hx.mqtt.mapper.HxUserWarehouseMapper;
import com.hx.mqtt.service.HxUserService;
import com.hx.mqtt.service.HxUserWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户仓库权限服务实现类
 */
@Service
@RequiredArgsConstructor
public class HxUserWarehouseServiceImpl extends ServiceImpl<HxUserWarehouseMapper, HxUserWarehouse> implements HxUserWarehouseService {

    private final HxUserService hxUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignUserWarehouse(UserWarehouseAssignReq req) {
        // 如果仓库ID列表为空或null，根据分配类型处理
        if (req.getWarehouseIds() == null) {
            req.setWarehouseIds(new ArrayList<>());
        }

        for (Long userId : req.getUserIds()) {
            LambdaQueryWrapper<HxUserWarehouse> lq = Wrappers.lambdaQuery();
            lq.eq(HxUserWarehouse::getHxUserId, userId);

            List<Long> addIdList = new ArrayList<>();

            if (RoleAddTypeEnum.ADD.getCode().equals(req.getAssignType())) {
                // 追加模式：只添加不存在的权限
                if (!req.getWarehouseIds().isEmpty()) {
                    Set<Long> existingWarehouseIds = list(lq).stream()
                            .map(HxUserWarehouse::getWarehouseId)
                            .collect(Collectors.toSet());

                    for (Long warehouseId : req.getWarehouseIds()) {
                        if (!existingWarehouseIds.contains(warehouseId)) {
                            addIdList.add(warehouseId);
                        }
                    }
                }
            } else if (RoleAddTypeEnum.REPLACE.getCode().equals(req.getAssignType())) {
                // 覆盖模式：先删除所有权限，再添加新权限
                remove(lq);
                if (!req.getWarehouseIds().isEmpty()) {
                    addIdList.addAll(req.getWarehouseIds());
                }
            }

            // 批量添加权限
            if (CollectionUtil.isNotEmpty(addIdList)) {
                List<HxUserWarehouse> userWarehouses = new ArrayList<>();
                for (Long warehouseId : addIdList) {
                    HxUserWarehouse userWarehouse = new HxUserWarehouse();
                    userWarehouse.setHxUserId(userId);
                    userWarehouse.setWarehouseId(warehouseId);
                    userWarehouse.setCreatedTime(new Date());
                    userWarehouse.setCreateBy("system");
                    userWarehouses.add(userWarehouse);
                }
                saveBatch(userWarehouses);
            }
        }
        return true;
    }

    @Override
    public List<Long> getUserWarehouseIds(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<HxUserWarehouse> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(HxUserWarehouse::getHxUserId, userId);
        wrapper.select(HxUserWarehouse::getWarehouseId);
        
        return list(wrapper).stream()
                .map(HxUserWarehouse::getWarehouseId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getUserWarehouseIdsByClientId(String clientId) {
        HxUser user = hxUserService.getByClientId(clientId);
        if (user == null) {
            return new ArrayList<>();
        }
        return getUserWarehouseIds(user.getHxUserId());
    }

    @Override
    public Boolean hasWarehousePermission(Long userId, Long warehouseId) {
        if (userId == null || warehouseId == null) {
            return false;
        }
        
        LambdaQueryWrapper<HxUserWarehouse> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(HxUserWarehouse::getHxUserId, userId);
        wrapper.eq(HxUserWarehouse::getWarehouseId, warehouseId);
        
        return count(wrapper) > 0;
    }

    @Override
    public Boolean hasWarehousePermissionByClientId(String clientId, Long warehouseId) {
        HxUser user = hxUserService.getByClientId(clientId);
        if (user == null) {
            return false;
        }
        return hasWarehousePermission(user.getHxUserId(), warehouseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean clearUserWarehousePermissions(Long userId) {
        if (userId == null) {
            return false;
        }

        LambdaQueryWrapper<HxUserWarehouse> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(HxUserWarehouse::getHxUserId, userId);

        return remove(wrapper);
    }
}
