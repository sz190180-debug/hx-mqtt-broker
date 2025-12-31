package com.hx.mqtt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.HxUserTaskChainTemplate;
import com.hx.mqtt.domain.rep.user.UserTaskChainTemplateRep;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateGroupSortReq;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateReq;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateSortReq;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateBatchSortReq;

import javax.validation.Valid;
import java.util.Collection;

public interface HxUserTaskChainTemplateService extends IService<HxUserTaskChainTemplate> {
    IPage<UserTaskChainTemplateRep> selectTaskChain(@Valid UserTaskChainTemplateReq req);

    HxUserTaskChainTemplate selectByClientIdAndChainId(String clientId, Long chainId);

    void clearLastChainIds(Collection<Long> lastChainIds);

    /**
     * 设置任务链模板排序
     *
     * @param req 排序请求
     */
    void setSortOrder(UserTaskChainTemplateSortReq req);

    /**
     * 批量设置任务链模板排序
     *
     * @param req 批量排序请求
     */
    void batchSetSortOrder(UserTaskChainTemplateBatchSortReq req);

    /**
     * 设置任务链模板排序
     *
     * @param req 排序请求
     */
    void setGroupSortOrder(UserTaskChainTemplateGroupSortReq req);

}
