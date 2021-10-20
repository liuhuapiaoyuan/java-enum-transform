package com.imcuttle.bar.service.impl;

import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.bar.data.RecordingApplicationAuditResult;
import com.imcuttle.bar.message.event.RecordingApplicationUpdateEvent;
import com.imcuttle.bar.message.producer.RecordingApplicationUpdateEventProducer;
import com.imcuttle.bar.service.RecordingApplicationAuditService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.storage.db.RecordingApplicationStorage;
import com.imcuttle.bar.util.RecordingApplicationStageUtil;
import com.imcuttle.thrift.RecordingFeedback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * @author luke
 */
@Slf4j
@Service
public class RecordingApplicationAuditServiceImpl implements RecordingApplicationAuditService {

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private RecordingApplicationStorage recordingApplicationStorage;

    @Autowired
    private RecordingApplicationUpdateEventProducer recordingApplicationUpdateEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(RecordingApplicationAuditResult auditResult) {
        long recordingApplicationId = auditResult.getRecordingApplicationId();

        // 1. 录题审核状态流转
        boolean result = recordingApplicationStorage.updateStage(recordingApplicationId, RecordingApplicationStage.SUBMITTED.toInt(), auditResult.getStage().toInt());
        if (!result) {
            log.warn("设置录题申请({})进度失败. targetStage:{}, sourceStage:{}", recordingApplicationId,
                    auditResult.getStage().toInt(), RecordingApplicationStage.SUBMITTED.toInt());
            throw new BadRequestException(String.format("录题申请(%s)已被其他人审核,请确认!", auditResult));
        }

        // 2. 创建反馈
        RecordingFeedback feedback = buildFeedback(auditResult);
        long feedbackId = recordingFeedbackService.create(feedback);
        if (feedbackId <= 0) {
            log.error("录题申请({})创建录题反馈失败.", recordingApplicationId);
            throw new BadRequestException(String.format("录题申请(%s)创建录题反馈失败,请确认!", auditResult));
        }
        recordingFeedbackService.processAllByApplicationId(recordingApplicationId);

        // 3. 发消息
        RecordingApplicationUpdateEvent updateEvent = buildUpdateEvent(recordingApplicationId);
        recordingApplicationUpdateEventProducer.publish(updateEvent);
    }

    private RecordingFeedback buildFeedback(RecordingApplicationAuditResult auditResult) {
        RecordingFeedback feedback = new RecordingFeedback();
        feedback.setApplicationId(auditResult.getRecordingApplicationId());
        feedback.setTargetId(auditResult.getRecordingApplicationId());
        feedback.setTargetType(FeedbackTargetType.APPLICATION.toInt());
        feedback.setType(auditResult.getFeedbackType());
        feedback.setDescription(auditResult.getDescription());
        feedback.setAttachments(auditResult.getAttachments());
        feedback.setCreator(auditResult.getAuditorId());
        return feedback;
    }

    private RecordingApplicationUpdateEvent buildUpdateEvent(long recordingApplicationId) {
        RecordingApplicationUpdateEvent updateEvent = new RecordingApplicationUpdateEvent();
        updateEvent.setRecordingApplicationId(recordingApplicationId);
        return updateEvent;
    }
}
