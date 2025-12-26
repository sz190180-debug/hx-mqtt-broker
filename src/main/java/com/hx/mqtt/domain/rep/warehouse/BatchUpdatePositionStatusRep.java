package com.hx.mqtt.domain.rep.warehouse;

import lombok.Data;

import java.util.List;

/**
 * 批量更新点位状态响应
 */
@Data
public class BatchUpdatePositionStatusRep {

    /**
     * 成功更新的点位数量
     */
    private Integer successCount;

    /**
     * 失败的点位数量
     */
    private Integer failCount;

    /**
     * 成功更新的点位ID列表
     */
    private List<Long> successPositionIds;

    /**
     * 失败的点位ID列表
     */
    private List<Long> failPositionIds;

    /**
     * 失败原因列表
     */
    private List<String> failReasons;

    /**
     * 总处理数量
     */
    private Integer totalCount;

    /**
     * 操作摘要
     */
    private String summary;

    public BatchUpdatePositionStatusRep() {
        this.successCount = 0;
        this.failCount = 0;
        this.totalCount = 0;
    }

    /**
     * 生成操作摘要
     */
    public void generateSummary() {
        this.summary = String.format("总共处理 %d 个点位，成功 %d 个，失败 %d 个", 
                                    totalCount, successCount, failCount);
    }
}
