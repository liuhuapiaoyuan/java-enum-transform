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

/**
 * @author chenkangbj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordingAuditResourceVO {

    @ApiModelProperty("目标id")
    private long targetId;

    @ApiModelProperty("题目所属的试卷id")
    private long examPaperId;

    /**
     * @see com.imcuttle.enums.FeedbackTargetType
     */
    @ApiModelProperty("目标类型 2 题目 3 试卷")
    private int targetType;

    @ApiModelProperty("题目来源 仅当targetType为题目时有数据")
    private int questionOrigin;

    @ApiModelProperty("当前目标 在审核任务中的状态")
    private int stage;
}
