/**
 * @(#)RecordingTaskTagVO.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xiechao01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecordingQuestionVO {

    @ApiModelProperty("题目id,大题带小问只传大题id")
    private int questionId;

    @ApiModelProperty("标注状态")
    private int tagStatus;

    @ApiModelProperty("题目来源: 1-新录入 2-已有题目")
    private int questionOrigin;

    @ApiModelProperty("录题任务 id")
    private long taskId;

    @ApiModelProperty("试卷 id，如果是散题模式为0")
    private long examPaperId;

    @ApiModelProperty("错题反馈")
    private List<RecordingFeedBackVO> recordingFeedBackVOS;
}
