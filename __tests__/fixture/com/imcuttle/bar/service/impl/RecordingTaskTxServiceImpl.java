/**
 * @(#)RecordingTaskTxServiceImpl.java, Apr 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.enums.ExamPaperRecordingTaskStage;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.FeedbackType;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.enums.RecordingMode;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.service.RecordingTaskTxService;
import com.imcuttle.bar.storage.db.ExamPaperRecordingTaskStorage;
import com.imcuttle.bar.storage.db.QuestionRecordingTaskStorage;
import com.imcuttle.bar.storage.db.RecordingFeedbackStorage;
import com.imcuttle.bar.storage.db.RecordingTaskStorage;
import com.imcuttle.thrift.ArmoryBadRequestException;
import com.imcuttle.thrift.AuditDetail;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingFeedback;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.thrift.TaskAuditSubmitDetail;
import com.fenbi.tutor.neoquestion.client.proxy.spi.TutorNeoQuestionProxy;
import com.fenbi.tutor.neoquestion.thrift.Question;
import com.fenbi.tutor.neoquestion.thrift.Vignette;
import com.fenbi.tutor.neoquestion.util.QuestionImageUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.enums.RecordingTaskStage.AUDITED;
import static com.imcuttle.enums.RecordingTaskStage.TO_BE_REVISED;
import static com.imcuttle.bar.util.CollectionUtil.getOrEmpty;
import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.CONTENT_TYPE_UBB;
import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.QUESTION_VIGNETTE_ID_DIVISION_NUMBER;
import static java.util.stream.Collectors.toList;

/**
 * @author chenkangbj
 */
@Service
@Slf4j
public class RecordingTaskTxServiceImpl implements RecordingTaskTxService {

    @Autowired
    private RecordingTaskStorage recordingTaskStorage;

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private RecordingFeedbackStorage recordingFeedbackStorage;

    @Autowired
    private QuestionRecordingTaskStorage questionRecordingTaskStorage;

    @Autowired
    private ExamPaperRecordingTaskStorage examPaperRecordingTaskStorage;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Autowired
    private TutorNeoQuestionProxy tutorNeoQuestionProxy;

    /**
     * 提交审核任务，并创建任务的反馈（如果任务不通过并且指定了反馈）
     *
     * @param task                审核任务
     * @param questionRelations   任务下的题目关联
     * @param feedBackCreatorId   反馈的创建人id
     * @param taskApproved        是否前进任务状态。true时为是
     * @param feedbackDescription 任务的反馈（如果有）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuditTask(RecordingTask task, List<QuestionRecordingTask> questionRelations,
                                int feedBackCreatorId, boolean taskApproved, String feedbackDescription) {
        log.info("submitAuditTask begin," +
                        "taskApproved = {}, creator = {}, reason = {}, task = {}, questionRelations = {}",
                taskApproved, feedBackCreatorId, feedbackDescription, task, questionRelations);

        List<TaskAuditSubmitDetail> taskAuditSubmitDetails = task.getTaskAuditSubmitDetails();
        if (CollectionUtils.isEmpty(taskAuditSubmitDetails)) {
            taskAuditSubmitDetails = Lists.newArrayList();
            task.setTaskAuditSubmitDetails(taskAuditSubmitDetails);
        }

        TaskAuditSubmitDetail detail = new TaskAuditSubmitDetail();
        detail.setAuditSubmitter(getUserId());
        detail.setTimes(task.getSubmitTimes());
        detail.setAuditSubmitTime(System.currentTimeMillis());
        detail.setOriginStage(task.getStage());
        detail.setLatestStage(taskApproved ? AUDITED.toInt() : TO_BE_REVISED.toInt());
        taskAuditSubmitDetails.add(detail);
        // 更新任务状态
        boolean updateTaskResult = taskApproved ?
                recordingTaskStorage.updateTaskStageAndTaskAuditSubmitDetails(task.getId(), AUDITED.toInt(), taskAuditSubmitDetails) :
                recordingTaskStorage.updateTaskStageAndTaskAuditSubmitDetails(task.getId(), TO_BE_REVISED.toInt(), taskAuditSubmitDetails);
        if (!updateTaskResult) {
            throw new RuntimeException(String.format(
                    "Update task stage failed! taskApproved = %s, task = %s", taskApproved, task));
        }

        /*
         * 更新任务下 各个题目关联状态
         *
         * 对于审核通过的题目，当任务通过时，状态会前进，否则保持不变；
         */
        List<QuestionRecordingTask> approvedQuestionRelations = Lists.newLinkedList();
        if (taskApproved) {
            boolean taskFirstApproved = checkIfTaskFirstApproved(task);

            approvedQuestionRelations = getOrEmpty(questionRelations)
                    .stream()
                    .filter(relation -> QuestionRecordingTaskStage.AUDIT_APPROVED.toInt() == relation.getStage())
                    .peek(relation -> {
                        AuditDetail auditDetail = new AuditDetail();
                        auditDetail.setAuditTimes(task.getSubmitTimes());
                        auditDetail.setOriginStage(QuestionRecordingTaskStage.AUDIT_APPROVED.toInt());
                        auditDetail.setLatestStage(QuestionRecordingTaskStage.TO_BE_TAGGED.toInt());
                        auditDetail.setAuditor(feedBackCreatorId);
                        auditDetail.setAuditTime(System.currentTimeMillis());

                        List<AuditDetail> auditDetails = CollectionUtils.isEmpty(relation.getAuditDetails()) ? Lists.newArrayList() : relation.getAuditDetails();
                        auditDetails.add(auditDetail);

                        relation.setAuditDetails(auditDetails);
                        relation.setStage(QuestionRecordingTaskStage.TO_BE_TAGGED.toInt());
                    })
                    .collect(Collectors.toList());

            if (taskFirstApproved) {
                List<Integer> questionIds = approvedQuestionRelations.stream().map(QuestionRecordingTask::getQuestionId).filter(questionId -> questionId < QUESTION_VIGNETTE_ID_DIVISION_NUMBER).collect(toList());
                Map<Integer, Question> questionId2question = tutorNeoQuestionProxy.getQuestions(questionIds, CONTENT_TYPE_UBB);

                List<Integer> vignetteIds = approvedQuestionRelations.stream().map(QuestionRecordingTask::getQuestionId).filter(questionId -> questionId >= QUESTION_VIGNETTE_ID_DIVISION_NUMBER).collect(toList());
                Map<Integer, Vignette> vignetteId2vignette = tutorNeoQuestionProxy.getVignettes(vignetteIds, CONTENT_TYPE_UBB);

                for (QuestionRecordingTask questionRecordingTask : approvedQuestionRelations) {
                    int questionId = questionRecordingTask.getQuestionId();
                    AuditDetail auditDetail = questionRecordingTask.getAuditDetails().get(questionRecordingTask.getAuditDetails().size() - 1);

                    List<String> imageIds = new ArrayList<>();
                    List<String> audioIds = new ArrayList<>();
                    if (questionId > QUESTION_VIGNETTE_ID_DIVISION_NUMBER) {
                        Vignette vignette = vignetteId2vignette.get(questionId);
                        if (Objects.isNull(vignette)) {
                            throw new RuntimeException("提交审核任务，题目不存在, 题目id:" + questionId);
                        }
                        imageIds.addAll(QuestionImageUtil.getVignetteAllImageIds(vignette));

                        if (StringUtils.isNotBlank(vignette.getAudioId())) {
                            audioIds.add(vignette.getAudioId());
                        }
                        if (CollectionUtils.isNotEmpty(vignette.getQuestionIds())) {
                            Map<Integer, Question> questionMap = tutorNeoQuestionProxy.getQuestions(vignette.getQuestionIds(), CONTENT_TYPE_UBB);
                            questionMap.values().forEach(question -> {
                                imageIds.addAll(QuestionImageUtil.getQuestionAllImageIds(question));

                                if (StringUtils.isNotBlank(question.getAudioId())) {
                                    audioIds.add(question.getAudioId());
                                }
                                if (StringUtils.isNotBlank(question.getTtsAudioId())) {
                                    audioIds.add(question.getTtsAudioId());
                                }
                            });
                        }
                        auditDetail.setUpdateUser(vignette.getUpdateUser());
                    } else {
                        Question question = questionId2question.get(questionId);
                        if (Objects.isNull(question)) {
                            throw new RuntimeException("提交审核任务，题目不存在, 题目id:" + questionId);
                        }
                        imageIds.addAll(QuestionImageUtil.getQuestionAllImageIds(question));

                        if (StringUtils.isNotBlank(question.getAudioId())) {
                            audioIds.add(question.getAudioId());
                        }
                        if (StringUtils.isNotBlank(question.getTtsAudioId())) {
                            audioIds.add(question.getTtsAudioId());
                        }
                        auditDetail.setUpdateUser(question.getUpdateUser());
                    }

                    auditDetail.setImageIds(imageIds);
                    auditDetail.setAudioIds(audioIds);
                }
            }
            questionRecordingTaskStorage.batchUpdate(approvedQuestionRelations);
            recordingFeedbackService.processAll(task.getId());
            log.info("update question relation stages, approvedQuestionRelations = {}", approvedQuestionRelations);
        } else {
            approvedQuestionRelations = getOrEmpty(questionRelations)
                    .stream()
                    .peek(relation -> {
                        AuditDetail auditDetail = new AuditDetail();
                        auditDetail.setAuditTimes(task.getSubmitTimes());
                        auditDetail.setOriginStage(relation.getStage());
                        auditDetail.setLatestStage(relation.getStage());
                        auditDetail.setAuditor(feedBackCreatorId);
                        auditDetail.setAuditTime(System.currentTimeMillis());

                        List<AuditDetail> auditDetails = CollectionUtils.isEmpty(relation.getAuditDetails()) ? Lists.newArrayList() : relation.getAuditDetails();
                        auditDetails.add(auditDetail);

                        relation.setAuditDetails(auditDetails);
                    })
                    .collect(Collectors.toList());
            questionRecordingTaskStorage.batchUpdate(approvedQuestionRelations);
        }

        // 创建任务的反馈
        if (!taskApproved && StringUtils.isNotEmpty(feedbackDescription)) {
            RecordingFeedback feedback = new RecordingFeedback();
            feedback.setDescription(feedbackDescription);
            feedback.setApplicationId(task.getApplicationId());
            feedback.setTaskId(task.getId());
            feedback.setTargetId(task.getId());
            feedback.setTargetType(FeedbackTargetType.RECORDING_TASK.toInt());
            feedback.setType(FeedbackType.TASK_AUDIT_FAILED.toInt());
            feedback.setCreateTime(System.currentTimeMillis());
            feedback.setCreator(feedBackCreatorId);
            recordingFeedbackService.create(feedback);
        }
    }

    private boolean checkIfTaskFirstApproved(RecordingTask recordingTask) {
        List<TaskAuditSubmitDetail> taskAuditSubmitDetails = recordingTask.getTaskAuditSubmitDetails();
        if (taskAuditSubmitDetails.stream().filter(task -> task.getLatestStage() == AUDITED.toInt()).count() > 1) {
            return false;
        } else if (taskAuditSubmitDetails.stream().filter(task -> task.getLatestStage() == 0).count() == 0) {
            //兼容逻辑,只有一次通过的记录，并且没有没有记录任务状态变化的记录，则说明是第一次通过审核
            return true;
        } else {
            //兼容逻辑，有一次通过的记录和若干次没有记录任务状态变化的记录，则feedback数量+1等于总记录次数时，说明这次是第一次通过
            List<RecordingFeedback> recordingFeedbacks = recordingFeedbackService.getFeedBacksOfSpecificTarget(recordingTask.getId(), FeedbackTargetType.RECORDING_TASK, false);
            int taskAuditFailedCount = (int) recordingFeedbacks.stream().filter(recordingFeedback -> recordingFeedback.getType() == FeedbackType.TASK_AUDIT_FAILED.toInt()).count();

            return taskAuditFailedCount + 1 == taskAuditSubmitDetails.size();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishTask(RecordingTask task) {
        // 更新关联 - 更新题目 / 试卷关联的状态
        if (!questionRecordingTaskStorage.updateStage(task.getId(),
                ImmutableSet.of(QuestionRecordingTaskStage.TAG_FINISHED.toInt()),
                QuestionRecordingTaskStage.PUBLISHED.toInt())) {
            throw new RuntimeException(String.format("更新题目关联的发布状态失败！taskId = %d", task.getId()));
        }
        boolean needPublishExamPaper = (task.getRecordingMode() == RecordingMode.EXAM_PAPER.toInt());
        if (needPublishExamPaper && !examPaperRecordingTaskStorage.updateStage(task.getId(),
                        ImmutableSet.of(ExamPaperRecordingTaskStage.AUDIT_APPROVED.toInt()),
                        ExamPaperRecordingTaskStage.PUBLISHED.toInt())) {
            throw new RuntimeException(String.format("更新试卷关联的发布状态失败！taskId = %d", task.getId()));
        }

        // 更新任务 - 更新录题任务的状态
        if (!recordingTaskStorage.updateTaskStage(task.getId(), RecordingTaskStage.PUBLISHED.toInt())) {
            throw new RuntimeException(String.format("更新任务状态为已发布时失败！taskId = %d", task.getId()));
        }

        // 处理反馈 - 确保最终所有反馈都是已处理的
        recordingFeedbackService.processAll(task.getId());

        // 发消息更新索引
        recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(task.getApplicationId(), task.getId()));
    }
}
