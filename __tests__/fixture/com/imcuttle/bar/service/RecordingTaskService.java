/**
 * @(#)RecordingTaskService.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.enums.RecordingTaskRoleEnum;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.thrift.TaskAuditSubmitDetail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author chenkangbj
 */
public interface RecordingTaskService {

    List<Long> create(List<RecordingTask> recordingTasks);

    boolean update(RecordingTask recordingTask);

    List<RecordingTask> getSubmittedTasksByAuditor(int auditorId, int page, int pageSize);

    RecordingTask getTaskById(long taskId);

    Optional<RecordingTask> getTaskByIdAndAuditor(long taskId, int auditorId);

    int countSubmittedTasksByAuditor(int auditorId);

    Map<Integer, Integer> getRecordingTaskCountByRole(RecordingTaskRoleEnum taskRole, int userId);

    Map<Long, List<RecordingTask>> getByApplicationIds(Collection<Long> applicationIds);

    List<RecordingTask> getByRecorderAndStage(RecordingTaskStage taskStage, int userId, int page, int pageSize);

    int countRejectedTasksByAuditor(int userId);

    List<RecordingTask> getRejectedTasksByAuditor(int userId, int page, int pageSize);

    RecordingTask checkTaskStageAndGet(long taskId, RecordingTaskStage expectedStage);

    boolean submit(long taskId);

    boolean updateTaskStage(long taskId, int stage);

    boolean updateTaskStageByApplicationId(long applicationId, int stage);

    boolean updateTagger(long task, int newTaggerId);

    void publishTask(RecordingTask task);

    Map<Long, RecordingTask> getTaskByIds(List<Long> taskIds);

    Map<Long, RecordingApplication> getRecordingApplicationByTaskIds(List<Long> taskIds);
}
