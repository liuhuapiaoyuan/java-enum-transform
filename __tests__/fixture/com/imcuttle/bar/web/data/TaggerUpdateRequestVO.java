/**
 * @(#)TaggerUpdateRequestVO.java, Apr 12, 2021.
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
public class TaggerUpdateRequestVO {

    @ApiModelProperty("要更新标注人的任务id")
    private long taskId;

    @ApiModelProperty("是否是退领")
    private boolean cancelTagger;
}
