package com.hx.mqtt.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.domain.entity.HxRcsIp;
import com.hx.mqtt.domain.rep.api.*;
import com.hx.mqtt.domain.req.api.TaskChainAddReq;
import com.hx.mqtt.domain.req.api.TaskChainTemplateQueryReq;
import com.hx.mqtt.service.RcsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.hx.mqtt.common.GlobalConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RcsApiServiceImpl implements RcsApiService {

    private final RestTemplate restTemplate;

    @Override
    public List<AmrData> onlineAmr() {
        return get(AMR_ONLINE_AMR);
    }

    @Override
    public List<TaskChain> getTaskChainByIds(Long... ids) {
        String url = String.format(GET_TASK_CHAIN, StrUtil.join(",", (Object) ids));
        return get(url);
    }

    @Override
    public MapVertexesRep getMapVertexes(Long mapId) {
        HxRcsIp hxRcsIp = GlobalCache.RCS_IP.get();
        if (hxRcsIp == null) {
            return null;
        }

        String url = "http://" + hxRcsIp.getIp() + ":" + hxRcsIp.getPort() + String.format(MAP_VERTEXES, mapId);

        HttpEntity<String> entity = new HttpEntity<>(defaultHeaders(hxRcsIp));
        // 使用exchange方法发送带有header的请求
        ResponseEntity<MapVertexesRep> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                MapVertexesRep.class
        );
        return response.getBody() == null ? null : response.getBody();
    }


    @Override
    public Integer taskAdd(TaskChainAddReq req) {
        ApiResponse<Integer> response = post(TASK_ADD, req);
        return response == null ? null : response.getData();
    }

    @Override
    public String taskPause(Long amrId) {
        String url = String.format(TASK_PAUSE, amrId);
        ApiResponse<String> response = post(url, null);
        return response == null ? null : response.getData();
    }

    @Override
    public String taskResume(Long amrId) {
        String url = String.format(TASK_RESUME, amrId);
        ApiResponse<String> response = post(url, null);
        return response == null ? null : response.getData();
    }

    @Override
    public Object taskCancel(Long taskId) {
        String url = String.format(TASK_CANCEL, taskId);
        return post(url, null);
    }

    @Override
    public TaskChainPageRep taskChainTemplateList(TaskChainTemplateQueryReq req) {
        ApiResponse<TaskChainPageRep> response = post(TASK_CHAIN_TEMPLATE_LIST, req);
        return response == null ? null : JSONObject.parseObject(JSONObject.toJSONString(response.getData()),
                TaskChainPageRep.class);
    }

    @Override
    public Integer taskChainTemplateSubmit(Long id) {
        return get(String.format(TASK_CHAIN_ADD, id));
    }

    private <T> T get(String uri, Object... uriVariables) {
        HxRcsIp hxRcsIp = GlobalCache.RCS_IP.get();
        if (hxRcsIp == null) {
            return null;
        }
        String url = "http://" + hxRcsIp.getIp() + ":" + hxRcsIp.getPort() + uri;
        if (!AMR_ONLINE_AMR.equals(uri)) {
            log.info("get url {} request:{}", url, JSONObject.toJSONString(uriVariables));
        }
        HttpEntity<?> entity = new HttpEntity<>(defaultHeaders(hxRcsIp));
        for (int i = 0; i < 3; i++) {
            ResponseEntity<ApiResponse> exchange = restTemplate.exchange(url, HttpMethod.GET, entity,
                    ApiResponse.class,
                    uriVariables);
            ApiResponse body = exchange.getBody();

            if (body == null) {
                log.warn("request get {} fail,body:{}", url, exchange.getBody());
            } else if (!body.isState()) {
                log.warn("request get {} fail,body:{}", url, exchange.getBody());
                return null;
            } else {
                if (!AMR_ONLINE_AMR.equals(uri)) {
                    log.info("get url {} response:{}", url, JSONObject.toJSONString(exchange.getBody()));
                }
                return (T) body.getData();
            }
        }
        return null;
    }

    private <T> ApiResponse<T> post(String uri, Object requestBody) {
        // 1. 验证配置
        HxRcsIp hxRcsIp = GlobalCache.RCS_IP.get();
        if (hxRcsIp == null) {
            log.error("RCS IP配置缺失");
            return null;
        }

        // 2. 构造URL
        String url = String.format("http://%s:%s%s", hxRcsIp.getIp(), hxRcsIp.getPort(), uri);

        log.info("post url {} request:{}", url, JSONObject.toJSONString(requestBody));

        // 3. 准备请求
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, defaultHeaders(hxRcsIp));

        // 4. 发送请求
        ResponseEntity<ApiResponse<T>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<T>>() {
                }
        );

        // 5. 处理响应
        if (response.getBody() == null || !response.getBody().isState()) {
            log.warn("request post {} fail,body:{}", url, response.getBody());
            return null;
        }


        log.info("post url {} response:{}", url, JSONObject.toJSONString(response.getBody()));
        return response.getBody();
    }

    private HttpHeaders defaultHeaders(HxRcsIp hxRcsIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("token", hxRcsIp.getToken());
        headers.set("name", hxRcsIp.getName());
        return headers;
    }
}
