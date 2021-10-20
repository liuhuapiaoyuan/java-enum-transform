/**
 * @(#)RecordingAuditResourceVO.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenkangbj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordingAuditResourceListVO {

    @ApiModelProperty("审核任务下的资源列表")
    private List<RecordingAuditResourceVO> resources;
}
