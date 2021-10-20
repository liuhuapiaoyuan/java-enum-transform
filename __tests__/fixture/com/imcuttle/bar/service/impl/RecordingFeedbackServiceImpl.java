/**
 * @(#)RecordingFeedbackServiceImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.bar.data.QuestionRecordingTaskPair;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.storage.db.RecordingFeedbackStorage;
import com.imcuttle.thrift.RecordingFeedback;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.thrift.TutorArmoryConstants.LONG_DEFAULT_ID;
import static com.imcuttle.thrift.TutorArmoryConstants.LONG_ID_NO_LIMIT;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class RecordingFeedbackServiceImpl implements RecordingFeedbackService {

    @Autowired
    private RecordingFeedbackStorage recordingFeedbackStorage;

    @Override
    public List<RecordingFeedback> getFeedBacksOfSpecificTarget(long targetId, FeedbackTargetType targetType, boolean onlyUnProcessed) {
        return getFeedBacksOfSpecificTargetOfTask(targetId, targetType, LONG_ID_NO_LIMIT, LONG_ID_NO_LIMIT, onlyUnProcessed);
    }

    @Override
    public List<RecordingFeedback> getFeedBacksOfSpecificTargetOfTask(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId, boolean onlyUnProcessed) {
        if (Objects.isNull(targetType)) {
            return Lists.newArrayList();
        }
        return recordingFeedbackStorage.getByTargetIdAndTargetTypeConditionally(targetId, targetType, examPaperId, taskId, onlyUnProcessed)
                .stream()
                .sorted(Comparator.comparing(RecordingFeedback::getCreateTime).reversed())
                .collect(toList());
    }

    @Override
    public long create(RecordingFeedback recordingFeedback) {
        clearExamPaperIdForPaperFeedback(recordingFeedback);
        return recordingFeedbackStorage.create(recordingFeedback);
    }

    @Override
    public List<Long> batchCreateWithProcessedAndCreatedTime(Collection<RecordingFeedback> recordingFeedbacks) {
        recordingFeedbacks.forEach(this::clearExamPaperIdForPaperFeedback);
        return recordingFeedbackStorage.batchCreateWithProcessedAndCreatedTime(recordingFeedbacks);
    }

    @Override
    public Map<Long, List<RecordingFeedback>> getUnprocessedFeedBacksOfRecordingTask(List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Maps.newHashMap();
        }
        return recordingFeedbackStorage.getUnprocessedFeedBacksOfRecordingTask(taskIds)
                .stream()
                .collect(Collectors.groupingBy(RecordingFeedback::getTargetId));
    }

    @Override
    public boolean processAll(long targetId, FeedbackTargetType targetType) {
        return recordingFeedbackStorage.processAll(targetId, targetType);
    }

    @Override
    public Map<Long, List<RecordingFeedback>> getAllTypeUnprocessedFeedBacksByTaskIds(List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Maps.newHashMap();
        }
        return recordingFeedbackStorage.getAllTypeUnprocessedFeedBacksByTaskIds(taskIds)
                .stream()
                .collect(Collectors.groupingBy(RecordingFeedback::getTaskId,
                        collectingAndThen(toList(), feedbacks -> feedbacks.stream().sorted(Comparator.comparing(RecordingFeedback::getCreateTime).reversed()).collect(toList()))));
    }

    @Override
    public void processFeedbackForAudit(RecordingFeedback feedback, boolean notBeenAudited, boolean everBeenApproved, boolean approved) {
        Optional<FeedbackTargetType> targetTypeOptional = FeedbackTargetType.findByInt(feedback.getTargetType());
        if (!targetTypeOptional.isPresent()) {
            throw new BadRequestException(String.format("FeedbackTargetType (%s) 不存在", feedback.getTargetType()));
        }
        FeedbackTargetType targetType = targetTypeOptional.get();

        if (notBeenAudited) {
            // 首次审核 新建反馈
            create(feedback);
        } else {
            // 非首次审核 更新
            Optional<RecordingFeedback> sameTurnFeedbackOp = recordingFeedbackStorage
                    .getByTargetIdAndTargetTypeConditionally(feedback.getTargetId(), targetType,
                            feedback.getExamPaperId(), feedback.getTaskId(), true)
                    .stream()
                    .findFirst();
            if (sameTurnFeedbackOp.isPresent()) {
                RecordingFeedback sameTurnFeedback = sameTurnFeedbackOp.get();
                sameTurnFeedback.setType(feedback.getType());
                sameTurnFeedback.setReason(feedback.getReason());
                sameTurnFeedback.setDescription(feedback.getDescription());
                sameTurnFeedback.setAttachments(feedback.getAttachments());
                sameTurnFeedback.setCreateTime(System.currentTimeMillis());
                sameTurnFeedback.setCreator(getUserId());
                updateFeedbackInfo(sameTurnFeedback);
                return;
            }

            // 特殊情况下（上一轮的审核通过反馈）在这轮不算首次审核，需要新建 而不是更新
            create(feedback);
        }
    }

    @Override
    public Map<Long, List<RecordingFeedback>> batchGetFeedBacksByTargetId(List<Long> targetIds, FeedbackTargetType targetType, long taskId, boolean onlyUnProcessed) {
        return recordingFeedbackStorage.batchGetFeedBacks(targetIds, targetType, taskId, onlyUnProcessed).stream()
                .collect(Collectors.groupingBy(RecordingFeedback::getTargetId,
                        collectingAndThen(toList(), feedbacks -> feedbacks.stream().sorted(Comparator.comparing(RecordingFeedback::getCreateTime).reversed()).collect(toList()))));
    }

    /**
     * 更新反馈的基本信息（包括创建时间）
     *
     * @param recordingFeedback
     * @return
     */
    @Override
    public boolean updateFeedbackInfo(RecordingFeedback recordingFeedback) {
        return recordingFeedbackStorage.update(recordingFeedback);
    }

    @Override
    public Optional<RecordingFeedback> getById(long feedbackId) {
        Map<Long, RecordingFeedback> recordingFeedbackMap = recordingFeedbackStorage.getByIds(Collections.singletonList(feedbackId));
        return Optional.ofNullable(recordingFeedbackMap.get(feedbackId));
    }

    @Override
    public boolean delete(long feedbackId) {
        return recordingFeedbackStorage.delete(feedbackId);
    }

    @Override
    public boolean deleteFeedBackOfQuestions(Collection<QuestionRecordingTaskPair> questionIdentifiers) {
        if (CollectionUtils.isEmpty(questionIdentifiers)) {
            return true;
        }

        return recordingFeedbackStorage.deleteFeedBackOfQuestions(questionIdentifiers);
    }

    @Override
    public boolean processAll(long taskId) {
        return recordingFeedbackStorage.processAll(taskId);
    }

    @Override
    public boolean processAllByApplicationId(long applicationId) {
        return recordingFeedbackStorage.processAllByApplicationId(applicationId);
    }

    /**
     * 非题目反馈 一律抹掉examPaperId
     *
     * @param recordingFeedback
     */
    private void clearExamPaperIdForPaperFeedback(RecordingFeedback recordingFeedback) {
        if (recordingFeedback.getTargetType() != FeedbackTargetType.QUESTION.toInt()) {
            recordingFeedback.setExamPaperId(LONG_DEFAULT_ID);
        }
    }
}
