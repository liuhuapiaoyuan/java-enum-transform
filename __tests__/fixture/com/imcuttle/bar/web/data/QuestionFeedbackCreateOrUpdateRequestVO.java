/**
 * @(#)RecordingFeedBackCreateRequestVO.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xiechao01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionFeedbackCreateOrUpdateRequestVO {

    @ApiModelProperty("录题申请ID")
    private long applicationId;

    @ApiModelProperty("录题任务 ID")
    private long taskId;

    @ApiModelProperty("题目id")
    private int questionId;

    @ApiModelProperty("试卷id 如果是套卷录题任务需要传")
    private long examPaperId;

    @ApiModelProperty("反馈原因")
    private List<String> reason;

    @ApiModelProperty("反馈的具体描述")
    private String description;

    @ApiModelProperty("配图")
    private List<String> attachments;
}
