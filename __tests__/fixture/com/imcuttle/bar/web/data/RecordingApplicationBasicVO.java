/**
 * @(#)RecordingApplicationBasicVO.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linbonan
 */
@Data
public class RecordingApplicationBasicVO {

    @ApiModelProperty("ID")
    private long id;

    @ApiModelProperty("学段ID")
    private int phaseId;

    @ApiModelProperty("学科ID")
    private int subjectId;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("题目来源: 1-外部题源 2-辅导自编")
    private int questionSource;

    @ApiModelProperty("申请时间")
    private long submitTime;

    @ApiModelProperty("保密日期")
    private long secrecyEndTime;

    @ApiModelProperty("发布时间")
    private long publishTime;

    @ApiModelProperty("进度")
    private int stage;

    @ApiModelProperty("原件名称")
    private String fileName;

    @ApiModelProperty("题目原件的ossKey")
    private String resourceId;

    @ApiModelProperty("应录题数")
    private int estimatedQuestionNum;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人ID")
    private int creator;
}
