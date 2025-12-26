package com.hx.mqtt.controller.api;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.common.MqttClientManager;
import com.hx.mqtt.domain.entity.HxUser;
import com.hx.mqtt.domain.rep.user.HxUserRep;
import com.hx.mqtt.domain.rep.user.UserAmrRep;
import com.hx.mqtt.domain.rep.user.UserTaskChainTemplateRep;
import com.hx.mqtt.domain.req.user.*;
import com.hx.mqtt.mapper.HxUserMapper;
import com.hx.mqtt.service.HxUserAmrService;
import com.hx.mqtt.service.HxUserTaskChainTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserApiController {

    private final HxUserMapper hxUserMapper;
    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;
    private final HxUserAmrService hxUserAmrService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public HttpResp<Void> add(@Valid @RequestBody UserAddReq userAddReq) {
        HxUser hxUser = new HxUser();
        BeanUtils.copyProperties(userAddReq, hxUser);
        hxUserMapper.insert(hxUser);
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/edit")
    public HttpResp<Void> edit(@Valid @RequestBody UserEditReq userEditReq) {
        LambdaUpdateWrapper<HxUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(HxUser::getHxUserId, userEditReq.getHxUserId());

        // 只更新非null字段
        HxUser hxUser = new HxUser();
        BeanUtils.copyProperties(userEditReq, hxUser);
        hxUserMapper.update(hxUser, wrapper);

        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delete")
    public HttpResp<Void> delete(@Valid @RequestBody UserDeleteReq req) {
        hxUserMapper.deleteById(req.getHxUserId());
        return HttpResp.success();
    }

    @PostMapping("/select")
    public HttpResp<List<HxUserRep>> select(@RequestBody UserPageReq req) {
        LambdaQueryWrapper<HxUser> lq = Wrappers.lambdaQuery();
        lq.like(StrUtil.isNotBlank(req.getClientId()), HxUser::getClientId, req.getClientId());
        lq.like(StrUtil.isNotBlank(req.getRemark()), HxUser::getRemark, req.getRemark());
        return HttpResp.success(hxUserMapper.selectList(lq).stream().map(user -> {
            HxUserRep hxUserRep = new HxUserRep();
            BeanUtils.copyProperties(user, hxUserRep);
            hxUserRep.setIsOnline(MqttClientManager.isOnline(hxUserRep.getClientId()));
            return hxUserRep;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/select/taskChain")
    public HttpResp<IPage<UserTaskChainTemplateRep>> selectTaskChain(@Valid @RequestBody UserTaskChainTemplateReq req) {
        return HttpResp.success(hxUserTaskChainTemplateService.selectTaskChain(req));
    }

    @PostMapping("/select/amr")
    public HttpResp<IPage<UserAmrRep>> selectAmr(@Valid @RequestBody UserAmrReq req) {
        return HttpResp.success(hxUserAmrService.selectAmr(req));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/taskChain/setSortOrder")
    public HttpResp<Void> setTaskChainSortOrder(@Valid @RequestBody UserTaskChainTemplateSortReq req) {
        hxUserTaskChainTemplateService.setSortOrder(req);
        return HttpResp.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/taskChain/batchSetSortOrder")
    public HttpResp<Void> batchSetTaskChainSortOrder(@Valid @RequestBody UserTaskChainTemplateBatchSortReq req) {
        hxUserTaskChainTemplateService.batchSetSortOrder(req);
        return HttpResp.success();
    }
}
