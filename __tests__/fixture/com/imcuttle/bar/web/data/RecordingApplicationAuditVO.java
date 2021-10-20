/**
 * @(#)RecordingApplicationBasicVO.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lukebj
 */
@Data
public class RecordingApplicationAuditVO extends RecordingApplicationBasicVO {

    @ApiModelProperty("创建人ID")
    private int creator;

    @ApiModelProperty("创建人名称")
    private String creatorName;
}
