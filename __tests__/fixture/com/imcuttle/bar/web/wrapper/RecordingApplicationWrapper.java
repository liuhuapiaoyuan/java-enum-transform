/**
 * @(#)RecordingApplicationWrapper.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.bar.web.data.RecordingApplicationBasicVO;
import com.imcuttle.bar.web.data.RecordingApplicationBriefVO;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author linbonan
 */
public class RecordingApplicationWrapper {

    public static RecordingApplicationBasicVO wrapBasic(RecordingApplication recordingApplication) {
        if (Objects.isNull(recordingApplication)) {
            return null;
        }
        RecordingApplicationBasicVO basicVO = new RecordingApplicationBasicVO();
        BeanUtils.copyProperties(recordingApplication, basicVO);
        return basicVO;
    }

    public static RecordingApplicationBriefVO wrapBriefInfo(RecordingApplication recordingApplication,
                                                            String creatorName,
                                                            List<QuestionRecordingTask> questionRecordingTasks,
                                                            List<ExamPaperRecordingTask> examPaperRecordingTasks) {
        if (Objects.isNull(recordingApplication)) {
            return null;
        }
        RecordingApplicationBriefVO briefVO = new RecordingApplicationBriefVO();
        BeanUtils.copyProperties(recordingApplication, briefVO);
        briefVO.setCreatorName(creatorName);

        long recordedQuestionCount = Optional.ofNullable(questionRecordingTasks).orElse(Lists.newArrayList())
                .stream()
                .map(QuestionRecordingTask::getQuestionId)
                .count();
        briefVO.setRecordedQuestionCount(Math.toIntExact(recordedQuestionCount));

        long recordedExamPaperCount = Optional.ofNullable(examPaperRecordingTasks).orElse(Lists.newArrayList())
                .stream()
                .map(ExamPaperRecordingTask::getExamPaperId)
                .distinct()
                .count();
        briefVO.setRecordedExamPaperCount(Math.toIntExact(recordedExamPaperCount));
        return briefVO;
    }

    public static RecordingApplication unwrap(RecordingApplicationBasicVO basicVO) {
        if (Objects.isNull(basicVO)) {
            return null;
        }
        RecordingApplication recordingApplication = new RecordingApplication();
        BeanUtils.copyProperties(basicVO, recordingApplication);
        return recordingApplication;
    }

    private RecordingApplicationWrapper() {
    }
}
