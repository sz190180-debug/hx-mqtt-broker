package com.hx.mqtt.controller.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.common.enums.RoleAddTypeEnum;
import com.hx.mqtt.common.enums.TaskChainTypeEnum;
import com.hx.mqtt.domain.entity.HxUserTaskChainTemplate;
import com.hx.mqtt.domain.entity.TaskChainTemplate;
import com.hx.mqtt.domain.entity.TaskTemplate;
import com.hx.mqtt.domain.rep.api.TaskChainPageRep;
import com.hx.mqtt.domain.rep.api.TaskChainTemplatePo;
import com.hx.mqtt.domain.rep.api.TaskChainTemplateWrapper;
import com.hx.mqtt.domain.req.api.TaskChainTemplateQueryReq;
import com.hx.mqtt.domain.req.chain.*;
import com.hx.mqtt.service.HxUserTaskChainTemplateService;
import com.hx.mqtt.service.RcsApiService;
import com.hx.mqtt.service.TaskChainTemplateService;
import com.hx.mqtt.service.TaskTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/task/chain")
public class TaskChainTemplateApiController {

    private final RcsApiService rcsApiService;
    private final TaskTemplateService taskTemplateService;
    private final TaskChainTemplateService taskChainTemplateService;
    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public HttpResp<Void> add(@RequestBody TaskChainAddReq taskChainAddReq) {
        TaskChainTemplateQueryReq req = new TaskChainTemplateQueryReq();
        req.setCurrentPage(taskChainAddReq.getCurrentPage());
        TaskChainPageRep rep = rcsApiService.taskChainTemplateList(req);
        if (CollectionUtil.isEmpty(rep.getList())) {
            return HttpResp.success();
        }

        List<TaskChainTemplate> templates = new ArrayList<>();
        List<TaskTemplate> taskTemplates = new ArrayList<>();
        for (TaskChainTemplateWrapper wrapper : rep.getList()) {
            TaskChainTemplatePo taskChainTemplatePo = wrapper.getTaskChainTemplatePo();
            TaskChainTemplate taskTemplatePo = new TaskChainTemplate();
            BeanUtils.copyProperties(taskChainTemplatePo, taskTemplatePo);
            taskTemplatePo.setAlias(taskChainTemplatePo.getName());
            templates.add(taskTemplatePo);
            taskTemplates.addAll(wrapper.getTaskTemplatePos().stream().map(v -> {
                TaskTemplate taskTemplate = new TaskTemplate();
                BeanUtils.copyProperties(v, taskTemplate);
                return taskTemplate;
            }).collect(Collectors.toList()));
        }
        taskChainTemplateService.saveOrUpdateBatch(templates);
        taskTemplateService.saveOrUpdateBatch(taskTemplates);
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    public HttpResp<Void> update(@RequestBody TaskChainUpdateReq req) {
        TaskChainTemplate taskChainTemplate = taskChainTemplateService.getById(req.getId());
        if (taskChainTemplate == null) {
            throw new RuntimeException("未能找到任务链模板相关信息");
        }
        if (StringUtils.isNotBlank(req.getAlias())) {
            LambdaQueryWrapper<TaskChainTemplate> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(TaskChainTemplate::getAlias, req.getAlias());
            long count = taskChainTemplateService.count(wrapper);
            if (count > 0) {
                throw new RuntimeException("别名已存在");
            }
        }
        taskChainTemplate.setAlias(req.getAlias());
        taskChainTemplateService.updateById(taskChainTemplate);
        return HttpResp.success();
    }

    @PostMapping("/list")
    public HttpResp<Page<TaskChainTemplate>> list(@RequestBody TaskChainListReq req) {
        // 构建分页参数（页码从1开始）
        Page<TaskChainTemplate> page = new Page<>(req.getPageNum(), req.getPageSize());

        LambdaQueryWrapper<TaskChainTemplate> lq = Wrappers.lambdaQuery();
        lq.like(StrUtil.isNotBlank(req.getName()), TaskChainTemplate::getName, req.getName());
        lq.like(StrUtil.isNotBlank(req.getAlias()), TaskChainTemplate::getAlias, req.getAlias());
        return HttpResp.success(taskChainTemplateService.page(page, lq));
    }

    @GetMapping("/subtasks/{id}")
    public HttpResp<List<TaskTemplate>> subtasks(@PathVariable("id") Long id) {

        LambdaQueryWrapper<TaskTemplate> lq = Wrappers.lambdaQuery();
        lq.like(TaskTemplate::getTaskChainTemplateId, id);
        return HttpResp.success(taskTemplateService.list(lq));
    }

    @PostMapping("/assignRoles")
    public HttpResp<Void> assignRoles(@Valid @RequestBody TaskChainAssignRolesReq req) {
        // 验证任务链类型和仓库信息的匹配性
        validateWarehouseRequirement(req);

        for (Long userId : req.getUserIds()) {
            LambdaQueryWrapper<HxUserTaskChainTemplate> lq = Wrappers.lambdaQuery();
            lq.eq(HxUserTaskChainTemplate::getHxUserId, userId);
            List<Long> addIdList = new ArrayList<>();
            if (RoleAddTypeEnum.ADD.getCode().equals(req.getAddType())) {
                Map<Long, HxUserTaskChainTemplate> idSet =
                        hxUserTaskChainTemplateService.list(lq).stream().collect(Collectors.toMap(HxUserTaskChainTemplate::getTaskChainTemplateId, v -> v,
                                (v1, v2) -> v1));
                for (Long taskChainId : req.getTaskChainIds()) {
                    if (idSet.containsKey(taskChainId)) {
                        HxUserTaskChainTemplate template = idSet.get(taskChainId);
                        template.setWarehouseId(req.getWarehouseId());
                        template.setColumnId(req.getColumnId());
                        hxUserTaskChainTemplateService.updateById(template);
                        continue;
                    }
                    addIdList.add(taskChainId);
                }
            } else if (RoleAddTypeEnum.REPLACE.getCode().equals(req.getAddType())) {
                hxUserTaskChainTemplateService.remove(lq);
                addIdList.addAll(req.getTaskChainIds());
            }
            if (CollectionUtil.isNotEmpty(addIdList)) {
                hxUserTaskChainTemplateService.saveBatch(addIdList.stream().map(id -> {
                    HxUserTaskChainTemplate taskChainTemplate = new HxUserTaskChainTemplate();
                    taskChainTemplate.setHxUserId(userId);
                    taskChainTemplate.setTaskChainTemplateId(id);
                    taskChainTemplate.setWarehouseId(req.getWarehouseId());
                    taskChainTemplate.setColumnId(req.getColumnId());
                    return taskChainTemplate;
                }).collect(Collectors.toList()));
            }
        }

        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/updateGroupName")
    public HttpResp<Void> updateGroupName(@Valid @RequestBody UpdateGroupNameReq req) {
        List<HxUserTaskChainTemplate> list = new ArrayList<>();
        for (Long id : req.getIdList()) {
            HxUserTaskChainTemplate template = new HxUserTaskChainTemplate();
            template.setId(id);
            template.setGroupName(req.getGroupName());
            list.add(template);
        }

        hxUserTaskChainTemplateService.updateBatchById(list);
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/updateGlobalGroupName")
    public HttpResp<Void> updateGlobalGroupName(@Valid @RequestBody GlobalUpdateGroupNameReq req) {
        // 根据模板ID列表，更新所有关联用户的分组名称
        LambdaUpdateWrapper<HxUserTaskChainTemplate> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.in(HxUserTaskChainTemplate::getTaskChainTemplateId, req.getTemplateIds());
        updateWrapper.set(HxUserTaskChainTemplate::getGroupName, req.getGroupName());

        hxUserTaskChainTemplateService.update(updateWrapper);
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/deleteMapping")
    public HttpResp<Void> deleteMapping(@RequestBody TaskChainMappingReq req) {
        hxUserTaskChainTemplateService.removeById(req.getId());
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/batchUpdateType")
    public HttpResp<Void> batchUpdateType(@RequestBody TaskChainUpdateTypeReq req) {
        LambdaUpdateWrapper<TaskChainTemplate> lq = Wrappers.lambdaUpdate();
        lq.in(TaskChainTemplate::getId, req.getIds());
        lq.set(TaskChainTemplate::getChainType, req.getChainType());
        taskChainTemplateService.update(lq);
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/batchDelete")
    public HttpResp<Void> batchDelete(@RequestBody TaskChainBatchDeleteReq req) {
        hxUserTaskChainTemplateService.removeByIds(req.getIds());
        return HttpResp.success();
    }

    /**
     * 验证任务链类型和仓库信息的匹配性
     * 出库入库任务链必须提供仓库信息，普通任务链不需要
     */
    private void validateWarehouseRequirement(TaskChainAssignRolesReq req) {
        // 查询所有任务链模板的类型
        LambdaQueryWrapper<TaskChainTemplate> lq = Wrappers.lambdaQuery();
        lq.in(TaskChainTemplate::getId, req.getTaskChainIds());
        lq.select(TaskChainTemplate::getId, TaskChainTemplate::getChainType, TaskChainTemplate::getName);
        List<TaskChainTemplate> templates = taskChainTemplateService.list(lq);

        for (TaskChainTemplate template : templates) {
            TaskChainTypeEnum chainType = TaskChainTypeEnum.getByCode(template.getChainType());
            if (chainType != null && chainType.requiresWarehouse()) {
                // 出库入库任务链必须提供仓库信息
                if (req.getWarehouseId() == null) {
                    throw new RuntimeException(String.format("任务链模板 '%s' 为%s类型，必须指定仓库信息",
                            template.getName(), chainType.getDescription()));
                }
            }
        }
    }
}
