/**
 * @(#)SubmitAuditTaskRequestVO.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenkangbj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitAuditTaskRequestVO {

    @ApiModelProperty("任务审核结果 是否通过")
    private boolean passed;

    @ApiModelProperty("原因")
    private String reason;
}
