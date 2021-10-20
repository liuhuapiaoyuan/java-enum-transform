/**
 * @(#)ExamPaperRecordingTaskService.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.bar.data.SaveAuditTaskRequest;
import com.imcuttle.thrift.ArmoryUnAuthorizedException;
import com.imcuttle.thrift.ExamPaperRecordingTask;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author chenyibo
 */
public interface ExamPaperRecordingTaskService {

    boolean removeExamPaperRecordingTask(long taskId, long examPaperId, int operatorUserId) throws ArmoryUnAuthorizedException;

    Map<Long, List<ExamPaperRecordingTask>> getEffectiveRelationsByRecordingApplicationIds(Collection<Long> applicationIds);

    Map<Long, List<ExamPaperRecordingTask>> getEffectiveRelationsByTaskIds(Collection<Long> taskIds);

    boolean saveExamPaperRecordingTask(long taskId, long examPaperId, int operatorUserId) throws ArmoryUnAuthorizedException;

    Map<Long, List<ExamPaperRecordingTask>> getEffectiveRelationsByRecordingTaskIds(Collection<Long> taskIds);

    boolean submit(long taskId);

    void saveAuditTask(SaveAuditTaskRequest request);

    Optional<ExamPaperRecordingTask> getEffectiveRelationByTaskIdAndExamPaperId(long taskId, long targetId);

    Map<Long, List<ExamPaperRecordingTask>> getAllRelationsByRecordingApplicationIds(Collection<Long> applicationIds);
}
