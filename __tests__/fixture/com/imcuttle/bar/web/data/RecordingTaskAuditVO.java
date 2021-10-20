/**
 * @(#)RecordingTaskAuditVO.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author chenkangbj
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordingTaskAuditVO {

    // 录题任务自身的属性

    @ApiModelProperty("录题任务 ID")
    private long id;

    @ApiModelProperty("录题任务 阶段")
    private String phase;

    @ApiModelProperty("录题任务 科目")
    private int subject;

    @ApiModelProperty("录题任务 实际录题数（不包括试卷）")
    private int questionNum;

    @ApiModelProperty("录题任务 状态")
    private int stage;

    @ApiModelProperty("录题任务 关联的录题申请ID")
    private long applicationId;

    @ApiModelProperty("提交次数(等价于审核轮次)")
    private int submitTimes;

    // 录题申请属性

    @ApiModelProperty("录题任务关联的录题申请 名称")
    private String name;

    @ApiModelProperty("录题任务关联的录题申请 题目来源")
    private int questionSource;

    @ApiModelProperty("录题任务关联的录题申请 申请人名称")
    private String creator;

    @ApiModelProperty("录题任务关联的录题申请 申请时间")
    private long submitTime;

    // 其它关联属性

    @ApiModelProperty("已审核数")
    private int auditedCount;

    @ApiModelProperty("应审核数（审核任务下题目 + 试卷数）")
    private int needAuditCount;

    @ApiModelProperty("已审核目标中 审核通过数")
    private int passedCount;

    @ApiModelProperty("已审核目标中 审核不通过数")
    private int notPassedCount;

    @ApiModelProperty("纠错阶段: 审核阶段纠错、标注阶段纠错")
    private String feedbackStageName;

    @ApiModelProperty("错误反馈数量 key的枚举值： 2 题目，3 试卷，4 录入任务")
    private Map<Integer, Integer> feedbackType2Count;
}
