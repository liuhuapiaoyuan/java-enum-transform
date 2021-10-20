/**
 * @(#)RecordingApplicationDetailServiceImpl.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.bar.service.ExamPaperRecordingTaskService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingApplicationDetailService;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingApplicationDetail;
import com.imcuttle.thrift.RecordingTask;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class RecordingApplicationDetailServiceImpl implements RecordingApplicationDetailService {

    @Autowired
    private RecordingApplicationService recordingApplicationService;

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private ExamPaperRecordingTaskService examPaperRecordingTaskService;

    @Override
    public Map<Long, RecordingApplicationDetail> getByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Maps.newHashMap();
        }

        Map<Long, RecordingApplication> applications = recordingApplicationService.getByIds(ids);
        Map<Long, List<RecordingTask>> recordingTasks = recordingTaskService.getByApplicationIds(ids);
        Map<Long, List<QuestionRecordingTask>> questions = questionRecordingTaskService.getEffectiveRelationsByRecordingApplicationIds(ids);
        Map<Long, List<ExamPaperRecordingTask>> examPapers = examPaperRecordingTaskService.getEffectiveRelationsByRecordingApplicationIds(ids);

        return wrapRecordingApplicationDetails(ids, applications, recordingTasks, questions, examPapers);
    }

    @Override
    public Map<Long, RecordingApplicationDetail> getByIdsIncludeAllRelations(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Maps.newHashMap();
        }

        Map<Long, RecordingApplication> applications = recordingApplicationService.getByIds(ids);
        Map<Long, List<RecordingTask>> recordingTasks = recordingTaskService.getByApplicationIds(ids);
        Map<Long, List<QuestionRecordingTask>> questions = questionRecordingTaskService.getAllRelationsByRecordingApplicationIds(ids);
        Map<Long, List<ExamPaperRecordingTask>> examPapers = examPaperRecordingTaskService.getAllRelationsByRecordingApplicationIds(ids);

        return wrapRecordingApplicationDetails(ids, applications, recordingTasks, questions, examPapers);
    }

    @NotNull
    private Map<Long, RecordingApplicationDetail> wrapRecordingApplicationDetails(Collection<Long> ids, Map<Long, RecordingApplication> applications, Map<Long, List<RecordingTask>> recordingTasks, Map<Long, List<QuestionRecordingTask>> questions, Map<Long, List<ExamPaperRecordingTask>> examPapers) {
        Map<Long, RecordingApplicationDetail> details = Maps.newHashMap();
        for (Long applicationId : ids) {
            Optional.ofNullable(applications.get(applicationId))
                    .ifPresent(application -> {
                        RecordingApplicationDetail detail = new RecordingApplicationDetail();
                        detail.setApplication(application);
                        detail.setTaskList(recordingTasks.getOrDefault(applicationId, Lists.newArrayList()));
                        detail.setContainedQuestions(questions.getOrDefault(applicationId, Lists.newArrayList()));
                        detail.setContainedExamPapers(examPapers.getOrDefault(applicationId, Lists.newArrayList()));
                        details.put(applicationId, detail);
                    });
        }
        return details;
    }

    @Override
    public Set<Integer> getAllUserIdsInvolvedWithApplication(RecordingApplicationDetail detail) {
        if (Objects.isNull(detail)) {
            return Sets.newHashSet();
        }

        Set<Integer> userIds = Sets.newHashSet();
        Optional.ofNullable(detail.getApplication()).map(RecordingApplication::getCreator).ifPresent(userIds::add);
        Optional.ofNullable(detail.getTaskList())
                .orElse(Lists.newArrayList())
                .forEach(task -> {
                    userIds.add(task.getRecorder());
                    userIds.add(task.getAuditor());
                    userIds.add(task.getTagger());
                });

        return userIds.stream().filter(id -> id > 0).collect(Collectors.toSet());
    }
}
