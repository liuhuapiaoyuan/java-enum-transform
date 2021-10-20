/**
 * @(#)TutorArmoryServer.java, May 06, 2015.
 * <p>
 * Copyright 2015 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.rpc;

import com.imcuttle.bar.service.ExamPaperRecordingTaskService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingApplicationDetailService;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.thrift.BindExamPaperReq;
import com.imcuttle.thrift.BindQuestionReq;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingApplicationDetail;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.thrift.TutorArmoryThrift;
import com.imcuttle.thrift.UnbindExamPaperReq;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.imcuttle.thrift.TutorArmoryConstants.INT_ID_NO_LIMIT;
import static com.imcuttle.thrift.TutorArmoryConstants.LONG_DEFAULT_ID;

/**
 * @author linbonan
 */
@Service
public class RpcHandler implements TutorArmoryThrift.Iface {

    @Autowired
    private ExamPaperRecordingTaskService examPaperRecordingTaskService;

    @Autowired
    private RecordingApplicationDetailService recordingApplicationDetailService;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private RecordingApplicationService recordingApplicationService;

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Deprecated
    @Override
    public boolean saveExamPaperRecordingTask(long taskId, long examPaperId) throws TException {
        //兼容上线
        return examPaperRecordingTaskService.saveExamPaperRecordingTask(taskId, examPaperId, INT_ID_NO_LIMIT);
    }

    @Deprecated
    @Override
    public boolean removeExamPaperRecordingTask(long taskId, long examPaperId) throws TException {
        return examPaperRecordingTaskService.removeExamPaperRecordingTask(taskId, examPaperId, INT_ID_NO_LIMIT);
    }

    @Override
    public boolean saveExamPaperOfRecordingTask(BindExamPaperReq request) throws TException {
        return examPaperRecordingTaskService.saveExamPaperRecordingTask(request.getTaskId(), request.getExamPaperId(), request.getOperatorUserId());
    }

    @Override
    public boolean removeExamPaperFromRecordingTask(UnbindExamPaperReq request) throws TException {
        return examPaperRecordingTaskService.removeExamPaperRecordingTask(request.getTaskId(), request.getExamPaperId(), request.getOperatorUserId());
    }

    @Override
    public boolean bindQuestion2RecordingTask(BindQuestionReq req) throws TException {
        return questionRecordingTaskService.bindQuestion2RecordingTask(req);
    }

    @Override
    public Map<Long, RecordingApplicationDetail> getRecordingApplicationDetails(List<Long> ids) throws TException {
        if (CollectionUtils.isEmpty(ids)) {
            return Maps.newHashMap();
        }

        return recordingApplicationDetailService.getByIds(ids);
    }

    @Override
    public Map<Long, RecordingApplicationDetail> getRecordingApplicationDetailsIncludeAllRelations(List<Long> ids) throws TException {
        if (CollectionUtils.isEmpty(ids)) {
            return Maps.newHashMap();
        }

        return recordingApplicationDetailService.getByIdsIncludeAllRelations(ids);
    }

    @Override
    public boolean updateQuestionRecordingTaskSnapshots(long taskId, long examPaperId, int questionId, List<String> snapshots) throws TException {
        return questionRecordingTaskService.updateQuestionRecordingTaskSnapshots(taskId, examPaperId, questionId, snapshots);
    }

    @Override
    public List<Long> scanRecordingApplicationId(long cursorIdExclusive, int batchSize) throws TException {
        return recordingApplicationService.scanId(cursorIdExclusive, batchSize);
    }

    @Override
    @Deprecated
    public boolean updateQuestionTagStatus(long taskId, int questionId) throws TException {
        return questionRecordingTaskService.tagFinish(taskId, questionId, LONG_DEFAULT_ID);
    }

    @Override
    public boolean updateQuestionTagStatusByTaskIdAndQuestionIdAndExamPaperId(long taskId, int questionId, long examPaperId) throws TException {
        return questionRecordingTaskService.tagFinish(taskId, questionId, examPaperId);
    }

    @Override
    public long getRecordingApplicationIdByTaskId(long taskId) throws TException {
        return Optional.ofNullable(recordingTaskService.getTaskById(taskId))
                .map(RecordingTask::getApplicationId).orElse(LONG_DEFAULT_ID);
    }

    @Override
    public Map<Long, RecordingApplication> getRecordingApplicationByTaskIds(List<Long> taskIds) throws TException {
        return recordingTaskService.getRecordingApplicationByTaskIds(taskIds);
    }

    @Override
    public boolean replaceQuestionAndFeedback(long taskId, long examPaperId, int needReplaceId, int replacedId) throws TException {
        return questionRecordingTaskService.replaceQuestionAndFeedback(taskId, examPaperId, needReplaceId, replacedId);
    }
}
