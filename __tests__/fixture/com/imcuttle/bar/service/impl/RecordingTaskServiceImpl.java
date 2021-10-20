/**
 * @(#)RecordingTaskServiceImpl.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.commons.security.SecurityHelper;
import com.imcuttle.enums.RecordingQuestionOrigin;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.enums.RecordingTaskRoleEnum;
import com.imcuttle.bar.exceptions.CheckFailedException;
import com.imcuttle.bar.message.event.RecordingTaskCreatedEvent;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskCreatedEventProducer;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.CmsLogService;
import com.imcuttle.bar.service.ExamPaperRecordingTaskService;
import com.imcuttle.bar.service.PaperService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.QuestionService;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.bar.service.RecordingTaskTxService;
import com.imcuttle.bar.storage.db.RecordingTaskStorage;
import com.imcuttle.bar.util.UserUtil;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.thrift.TaskAuditSubmitDetail;
import com.fenbi.tutor.cmslog.thrift.CmsLogEx;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.imcuttle.enums.ExamPaperRecordingTaskStage.AUDIT_APPROVED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.TAG_FINISHED;
import static com.imcuttle.enums.RecordingTaskStage.AUDITED;
import static com.imcuttle.enums.RecordingTaskStage.RECORDING;
import static com.imcuttle.enums.RecordingTaskStage.SUBMITTED;
import static com.imcuttle.enums.RecordingTaskStage.TO_BE_REVISED;
import static com.imcuttle.bar.util.CmsLogUtil.getCmsLogIdTypeByQuestionId;
import static com.imcuttle.bar.util.CollectionUtil.getOrEmpty;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.EXAM_PAPER;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.RECORDING_APPLICATION_ID;

/**
 * @author xiechao01
 */
@Service
@Slf4j
public class RecordingTaskServiceImpl implements RecordingTaskService {

    @Autowired
    private RecordingTaskStorage recordingTaskStorage;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private ExamPaperRecordingTaskService examPaperRecordingTaskService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private RecordingTaskTxService recordingTaskTxService;

    @Autowired
    private RecordingTaskCreatedEventProducer recordingTaskCreatedEventProducer;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Autowired
    private CmsLogService cmsLogService;

    @Autowired
    private RecordingApplicationService recordingApplicationService;

    @Override
    public List<Long> create(List<RecordingTask> recordingTasks) {
        log.info("creating recordingTasks: {}", recordingTasks);
        List<Long> ids = recordingTaskStorage.batchCreate(recordingTasks);
        recordingTasks.forEach(recordingTask -> {
            recordingTaskCreatedEventProducer.publish(new RecordingTaskCreatedEvent(recordingTask.getApplicationId(), recordingTask.getId()));
        });
        return ids;
    }

    @Override
    public boolean update(RecordingTask recordingTask) {
        log.info("updating recordingTask: {}, estimatedQuestionNum: {}, recorderId: {}, auditorId: {}",
                recordingTask.getId(), recordingTask.getEstimatedQuestionNum(), recordingTask.getRecorder(), recordingTask.getAuditor());
        boolean result = recordingTaskStorage.update(recordingTask.getId(), recordingTask.getEstimatedQuestionNum(), recordingTask.getRecorder(), recordingTask.getAuditor());
        recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(recordingTask.getApplicationId(), recordingTask.getId()));
        return result;
    }

    @Override
    public RecordingTask getTaskById(long taskId) {
        return recordingTaskStorage.getById(taskId);
    }

    @Override
    public Optional<RecordingTask> getTaskByIdAndAuditor(long taskId, int auditorId) {
        RecordingTask task = getTaskById(taskId);
        if (auditorId == task.getAuditor()) {
            return Optional.of(task);
        }
        return Optional.empty();
    }

    @Override
    public int countSubmittedTasksByAuditor(int auditorId) {
        return recordingTaskStorage
                .countTasksByAuditor(auditorId, ImmutableList.of(SUBMITTED.toInt()))
                .getOrDefault(SUBMITTED.toInt(), 0);
    }

    @Override
    public Map<Integer, Integer> getRecordingTaskCountByRole(RecordingTaskRoleEnum taskRole, int userId) {
        Map<Integer, Integer> result = new HashMap<>();
        switch (taskRole) {
            case RECORDING_TASK_RECORDER:
                List<Integer> stageList = Arrays.asList(RECORDING.toInt(), TO_BE_REVISED.toInt());
                result = recordingTaskStorage.countByRecorder(userId, stageList);
                for (Integer stage : stageList) {
                    if (!result.containsKey(stage) || result.get(stage) == null) {
                        result.put(stage, 0);
                    }
                }
                break;
            case RECORDING_TASK_AUDITOR:
                result = recordingTaskStorage.countTasksByAuditor(userId, Arrays.asList(SUBMITTED.toInt(), TO_BE_REVISED.toInt()));
                result.putIfAbsent(SUBMITTED.toInt(), 0);
                result.putIfAbsent(TO_BE_REVISED.toInt(), 0);
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public Map<Long, List<RecordingTask>> getByApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Maps.newHashMap();
        }

        return recordingTaskStorage.getByRecordingApplicationIds(applicationIds)
                .stream()
                .collect(Collectors.groupingBy(RecordingTask::getApplicationId));
    }

    @Override
    public List<RecordingTask> getByRecorderAndStage(RecordingTaskStage taskStage, int userId, int page, int pageSize) {
        return recordingTaskStorage.getByRecorderAndStage(taskStage, userId, page, pageSize);
    }

    @Override
    public int countRejectedTasksByAuditor(int userId) {
        return recordingTaskStorage
                .countTasksByAuditor(userId, ImmutableList.of(TO_BE_REVISED.toInt()))
                .getOrDefault(TO_BE_REVISED.toInt(), 0);
    }

    @Override
    public List<RecordingTask> getSubmittedTasksByAuditor(int auditorId, int page, int pageSize) {
        return recordingTaskStorage.getByAuditorAndStage(auditorId, page, pageSize, SUBMITTED.toInt());
    }

    @Override
    public List<RecordingTask> getRejectedTasksByAuditor(int userId, int page, int pageSize) {
        return recordingTaskStorage.getByAuditorAndStage(userId, page, pageSize, TO_BE_REVISED.toInt());
    }

    @Override
    public boolean submit(long taskId) {
        return recordingTaskStorage.submit(taskId);
    }

    @Override
    public RecordingTask checkTaskStageAndGet(long taskId, RecordingTaskStage expectedStage) {
        if (expectedStage == null) {
            throw new CheckFailedException("expectedStage 为空");
        }

        RecordingTask task = getTaskById(taskId);
        doCheckTaskStage(task, expectedStage);
        return task;
    }

    @Override
    public boolean updateTaskStage(long taskId, int stage) {
        log.info("Update task stage, taskId = {}, stage = {}", taskId, stage);
        return recordingTaskStorage.updateTaskStage(taskId, stage);
    }

    @Override
    public boolean updateTaskStageByApplicationId(long applicationId, int stage) {
        log.info("Update task stage by application id, applicationId = {}, stage = {}", applicationId, stage);
        return recordingTaskStorage.updateTaskStageByApplicationId(applicationId, stage);
    }

    @Override
    public boolean updateTagger(long taskId, int newTaggerId) {
        return recordingTaskStorage.updateTagger(taskId, newTaggerId);
    }

    @Override
    public void publishTask(RecordingTask task) {
        doCheckTaskStage(task, AUDITED);

        // 前置校验 - 检查关联关系的状态
        List<QuestionRecordingTask> questionRelations = questionRecordingTaskService
                .getEffectiveRelationsByTaskIds(Collections.singletonList(task.getId()))
                .getOrDefault(task.getId(), Lists.newLinkedList());
        List<ExamPaperRecordingTask> paperRelations = examPaperRecordingTaskService
                .getEffectiveRelationsByTaskIds(Collections.singletonList(task.getId()))
                .getOrDefault(task.getId(), Lists.newLinkedList());
        checkAllRelationsMatch(questionRelations, relation -> TAG_FINISHED.toInt() == relation.getStage());
        checkAllRelationsMatch(paperRelations, relation -> AUDIT_APPROVED.toInt() == relation.getStage());

        // 检查题目 - 检查题目是否全部已设置难度 & 全部设置的考点有效
        List<Integer> wholeQuestionIds = questionRelations.stream()
                .map(QuestionRecordingTask::getQuestionId)
                .collect(Collectors.toList());
        questionService.checkWholeQuestions(wholeQuestionIds);

        // 发布题目 - 更新 非题库已有题目 的状态
        List<Integer> newlyRecordedQuestionIds = questionRelations.stream()
                .filter(relation -> RecordingQuestionOrigin.RECORDED.toInt() == relation.getQuestionOrigin())
                .map(QuestionRecordingTask::getQuestionId)
                .collect(Collectors.toList());
        if (!questionService.publishWholeQuestions(newlyRecordedQuestionIds)) {
            throw new RuntimeException(String.format("发布题目失败，newlyRecordedQuestionIds = %s", newlyRecordedQuestionIds));
        }

        // 发布试卷 - 更新试卷的状态
        Set<Long> paperIds = paperRelations.stream()
                .map(ExamPaperRecordingTask::getExamPaperId)
                .filter(examPaperId -> examPaperId > 0L)
                .collect(Collectors.toSet());
        paperIds.addAll(questionRelations.stream()
                .map(QuestionRecordingTask::getExamPaperId)
                .filter(examPaperId -> examPaperId > 0L)
                .collect(Collectors.toList()));
        if (!paperService.publishArmoryPapers(Lists.newArrayList(paperIds))) {
            throw new RuntimeException(String.format("发布Armory试卷失败，paperIds = %s", paperIds));
        }

        recordingTaskTxService.publishTask(task);

        List<CmsLogEx> cmsLogs = getCmsLogList(task.getApplicationId(), task.getId(), paperIds, newlyRecordedQuestionIds);
        cmsLogService.batchCreateCmsLog(cmsLogs);
    }

    @Override
    public Map<Long, RecordingTask> getTaskByIds(List<Long> taskIds) {
        return recordingTaskStorage.getByIds(taskIds).stream().collect(Collectors.toMap(RecordingTask::getId, Function.identity()));
    }

    @Override
    public Map<Long, RecordingApplication> getRecordingApplicationByTaskIds(List<Long> taskIds) {
        Map<Long, Long> taskId2ApplicationIdMap = recordingTaskStorage.getByIds(taskIds).stream().collect(Collectors.toMap(RecordingTask::getId, RecordingTask::getApplicationId));
        Map<Long, RecordingApplication> recordingApplicationMap = recordingApplicationService.getByIds(taskId2ApplicationIdMap.values());
        Map<Long, RecordingApplication> result = new HashMap<>();
        taskId2ApplicationIdMap.forEach((taskId, applicationId) -> {
            if (recordingApplicationMap.containsKey(applicationId) && recordingApplicationMap.get(applicationId) != null) {
                result.put(taskId, recordingApplicationMap.get(applicationId));
            }
        });

        return result;
    }

    private List<CmsLogEx> getCmsLogList(long applicationId, long taskId, Set<Long> paperIds, List<Integer> questionIds) {
        List<CmsLogEx> cmsLogs = Lists.newArrayList();
        int userType = UserUtil.getUserType();
        int userId = SecurityHelper.getUserId();
        String ldap = Optional.ofNullable(SecurityHelper.getLdapId()).orElse("");
        // 录题任务
        cmsLogs.add(buildCmsLogEx(applicationId, RECORDING_APPLICATION_ID, "发布录题任务", userType, userId, ldap));

        // 试卷
        paperIds.forEach(paperId -> cmsLogs.add(buildCmsLogEx(paperId, EXAM_PAPER,
                String.format("试卷状态更新为已发布，所属录题申请 id:%s，录题任务 id:%s", applicationId, taskId), userType, userId, ldap)));

        // 题目
        questionIds.forEach(questionId -> cmsLogs.add(buildCmsLogEx(questionId, getCmsLogIdTypeByQuestionId(questionId),
                String.format("题目状态更新为已发布，所属录题申请 id:%s，录题任务 id:%s", applicationId, taskId), userType, userId, ldap)));
        return cmsLogs;
    }

    private CmsLogEx buildCmsLogEx(long id, int idType, String content, int userType, int userId, String ldap) {
        return new CmsLogEx(id, idType, userType, ldap, userId, content, System.currentTimeMillis());
    }

    private void doCheckTaskStage(RecordingTask task, RecordingTaskStage expectedStage) {
        if (task == null) {
            throw new CheckFailedException("录题任务为空！");
        }

        if (task.getStage() != expectedStage.toInt()) {
            throw new CheckFailedException(String.format("录题任务 ID = %d 未处于期望的状态！stage = %d", task.getId(), expectedStage.toInt()));
        }
    }

    private <T> void checkAllRelationsMatch(List<T> relations, Predicate<T> condition) {
        getOrEmpty(relations).forEach(relation -> {
            if (!condition.test(relation)) {
                throw new CheckFailedException("任务下存在未完成标注的题目 / 未审核通过的试卷，不能发布，请检查！");
            }
        });
    }
}
