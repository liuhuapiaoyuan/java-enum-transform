/**
 * @(#)RecordingTaskExamPaperQuestionsVO.java, 4月 13, 2021.
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
public class RecordingTaskExamPaperQuestionsVO {

    @ApiModelProperty("录题任务id")
    private long taskId;

    @ApiModelProperty("题目关联试卷")
    private List<ExamPaperRecordingTaskVO> examPaperRecordingTaskVOS;

    @ApiModelProperty("题目关联散题")
    private List<QuestionRecordingTaskVO> questionRecordingTaskVOS;

    @ApiModelProperty("错题反馈内容")
    private List<RecordingFeedBackVO> recordingFeedBackVOs;

    @ApiModelProperty("审核不通过的题目和试卷数量")
    private int auditNotPassedCount;
}
