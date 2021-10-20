/**
 * @(#)RecordingApplicationBriefVO.java, 4月 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author linbonan
 */
@Data
public class RecordingApplicationProgressVO extends RecordingApplicationBriefVO {

    @ApiModelProperty("任务提交审核的轮次(包含多个任务时取最大)")
    private int taskSubmitTimes;

    @ApiModelProperty("总耗时(毫秒)")
    private long completeDurationInMilliseconds;

    @ApiModelProperty("该录题申请下的录题任务")
    private List<RecordingTaskVO> tasks;
}
