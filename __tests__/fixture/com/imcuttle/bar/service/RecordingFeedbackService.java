/**
 * @(#)RecordingFeedbackService.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.bar.data.QuestionRecordingTaskPair;
import com.imcuttle.thrift.RecordingFeedback;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author linbonan
 */
public interface RecordingFeedbackService {

    List<RecordingFeedback> getFeedBacksOfSpecificTarget(long targetId, FeedbackTargetType targetType, boolean onlyUnProcessed);

    List<RecordingFeedback> getFeedBacksOfSpecificTargetOfTask(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId, boolean onlyUnProcessed);

    long create(RecordingFeedback recordingFeedback);

    List<Long> batchCreateWithProcessedAndCreatedTime(Collection<RecordingFeedback> recordingFeedbacks);

    Map<Long, List<RecordingFeedback>> getUnprocessedFeedBacksOfRecordingTask(List<Long> taskIds);

    boolean processAll(long targetId, FeedbackTargetType targetType);

    Map<Long, List<RecordingFeedback>> getAllTypeUnprocessedFeedBacksByTaskIds(List<Long> taskIds);

    void processFeedbackForAudit(RecordingFeedback feedback, boolean notBeenAudited, boolean everBeenApproved, boolean approved);

    Map<Long, List<RecordingFeedback>> batchGetFeedBacksByTargetId(List<Long> targetIds, FeedbackTargetType targetType, long taskId, boolean onlyUnProcessed);

    boolean updateFeedbackInfo(RecordingFeedback recordingFeedback);

    Optional<RecordingFeedback> getById(long feedbackId);

    boolean delete(long feedbackId);

    boolean deleteFeedBackOfQuestions(Collection<QuestionRecordingTaskPair> questionIdentifiers);

    boolean processAll(long taskId);

    boolean processAllByApplicationId(long applicationId);
}
