/**
 * @(#)RecordingTagVO.java, Apr 12, 2021.
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
public class RecordingTagVO {

    // 录题任务自身的属性

    @ApiModelProperty("录题任务 ID")
    private long id;

    @ApiModelProperty("录题任务 阶段")
    private String phase;

    @ApiModelProperty("录题任务 科目")
    private int subject;

    @ApiModelProperty("录题任务 标注人名称")
    private String tagger;

    @ApiModelProperty("录题任务 关联的录题申请ID")
    private long applicationId;

    // 录题申请属性

    @ApiModelProperty("录题任务关联的录题申请 名称")
    private String name;

    @ApiModelProperty("录题任务关联的录题申请 题目来源")
    private int questionSource;

    @ApiModelProperty("录题任务关联的录题申请 申请人名称")
    private String creator;

    @ApiModelProperty("录题任务关联的录题申请 申请时间")
    private long submitTime;

    // 其他关联属性

    @ApiModelProperty("已标注 + 放弃标注数")
    private int taggedCount;

    @ApiModelProperty("应标注数（录题任务的录题数）")
    private int shouldTagCount;

    @ApiModelProperty("错题反馈数")
    private int feedbackCount;
}
