/**
 * @(#)RecordingStageUtil.java, 4月 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.utils;

import com.imcuttle.enums.ExamPaperRecordingTaskStage;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;

import static com.imcuttle.enums.QuestionRecordingTaskStage.AUDIT_APPROVED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.AUDIT_FAILED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.PUBLISHED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.TAG_FAILED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.TAG_FINISHED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.TO_BE_AUDITED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.TO_BE_TAGGED;

/**
 * @author linbonan
 */
public class RecordingStageUtil {

    /**
     * 题目是否已经被审核员处理过(不区分审核结果)
     */
    public static boolean hasBeenProcessedByAuditor(QuestionRecordingTask questionRecordingTask) {
        return QuestionRecordingTaskStage.findByInt(questionRecordingTask.getStage())
                .map(stage -> (stage == AUDIT_APPROVED || stage == AUDIT_FAILED
                        || stage == TO_BE_TAGGED || stage == TAG_FINISHED || stage == TAG_FAILED // 能进入标注阶段意味着已经经过了审核处理
                        || stage == PUBLISHED))
                .orElse(false);
    }

    /**
     * 试卷是否已经被审核员处理过(不区分审核结果)
     */
    public static boolean hasBeenProcessedByAuditor(ExamPaperRecordingTask examPaperRecordingTask) {
        return ExamPaperRecordingTaskStage.findByInt(examPaperRecordingTask.getStage())
                .map(stage -> (stage == ExamPaperRecordingTaskStage.AUDIT_APPROVED
                        || stage == ExamPaperRecordingTaskStage.AUDIT_FAILED
                        || stage == ExamPaperRecordingTaskStage.PUBLISHED))
                .orElse(false);
    }

    /**
     * 题目是否已经被标注员处理过(不区分结果)
     */
    public static boolean hasBeenProcessedByTagger(QuestionRecordingTask questionRecordingTask) {
        return QuestionRecordingTaskStage.findByInt(questionRecordingTask.getStage())
                .map(stage -> (stage == TAG_FINISHED || stage == TAG_FAILED || stage == PUBLISHED))
                .orElse(false);
    }

    /**
     * 题目是否审核通过
     */
    public static boolean auditApproved(QuestionRecordingTask questionRecordingTask) {
        return QuestionRecordingTaskStage.findByInt(questionRecordingTask.getStage())
                .map(stage -> (stage == AUDIT_APPROVED
                        || stage == TO_BE_TAGGED || stage == TAG_FINISHED || stage == TAG_FAILED // 能进入标注阶段意味着已经经过了审核处理
                        || stage == PUBLISHED))
                .orElse(false);
    }

    /**
     * 试卷是否审核通过
     */
    public static boolean auditApproved(ExamPaperRecordingTask examPaperRecordingTask) {
        return ExamPaperRecordingTaskStage.findByInt(examPaperRecordingTask.getStage())
                .map(stage -> (stage == ExamPaperRecordingTaskStage.AUDIT_APPROVED
                        || stage == ExamPaperRecordingTaskStage.PUBLISHED))
                .orElse(false);
    }

    /**
     * 题目是否能审核
     */
    public static boolean canAudit(QuestionRecordingTask questionRecordingTask) {
        return QuestionRecordingTaskStage.findByInt(questionRecordingTask.getStage())
                .map(stage -> (stage == AUDIT_APPROVED
                        || stage == TO_BE_AUDITED || stage == AUDIT_FAILED
                        || stage == TO_BE_TAGGED || stage == TAG_FINISHED)) // 可以返回上一题多次审核 或 标注阶段回退
                .orElse(false);
    }

    /**
     * 试卷是否能被审核
     */
    public static boolean canAudit(ExamPaperRecordingTask examPaperRecordingTask) {
        return ExamPaperRecordingTaskStage.findByInt(examPaperRecordingTask.getStage())
                .map(stage -> (stage == ExamPaperRecordingTaskStage.AUDIT_APPROVED // 可以返回上一试卷多次审核 或 标注阶段回退
                        || stage == ExamPaperRecordingTaskStage.AUDIT_FAILED
                        || stage == ExamPaperRecordingTaskStage.TO_BE_AUDITED))
                .orElse(false);
    }

    private RecordingStageUtil() {
    }
}
