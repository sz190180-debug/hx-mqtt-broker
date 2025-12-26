package com.hx.mqtt.controller.api;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.domain.entity.HxRcsIp;
import com.hx.mqtt.domain.req.rcs.RcsAddReq;
import com.hx.mqtt.mapper.HxRcsIpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.hx.mqtt.common.GlobalCache.RCS_IP;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rcs")
public class RcsApiController {

    private final HxRcsIpMapper hxRcsIpMapper;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public HttpResp<Void> add(@Valid @RequestBody RcsAddReq rcsAddReq) {
        hxRcsIpMapper.delete(Wrappers.lambdaQuery());
        HxRcsIp hxRcsIp = new HxRcsIp();
        BeanUtils.copyProperties(rcsAddReq, hxRcsIp);
        hxRcsIpMapper.insert(hxRcsIp);
        RCS_IP.set(hxRcsIp);
        return HttpResp.success();
    }

    @GetMapping("/select")
    public HttpResp<HxRcsIp> select() {
        Wrapper<HxRcsIp> lq = Wrappers.lambdaQuery();
        return HttpResp.success(hxRcsIpMapper.selectOne(lq));
    }
}
