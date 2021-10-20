/**
 * @(#)QuestionRecordingTaskWrapper.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.bar.data.RecordingAuditResource;
import com.imcuttle.bar.web.data.QuestionRecordingTaskVO;
import com.imcuttle.bar.web.data.RecordingAuditResourceDetailVO;
import com.imcuttle.bar.web.data.RecordingFeedBackVO;
import com.imcuttle.thrift.QuestionRecordingTask;
import org.springframework.beans.BeanUtils;

/**
 * @author chenyibo
 */
public class QuestionRecordingTaskWrapper {

    public static QuestionRecordingTaskVO wrap(QuestionRecordingTask questionRecordingTask) {
        if (questionRecordingTask == null) {
            return null;
        }

        QuestionRecordingTaskVO questionRecordingTaskVO = new QuestionRecordingTaskVO();
        BeanUtils.copyProperties(questionRecordingTask, questionRecordingTaskVO);

        return questionRecordingTaskVO;
    }

    public static RecordingAuditResource wrapToRecordingAuditResource(QuestionRecordingTask questionRecordingTask) {
        if (questionRecordingTask == null) {
            return null;
        }

        RecordingAuditResource recordingAuditResource = new RecordingAuditResource();
        BeanUtils.copyProperties(questionRecordingTask, recordingAuditResource);
        recordingAuditResource.setTargetId(questionRecordingTask.getQuestionId());
        recordingAuditResource.setTargetType(FeedbackTargetType.QUESTION.toInt());

        return recordingAuditResource;
    }

    public static RecordingAuditResourceDetailVO wrapToRecordingAuditResourceDetailVO(QuestionRecordingTask questionRecordingTask, RecordingFeedBackVO feedBack) {
        RecordingAuditResourceDetailVO recordingAuditResourceVO = new RecordingAuditResourceDetailVO();
        recordingAuditResourceVO.setFeedBack(feedBack);
        if (questionRecordingTask != null ) {
            BeanUtils.copyProperties(questionRecordingTask, recordingAuditResourceVO);
            recordingAuditResourceVO.setTargetId(questionRecordingTask.getQuestionId());
            recordingAuditResourceVO.setTargetType(FeedbackTargetType.QUESTION.toInt());
        }

        return recordingAuditResourceVO;
    }
}
