package com.hx.mqtt.controller.api;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.common.enums.RoleAddTypeEnum;
import com.hx.mqtt.domain.entity.Amr;
import com.hx.mqtt.domain.entity.HxUserAmr;
import com.hx.mqtt.domain.req.amr.AmrAssignRolesReq;
import com.hx.mqtt.domain.req.amr.AmrMappingReq;
import com.hx.mqtt.domain.req.amr.AmrTableUpdateReq;
import com.hx.mqtt.service.AmrService;
import com.hx.mqtt.service.HxUserAmrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/amr/table")
public class AmrTableApiController {

    private final AmrService amrService;
    private final HxUserAmrService hxUserAmrService;

    @PostMapping("/list")
    public HttpResp<List<Amr>> list() {
        return HttpResp.success(amrService.list());
    }

    @PostMapping("/update")
    public HttpResp<Void> update(@RequestBody AmrTableUpdateReq req) {
        Amr amr = amrService.getById(req.getAmrId());
        if (amr == null) {
            throw new RuntimeException("未能找到车辆相关信息");
        }
        if (StringUtils.isNotBlank(req.getAlias())) {
            LambdaQueryWrapper<Amr> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Amr::getAlias, req.getAlias());
            long count = amrService.count(wrapper);
            if (count > 0) {
                throw new RuntimeException("别名已存在");
            }
        }
        amr.setAlias(req.getAlias());
        amrService.updateById(amr);
        return HttpResp.success();
    }

    @PostMapping("/assignRoles")
    public HttpResp<Void> assignRoles(@Valid @RequestBody AmrAssignRolesReq req) {
        for (Long userId : req.getUserIds()) {
            LambdaQueryWrapper<HxUserAmr> lq = Wrappers.lambdaQuery();
            lq.eq(HxUserAmr::getHxUserId, userId);
            List<Long> addIdList = new ArrayList<>();
            if (RoleAddTypeEnum.ADD.getCode().equals(req.getAddType())) {
                Set<Long> idSet =
                        hxUserAmrService.list(lq).stream().map(HxUserAmr::getAmrId).collect(Collectors.toSet());
                for (Long amrId : req.getAmrIds()) {
                    if (idSet.contains(amrId)) {
                        continue;
                    }
                    addIdList.add(amrId);
                }
            } else if (RoleAddTypeEnum.REPLACE.getCode().equals(req.getAddType())) {
                hxUserAmrService.remove(lq);
                addIdList.addAll(req.getAmrIds());
            }
            if (CollectionUtil.isNotEmpty(addIdList)) {
                hxUserAmrService.saveBatch(addIdList.stream().map(id -> {
                    HxUserAmr hxUserAmr = new HxUserAmr();
                    hxUserAmr.setHxUserId(userId);
                    hxUserAmr.setAmrId(id);
                    return hxUserAmr;
                }).collect(Collectors.toList()));
            }
        }

        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/deleteMapping")
    public HttpResp<Void> deleteMapping(@RequestBody AmrMappingReq req) {
        hxUserAmrService.removeById(req.getId());
        return HttpResp.success();
    }
}
