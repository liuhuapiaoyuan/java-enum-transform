/**
 * @(#)ExamPaperRecordingTaskStorage.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db;

import com.imcuttle.thrift.AuditDetail;
import com.imcuttle.thrift.ExamPaperRecordingTask;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author chenyibo
 */
public interface ExamPaperRecordingTaskStorage {

    boolean updateExamPaperRecordingTaskStage(long taskId, long examPaperId, int stage);

    List<ExamPaperRecordingTask> getExamPaperRecordingTaskByTaskIdAndExamPaperId(long taskId, long examPaperId);

    List<ExamPaperRecordingTask> getEffectiveRelationsByRecordingApplicationIds(Collection<Long> applicationIds);

    List<ExamPaperRecordingTask> getExamPaperRecordingTaskByTaskId(long taskId);

    List<ExamPaperRecordingTask> getEffectiveRelationsByTaskIds(Collection<Long> taskIds);

    boolean create(ExamPaperRecordingTask examPaperRecordingTask);

    List<Long> batchCreate(List<ExamPaperRecordingTask> examPaperRecordingTasks);

    List<ExamPaperRecordingTask> getEffectiveRelationsByRecordingTaskIds(Collection<Long> taskIds);

    boolean updateStageById(long id, int originStage, int targetStage);

    Optional<ExamPaperRecordingTask> getExamPaperRecordingTaskById(long id);

    boolean updateStage(long taskId, Set<Integer> originStages, int targetStage);

    boolean updateExamPaperRecordingTaskStageAndAuditDetails(long taskId, long examPaperId, int stage, List<AuditDetail> auditDetails);

    List<ExamPaperRecordingTask> getAllRelationsByRecordingApplicationIds(Collection<Long> applicationIds);
}

