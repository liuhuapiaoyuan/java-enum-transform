/**
 * @(#)RecordingTaskUtil.java, 5月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.util;

import com.imcuttle.enums.ExamPaperRecordingTaskStage;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.enums.RecordingMode;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.utils.RecordingStageUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.imcuttle.enums.RecordingMode.EXAM_PAPER;
import static com.imcuttle.enums.RecordingMode.SINGLE_QUESTION;
import static com.imcuttle.bar.util.CollectionUtil.getOrEmpty;

/**
 * @author linbonan
 */
@Slf4j
public class RecordingTaskUtil {

    public static int getNotAuditPassedCount(RecordingMode mode,
                                  List<QuestionRecordingTask> questionRelations,
                                  List<ExamPaperRecordingTask> paperRelations) {
        if (EXAM_PAPER == mode) {
            int questionNotPassedCount = (int) getOrEmpty(questionRelations).stream()
                    .filter(task -> !RecordingStageUtil.auditApproved(task))
                    .filter(RecordingStageUtil::hasBeenProcessedByAuditor)
                    .count();
            int paperNotPassedCount = (int) getOrEmpty(paperRelations).stream()
                    .filter(task -> !RecordingStageUtil.auditApproved(task))
                    .filter(RecordingStageUtil::hasBeenProcessedByAuditor)
                    .count();
            return paperNotPassedCount + questionNotPassedCount;
        }

        if (SINGLE_QUESTION == mode) {
            return (int) getOrEmpty(questionRelations).stream()
                    .filter(task -> !RecordingStageUtil.auditApproved(task))
                    .filter(RecordingStageUtil::hasBeenProcessedByAuditor)
                    .count();
        }

        log.error("Unsupported RecordingMode = {}!", mode);
        return 0;
    }

    public static int getAuditPassedCount(RecordingMode mode,
                               List<QuestionRecordingTask> questionRelations,
                               List<ExamPaperRecordingTask> paperRelations) {
        if (EXAM_PAPER == mode) {
            int questionPassedCount = (int) getOrEmpty(questionRelations).stream()
                    .filter(RecordingStageUtil::auditApproved)
                    .count();
            int paperPassedCount = (int) getOrEmpty(paperRelations).stream()
                    .filter(RecordingStageUtil::auditApproved)
                    .count();
            return paperPassedCount + questionPassedCount;
        }

        if (SINGLE_QUESTION == mode) {
            return (int) getOrEmpty(questionRelations).stream()
                    .filter(RecordingStageUtil::auditApproved)
                    .count();
        }

        log.error("Unsupported RecordingMode = {}!", mode);
        return 0;
    }

    public static int getToBeAuditCount(RecordingMode mode,
                                  List<QuestionRecordingTask> questionRelations,
                                  List<ExamPaperRecordingTask> paperRelations) {
        if (EXAM_PAPER == mode) {
            int toBeAuditQuestionCount = (int) getOrEmpty(questionRelations).stream()
                    .filter(task -> QuestionRecordingTaskStage.TO_BE_AUDITED.toInt() == task.getStage())
                    .count();
            int toBeAuditPaperCount = (int) getOrEmpty(paperRelations).stream()
                    .filter(task -> ExamPaperRecordingTaskStage.TO_BE_AUDITED.toInt() == task.getStage())
                    .count();
            return toBeAuditPaperCount + toBeAuditQuestionCount;
        }

        if (SINGLE_QUESTION == mode) {
            return (int) getOrEmpty(questionRelations).stream()
                    .filter(task -> QuestionRecordingTaskStage.TO_BE_AUDITED.toInt() == task.getStage())
                    .count();
        }

        log.error("Unsupported RecordingMode = {}!", mode);
        return 0;
    }

    public static int getNeedAuditCount(RecordingMode mode,
                                  List<QuestionRecordingTask> questionRelations,
                                  List<ExamPaperRecordingTask> paperRelations) {
        if (EXAM_PAPER == mode) {
            int needAuditQuestionCount = getOrEmpty(questionRelations).size();
            int needAuditPaperCount = getOrEmpty(paperRelations).size();
            return needAuditQuestionCount + needAuditPaperCount;
        }

        if (SINGLE_QUESTION == mode) {
            return getOrEmpty(questionRelations).size();
        }

        log.error("Unsupported RecordingMode = {}!", mode);
        return 0;
    }

    private RecordingTaskUtil() {
    }
}
