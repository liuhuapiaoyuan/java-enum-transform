/**
 * @(#)QuestionRecordingTaskVO.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author chenyibo
 */
@Data
public class QuestionRecordingTaskVO {

    @ApiModelProperty("id")
    private long id;

    @ApiModelProperty("录题申请id")
    private long applicationId;

    @ApiModelProperty("录题任务id")
    private long taskId;

    @ApiModelProperty("所属试卷id 散题为0")
    private long examPaperId;

    @ApiModelProperty("题目id")
    private int questionId;

    @ApiModelProperty("题目来源")
    private int questionOrigin;

    @ApiModelProperty("状态")
    private int stage;

    @ApiModelProperty("顺序")
    private int ordinal;
}
