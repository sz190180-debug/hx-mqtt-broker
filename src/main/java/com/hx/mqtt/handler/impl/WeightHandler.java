package com.hx.mqtt.handler.impl;

import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.req.api.BasePageMqttReq;
import com.hx.mqtt.domain.req.mqtt.WeightReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class WeightHandler implements MqttTopicHandler {

    // 历史记录队列
    private final LinkedList<Double> weightHistory = new LinkedList<>();

    // 显式锁，替代 synchronized，性能更好且逻辑更清晰
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.WEIGHT_REQ;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {

        WeightReq weightReq = WeightReq.fromJson(payload);

        // 1. 使用 ThreadLocalRandom 避免多线程竞争 Random 实例
        // 模拟 0kg 到 5kg 之间的波动
//        double simulatedWeight = ThreadLocalRandom.current().nextDouble() * 5.0;

        // 保留两位小数
//        simulatedWeight = (double) Math.round(simulatedWeight * 100) / 100;

        Double simulatedWeight = weightReq.getWeight();

        log.info("收到重量主题消息，模拟生成重量: {} kg", simulatedWeight);

        // 2. 加锁处理历史数据
        lock.lock();
        try {
            // 更新历史记录，只保留最近 3 次
            if (weightHistory.size() >= 3) {
                weightHistory.removeFirst();
            }
            weightHistory.add(simulatedWeight);

            // 3. 判断逻辑
            boolean isStable = false;
            boolean isHeavyEnough = simulatedWeight > 1.0;

            if (weightHistory.size() == 3) {
                // 计算最近三次的最大误差
                double max = weightHistory.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                double min = weightHistory.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                double error = max - min;

                if (error <= 1.0) {
                    isStable = true;
                }
                log.debug("最近三次重量: {}, 误差: {}, 是否稳定: {}", weightHistory, error, isStable);
            }

            // 4. 更新全局缓存 (原子操作)
            if (isStable && isHeavyEnough) {
                GlobalCache.setCurrentWeight(simulatedWeight);
                log.info(">>> 重量有效，写入全局缓存: {}", simulatedWeight);
            } else {
                GlobalCache.setCurrentWeight(0.0); // 置为0或无效值
            }

        } finally {
            // 必须在 finally 中释放锁
            lock.unlock();
        }

        return MqttResp.success(context.getReqId());
    }
}