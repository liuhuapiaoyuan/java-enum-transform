/**
 * @(#)RecordingTaskLogicImpl.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.common.lambda.Lambdas;
import com.fenbi.commons.paging.Page;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.fenbi.commons2.rest.exception.NotFoundException;
import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.enums.RecordingMode;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.data.Change;
import com.imcuttle.bar.enums.RecordingTaskRoleEnum;
import com.imcuttle.bar.exceptions.TaskValidateException;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.CmsLogService;
import com.imcuttle.bar.service.ExamPaperRecordingTaskService;
import com.imcuttle.bar.service.LogDiffService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.bar.service.TaskProcessService;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.util.MapUtil;
import com.imcuttle.bar.util.RecordingTaskUtil;
import com.imcuttle.bar.web.data.ExamPaperRecordingTaskVO;
import com.imcuttle.bar.web.data.QuestionRecordingTaskVO;
import com.imcuttle.bar.web.data.RecordingFeedBackVO;
import com.imcuttle.bar.web.data.RecordingTaskCreateRequestVO;
import com.imcuttle.bar.web.data.RecordingTaskExamPaperQuestionsVO;
import com.imcuttle.bar.web.data.RecordingTaskUpdateRequestVO;
import com.imcuttle.bar.web.data.RecordingTaskVO;
import com.imcuttle.bar.web.logic.RecordingTaskLogic;
import com.imcuttle.bar.web.wrapper.ExamPaperRecordingTaskWrapper;
import com.imcuttle.bar.web.wrapper.QuestionRecordingTaskWrapper;
import com.imcuttle.bar.web.wrapper.RecordingFeedBackWrapper;
import com.imcuttle.bar.web.wrapper.RecordingTaskVOWrapper;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingFeedback;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.thrift.TaskAuditSubmitDetail;
import com.fenbi.tutor.crmaccount.thrift.UserInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_RECORDING;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_RECORDING_OF_OTHERS;
import static com.imcuttle.bar.enums.FeatureEnum.RECORDING_APPLICATION_TASK_ASSIGN;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.RECORDING_APPLICATION_ID;

/**
 * @author xiechao01
 */
@Service
@Slf4j
public class RecordingTaskLogicImpl extends BaseLogic implements RecordingTaskLogic {

    private static final String REASON_HEAD = "无法提交录题任务，原因：";
    private static final String BR_HTML = "<br>";
    private static final String ASSIGN_TASK = "分配任务";
    private static final String CREATE_RECORDING_TASK = "创建录题任务";

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Autowired
    private RecordingApplicationService recordingApplicationService;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private ExamPaperRecordingTaskService examPaperRecordingTaskService;

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Autowired
    @Qualifier("examPaperTaskProcessService")
    private TaskProcessService examPaperTaskProcessService;

    @Autowired
    @Qualifier("singleQuestionTaskProcessService")
    private TaskProcessService singleQuestionTaskProcessService;

    @Autowired
    private CmsLogService cmsLogService;

    @Autowired
    @Qualifier("recordingTaskLogDiffService")
    private LogDiffService<RecordingTask> recordingTaskLogDiffService;

    @Override
    public long create(RecordingTaskCreateRequestVO createRequestVO) {
        long applicationId = createRequestVO.getApplicationId();
        RecordingApplication recordingApplication = recordingApplicationService.getByIds(Collections.singletonList(applicationId)).get(applicationId);
        if (recordingApplication == null) {
            throw new BadRequestException("录题申请不存在! id: " + applicationId);
        }
        if (recordingApplication.getStage() != RecordingApplicationStage.TO_BE_ASSIGNED.toInt()) {
            throw new BadRequestException("录题申请不处于待分配状态");
        }

        RecordingTask recordingTask = new RecordingTask();
        recordingTask.setSubjectId(recordingApplication.getSubjectId());
        recordingTask.setPhaseId(recordingApplication.getPhaseId());
        recordingTask.setApplicationId(createRequestVO.getApplicationId());
        recordingTask.setApplicationSubmitTime(recordingApplication.getSubmitTime());
        recordingTask.setEstimatedQuestionNum(createRequestVO.getEstimatedQuestionNum());
        recordingTask.setRecordingMode(createRequestVO.getRecordingMode());
        recordingTask.setRecorder(createRequestVO.getRecorderId());
        recordingTask.setAuditor(createRequestVO.getAuditorId());
        recordingTask.setStage(RecordingTaskStage.RECORDING.toInt());

        verifyForSaveRecordingTask(recordingTask);

        List<Long> recordingTaskIds = recordingApplicationService.createRecordingTasksForApplication(applicationId, Collections.singletonList(recordingTask));
        if (CollectionUtils.isEmpty(recordingTaskIds)) {
            throw new BadRequestException("分配录题任务失败，录题申请不处于待分配状态");
        }

        cmsLogService.log(applicationId, RECORDING_APPLICATION_ID, ASSIGN_TASK);
        cmsLogService.log(applicationId, RECORDING_APPLICATION_ID, CREATE_RECORDING_TASK);
        return recordingTaskIds.get(0);
    }

    @Override
    public boolean update(RecordingTaskUpdateRequestVO requestVO) {
        long applicationId = requestVO.getApplicationId();
        List<RecordingTask> recordingTasks = recordingTaskService.getByApplicationIds(Collections.singletonList(applicationId)).get(applicationId);
        if (CollectionUtils.isEmpty(recordingTasks)) {
            throw new BadRequestException("录题申请下无录题任务");
        }

        RecordingTask targetTask = recordingTasks.get(0);
        RecordingTask recordingTask = new RecordingTask();
        recordingTask.setId(targetTask.getId());
        recordingTask.setApplicationId(targetTask.getApplicationId());
        recordingTask.setEstimatedQuestionNum(requestVO.getEstimatedQuestionNum());
        recordingTask.setRecorder(requestVO.getRecorderId());
        recordingTask.setAuditor(requestVO.getAuditorId());
        recordingTask.setSubjectId(targetTask.getSubjectId());
        recordingTask.setPhaseId(targetTask.getPhaseId());

        verifyForSaveRecordingTask(recordingTask);

        boolean result = recordingTaskService.update(recordingTask);
        List<Change> changes = recordingTaskLogDiffService.diff(recordingTasks.get(0), recordingTask, Arrays.asList("estimatedQuestionNum", "recorder", "auditor"));
        if (CollectionUtils.isNotEmpty(changes)) {
            String cmsLog = "修改任务分配，" + Joiner.on("；").join(changes.stream().map(Change::toString).collect(Collectors.toList()));
            cmsLogService.log(applicationId, RECORDING_APPLICATION_ID, cmsLog);
        }
        return result;
    }

    private void verifyForSaveRecordingTask(RecordingTask recordingTask) {
        if (recordingTask.getEstimatedQuestionNum() <= 0) {
            throw new BadRequestException("应录题数需要是正整数");
        }
        if (recordingTask.getRecorder() <= 0) {
            throw new BadRequestException("需要设置录题人");
        }
        if (recordingTask.getAuditor() <= 0) {
            throw new BadRequestException("需要设置审核人");
        }

        authorizeFeature(recordingTask.getSubjectId(), recordingTask.getPhaseId(), RECORDING_APPLICATION_TASK_ASSIGN);
    }

    @Override
    public Map<Integer, Integer> getRecordingTasksByRole(RecordingTaskRoleEnum taskRole) {
        return recordingTaskService.getRecordingTaskCountByRole(taskRole, getUserId());
    }

    @Override
    public Page<RecordingTaskVO> getRecordingTaskByStatus(RecordingTaskStage taskStage, int page, int pageSize) {
        int userId = getUserId();
        int totalItem = recordingTaskService.getRecordingTaskCountByRole(RecordingTaskRoleEnum.RECORDING_TASK_RECORDER, userId)
                .getOrDefault(taskStage.toInt(), 0);
        List<RecordingTask> recordingTaskList = recordingTaskService.getByRecorderAndStage(taskStage, userId, page, pageSize);

        List<RecordingTaskVO> recordingTaskVOList = wrapVOList(recordingTaskList, taskStage == RecordingTaskStage.TO_BE_REVISED);

        Page<RecordingTaskVO> result = new Page<>(totalItem, page, pageSize);
        result.setList(recordingTaskVOList);
        return result;
    }

    @Override
    public RecordingTaskVO getRecordingTask(long taskId) {
        RecordingTask recordingTask = recordingTaskService.getTaskById(taskId);
        if (recordingTask == null) {
            throw new NotFoundException("录题任务id不存在:" + taskId);
        }
        Optional<RecordingTaskStage> taskStageOptional = RecordingTaskStage.findByInt(recordingTask.getStage());
        if (!taskStageOptional.isPresent()) {
            throw new RuntimeException("录题任务的 stage 不合法");
        }

        boolean hasFeedback = (taskStageOptional.get() == RecordingTaskStage.TO_BE_REVISED);
        List<RecordingTaskVO> vos = wrapVOList(Arrays.asList(recordingTask), hasFeedback);
        return vos.get(0);
    }

    @Override
    public RecordingTaskExamPaperQuestionsVO getRecordingTaskExamPaperQuestions(long taskId) {
        RecordingTask recordingTask = recordingTaskService.getTaskById(taskId);
        if (recordingTask == null) {
            throw new BadRequestException("录题任务不存在， taskId: " + taskId);
        }
        RecordingTaskExamPaperQuestionsVO recordingTaskExamPaperQuestionsVO = new RecordingTaskExamPaperQuestionsVO();

        Map<Long, List<ExamPaperRecordingTask>> taskId2ExamPaperRecordingTasks = examPaperRecordingTaskService.getEffectiveRelationsByRecordingTaskIds(Collections.singletonList(taskId));
        List<ExamPaperRecordingTask> examPaperRecordingTasks = CollectionUtils.isEmpty(taskId2ExamPaperRecordingTasks.get(taskId)) ? Lists.newArrayList() : taskId2ExamPaperRecordingTasks.get(taskId);
        List<ExamPaperRecordingTaskVO> examPaperRecordingTaskVOS = examPaperRecordingTasks.stream().sorted(Comparator.comparingInt(ExamPaperRecordingTask::getOrdinal)).map(ExamPaperRecordingTaskWrapper::wrap).collect(Collectors.toList());

        Map<Long, List<QuestionRecordingTask>> taskId2QuestionRecordingTasks = questionRecordingTaskService.getEffectiveRelationsByTaskIds(Collections.singletonList(taskId));
        List<QuestionRecordingTask> questionRecordingTasks = CollectionUtils.isEmpty(taskId2QuestionRecordingTasks.get(taskId)) ? Lists.newArrayList() : taskId2QuestionRecordingTasks.get(taskId);
        List<QuestionRecordingTaskVO> questionRecordingTaskVOS = questionRecordingTasks.stream().sorted(Comparator.comparingInt(QuestionRecordingTask::getOrdinal)).map(QuestionRecordingTaskWrapper::wrap).collect(Collectors.toList());

        Map<Long, List<RecordingFeedback>> taskId2FeedBacks = recordingFeedbackService.getAllTypeUnprocessedFeedBacksByTaskIds(Collections.singletonList(taskId));
        List<RecordingFeedback> recordingFeedBacks = CollectionUtils.isEmpty(taskId2FeedBacks.get(taskId)) ? Lists.newArrayList() : taskId2FeedBacks.get(taskId);
        Set<Integer> creatorIds = recordingFeedBacks.stream().map(RecordingFeedback::getCreator).collect(Collectors.toSet());
        Map<Integer, String> userNames = userService.getUserNames(creatorIds);
        List<RecordingFeedBackVO> recordingFeedBackVOS = recordingFeedBacks.stream().map(feedBack -> RecordingFeedBackWrapper.wrap(feedBack, userNames)).collect(Collectors.toList());

        int auditNotPassedCount = RecordingMode.findByInt(recordingTask.getRecordingMode())
                .map(recordingMode -> RecordingTaskUtil.getNotAuditPassedCount(recordingMode, questionRecordingTasks, examPaperRecordingTasks))
                .orElse(0);

        recordingTaskExamPaperQuestionsVO.setExamPaperRecordingTaskVOS(examPaperRecordingTaskVOS);
        recordingTaskExamPaperQuestionsVO.setQuestionRecordingTaskVOS(questionRecordingTaskVOS);
        recordingTaskExamPaperQuestionsVO.setRecordingFeedBackVOs(recordingFeedBackVOS);
        recordingTaskExamPaperQuestionsVO.setTaskId(taskId);
        recordingTaskExamPaperQuestionsVO.setAuditNotPassedCount(auditNotPassedCount);

        return recordingTaskExamPaperQuestionsVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submit(long taskId) {
        RecordingTask recordingTask = recordingTaskService.getTaskById(taskId);
        if (recordingTask == null) {
            throw new NotFoundException();
        }

        authorizeFeature(recordingTask.getSubjectId(), recordingTask.getPhaseId(),
                recordingTask.getRecorder() == getUserId() ? QUESTION_RECORDING : QUESTION_RECORDING_OF_OTHERS);

        //校验
        RecordingMode recordingMode = RecordingMode.findByInt(recordingTask.getRecordingMode())
                .orElseThrow(() -> new RuntimeException("录题任务的 录入方式mode 不合法"));
        validateTaskForSubmit(taskId, recordingMode);

        if (recordingTask.getStage() != RecordingTaskStage.RECORDING.toInt() &&
                recordingTask.getStage() != RecordingTaskStage.TO_BE_REVISED.toInt()) {
            throw new TaskValidateException("任务状态不是待录题 或者 驳回待修改，不允许提交");
        }

        //任务的状态从 录题中-> 待审核 & submitTimes +1
        boolean result = recordingTaskService.submit(taskId);
        if (recordingMode.equals(RecordingMode.EXAM_PAPER)) {
            //套卷的状态从 已录入-> 待审核 可能一条都不更新
            examPaperRecordingTaskService.submit(taskId);
        }
        //题目的状态从 已录入-> 待审核 可能一条都不更新
        questionRecordingTaskService.submit(taskId);
        if (!result) {
            throw new RuntimeException("提交录题任务失败");
        }
        recordingFeedbackService.processAll(taskId);
        recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(recordingTask.getApplicationId(), taskId));
        cmsLogService.log(recordingTask.getApplicationId(), RECORDING_APPLICATION_ID, "提交录题任务");
        return result;
    }

    @Override
    public List<RecordingTaskVO> getRecordingTasksByApplicationId(long applicationId) {
        if (applicationId <= 0L) {
            return Lists.newArrayList();
        }

        RecordingApplication application = recordingApplicationService.getByIds(Lists.newArrayList(applicationId)).get(applicationId);
        if (Objects.isNull(application)) {
            throw new NotFoundException();
        }

        List<RecordingTask> recordingTasks = recordingTaskService.getByApplicationIds(Lists.newArrayList(applicationId))
                .getOrDefault(applicationId, Lists.newArrayList());
        return wrapVOList(recordingTasks, true);
    }

    private void validateTaskForSubmit(long taskId, RecordingMode recordingMode) {
        StringBuilder submitErrorTexts = new StringBuilder(REASON_HEAD).append(BR_HTML);
        List<String> errorReasons = new ArrayList<>();
        if (recordingMode.equals(RecordingMode.EXAM_PAPER)) {
            errorReasons = examPaperTaskProcessService.validateForSubmit(taskId);
        } else if (recordingMode.equals(RecordingMode.SINGLE_QUESTION)) {
            errorReasons = singleQuestionTaskProcessService.validateForSubmit(taskId);
        }
        if (CollectionUtils.isNotEmpty(errorReasons)) {
            submitErrorTexts.append(Joiner.on(BR_HTML).join(errorReasons));
            throw new TaskValidateException(submitErrorTexts.toString());
        }
    }

    private List<RecordingTaskVO> wrapVOList(List<RecordingTask> recordingTaskList, boolean needEnrichFeedback) {
        Map<Long, Long> taskId2ApplicationId = recordingTaskList.stream().collect(Collectors.toMap(
                RecordingTask::getId, RecordingTask::getApplicationId, Lambdas.pickFirst()));
        Map<Long, RecordingApplication> id2Application = recordingApplicationService
                .getByIds(taskId2ApplicationId.values());
        Map<Long, RecordingApplication> taskId2Application = MapUtil
                .convertValue(taskId2ApplicationId, id2Application::get);

        Map<Integer, UserInfo> userInfoMap = userService.getUserInfo(taskId2Application.values().stream().map(RecordingApplication::getCreator).collect(Collectors.toList()));

        List<Long> taskIds = new ArrayList<>(taskId2Application.keySet());
        //题目录题任务关联关系
        Map<Long, List<QuestionRecordingTask>> effectiveQuestionRelationsByTaskIds = questionRecordingTaskService.getEffectiveRelationsByTaskIds(taskIds);

        Map<Long, List<ExamPaperRecordingTask>> effectiveExamPaperRelationsByTaskIds = new HashMap<>();
        Map<Long, List<RecordingFeedback>> unprocessedRecordingTaskFeedback = new HashMap<>();
        if (needEnrichFeedback) {
            //试卷反馈
            effectiveExamPaperRelationsByTaskIds = examPaperRecordingTaskService.getEffectiveRelationsByRecordingTaskIds(taskIds);
            //其他反馈
            unprocessedRecordingTaskFeedback = recordingFeedbackService.getUnprocessedFeedBacksOfRecordingTask(taskIds);
        }

        return RecordingTaskVOWrapper.wrap(recordingTaskList, taskId2Application, userInfoMap,
                effectiveQuestionRelationsByTaskIds, effectiveExamPaperRelationsByTaskIds, unprocessedRecordingTaskFeedback);
    }
}
