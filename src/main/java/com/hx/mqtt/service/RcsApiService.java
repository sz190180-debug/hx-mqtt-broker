package com.hx.mqtt.service;

import com.hx.mqtt.domain.rep.api.AmrData;
import com.hx.mqtt.domain.rep.api.MapVertexesRep;
import com.hx.mqtt.domain.rep.api.TaskChain;
import com.hx.mqtt.domain.rep.api.TaskChainPageRep;
import com.hx.mqtt.domain.req.api.TaskChainAddReq;
import com.hx.mqtt.domain.req.api.TaskChainTemplateQueryReq;

import java.util.List;

public interface RcsApiService {

    /**
     * 在线车辆
     *
     * @return 车辆数据
     */
    List<AmrData> onlineAmr();

    /**
     * 查询任务链信息
     *
     * @param ids id
     * @return 任务链
     */
    List<TaskChain> getTaskChainByIds(Long... ids);

    /**
     * 获取地图点位
     *
     * @param mapId 地图id
     * @return 点位
     */
    MapVertexesRep getMapVertexes(Long mapId);

    /**
     * 任务下发接口
     *
     * @param req 请求
     * @return 任务id
     */
    Integer taskAdd(TaskChainAddReq req);

    /**
     * 车辆暂停接口
     *
     * @param amrId 小车id
     * @return 结果
     */
    String taskPause(Long amrId);

    /**
     * 车辆恢复接口
     *
     * @param amrId 小车id
     * @return 结果
     */
    String taskResume(Long amrId);

    /**
     * 任务取消接口
     *
     * @param taskId 任务id
     */
    Object taskCancel(Long taskId);

    /**
     * 查询任务链模板
     *
     * @param req req
     */
    TaskChainPageRep taskChainTemplateList(TaskChainTemplateQueryReq req);

    /**
     * 根据任务链下发任务
     */
    Integer taskChainTemplateSubmit(Long id);
}
