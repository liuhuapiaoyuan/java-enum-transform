/**
 * @(#)NewRecordingTaskParamVO.java, Apr 15, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author duanou
 */
@Data
public class RecordingTaskCreateRequestVO {

    @ApiModelProperty("录题申请 id")
    private long applicationId;

    @ApiModelProperty("应录题数")
    private int estimatedQuestionNum;

    @ApiModelProperty("录入方式")
    private int recordingMode;

    @ApiModelProperty("录题人 id")
    private int recorderId;

    @ApiModelProperty("审核人 id")
    private int auditorId;
}
