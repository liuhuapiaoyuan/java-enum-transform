/**
 * @(#)RecordingApplicationBasicVO.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lukebj
 */
@Data
public class RecordingApplicationAuditResultVO {

    @ApiModelProperty("录题申请ID")
    private long recordingApplicationId;

    /**
     * @see com.imcuttle.enums.FeedbackType
     */
    @ApiModelProperty("审核结果(反馈类型)")
    private int feedbackType;

    @ApiModelProperty("原因(描述)")
    private String description;

    @ApiModelProperty("配图")
    private List<String> attachments;
}
