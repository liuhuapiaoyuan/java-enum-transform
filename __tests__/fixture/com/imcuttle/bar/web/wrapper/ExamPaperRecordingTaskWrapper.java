/**
 * @(#)ExamPaperRecordingTaskWrapper.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.bar.data.RecordingAuditResource;
import com.imcuttle.bar.web.data.ExamPaperRecordingTaskVO;
import com.imcuttle.bar.web.data.RecordingAuditResourceDetailVO;
import com.imcuttle.bar.web.data.RecordingFeedBackVO;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import org.springframework.beans.BeanUtils;

/**
 * @author chenyibo
 */
public class ExamPaperRecordingTaskWrapper {

    public static ExamPaperRecordingTaskVO wrap(ExamPaperRecordingTask examPaperRecordingTask) {
        if (examPaperRecordingTask == null) {
            return null;
        }

        ExamPaperRecordingTaskVO examPaperRecordingTaskVO = new ExamPaperRecordingTaskVO();
        BeanUtils.copyProperties(examPaperRecordingTask, examPaperRecordingTaskVO);
        return examPaperRecordingTaskVO;
    }

    public static RecordingAuditResource wrapToRecordingAuditResource(ExamPaperRecordingTask examPaperRecordingTask) {
        if (examPaperRecordingTask == null) {
            return null;
        }

        RecordingAuditResource recordingAuditResource = new RecordingAuditResource();
        BeanUtils.copyProperties(examPaperRecordingTask, recordingAuditResource);
        recordingAuditResource.setTargetId(examPaperRecordingTask.getExamPaperId());
        recordingAuditResource.setTargetType(FeedbackTargetType.EXAM_PAPER.toInt());
        return recordingAuditResource;
    }

    public static RecordingAuditResourceDetailVO wrapToRecordingAuditResourceDetailVO(ExamPaperRecordingTask examPaperRecordingTask, RecordingFeedBackVO feedBackVO) {
        RecordingAuditResourceDetailVO detailVO = new RecordingAuditResourceDetailVO();
        detailVO.setFeedBack(feedBackVO);
        if (examPaperRecordingTask != null) {
            BeanUtils.copyProperties(examPaperRecordingTask, detailVO);
            detailVO.setTargetId(examPaperRecordingTask.getExamPaperId());
            detailVO.setTargetType(FeedbackTargetType.EXAM_PAPER.toInt());
        }

        return detailVO;
    }
}
