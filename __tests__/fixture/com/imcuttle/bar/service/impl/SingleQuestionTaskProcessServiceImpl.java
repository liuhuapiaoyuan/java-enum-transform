/**
 * @(#)ExamPaperProcessServiceImpl.java, Apr 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.bar.data.RecordingAuditResource;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.TaskProcessService;
import com.imcuttle.bar.web.wrapper.QuestionRecordingTaskWrapper;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.fenbi.tutor.neoquestion.client.proxy.spi.TutorNeoQuestionProxy;
import com.fenbi.tutor.neoquestion.thrift.Question;
import com.fenbi.tutor.neoquestion.thrift.Vignette;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.CONTENT_TYPE_RAW;
import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.QUESTION_VIGNETTE_ID_DIVISION_NUMBER;
import static java.util.stream.Collectors.toList;

/**
 * @author xiechao01
 */
@Service
@Qualifier("singleQuestionTaskProcessService")
public class SingleQuestionTaskProcessServiceImpl implements TaskProcessService {

    private static final String QUESTION_DELETED_REASON = "任务中有「已作废」状态的题目：题目id %s";

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private TutorNeoQuestionProxy tutorNeoQuestionProxy;

    @Override
    public List<String> validateForSubmit(long taskId) {
        List<String> errorReasons = new ArrayList<>();
        List<QuestionRecordingTask> questionRecordingTasks = questionRecordingTaskService.getEffectiveRelationsByTaskIds(Arrays.asList(taskId))
                .getOrDefault(taskId, Lists.newArrayList());
        List<Integer> wholeQuestionIdsInTask = questionRecordingTasks.stream().map(QuestionRecordingTask::getQuestionId).collect(toList());
        List<Integer> vignetteIdsInTask = wholeQuestionIdsInTask.stream().filter(id -> id >= QUESTION_VIGNETTE_ID_DIVISION_NUMBER).collect(toList());
        List<Integer> questionIdsInTask = wholeQuestionIdsInTask.stream().filter(id -> id < QUESTION_VIGNETTE_ID_DIVISION_NUMBER).collect(toList());
        Map<Integer, Vignette> vignetteMap = tutorNeoQuestionProxy.getVignettes(vignetteIdsInTask, CONTENT_TYPE_RAW);
        List<Integer> questionIdsInVignette = vignetteMap.values().stream().map(Vignette::getQuestionIds).flatMap(Collection::stream).collect(toList());
        questionIdsInTask.addAll(questionIdsInVignette);
        Map<Integer, Question> questionMap = tutorNeoQuestionProxy.getQuestions(questionIdsInTask, CONTENT_TYPE_RAW);
        List<Integer> discardQuestionIds = questionMap.values().stream()
                .filter(Question::isDiscard)
                .map(Question::getId)
                .filter(wholeQuestionIdsInTask::contains)
                .collect(toList());
        List<Integer> discardVignetteIds = vignetteMap.values().stream()
                .filter(Vignette::isDiscard)
                .map(Vignette::getId)
                .filter(wholeQuestionIdsInTask::contains)
                .collect(toList());
        discardQuestionIds.addAll(discardVignetteIds);
        if (CollectionUtils.isNotEmpty(discardQuestionIds)) {
            errorReasons.add(String.format(QUESTION_DELETED_REASON, Joiner.on(",").join(discardQuestionIds)));
        }
        return errorReasons;
    }

    @Override
    public List<QuestionRecordingTask> getOrderedQuestionRecordingTask(long taskId) {
        return questionRecordingTaskService.getEffectiveRelationsByTaskIds(Collections.singletonList(taskId)).getOrDefault(taskId, Lists.newArrayList())
                .stream().sorted(Comparator.comparing(QuestionRecordingTask::getOrdinal)).collect(toList());
    }

    @Override
    public List<RecordingAuditResource> getOrderedRecordingAuditResource(long taskId) {
        return getOrderedQuestionRecordingTask(taskId).stream()
                .map(QuestionRecordingTaskWrapper::wrapToRecordingAuditResource).collect(toList());
    }
}
