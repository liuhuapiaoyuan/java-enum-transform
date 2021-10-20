/**
 * @(#)RecordingApplicationSearchParamVO.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import static com.imcuttle.bar.enums.SortTypeEnum.SUBMIT_TIME_DESC;

/**
 * @author lukebj
 */
@Data
public class RecordingTagSearchParamVO extends RecordingApplicationBasicSearchParamVO {

    /**
     * @see RecordingTagProcess
     */
    @ApiModelProperty("标注进度 1 全部 2 未完成 3 已完成")
    private int tagProcess;

    @ApiModelProperty("标注人姓名")
    private String taggerName;

    @ApiModelProperty("申请人姓名")
    private String creatorName;

    @ApiModelProperty("标注任务id")
    private long taskId;
}
