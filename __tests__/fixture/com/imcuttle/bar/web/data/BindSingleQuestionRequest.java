/**
 * @(#)BindSingleQuestionRequest.java, 4月 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author chenyibo
 */
@Data
public class BindSingleQuestionRequest {

    @ApiModelProperty("录题任务id")
    private long taskId;

    @ApiModelProperty("题目id")
    private int questionId;

    @ApiModelProperty("题目原图地址")
    private List<String> snapshotImgIds;

    @ApiModelProperty("套卷id")
    private long examPaperId;
}
