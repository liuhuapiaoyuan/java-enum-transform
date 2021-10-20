/**
 * @(#)RecordingFeedbackStorage.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db;

import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.bar.data.QuestionRecordingTaskPair;
import com.imcuttle.thrift.RecordingFeedback;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author linbonan
 */
public interface RecordingFeedbackStorage {

    List<RecordingFeedback> getByTargetIdAndTargetTypeConditionally(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId, boolean onlyUnProcessed);

    long create(RecordingFeedback recordingFeedback);

    List<Long> batchCreateWithProcessedAndCreatedTime(Collection<RecordingFeedback> recordingFeedbacks);

    List<RecordingFeedback> getUnprocessedFeedBacksOfRecordingTask(List<Long> taskIds);

    boolean processAll(long targetId, FeedbackTargetType targetType);

    List<RecordingFeedback> getAllTypeUnprocessedFeedBacksByTaskIds(List<Long> taskIds);

    boolean update(RecordingFeedback feedback);

    List<RecordingFeedback> batchGetFeedBacks(List<Long> targetIds, FeedbackTargetType targetType, long taskId, boolean onlyUnProcessed);

    Map<Long, RecordingFeedback> getByIds(List<Long> ids);

    boolean delete(long id);

    boolean batchUpdateProcessStatus(List<RecordingFeedback> unprocessedPassedFeedbacks);

    boolean deleteFeedBackOfQuestions(Collection<QuestionRecordingTaskPair> questionIdentifiers);

    boolean processAll(long taskId);

    boolean processAllByApplicationId(long applicationId);
}
