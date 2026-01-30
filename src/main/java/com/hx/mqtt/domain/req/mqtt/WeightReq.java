package com.hx.mqtt.domain.req.mqtt;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
public class WeightReq {

    /**
     * 目标点编码
     * 示例值：A1
     */
    private Double weight;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的MqttAccountRequest对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static WeightReq fromJson(String jsonStr) {
        try {
            // 假设 jsonStr 是你收到的原始字符串
            JSONObject parse = JSONObject.parseObject(jsonStr);

            // 1. 获取 weight 数组
            JSONArray weightArray = parse.getJSONArray("weight");

            WeightReq weightReq = new WeightReq();
            if (weightArray != null) {
                // 2. 转换为 ASCII 字符串
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < weightArray.size(); i++) {
                    sb.append((char) weightArray.getIntValue(i));
                }
                String decodedStr = sb.toString();

                // 3. 提取中间的 Double (正则匹配)
                Pattern pattern = Pattern.compile("-?\\d+\\.\\d+");
                Matcher matcher = pattern.matcher(decodedStr);

                if (matcher.find()) {
                    double rawValue = Double.parseDouble(matcher.group());

                    double result = new BigDecimal(rawValue)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();

                    weightReq.setWeight(result);
                }
            }

            return weightReq;
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的JSON格式", e);
        }
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @return JSON格式的字符串
     */
    public String toJson() {
        return JSONObject.toJSONString(this);
    }

}
