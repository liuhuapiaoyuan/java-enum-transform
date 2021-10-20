/**
 * @(#)RoleAttributeVO.java, Apr 15, 2021.
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
public class RoleAttributeVO {

    @ApiModelProperty("阶段 id")
    private int phaseId;

    @ApiModelProperty("科目 id")
    private int subjectId;
}
