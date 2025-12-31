package com.hx.mqtt.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.HxUser;
import com.hx.mqtt.domain.entity.HxUserTaskChainTemplate;
import com.hx.mqtt.domain.entity.TaskChainTemplate;
import com.hx.mqtt.domain.rep.user.UserTaskChainTemplateRep;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateGroupSortReq;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateReq;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateSortReq;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateBatchSortReq;
import com.hx.mqtt.mapper.HxUserTaskChainTemplateMapper;
import com.hx.mqtt.mapper.TaskChainTemplateMapper;
import com.hx.mqtt.service.HxUserService;
import com.hx.mqtt.service.HxUserTaskChainTemplateService;
import com.hx.mqtt.service.WarehouseColumnService;
import com.hx.mqtt.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HxUserTaskChainTemplateServiceImpl extends ServiceImpl<HxUserTaskChainTemplateMapper,
        HxUserTaskChainTemplate> implements HxUserTaskChainTemplateService {

    private final TaskChainTemplateMapper taskChainTemplateMapper;

    private final HxUserService hxUserService;

    private final WarehouseService warehouseService;

    private final WarehouseColumnService warehouseColumnService;

    @Autowired
    @Lazy
    private HxUserTaskChainTemplateService hxUserTaskChainTemplateService;

    @Override
    public IPage<UserTaskChainTemplateRep> selectTaskChain(@Valid UserTaskChainTemplateReq req) {
        List<Long> taskChainIds = new ArrayList<>();
        if (StrUtil.isNotBlank(req.getName()) || StrUtil.isNotBlank(req.getAlias())) {
            LambdaQueryWrapper<TaskChainTemplate> lq = Wrappers.lambdaQuery();
            lq.like(StrUtil.isNotBlank(req.getName()), TaskChainTemplate::getName, req.getName());
            lq.like(StrUtil.isNotBlank(req.getAlias()), TaskChainTemplate::getAlias, req.getAlias());
            List<TaskChainTemplate> templates = taskChainTemplateMapper.selectList(lq);
            if (CollectionUtil.isEmpty(templates)) {
                taskChainIds.add(null);
            } else {
                taskChainIds.addAll(templates.stream().map(TaskChainTemplate::getId).collect(Collectors.toList()));
            }
        }
        Page<HxUserTaskChainTemplate> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<HxUserTaskChainTemplate> lq = Wrappers.lambdaQuery();
        lq.eq(HxUserTaskChainTemplate::getHxUserId, req.getHxUserId());
        lq.eq(StrUtil.isNotBlank(req.getGroupName()), HxUserTaskChainTemplate::getGroupName, req.getGroupName());
        lq.in(CollectionUtil.isNotEmpty(taskChainIds), HxUserTaskChainTemplate::getTaskChainTemplateId, taskChainIds);

        // 添加排序
        if ("desc".equalsIgnoreCase(req.getSortOrder())) {
            lq.orderByDesc(HxUserTaskChainTemplate::getSortOrder);
        } else {
            lq.orderByAsc(HxUserTaskChainTemplate::getSortOrder);
        }

        Page<HxUserTaskChainTemplate> pageResult = this.page(page, lq);

        Map<Long, TaskChainTemplate> templateMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(pageResult.getRecords())) {
            Set<Long> collect =
                    pageResult.getRecords().stream().map(HxUserTaskChainTemplate::getTaskChainTemplateId).collect(Collectors.toSet());
            LambdaQueryWrapper<TaskChainTemplate> hlq = Wrappers.lambdaQuery();
            hlq.in(TaskChainTemplate::getId, collect);
            templateMap.putAll(taskChainTemplateMapper.selectList(hlq).stream().collect(Collectors.toMap(TaskChainTemplate::getId, v -> v)));
        }
        return pageResult.convert(entity -> {
            UserTaskChainTemplateRep rep = new UserTaskChainTemplateRep();
            BeanUtils.copyProperties(entity, rep);
            TaskChainTemplate taskChainTemplate = templateMap.get(rep.getTaskChainTemplateId());
            if (taskChainTemplate != null) {
                rep.setAlias(taskChainTemplate.getAlias());
                rep.setName(taskChainTemplate.getName());
            }

            // 设置仓库信息
            if (entity.getWarehouseId() != null) {
                try {
                    var warehouse = warehouseService.getById(entity.getWarehouseId());
                    if (warehouse != null) {
                        rep.setWarehouseName(warehouse.getWarehouseName());
                        rep.setWarehouseId(warehouse.getWarehouseId());
                    }
                } catch (Exception e) {
                    // 忽略异常，避免影响主要功能
                }
            }

            // 设置库位列信息
            if (entity.getColumnId() != null) {
                try {
                    var column = warehouseColumnService.getById(entity.getColumnId());
                    if (column != null) {
                        rep.setColumnName(column.getColumnName());
                    }
                } catch (Exception e) {
                    // 忽略异常，避免影响主要功能
                }
            }

            return rep;
        });
    }

    @Override
    public HxUserTaskChainTemplate selectByClientIdAndChainId(String clientId, Long chainId) {
        if (StrUtil.isBlank(clientId) || StrUtil.isBlank(clientId)) {
            return null;
        }
        HxUser hxUser = hxUserService.getByClientId(clientId);
        if (hxUser == null) {
            return null;
        }
        LambdaQueryWrapper<HxUserTaskChainTemplate> lq = Wrappers.lambdaQuery();
        lq.eq(HxUserTaskChainTemplate::getHxUserId, hxUser.getHxUserId());
        lq.eq(HxUserTaskChainTemplate::getTaskChainTemplateId, chainId);
        return getOne(lq);
    }

    @Override
    public void clearLastChainIds(Collection<Long> lastChainIds) {
        if (CollectionUtil.isEmpty(lastChainIds)) {
            return;
        }
        LambdaQueryWrapper<HxUserTaskChainTemplate> lq = Wrappers.lambdaQuery();
        lq.in(HxUserTaskChainTemplate::getLastTaskChainId, lastChainIds);
        List<HxUserTaskChainTemplate> list = list(lq);
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        LambdaUpdateWrapper<HxUserTaskChainTemplate> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.in(HxUserTaskChainTemplate::getId,
                        list.stream().map(HxUserTaskChainTemplate::getId).collect(Collectors.toList()))
                .set(HxUserTaskChainTemplate::getLastTaskChainId, null); // 设置为null
        hxUserTaskChainTemplateService.update(updateWrapper);
    }

    @Override
    public void setSortOrder(UserTaskChainTemplateSortReq req) {
        HxUserTaskChainTemplate entity = getById(req.getId());
        if (entity == null) {
            throw new RuntimeException("用户任务链模板不存在");
        }
        entity.setSortOrder(req.getSortOrder());
        updateById(entity);
    }

    @Override
    public void batchSetSortOrder(UserTaskChainTemplateBatchSortReq req) {
        if (CollectionUtil.isEmpty(req.getSortItems())) {
            return;
        }

        // 1. 提取所有 ID
        List<Long> ids = req.getSortItems().stream()
                .map(UserTaskChainTemplateBatchSortReq.SortItem::getId)
                .collect(Collectors.toList());

        // 2. 查询实体
        List<HxUserTaskChainTemplate> entities = listByIds(ids);
        if (CollectionUtil.isEmpty(entities)) {
            throw new RuntimeException("用户任务链模板不存在");
        }

        // 3. 将 list 转为 Map<ID, Item对象>，方便同时获取两个排序值
        // Function.identity() 或者 item -> item 代表值是对象本身
        Map<Long, UserTaskChainTemplateBatchSortReq.SortItem> itemMap = req.getSortItems().stream()
                .collect(Collectors.toMap(
                        UserTaskChainTemplateBatchSortReq.SortItem::getId,
                        item -> item,
                        (v1, v2) -> v1 // 如果前端传了重复ID，取第一个，防止报错
                ));

        // 4. 遍历实体并赋值
        entities.forEach(entity -> {
            UserTaskChainTemplateBatchSortReq.SortItem item = itemMap.get(entity.getId());
            if (item != null) {
                // 更新 排序值
                if (item.getSortOrder() != null) {
                    entity.setSortOrder(item.getSortOrder());
                }
                // 更新 分组排序值 (新增)
                if (item.getGroupSortOrder() != null) {
                    entity.setGroupSortOrder(item.getGroupSortOrder());
                }
            }
        });

        // 5. 批量更新
        updateBatchById(entities);
    }

    @Override
    public void setGroupSortOrder(UserTaskChainTemplateGroupSortReq req) {
        HxUserTaskChainTemplate entity = getById(req.getId());
        if (entity == null) {
            throw new RuntimeException("用户任务链模板不存在");
        }
        entity.setGroupSortOrder(req.getGroupSortOrder());
        updateById(entity);
    }

}
