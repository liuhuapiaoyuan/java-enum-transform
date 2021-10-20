/**
 * @(#)RecordingQuestionVOWrapper.java, Apr 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.bar.web.data.RecordingQuestionVO;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingFeedback;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiechao01
 */
public class RecordingQuestionWrapper {

    public static List<RecordingQuestionVO> wrap(List<QuestionRecordingTask> questionRecordingTasks, Map<Long, List<RecordingFeedback>> quesitonId2LatestFeedbacks) {
        return questionRecordingTasks.stream().map(questionRecordingTask -> {
            long questionId = questionRecordingTask.getQuestionId();
            List<RecordingFeedback> feedbacks = quesitonId2LatestFeedbacks.getOrDefault(questionId, Lists.newArrayList())
                    .stream().filter(feedback -> feedback.getExamPaperId() == questionRecordingTask.getExamPaperId())
                    .collect(Collectors.toList());
            return wrap(questionRecordingTask, feedbacks);
        }).collect(Collectors.toList());
    }

    public static RecordingQuestionVO wrap(QuestionRecordingTask questionRecordingTask, List<RecordingFeedback> recordingFeedbacks) {
        RecordingQuestionVO vo = new RecordingQuestionVO();
        BeanUtils.copyProperties(questionRecordingTask, vo);
        vo.setTagStatus(questionRecordingTask.getStage());
        if (CollectionUtils.isEmpty(recordingFeedbacks)) {
            vo.setRecordingFeedBackVOS(Lists.newArrayList());
        } else {
            vo.setRecordingFeedBackVOS(Arrays.asList(RecordingFeedBackWrapper.wrap(recordingFeedbacks.get(0), new HashMap<>())));
        }
        return vo;
    }
}
