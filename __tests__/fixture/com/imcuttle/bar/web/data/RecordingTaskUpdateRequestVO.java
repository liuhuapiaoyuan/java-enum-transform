/**
 * @(#)RecordingTaskUpdateRequestVO.java, Apr 16, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tangjucai
 */
@Data
public class RecordingTaskUpdateRequestVO {

    @ApiModelProperty("录题申请 id")
    private long applicationId;

    @ApiModelProperty("应录题数")
    private int estimatedQuestionNum;

    @ApiModelProperty("录题人 id")
    private int recorderId;

    @ApiModelProperty("审核人 id")
    private int auditorId;
}
