/**
 * @(#)RecordingTaskStorage.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db;

import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.thrift.TaskAuditSubmitDetail;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author chenkangbj
 */
public interface RecordingTaskStorage {

    boolean update(long recordingTaskId, int estimatedQuestionNum, int auditorId, int recorderId);

    List<Long> batchCreate(List<RecordingTask> recordingTasks);

    Map<Integer, Integer> countTasksByAuditor(int auditorId, List<Integer> stages);

    RecordingTask getById(long taskId);

    List<RecordingTask> getByIds(Collection<Long> ids);

    Map<Integer, Integer> countByRecorder(int userId, Collection<Integer> stageList);

    List<RecordingTask> getByRecordingApplicationIds(Collection<Long> applicationIds);

    List<RecordingTask> getByRecorderAndStage(RecordingTaskStage taskStage, int userId, int page, int pageSize);

    List<RecordingTask> getByAuditorAndStage(int auditorId, int page, int pageSize, int stage);

    boolean updateTaskStage(long taskId, int stage);

    boolean submit(long taskId);

    boolean updateTagger(long taskId, int newTaggerId);

    boolean updateTaskStageByApplicationId(long applicationId, int stage);

    boolean updateTaskStageAndTaskAuditSubmitDetails(long taskId, int stage, List<TaskAuditSubmitDetail> taskAuditSubmitDetails);
}
