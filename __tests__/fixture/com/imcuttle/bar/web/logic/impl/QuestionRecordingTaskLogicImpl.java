/**
 * @(#)QuestionRecordingTaskLogicImpl.java, 4月 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.common.lambda.Lambdas;
import com.fenbi.commons.security.SecurityHelper;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.fenbi.commons2.rest.exception.DataConflictedException;
import com.fenbi.commons2.rest.exception.ForbiddenException;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.enums.RecordingMode;
import com.imcuttle.enums.RecordingQuestionOrigin;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.constant.LockKeyFieldConstant;
import com.imcuttle.bar.enums.LockPrefixEnum;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.CmsLogService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.bar.util.lock.DistributedLock;
import com.imcuttle.bar.util.lock.DistributedLockKey;
import com.imcuttle.bar.web.data.BindBatchQuestionRequest;
import com.imcuttle.bar.web.data.BindSingleQuestionRequest;
import com.imcuttle.bar.web.logic.QuestionRecordingTaskLogic;
import com.imcuttle.thrift.ArmoryBadRequestException;
import com.imcuttle.thrift.ArmoryUnAuthorizedException;
import com.imcuttle.thrift.BindBatchQuestionReq;
import com.imcuttle.thrift.BindQuestionReq;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingTask;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_RECORDING;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_RECORDING_OF_OTHERS;
import static com.imcuttle.thrift.TutorArmoryConstants.LONG_DEFAULT_ID;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.RECORDING_APPLICATION_ID;

/**
 * @author chenyibo
 */
@Slf4j
@Service
public class QuestionRecordingTaskLogicImpl extends BaseLogic implements QuestionRecordingTaskLogic {

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Autowired
    private CmsLogService cmsLogService;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Override
    public boolean bindSingleQuestion2RecordingTask(BindSingleQuestionRequest request) {
        RecordingTask task = Optional.ofNullable(recordingTaskService.getTaskById(request.getTaskId()))
                .orElseThrow(() -> new BadRequestException("指定的任务不存在"));

        BindQuestionReq bindQuestionReq = new BindQuestionReq();
        bindQuestionReq.setExamPaperId(request.getExamPaperId());
        bindQuestionReq.setSnapshotImgIds(CollectionUtils.isEmpty(request.getSnapshotImgIds()) ? Lists.newArrayList() : request.getSnapshotImgIds());
        bindQuestionReq.setQuestionId(request.getQuestionId());
        bindQuestionReq.setTaskId(request.getTaskId());
        bindQuestionReq.setQuestionOrigin(RecordingQuestionOrigin.RELEASED.toInt());
        bindQuestionReq.setOperatorUserId(SecurityHelper.getUserId());

        boolean result;
        try {
            result = questionRecordingTaskService.bindQuestion2RecordingTask(bindQuestionReq);
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(task.getApplicationId(),task.getId()));
            // 加入已有题目时，只有散题需要打cmsLog
            if (request.getExamPaperId() == LONG_DEFAULT_ID) {
                cmsLogService.log(task.getApplicationId(), RECORDING_APPLICATION_ID, "将题库已有题目(id:" + request.getQuestionId() + ")加入任务");
            }
        } catch (ArmoryBadRequestException e) {
            throw new BadRequestException(e.getMsg());
        } catch (ArmoryUnAuthorizedException e) {
            throw new ForbiddenException("没有权限将散题加入当前录题任务");
        }

        return result;
    }

    @Override
    public boolean removeQuestionFromTask(long taskId, int questionId) {
        RecordingTask task = verifyAndGetTaskForUpdateQuestions(taskId);
        if (task.getRecordingMode() != RecordingMode.SINGLE_QUESTION.toInt()) {
            // 试卷录入模式中, 题目的移除要通过与试卷解除关联来实现
            throw new BadRequestException("任务不是散题的录入模式");
        }
        if (task.getStage() == RecordingTaskStage.PUBLISHED.toInt()) {
            throw new BadRequestException("任务已发布，不允许再删除题目！");
        }

        QuestionRecordingTask questionRecordingTask = questionRecordingTaskService.get(taskId, questionId, LONG_DEFAULT_ID)
                .orElseThrow(() -> new BadRequestException("任务中没有要删除的题目"));

        if (questionRecordingTask.getStage() == QuestionRecordingTaskStage.DELETED.toInt()) {
            return true;
        }

        try {
            boolean result = questionRecordingTaskService.deleteAndCleanFeedback(questionRecordingTask);
            printCmsLog(task.getApplicationId(), questionRecordingTask.getQuestionOrigin(), questionId);
            return result;
        } catch (IllegalStateException e) {
            log.error("从任务中移除题目失败, taskId = {}, questionId = {}", taskId, questionId, e);
            return false;
        }
    }

    private void printCmsLog(long applicationId, int questionOrigin, int questionId) {
        String content;
        if (questionOrigin == RecordingQuestionOrigin.RELEASED.toInt()) {
            content = "将题库已有题目(id:" + questionId + ")移出任务";
        } else {
            content = "删除题目，id:" + questionId;
        }
        cmsLogService.log(applicationId, RECORDING_APPLICATION_ID, content);
    }

    @Override
    @DistributedLock(prefix = LockPrefixEnum.RECORDING_TASK)
    public boolean reorderQuestions(@DistributedLockKey(field = LockKeyFieldConstant.PARAMETER_IDENTITY) long taskId, List<Integer> questionIds) {
        RecordingTask task = verifyAndGetTaskForUpdateQuestions(taskId);
        if (task.getRecordingMode() != RecordingMode.SINGLE_QUESTION.toInt()) {
            // 试卷录入模式中, 题目的顺序调整要在试卷中进行
            throw new BadRequestException("任务不是散题的录入模式");
        }

        Map<Integer, QuestionRecordingTask> existedRecords = questionRecordingTaskService.getEffectiveRelationsByTaskIds(Lists.newArrayList(taskId))
                .getOrDefault(taskId, Lists.newArrayList())
                .stream()
                .collect(Collectors.toMap(QuestionRecordingTask::getQuestionId, Function.identity(), Lambdas.pickLast()));
        if (MapUtils.isEmpty(existedRecords) || existedRecords.size() != questionIds.size()) {
            throw new DataConflictedException("任务当前包含的题目数量与页面不一致, 请刷新后重试");
        }

        List<QuestionRecordingTask> toBeUpdatedRecords = Lists.newArrayList();
        int ordinal = 0;
        for (Integer questionId : questionIds) {
            QuestionRecordingTask questionRecordingTask = existedRecords.get(questionId);
            if (Objects.isNull(questionRecordingTask)) {
                throw new DataConflictedException("任务当前包含的题目与页面不一致, 请刷新后重试");
            }

            if (questionRecordingTask.getOrdinal() != ordinal) {
                questionRecordingTask.setOrdinal(ordinal);
                toBeUpdatedRecords.add(questionRecordingTask);
            }
            ordinal++;
        }

        if (CollectionUtils.isNotEmpty(toBeUpdatedRecords)) {
            return questionRecordingTaskService.batchUpdate(toBeUpdatedRecords);
        }
        return true;
    }

    private RecordingTask verifyAndGetTaskForUpdateQuestions(long taskId) {
        RecordingTask task = Optional.ofNullable(recordingTaskService.getTaskById(taskId))
                .orElseThrow(() -> new BadRequestException("指定的任务不存在"));

        authorizeFeature(task.getSubjectId(), task.getPhaseId(),
                task.getRecorder() == getUserId() ? QUESTION_RECORDING : QUESTION_RECORDING_OF_OTHERS);

        return task;
    }

    @Override
    public boolean bindBatchQuestion2RecordingTask(BindBatchQuestionRequest request) {
        RecordingTask task = Optional.ofNullable(recordingTaskService.getTaskById(request.getTaskId()))
                .orElseThrow(() -> new BadRequestException("指定的任务不存在"));

        if (CollectionUtils.isEmpty(request.getQuestionIds())) {
            return true;
        }

        BindBatchQuestionReq bindBatchQuestionReq = new BindBatchQuestionReq();
        bindBatchQuestionReq.setExamPaperId(request.getExamPaperId());
        bindBatchQuestionReq.setQuestionId2SnapshotImgIds(MapUtils.isEmpty(request.getQuestionId2SnapshotImgIds()) ? Maps.newHashMap() : request.getQuestionId2SnapshotImgIds());
        bindBatchQuestionReq.setQuestionIds(request.getQuestionIds());
        bindBatchQuestionReq.setTaskId(request.getTaskId());
        bindBatchQuestionReq.setQuestionOrigin(RecordingQuestionOrigin.RELEASED.toInt());
        bindBatchQuestionReq.setOperatorUserId(SecurityHelper.getUserId());

        boolean result;
        try {
            result = questionRecordingTaskService.bindBatchQuestion2RecordingTask(bindBatchQuestionReq);
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(task.getApplicationId(), task.getId()));
            // 加入已有题目时，只有散题需要打cmsLog
            if (request.getExamPaperId() == LONG_DEFAULT_ID) {
                cmsLogService.log(task.getApplicationId(), RECORDING_APPLICATION_ID, "将题库已有题目(id:" + request.getQuestionIds() + ")加入任务");
            }
        } catch (ArmoryBadRequestException e) {
            throw new BadRequestException(e.getMsg());
        } catch (ArmoryUnAuthorizedException e) {
            throw new ForbiddenException("没有权限将散题加入当前录题任务");
        }

        return result;
    }
}
