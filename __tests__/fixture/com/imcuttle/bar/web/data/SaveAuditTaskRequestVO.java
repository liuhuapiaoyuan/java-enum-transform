/**
 * @(#)SaveAuditTaskRequestVO.java, Apr 13, 2021.
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
public class SaveAuditTaskRequestVO {

    // 审核相关

    @ApiModelProperty("审核任务ID")
    private long taskId;

    @ApiModelProperty("题目所属的试卷ID")
    private long examPaperId;

    @ApiModelProperty("实体ID（题目ID / 试卷ID）")
    private long targetId;

    @ApiModelProperty("目标类型（2 题目 3 试卷）")
    private int targetType;

    @ApiModelProperty("审核结果（通过 / 不通过）")
    private boolean passed;

    // 反馈相关

    @ApiModelProperty("反馈原因")
    private List<String> reason;

    @ApiModelProperty("反馈的具体描述")
    private String description;

    @ApiModelProperty("配图")
    private List<String> attachments;
}
