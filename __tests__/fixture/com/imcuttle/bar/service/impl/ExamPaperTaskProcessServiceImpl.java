/**
 * @(#)ExamPaperProcessServiceImpl.java, Apr 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.bar.data.RecordingAuditResource;
import com.imcuttle.bar.service.ExamPaperRecordingTaskService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.TaskProcessService;
import com.imcuttle.bar.web.wrapper.ExamPaperRecordingTaskWrapper;
import com.imcuttle.bar.web.wrapper.QuestionRecordingTaskWrapper;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.fenbi.tutor.neoquestion.client.proxy.spi.TutorNeoQuestionProxy;
import com.fenbi.tutor.neoquestion.thrift.Chapter;
import com.fenbi.tutor.neoquestion.thrift.ChapterQuestion;
import com.fenbi.tutor.neoquestion.thrift.ExamPaper;
import com.fenbi.tutor.neoquestion.thrift.Question;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.CONTENT_TYPE_RAW;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author xiechao01
 */
@Service
@Qualifier("examPaperTaskProcessService")
@Slf4j
public class ExamPaperTaskProcessServiceImpl implements TaskProcessService {

    private static final String EXAM_PAPER_ZERO_QUESTION_REASON = "%d. 试卷中没有题目: 试卷id %s";
    private static final String EXAM_PAPER_QUESTION_NO_SCORE_REASON = "%d. 试卷中题目未设置分值: 试卷id %s";
    private static final String EXAM_PAPER_DELETED_REASON = "%d. 试卷中有「已作废」状态的题目: 试卷id %s";
    private static final String RECORDING_TASK_EXAM_PAPER_NOT_EXIST_REASON = "%d. 录题任务下不存在试卷";

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private ExamPaperRecordingTaskService examPaperRecordingTaskService;

    @Autowired
    private TutorNeoQuestionProxy tutorNeoQuestionProxy;

    @Override
    public List<String> validateForSubmit(long taskId) {
        List<String> errorReasons = new ArrayList<>();
        int badReasonIndex = 1;
        List<ExamPaperRecordingTask> examPaperRecordingTasks = examPaperRecordingTaskService.getEffectiveRelationsByRecordingTaskIds(Arrays.asList(taskId))
                .getOrDefault(taskId, Lists.newArrayList());
        if (CollectionUtils.isEmpty(examPaperRecordingTasks)) {
            errorReasons.add(String.format(RECORDING_TASK_EXAM_PAPER_NOT_EXIST_REASON, badReasonIndex++));
        }
        List<Long> examPaperIds = examPaperRecordingTasks.stream().map(ExamPaperRecordingTask::getExamPaperId).collect(toList());
        validateIllegalIds(taskId, examPaperIds);
        Map<Long, ExamPaper> examPaperMap = tutorNeoQuestionProxy.batchGetExamPapers(examPaperIds);
        List<Long> zeroQuestionExamPaperIds = new ArrayList<>();
        Set<Long> scoreNotSetExamPaperIds = new HashSet<>();
        Map<Integer, Long> questionId2ExamPaperIdMap = new HashMap<>();
        examPaperMap.forEach(((examPaperId, examPaper) -> {
            List<ChapterQuestion> chapterQuestions = examPaper.getChapters().stream().map(Chapter::getChapterQuestions)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .collect(toList());
            List<Integer> examPaperQuestionIds = chapterQuestions.stream().map(ChapterQuestion::getQuestionId).collect(toList());
            if (CollectionUtils.isEmpty(examPaperQuestionIds)) {
                zeroQuestionExamPaperIds.add(examPaperId);
            }
            //试卷exam paper中只有小问id
            examPaperQuestionIds.forEach(questionId -> questionId2ExamPaperIdMap.put(questionId, examPaperId));
            if (examPaper.isWithScore()) {
                Map<Integer, Double> questionId2ScoreMap = chapterQuestions.stream()
                        .collect(Collectors.toMap(ChapterQuestion::getQuestionId, ChapterQuestion::getScore));
                examPaperQuestionIds.forEach(questionId -> {
                    double score = questionId2ScoreMap.getOrDefault(questionId, BigDecimal.ZERO.doubleValue());
                    if (BigDecimal.valueOf(score).compareTo(BigDecimal.ZERO) == 0) {
                        scoreNotSetExamPaperIds.add(examPaperId);
                    }
                });
            }
        }));
        if (CollectionUtils.isNotEmpty(zeroQuestionExamPaperIds)) {
            errorReasons.add(String.format(EXAM_PAPER_ZERO_QUESTION_REASON, badReasonIndex++, Joiner.on(",").join(zeroQuestionExamPaperIds)));
        }
        if (CollectionUtils.isNotEmpty(scoreNotSetExamPaperIds)) {
            errorReasons.add(String.format(EXAM_PAPER_QUESTION_NO_SCORE_REASON, badReasonIndex++, Joiner.on(",").join(scoreNotSetExamPaperIds)));
        }
        Map<Integer, Question> questionMap = tutorNeoQuestionProxy.getQuestions(questionId2ExamPaperIdMap.keySet(), CONTENT_TYPE_RAW);
        List<Integer> discardQuestionIds = questionMap.values().stream().filter(Question::isDiscard).map(Question::getId).collect(toList());
        Set<Long> examPaperIdWithDiscardQuestion = new HashSet<>();
        discardQuestionIds.forEach(discardQuestionId -> {
            if (questionId2ExamPaperIdMap.containsKey(discardQuestionId) && questionId2ExamPaperIdMap.get(discardQuestionId) != null) {
                examPaperIdWithDiscardQuestion.add(questionId2ExamPaperIdMap.get(discardQuestionId));
            }
        });
        if (CollectionUtils.isNotEmpty(examPaperIdWithDiscardQuestion)) {
            errorReasons.add(String.format(EXAM_PAPER_DELETED_REASON, badReasonIndex, Joiner.on(",").join(examPaperIdWithDiscardQuestion)));
        }
        return errorReasons;
    }

    private void validateIllegalIds(long taskId, List<Long> examPaperIds) {
        if (examPaperIds.contains(0L)) {
            log.error("taskId {} 关联 试卷id {} 包含 0", taskId, examPaperIds);
            examPaperIds.remove(0L);
        }
    }

    @Override
    public List<QuestionRecordingTask> getOrderedQuestionRecordingTask(long taskId) {
        List<ExamPaperRecordingTask> examPaperRecordingTasks = getOrderedExamPaperRecordingTasksByTaskId(taskId);
        List<Long> examPaperIds = examPaperRecordingTasks.stream()
                .sorted(Comparator.comparingInt(ExamPaperRecordingTask::getOrdinal))
                .map(ExamPaperRecordingTask::getExamPaperId)
                .collect(toList());
        Map<Long, List<QuestionRecordingTask>> examPaperId2QuestionRecordingTasks = getExamPaperId2OrderedQuestionRecordingTasksMapByTaskId(taskId);
        List<QuestionRecordingTask> orderedTasks = new ArrayList<>();
        examPaperIds.forEach(examPaperId -> {
            orderedTasks.addAll(examPaperId2QuestionRecordingTasks.getOrDefault(examPaperId, Lists.newArrayList()));
        });
        return orderedTasks;
    }

    @Override
    public List<RecordingAuditResource> getOrderedRecordingAuditResource(long taskId) {
        // examPaper
        List<ExamPaperRecordingTask> orderedExamPaperRecordingTasks = getOrderedExamPaperRecordingTasksByTaskId(taskId);

        // question
        Map<Long, List<QuestionRecordingTask>> orderedExamPaperId2QuestionRecordingTasks = getExamPaperId2OrderedQuestionRecordingTasksMapByTaskId(taskId);

        // combine
        List<RecordingAuditResource> orderedResources = new ArrayList<>();
        orderedExamPaperRecordingTasks.forEach(examPaperRecordingTask -> {
            orderedResources.addAll(orderedExamPaperId2QuestionRecordingTasks.getOrDefault(examPaperRecordingTask.getExamPaperId(), Lists.newArrayList())
                    .stream().map(QuestionRecordingTaskWrapper::wrapToRecordingAuditResource).collect(toList()));
            orderedResources.add(ExamPaperRecordingTaskWrapper.wrapToRecordingAuditResource(examPaperRecordingTask));
        });

        // return
        return orderedResources;
    }

    private List<ExamPaperRecordingTask> getOrderedExamPaperRecordingTasksByTaskId(long taskId) {
        return examPaperRecordingTaskService.getEffectiveRelationsByRecordingTaskIds(Collections.singletonList(taskId))
                .getOrDefault(taskId, Lists.newArrayList()).stream()
                .sorted(Comparator.comparing(ExamPaperRecordingTask::getExamPaperId)).collect(toList());
    }

    private Map<Long, List<QuestionRecordingTask>> getExamPaperId2OrderedQuestionRecordingTasksMapByTaskId(long taskId) {
        return questionRecordingTaskService.getEffectiveRelationsByTaskIds(Collections.singletonList(taskId))
                .getOrDefault(taskId, Lists.newArrayList())
                .stream().collect(Collectors.groupingBy(QuestionRecordingTask::getExamPaperId,
                        collectingAndThen(toList(), tasks -> tasks.stream().sorted(Comparator.comparingInt(QuestionRecordingTask::getOrdinal)).collect(toList()))));
    }
}
