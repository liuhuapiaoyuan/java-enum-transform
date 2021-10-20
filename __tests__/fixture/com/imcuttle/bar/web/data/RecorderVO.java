/**
 * @(#)RecorderVO.java, Apr 15, 2021.
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
public class RecorderVO {

    @ApiModelProperty("录题人 id")
    private long recorderId;

    @ApiModelProperty("录题人姓名")
    private String recorderName;

    @ApiModelProperty("录题人手机号")
    private String recorderPhone;
}
