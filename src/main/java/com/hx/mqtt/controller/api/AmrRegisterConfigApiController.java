package com.hx.mqtt.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.domain.entity.AmrRegisterConfig;
import com.hx.mqtt.domain.req.amr.AmrRegisterConfigAddReq;
import com.hx.mqtt.domain.req.amr.AmrRegisterConfigPageReq;
import com.hx.mqtt.domain.req.amr.AmrRegisterConfigUpdateReq;
import com.hx.mqtt.service.AmrRegisterConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/amr/register")
@RequiredArgsConstructor
public class AmrRegisterConfigApiController {

    private final AmrRegisterConfigService configService;

    @PostMapping("/list")
    public HttpResp<Page<AmrRegisterConfig>> list(@RequestBody AmrRegisterConfigPageReq req) {
        Page<AmrRegisterConfig> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<AmrRegisterConfig> wrapper = Wrappers.lambdaQuery();

        // 如果前端传了amrId，则进行筛选
        if (req.getAmrId() != null) {
            wrapper.eq(AmrRegisterConfig::getAmrId, req.getAmrId());
        }

        wrapper.orderByDesc(AmrRegisterConfig::getCreateTime);
        return HttpResp.success(configService.page(page, wrapper));
    }

    @PostMapping("/add")
    public HttpResp<Void> add(@Valid @RequestBody AmrRegisterConfigAddReq req) {
        AmrRegisterConfig config = new AmrRegisterConfig();
        BeanUtils.copyProperties(req, config);
        configService.save(config);
        return HttpResp.success();
    }

    @PostMapping("/update")
    public HttpResp<Void> update(@Valid @RequestBody AmrRegisterConfigUpdateReq req) {
        AmrRegisterConfig config = new AmrRegisterConfig();
        BeanUtils.copyProperties(req, config);
        configService.updateById(config);
        return HttpResp.success();
    }

    @PostMapping("/delete")
    public HttpResp<Void> delete(@RequestBody List<Long> ids) {
        configService.removeByIds(ids);
        return HttpResp.success();
    }
}