/**
 * @(#)QuestionFeedbackDeleteRequestVO.java, Apr 16, 2021.
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

/**
 * @author xiechao01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionFeedbackDeleteRequestVO {

    @ApiModelProperty("录题任务 ID")
    private long taskId;

    @ApiModelProperty("题目id")
    private int questionId;

    @ApiModelProperty("试卷id 如果是套卷录题任务需要传")
    private long examPaperId;
}
