/**
 * @(#)QuestionRecordingTaskStorage.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db;

import com.imcuttle.thrift.QuestionRecordingTask;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author chenyibo
 */
public interface QuestionRecordingTaskStorage {

    boolean updateQuestionRecordingTaskStage(long taskId, long examPaperId, int questionId, int stage);

    List<QuestionRecordingTask> getQuestionRecordingTasksByTaskIdAndExamPaperId(long taskId, long examPaperId);

    List<QuestionRecordingTask> getEffectiveRelationsByRecordingApplicationIds(Collection<Long> applicationIds);

    List<QuestionRecordingTask> getAllRelationsByRecordingApplicationIds(Collection<Long> applicationIds);

    List<QuestionRecordingTask> getEffectiveRelationsByTaskIds(Collection<Long> taskIds);

    List<QuestionRecordingTask> getQuestionRecordingTasksByTaskId(long taskId);

    boolean batchInsert(List<QuestionRecordingTask> questionRecordingTasks);

    boolean batchUpdate(List<QuestionRecordingTask> questionRecordingTasks);

    long insert(QuestionRecordingTask questionRecordingTask);

    Optional<QuestionRecordingTask> get(long taskId, int questionId, long examPaperId);

    boolean resume(long id, QuestionRecordingTask questionRecordingTask);

    int getMaxOrdinal(long taskId);

    boolean updateStage(long taskId, Set<Integer> originStages, int targetStage);

    boolean updateQuestionRecordingTaskSnapshots(long taskId, long examPaperId, int questionId, List<String> snapshots);

    boolean decreaseOrdinal(long taskId, long examPaperId, int startOrdinalExclusive, int count);

    List<QuestionRecordingTask> getTasks(long taskId, List<Integer> questionIds, long examPaperId);

    boolean batchResume(List<Long> ids, List<QuestionRecordingTask> questionRecordingTask);
}

