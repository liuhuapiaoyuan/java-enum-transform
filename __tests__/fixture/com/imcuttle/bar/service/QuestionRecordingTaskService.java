/**
 * @(#)QuestionRecordingTaskService.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.bar.data.SaveAuditTaskRequest;
import com.imcuttle.bar.util.lock.DistributedLockKey;
import com.imcuttle.thrift.ArmoryBadRequestException;
import com.imcuttle.thrift.ArmoryUnAuthorizedException;
import com.imcuttle.thrift.BindBatchQuestionReq;
import com.imcuttle.thrift.BindQuestionReq;
import com.imcuttle.thrift.QuestionRecordingTask;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author linbonan
 */
public interface QuestionRecordingTaskService {

    Map<Long, List<QuestionRecordingTask>> getEffectiveRelationsByRecordingApplicationIds(Collection<Long> applicationIds);

    Map<Long, List<QuestionRecordingTask>> getAllRelationsByRecordingApplicationIds(Collection<Long> applicationIds);

    Map<Long, List<QuestionRecordingTask>> getEffectiveRelationsByTaskIds(List<Long> taskIds);

    boolean createOrUpdateQuestionRecordingTasks(List<QuestionRecordingTask> allExistQuestionRecordingTasks, List<QuestionRecordingTask> latestQuestionRecordingTasks);

    boolean bindQuestion2RecordingTask(BindQuestionReq req) throws ArmoryBadRequestException, ArmoryUnAuthorizedException;

    void saveAuditTask(SaveAuditTaskRequest request);

    List<QuestionRecordingTask> getQuestionRecordingTasksByTaskId(long taskId);

    boolean submit(long taskId);

    boolean updateQuestionRecordingTaskSnapshots(long taskId, long examPaperId, int questionId, List<String> snapshots);

    Optional<QuestionRecordingTask> get(long taskId, int questionId, long examPaperId);

    Optional<QuestionRecordingTask> getEffectiveRelationByTaskIdAndExamPaperId(int questionId, long taskId, long examPaperId);

    boolean updateStage(long taskId, long examPaperId, int questionId, int stage);

    boolean tagFinish(long taskId, int questionId, long examPaperId);

    boolean deleteAndCleanFeedback(QuestionRecordingTask questionRecordingTask);

    boolean deleteQuestionsAndCleanFeedback(List<QuestionRecordingTask> questionRecordingTasks);

    boolean batchUpdate(List<QuestionRecordingTask> questionRecordingTasks);

    boolean bindBatchQuestion2RecordingTask(@DistributedLockKey(field = "taskId") BindBatchQuestionReq req) throws ArmoryBadRequestException, ArmoryUnAuthorizedException;

    boolean replaceQuestionAndFeedback(long taskId, long examPaperId, int needReplaceId, int replacedId);
}
