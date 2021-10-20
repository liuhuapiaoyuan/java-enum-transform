/**
 * @(#)RecordingTaskVO.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author xiechao01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecordingTaskVO {

    @ApiModelProperty("录题任务 id")
    private long taskId;

    @ApiModelProperty("录题申请 id")
    private long applicationId;

    @ApiModelProperty("录题任务 阶段")
    private int phaseId;

    @ApiModelProperty("录题任务 科目")
    private int subjectId;

    @ApiModelProperty("录题任务 状态")
    private int stage;

    @ApiModelProperty("录题人")
    private RecorderVO recorder;

    @ApiModelProperty("审核人")
    private AuditorVO auditor;

    @ApiModelProperty("标注人")
    private TaggerVO tagger;

    @ApiModelProperty("应录题数")
    private int estimatedQuestionNum;

    @ApiModelProperty("任务录入方式")
    private int recordingMode;

    @ApiModelProperty("提交次数(等价于审核轮次)")
    private int submitTimes;

    // 录题申请属性

    @ApiModelProperty("录题任务关联的录题申请 名称")
    private String name;

    @ApiModelProperty("录题任务关联的录题申请 题目来源")
    private int questionSource;

    @ApiModelProperty("录题任务关联的录题申请 申请人")
    private String creator;

    @ApiModelProperty("录题任务关联的录题申请 申请时间")
    private long submitTime;

    // 其它关联属性

    @ApiModelProperty("录题数")
    private int questionNum;

    @ApiModelProperty("纠错阶段: 审核阶段纠错、标注阶段纠错")
    private String feedbackStageName;

    @ApiModelProperty("错误反馈数量 key的枚举值： 2 题目，3 试卷，4 录入任务")
    private Map<Integer, Integer> feedbackType2Count;

    @ApiModelProperty("标注阶段处理过的题数")
    private int tagProcessedQuestionNum;

    @ApiModelProperty("审核阶段处理过的题数")
    private int auditProcessedQuestionNum;

    @ApiModelProperty("审核阶段处理过的试卷数")
    private int auditProcessedExamPaperNum;

    @ApiModelProperty("录入的试卷数")
    private int examPaperNum;
}
