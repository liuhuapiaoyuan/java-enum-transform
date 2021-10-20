/**
 * @(#)FeedbackLogicImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.commons2.rest.exception.BadRequestException;
import com.fenbi.commons2.rest.exception.NotFoundException;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.CmsLogService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.storage.db.RecordingTaskStorage;
import com.imcuttle.bar.web.data.QuestionFeedbackCreateOrUpdateRequestVO;
import com.imcuttle.bar.web.data.QuestionFeedbackDeleteRequestVO;
import com.imcuttle.bar.web.data.RecordingFeedBackVO;
import com.imcuttle.bar.web.logic.RecordingFeedbackLogic;
import com.imcuttle.bar.web.wrapper.RecordingFeedBackWrapper;
import com.imcuttle.thrift.AuditDetail;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingFeedback;
import com.imcuttle.thrift.RecordingTask;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.enums.FeedbackType.QUESTION_NEED_REVISED;
import static com.imcuttle.bar.util.CmsLogUtil.getCmsLogIdTypeByQuestionId;
import static com.imcuttle.thrift.TutorArmoryConstants.LONG_ID_NO_LIMIT;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class RecordingFeedbackLogicImpl implements RecordingFeedbackLogic {

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Autowired
    private CmsLogService cmsLogService;

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Override
    public List<RecordingFeedBackVO> getFeedBacksOfSpecificTarget(long targetId, FeedbackTargetType targetType) {
        return getFeedBacksOfSpecificTargetOfTask(targetId, targetType, LONG_ID_NO_LIMIT, LONG_ID_NO_LIMIT, false);
    }

    @Override
    public List<RecordingFeedBackVO> getFeedBacksOfSpecificTargetOfTask(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId) {
        return getFeedBacksOfSpecificTargetOfTask(targetId, targetType, examPaperId, taskId, false);
    }

    @Override
    public List<RecordingFeedBackVO> getFeedBacksOfSpecificTargetOfTask(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId, boolean onlyUnProcessed) {
        List<RecordingFeedback> feedbacks = recordingFeedbackService.getFeedBacksOfSpecificTargetOfTask(targetId, targetType, examPaperId, taskId, onlyUnProcessed);
        Set<Integer> creatorIds = feedbacks.stream().map(RecordingFeedback::getCreator).collect(Collectors.toSet());
        Map<Integer, String> userNames = userService.getUserNames(creatorIds);
        return feedbacks.stream().map(feedback -> RecordingFeedBackWrapper.wrap(feedback, userNames)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createOrUpdateWhenTag(QuestionFeedbackCreateOrUpdateRequestVO requestVO) {
        long taskId = requestVO.getTaskId();
        int questionId = requestVO.getQuestionId();
        long examPaperId = requestVO.getExamPaperId();
        Optional<QuestionRecordingTask> taskOptional = questionRecordingTaskService.get(taskId, questionId, examPaperId);
        if (!taskOptional.isPresent()) {
            throw new BadRequestException("题目未关联到录题任务上");
        }
        QuestionRecordingTask questionRecordingTask = taskOptional.get();
        long id = 0;
        List<RecordingFeedback> existUnprocessFeedbacks = recordingFeedbackService.getFeedBacksOfSpecificTargetOfTask(questionId, FeedbackTargetType.QUESTION, requestVO.getExamPaperId(), taskId, true);

        boolean res = true;
        Optional<QuestionRecordingTaskStage> stageOpt = QuestionRecordingTaskStage.findByInt(questionRecordingTask.getStage());
        if (!stageOpt.isPresent()) {
            throw new BadRequestException("题目状态不合法");
        }

        RecordingTask recordingTask = recordingTaskService.getTaskById(taskId);
        if (Objects.isNull(recordingTask)) {
            throw new BadRequestException("所属录题任务不存在");
        }

        QuestionRecordingTaskStage questionRecordingTaskStage = stageOpt.get();
        switch (questionRecordingTaskStage) {
            case TO_BE_TAGGED:
                //未标注，纠错
                if (CollectionUtils.isEmpty(existUnprocessFeedbacks)) {
                    RecordingFeedback recordingFeedback = buildRecordingFeedback(requestVO);
                    id = recordingFeedbackService.create(recordingFeedback);
                }
                break;
            case TAG_FINISHED:
                //标注完成，重新纠错
                if (CollectionUtils.isEmpty(existUnprocessFeedbacks)) {
                    RecordingFeedback recordingFeedback = buildRecordingFeedback(requestVO);
                    id = recordingFeedbackService.create(recordingFeedback);
                }
                break;
            case TAG_FAILED:
                //纠错过，修改feedback
                if (CollectionUtils.isNotEmpty(existUnprocessFeedbacks)) {
                    RecordingFeedback recordingFeedback = existUnprocessFeedbacks.get(0);
                    recordingFeedback.setReason(requestVO.getReason());
                    recordingFeedback.setDescription(requestVO.getDescription());
                    recordingFeedback.setAttachments(requestVO.getAttachments());
                    recordingFeedback.setCreateTime(System.currentTimeMillis());
                    recordingFeedback.setCreator(getUserId());
                    res = recordingFeedbackService.updateFeedbackInfo(recordingFeedback);
                    id = recordingFeedback.getId();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + questionRecordingTaskStage);
        }

        AuditDetail auditDetail = new AuditDetail();
        auditDetail.setAuditTimes(recordingTask.getSubmitTimes());
        auditDetail.setOriginStage(questionRecordingTask.getStage());
        auditDetail.setLatestStage(QuestionRecordingTaskStage.TAG_FAILED.toInt());
        auditDetail.setAuditor(getUserId());
        auditDetail.setAuditTime(System.currentTimeMillis());

        List<AuditDetail> auditDetails = CollectionUtils.isEmpty(questionRecordingTask.getAuditDetails()) ? Lists.newArrayList() : questionRecordingTask.getAuditDetails();
        auditDetails.add(auditDetail);

        questionRecordingTask.setStage(QuestionRecordingTaskStage.TAG_FAILED.toInt());
        questionRecordingTask.setAuditDetails(auditDetails);
        res &= questionRecordingTaskService.batchUpdate(Collections.singletonList(questionRecordingTask));
        if (res) {
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(questionRecordingTask.getApplicationId(), taskId));
        } else {
            throw new RuntimeException("update stage or feedback info failed");
        }

        String content = "标注纠错，原因:" + Joiner.on("、").join(requestVO.getReason()) + "，详细描述:" + requestVO.getDescription();
        cmsLogService.log(questionId, getCmsLogIdTypeByQuestionId(questionId), content);

        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletedFeedback(QuestionFeedbackDeleteRequestVO requestVO) {
        List<RecordingFeedback> feedbacks = recordingFeedbackService.getFeedBacksOfSpecificTargetOfTask(requestVO.getQuestionId(), FeedbackTargetType.QUESTION, requestVO.getExamPaperId(), requestVO.getTaskId(), true);
        if (CollectionUtils.isEmpty(feedbacks)) {
            throw new NotFoundException();
        }
        if (feedbacks.size() != 1) {
            log.warn("题目下关联了多个未处理的纠错反馈");
        }
        RecordingFeedback recordingFeedback = feedbacks.get(0);
        boolean res = recordingFeedbackService.delete(recordingFeedback.getId());
        res &= questionRecordingTaskService.updateStage(recordingFeedback.getTaskId(), requestVO.getExamPaperId(), (int) recordingFeedback.getTargetId(), QuestionRecordingTaskStage.TO_BE_TAGGED.toInt());
        if (res) {
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(recordingFeedback.getApplicationId(), recordingFeedback.getTaskId()));
        } else {
            throw new RuntimeException("process feedback or update questionRecordingTask stage failed");
        }

        cmsLogService.log(requestVO.getQuestionId(), getCmsLogIdTypeByQuestionId(requestVO.getQuestionId()), "取消纠错");

        return true;
    }

    private RecordingFeedback buildRecordingFeedback(QuestionFeedbackCreateOrUpdateRequestVO requestVO) {
        RecordingFeedback recordingFeedback = new RecordingFeedback();
        recordingFeedback.setApplicationId(requestVO.getApplicationId());
        recordingFeedback.setTaskId(requestVO.getTaskId());
        recordingFeedback.setTargetId(requestVO.getQuestionId());
        recordingFeedback.setExamPaperId(requestVO.getExamPaperId());
        recordingFeedback.setTargetType(FeedbackTargetType.QUESTION.toInt());
        recordingFeedback.setAttachments(requestVO.getAttachments());
        recordingFeedback.setReason(requestVO.getReason());
        recordingFeedback.setDescription(requestVO.getDescription());
        recordingFeedback.setCreator(getUserId());
        recordingFeedback.setType(QUESTION_NEED_REVISED.toInt());
        return recordingFeedback;
    }
}
