/**
 * @(#)TaggerVO.java, Apr 18, 2021.
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
public class TaggerVO {

    @ApiModelProperty("标注人 id")
    private long taggerId;

    @ApiModelProperty("标注人姓名")
    private String taggerName;

    @ApiModelProperty("标注人手机号")
    private String taggerPhone;
}
