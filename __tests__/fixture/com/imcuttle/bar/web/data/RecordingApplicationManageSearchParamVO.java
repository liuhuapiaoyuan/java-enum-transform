/**
 * @(#)RecordingApplicationManageSearchParamVO.java, 4月 14, 2021.
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
public class RecordingApplicationManageSearchParamVO extends RecordingApplicationBasicSearchParamVO {

    @ApiModelProperty("录题申请的创建人(姓名)")
    private String creatorName;

    @ApiModelProperty("录题人(姓名)")
    private String recorderName;

    @ApiModelProperty("审核人(姓名)")
    private String auditorName;

    @ApiModelProperty("标注人(姓名)")
    private String taggerName;

    @ApiModelProperty("录题任务的状态")
    private int taskStage;

}
