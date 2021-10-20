/**
 * @(#)RecordingFeedBackVO.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author linbonan
 */
@Data
public class RecordingFeedBackVO {

    @ApiModelProperty("ID")
    private long id;

    @ApiModelProperty("录题申请ID")
    private long applicationId;

    @ApiModelProperty("反馈的目标ID")
    private long targetId;

    @ApiModelProperty("反馈的目标类型")
    private int targetType;

    @ApiModelProperty("反馈目标所属的试卷ID（如果有）")
    private long examPaperId;

    @ApiModelProperty("反馈分类")
    private int type;

    @ApiModelProperty("反馈原因")
    private List<String> reason;

    @ApiModelProperty("反馈的具体描述")
    private String description;

    @ApiModelProperty("配图")
    private List<String> attachments;

    @ApiModelProperty("创建时间")
    private long createTime;

    @ApiModelProperty("创建人ID")
    private int creator;

    @ApiModelProperty("创建人姓名")
    private String creatorName;
}
