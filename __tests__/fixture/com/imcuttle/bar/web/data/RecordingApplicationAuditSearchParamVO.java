/**
 * @(#)RecordingApplicationSearchParamVO.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import static com.imcuttle.bar.enums.SortTypeEnum.SUBMIT_TIME_DESC;

/**
 * @author lukebj
 */
@Data
public class RecordingApplicationAuditSearchParamVO extends RecordingApplicationBasicSearchParamVO {

    @ApiModelProperty("录题申请的创建人姓名")
    private String creatorName;
}
