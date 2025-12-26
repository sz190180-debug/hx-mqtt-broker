package com.hx.mqtt.common;

public class GlobalConstant {

    public final static String SUBSCRIBE_ID = "subscriberClientId";

    public final static String GLOBAL_PATH = "/iot";

    public final static String TOPIC_PATTERN = GLOBAL_PATH + "/+/req/#";

    public final static String AMR_ONLINE_AMR = "/amr/onlineAmr";

    public final static String MAP_VERTEXES = "/api/map/getMapVertexes/%s";

    public final static String TASK_ADD = "/api/task/add";

    public final static String TASK_CHAIN_ADD = "/api/taskChainTemplate/submit/%s";

    public final static String TASK_CANCEL = "/api/task/cancel/%s";

    public final static String TASK_PAUSE = "/amr/taskCommand/pause/%s";

    public final static String TASK_RESUME = "/amr/taskCommand/resume/%s";

    public final static String GET_TASK_CHAIN = "/api/task/getTaskChainByIds?taskChainIds=%s";

    public final static String TASK_CHAIN_TEMPLATE_LIST = "/api/taskChainTemplate/list";
}
