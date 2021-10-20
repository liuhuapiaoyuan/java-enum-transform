/**
 * @(#)ExamPaperRecordingTaskServiceImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.enums.ExamPaperRecordingTaskStage;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.FeedbackType;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.enums.RecordingQuestionOrigin;
import com.imcuttle.bar.constant.LockKeyFieldConstant;
import com.imcuttle.bar.data.SaveAuditTaskRequest;
import com.imcuttle.bar.enums.LockPrefixEnum;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.AuthorizeService;
import com.imcuttle.bar.service.ExamPaperRecordingTaskService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.storage.db.ExamPaperRecordingTaskStorage;
import com.imcuttle.bar.storage.db.QuestionRecordingTaskStorage;
import com.imcuttle.bar.storage.db.RecordingTaskStorage;
import com.imcuttle.bar.util.lock.DistributedLock;
import com.imcuttle.bar.util.lock.DistributedLockKey;
import com.imcuttle.thrift.ArmoryUnAuthorizedException;
import com.imcuttle.thrift.AuditDetail;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingFeedback;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.utils.RecordingStageUtil;
import com.fenbi.tutor.neoquestion.client.proxy.spi.TutorNeoQuestionProxy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_RECORDING;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_RECORDING_OF_OTHERS;

/**
 * @author chenyibo
 */
@Service
@Slf4j
public class ExamPaperRecordingTaskServiceImpl implements ExamPaperRecordingTaskService {

    @Autowired
    private ExamPaperRecordingTaskStorage examPaperRecordingTaskStorage;

    @Autowired
    private QuestionRecordingTaskStorage questionRecordingTaskStorage;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Autowired
    private RecordingTaskStorage recordingTaskStorage;

    @Autowired
    private TutorNeoQuestionProxy tutorNeoQuestionProxy;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private AuthorizeService authorizeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributedLock(prefix = LockPrefixEnum.RECORDING_TASK)
    public boolean removeExamPaperRecordingTask(@DistributedLockKey(field = LockKeyFieldConstant.PARAMETER_IDENTITY) long taskId, long examPaperId, int operatorUserId) throws ArmoryUnAuthorizedException {
        List<RecordingTask> recordingTasks = recordingTaskStorage.getByIds(Collections.singletonList(taskId));
        if (CollectionUtils.isEmpty(recordingTasks)) {
            log.error("移除试卷-录题任务关联关系， 录题任务不存在, taskId:{}, examPaperId:{}", taskId, examPaperId);
            return false;
        }
        authorizeForUpdateExamPapers(recordingTasks.get(0), operatorUserId);

        List<ExamPaperRecordingTask> examPaperRecordingTasks = examPaperRecordingTaskStorage
                .getExamPaperRecordingTaskByTaskIdAndExamPaperId(taskId, examPaperId);
        if (CollectionUtils.isEmpty(examPaperRecordingTasks)) {
            return true;
        }

        boolean result = examPaperRecordingTaskStorage.updateExamPaperRecordingTaskStage(taskId, examPaperId, ExamPaperRecordingTaskStage.DELETED.toInt());

        List<QuestionRecordingTask> recordedQuestions = questionRecordingTaskService.getEffectiveRelationsByTaskIds(Lists.newArrayList(taskId))
                .getOrDefault(taskId, Lists.newArrayList())
                .stream()
                .filter(question -> question.getExamPaperId() == examPaperId)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(recordedQuestions)) {
            result &= questionRecordingTaskService.deleteQuestionsAndCleanFeedback(recordedQuestions);
        }

        if (result) {
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(recordingTasks.get(0).getApplicationId(), taskId));
        }
        return result;
    }

    @Override
    public Map<Long, List<ExamPaperRecordingTask>> getEffectiveRelationsByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Maps.newHashMap();
        }

        return examPaperRecordingTaskStorage.getEffectiveRelationsByRecordingApplicationIds(applicationIds)
                .stream()
                .collect(Collectors.groupingBy(ExamPaperRecordingTask::getApplicationId));
    }

    @Override
    public Map<Long, List<ExamPaperRecordingTask>> getAllRelationsByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Maps.newHashMap();
        }

        return examPaperRecordingTaskStorage.getAllRelationsByRecordingApplicationIds(applicationIds)
                .stream()
                .collect(Collectors.groupingBy(ExamPaperRecordingTask::getApplicationId));
    }

    @Override
    public Map<Long, List<ExamPaperRecordingTask>> getEffectiveRelationsByTaskIds(Collection<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Maps.newHashMap();
        }

        return examPaperRecordingTaskStorage.getEffectiveRelationsByTaskIds(taskIds)
                .stream()
                .collect(Collectors.groupingBy(ExamPaperRecordingTask::getTaskId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributedLock(prefix = LockPrefixEnum.RECORDING_TASK)
    public boolean saveExamPaperRecordingTask(@DistributedLockKey(field = LockKeyFieldConstant.PARAMETER_IDENTITY) long taskId, long examPaperId, int operatorUserId) throws ArmoryUnAuthorizedException {
        List<RecordingTask> recordingTasks = recordingTaskStorage.getByIds(Collections.singletonList(taskId));
        if (CollectionUtils.isEmpty(recordingTasks)) {
            log.error("保存试卷-录题任务关联关系， 录题任务不存在, taskId:{}, examPaperId:{}", taskId, examPaperId);
            return false;
        }

        authorizeForUpdateExamPapers(recordingTasks.get(0), operatorUserId);
        long applicationId = recordingTasks.get(0).getApplicationId();

        List<ExamPaperRecordingTask> examPaperRecordingTasks = examPaperRecordingTaskStorage
                .getExamPaperRecordingTaskByTaskId(taskId);

        boolean result = true;
        List<ExamPaperRecordingTask> existExamPaperRecordingTasks = examPaperRecordingTasks.stream().filter(task -> task.getExamPaperId() == examPaperId ).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(existExamPaperRecordingTasks)) {
            ExamPaperRecordingTask examPaperRecordingTask = new ExamPaperRecordingTask();
            examPaperRecordingTask.setExamPaperId(examPaperId);
            examPaperRecordingTask.setTaskId(taskId);
            examPaperRecordingTask.setApplicationId(applicationId);
            examPaperRecordingTask.setStage(ExamPaperRecordingTaskStage.RECORDED.toInt());
            examPaperRecordingTask.setCreator(operatorUserId);
            examPaperRecordingTask.setCreatedTime(System.currentTimeMillis());
            int ordinal = CollectionUtils.isEmpty(examPaperRecordingTasks) ? 0 : examPaperRecordingTasks.stream().map(ExamPaperRecordingTask::getOrdinal).max(Comparator.comparingInt(Integer::intValue)).get() + 1;
            examPaperRecordingTask.setOrdinal(ordinal);

            result = examPaperRecordingTaskStorage.create(examPaperRecordingTask);
        }

        List<Integer> questionIds = tutorNeoQuestionProxy.getOrderedWholeQuestionIdsByExamPaperId(examPaperId);
        List<QuestionRecordingTask> existQuestionRecordingTasks = questionRecordingTaskStorage.getQuestionRecordingTasksByTaskIdAndExamPaperId(taskId, examPaperId);

        Map<Integer, List<QuestionRecordingTask>> questionId2Tasks = existQuestionRecordingTasks.stream().collect(Collectors.groupingBy(QuestionRecordingTask::getQuestionId));
        List<QuestionRecordingTask> latestQuestionRecordingTasks = questionIds.stream().map(questionId -> {
            QuestionRecordingTask questionRecordingTask = new QuestionRecordingTask();

            questionRecordingTask.setApplicationId(applicationId);
            questionRecordingTask.setTaskId(taskId);
            questionRecordingTask.setExamPaperId(examPaperId);
            questionRecordingTask.setQuestionId(questionId);
            questionRecordingTask.setOrdinal(questionIds.indexOf(questionId));
            questionRecordingTask.setCreator(operatorUserId);
            questionRecordingTask.setCreatedTime(System.currentTimeMillis());
            if (questionId2Tasks.containsKey(questionId)) {
                questionRecordingTask.setQuestionOrigin(questionId2Tasks.get(questionId).get(0).getQuestionOrigin());
                QuestionRecordingTaskStage stage = QuestionRecordingTaskStage.findByInt(questionId2Tasks.get(questionId).get(0).getStage()).orElseThrow(BadRequestException::new);
                if (stage.equals(QuestionRecordingTaskStage.DELETED) || stage.equals(QuestionRecordingTaskStage.INACTIVATED)) {
                    stage = QuestionRecordingTaskStage.RECORDED;
                }
                questionRecordingTask.setStage(stage.toInt());
            } else {
                log.error("保存试卷-题目关联关系， 试卷里的题目在创建时未在armory创建关联关系记录， taskId:{}, examPaperId:{}, questionId:{}", taskId, examPaperId, questionId);
                questionRecordingTask.setQuestionOrigin(RecordingQuestionOrigin.RECORDED.toInt());
                questionRecordingTask.setStage(QuestionRecordingTaskStage.RECORDED.toInt());
            }

            return questionRecordingTask;
        }).collect(Collectors.toList());

        result = result && questionRecordingTaskService.createOrUpdateQuestionRecordingTasks(existQuestionRecordingTasks, latestQuestionRecordingTasks);

        if (result) {
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(recordingTasks.get(0).getApplicationId(), taskId));
        }
        return result;
    }

    @Override
    public Map<Long, List<ExamPaperRecordingTask>> getEffectiveRelationsByRecordingTaskIds(Collection<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Maps.newHashMap();
        }

        return examPaperRecordingTaskStorage.getEffectiveRelationsByRecordingTaskIds(taskIds)
                .stream()
                .collect(Collectors.groupingBy(ExamPaperRecordingTask::getTaskId));
    }

    @Override
    public boolean submit(long taskId) {
        Set<Integer> fromStages = ExamPaperRecordingTaskStage.getAvailableSourceStages(ExamPaperRecordingTaskStage.TO_BE_AUDITED)
                .stream()
                .map(ExamPaperRecordingTaskStage::toInt)
                .collect(Collectors.toSet());
        return examPaperRecordingTaskStorage.updateStage(taskId, fromStages, ExamPaperRecordingTaskStage.TO_BE_AUDITED.toInt());
    }

    @Override
    public void saveAuditTask(SaveAuditTaskRequest request) {
        Optional<ExamPaperRecordingTask> taskOptional = examPaperRecordingTaskStorage
                .getExamPaperRecordingTaskByTaskIdAndExamPaperId(request.getTaskId(), request.getTargetId())
                .stream().findFirst();
        if (!taskOptional.isPresent()) {
            throw new BadRequestException(String.format("任务 id = %d 下不存在试卷 id = %d！",
                    request.getTaskId(), request.getTargetId()));
        }

        RecordingTask recordingTask = recordingTaskStorage.getById(request.getTaskId());
        if (Objects.isNull(recordingTask)) {
            throw new BadRequestException(String.format("录题任务不存在,taskId:", request.getTaskId()));
        }

        ExamPaperRecordingTask examPaperRecordingTask = taskOptional.get();

        if (!RecordingStageUtil.canAudit(examPaperRecordingTask)) {
            throw new BadRequestException(String.format("任务 id = %d 的状态 stage = %d，不允许审核!",
                    request.getTaskId(), examPaperRecordingTask.getStage()));
        }

        int originStage = examPaperRecordingTask.getStage();
        int targetStage = request.isPassed() ?
                ExamPaperRecordingTaskStage.AUDIT_APPROVED.toInt() :
                ExamPaperRecordingTaskStage.AUDIT_FAILED.toInt();

        List<AuditDetail> auditDetails = CollectionUtils.isEmpty(examPaperRecordingTask.getAuditDetails()) ? Lists.newArrayList() : examPaperRecordingTask.getAuditDetails();

        AuditDetail auditDetail = new AuditDetail();
        auditDetail.setAuditTimes(recordingTask.getSubmitTimes());
        auditDetail.setOriginStage(originStage);
        auditDetail.setAuditor(getUserId());
        auditDetail.setAuditTime(System.currentTimeMillis());
        auditDetails.add(auditDetail);

        if (needUpdate(originStage, targetStage)) {
            auditDetail.setLatestStage(targetStage);
        } else {
            auditDetail.setLatestStage(originStage);
        }
        boolean success = examPaperRecordingTaskStorage
                .updateExamPaperRecordingTaskStageAndAuditDetails(request.getTaskId(), request.getTargetId(), targetStage, auditDetails);
        if (!success) {
            throw new BadRequestException("其他人已修改试卷录入任务状态,请刷新页面再试!");
        }

        // 处理反馈
        RecordingFeedback feedback = buildFeedback(request, examPaperRecordingTask);
        boolean notBeenAudited = ExamPaperRecordingTaskStage.TO_BE_AUDITED.toInt() == originStage;
        boolean everBeenApproved = RecordingStageUtil.auditApproved(examPaperRecordingTask);
        boolean approved = request.isPassed();
        recordingFeedbackService.processFeedbackForAudit(feedback, notBeenAudited, everBeenApproved, approved);
    }

    @Override
    public Optional<ExamPaperRecordingTask> getEffectiveRelationByTaskIdAndExamPaperId(long taskId, long examPaperId) {
        return examPaperRecordingTaskStorage
                .getExamPaperRecordingTaskByTaskIdAndExamPaperId(taskId, examPaperId)
                .stream()
                .findFirst()
                .filter(relation -> relation.getStage() > 0);
    }

    private boolean needUpdate(int originStage, int targetStage) {
        // stage无变化
        if (originStage == targetStage) {
            return false;
        }

        // 任何状态 -> 不通过
        if (targetStage == ExamPaperRecordingTaskStage.AUDIT_FAILED.toInt()) {
            return true;
        }

        // 不通过 -> 通过
        if ((originStage == ExamPaperRecordingTaskStage.AUDIT_FAILED.toInt()) && (targetStage == ExamPaperRecordingTaskStage.AUDIT_APPROVED.toInt())) {
            return true;
        }

        // 待审核 -> 通过
        if ((originStage == ExamPaperRecordingTaskStage.TO_BE_AUDITED.toInt()) && (targetStage == ExamPaperRecordingTaskStage.AUDIT_APPROVED.toInt())) {
            return true;
        }

        // 其他状态 -> 通过
        return false;
    }

    private RecordingFeedback buildFeedback(SaveAuditTaskRequest request, ExamPaperRecordingTask examPaperRecordingTask) {
        RecordingFeedback feedback = new RecordingFeedback();
        feedback.setApplicationId(examPaperRecordingTask.getApplicationId());
        feedback.setTaskId(examPaperRecordingTask.getTaskId());
        feedback.setTargetId(examPaperRecordingTask.getExamPaperId());
        feedback.setTargetType(FeedbackTargetType.EXAM_PAPER.toInt());
        feedback.setType(request.isPassed() ? FeedbackType.PAPER_AUDIT_APPROVED.toInt() : FeedbackType.PAPER_AUDIT_FAILED.toInt());
        feedback.setReason(request.getReason());
        feedback.setDescription(request.getDescription());
        feedback.setAttachments(request.getAttachments());
        feedback.setCreator(getUserId());
        return feedback;
    }

    private void authorizeForUpdateExamPapers(RecordingTask task, int userId) throws ArmoryUnAuthorizedException {
        boolean authorized = authorizeService.featureAuthorized(userId, task.getSubjectId(), task.getPhaseId(),
                task.getRecorder() == userId ? QUESTION_RECORDING : QUESTION_RECORDING_OF_OTHERS);
        if (!authorized) {
            throw new ArmoryUnAuthorizedException("没有权限执行当前操作");
        }
    }
}
