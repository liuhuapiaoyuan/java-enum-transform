/**
 * @(#)BindBatchQuestionRequest.java, 7月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zhangzhikuan
 */
@Data
public class BindBatchQuestionRequest {

    @ApiModelProperty("录题任务id")
    private long taskId;

    @ApiModelProperty("题目ids")
    private List<Integer> questionIds;

    @ApiModelProperty("题目原图地址")
    private Map<Integer, List<String>> questionId2SnapshotImgIds;

    @ApiModelProperty("套卷id")
    private long examPaperId;
}
