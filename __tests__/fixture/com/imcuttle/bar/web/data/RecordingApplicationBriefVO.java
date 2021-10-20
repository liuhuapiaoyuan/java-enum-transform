/**
 * @(#)RecordingApplicationBriefVO.java, 4月 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linbonan
 */
@Data
public class RecordingApplicationBriefVO extends RecordingApplicationBasicVO {

    @ApiModelProperty("创建人名称")
    private String creatorName;

    @ApiModelProperty("已录入的题目数量")
    private int recordedQuestionCount;

    @ApiModelProperty("已录入的试卷数量")
    private int recordedExamPaperCount;
}
