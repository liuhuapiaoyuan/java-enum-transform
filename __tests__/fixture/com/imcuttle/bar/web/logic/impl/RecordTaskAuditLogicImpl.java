/**
 * @(#)RecordTaskAuditLogicImpl.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.common.lambda.Lambdas;
import com.fenbi.commons.paging.Page;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.enums.ExamPaperRecordingTaskStage;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.FeedbackType;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.enums.RecordingMode;
import com.imcuttle.bar.data.RecordingAuditResource;
import com.imcuttle.bar.data.SaveAuditTaskRequest;
import com.imcuttle.bar.exceptions.CheckFailedException;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.CmsLogService;
import com.imcuttle.bar.service.ExamPaperRecordingTaskService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.bar.service.RecordingTaskTxService;
import com.imcuttle.bar.service.TaskProcessService;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.util.MapUtil;
import com.imcuttle.bar.util.RecordingTaskUtil;
import com.imcuttle.bar.util.UserUtil;
import com.imcuttle.bar.web.data.RecordingAuditResourceDetailVO;
import com.imcuttle.bar.web.data.RecordingAuditResourceListVO;
import com.imcuttle.bar.web.data.RecordingAuditResourceVO;
import com.imcuttle.bar.web.data.RecordingFeedBackVO;
import com.imcuttle.bar.web.data.RecordingTaskAuditVO;
import com.imcuttle.bar.web.data.SaveAuditTaskRequestVO;
import com.imcuttle.bar.web.data.SubmitAuditTaskRequestVO;
import com.imcuttle.bar.web.logic.RecordTaskAuditLogic;
import com.imcuttle.bar.web.wrapper.ExamPaperRecordingTaskWrapper;
import com.imcuttle.bar.web.wrapper.QuestionRecordingTaskWrapper;
import com.imcuttle.bar.web.wrapper.RecordingAuditResourceWrapper;
import com.imcuttle.bar.web.wrapper.RecordingFeedBackWrapper;
import com.imcuttle.bar.web.wrapper.SaveAuditTaskRequestWrapper;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingFeedback;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.utils.RecordingStageUtil;
import com.fenbi.tutor.cmslog.thrift.CmsLogEx;
import com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants;
import com.fenbi.tutor.common.constant.StudyPhase;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getLdapId;
import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.enums.RecordingMode.SINGLE_QUESTION;
import static com.imcuttle.enums.RecordingTaskStage.SUBMITTED;
import static com.imcuttle.enums.RecordingTaskStage.TO_BE_REVISED;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_AUDIT;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_AUDIT_OF_OTHERS;
import static com.imcuttle.bar.util.CmsLogUtil.getCmsLogIdTypeByQuestionId;
import static com.imcuttle.bar.util.CollectionUtil.getOrEmpty;
import static com.imcuttle.bar.util.IdUtil.isDefaultId;
import static com.imcuttle.thrift.TutorArmoryConstants.LONG_DEFAULT_ID;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.RECORDING_APPLICATION_ID;

/**
 * @author chenkangbj
 */
@Service
@Slf4j
public class RecordTaskAuditLogicImpl extends BaseLogic implements RecordTaskAuditLogic {

    private static final String AUDIT_STAGE_FEEDBACK = "审核阶段纠错";

    private static final String TAG_STAGE_FEEDBACK = "标注阶段纠错";

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Autowired
    private RecordingApplicationService recordingApplicationService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private ExamPaperRecordingTaskService examPaperRecordingTaskService;

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private CmsLogService cmsLogService;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Autowired
    private RecordingTaskTxService recordingTaskTxService;

    @Autowired
    @Qualifier("examPaperTaskProcessService")
    private TaskProcessService examPaperTaskProcessService;

    @Autowired
    @Qualifier("singleQuestionTaskProcessService")
    private TaskProcessService singleQuestionTaskProcessService;

    @Override
    public Page<RecordingTaskAuditVO> getMyAuditTask(long taskId, int page, int pageSize) {
        int userId = getUserId();
        // 查询审核员为当前用户的 待审核录题任务有序列表
        int totalItem = recordingTaskService.countSubmittedTasksByAuditor(userId);
        List<RecordingTask> toBeAuditTasks = Lists.newArrayList();
        if (isDefaultId(taskId)) {
            toBeAuditTasks.addAll(recordingTaskService.getSubmittedTasksByAuditor(userId, page, pageSize));
        } else {
            List<RecordingTask> task = recordingTaskService.getTaskByIdAndAuditor(taskId, userId)
                    .map(Collections::singletonList)
                    .orElse(Lists.newArrayList());
            toBeAuditTasks.addAll(task);
        }

        // 查询录题任务 关联的录题申请
        Map<Long, Long> taskId2ApplicationId = toBeAuditTasks.stream().collect(Collectors.toMap(
                RecordingTask::getId, RecordingTask::getApplicationId, Lambdas.pickFirst()));
        Map<Long, RecordingApplication> id2Application = recordingApplicationService
                .getByIds(taskId2ApplicationId.values());
        Map<Long, RecordingApplication> taskId2Application = MapUtil
                .convertValue(taskId2ApplicationId, id2Application::get);

        // 查询录题任务 关联的题目 & 试卷
        Map<Long, List<QuestionRecordingTask>> taskId2QuestionRelation = questionRecordingTaskService
                .getEffectiveRelationsByTaskIds(Lists.newArrayList(taskId2ApplicationId.keySet()));
        Map<Long, List<ExamPaperRecordingTask>> taskId2PaperRelation = examPaperRecordingTaskService
                .getEffectiveRelationsByTaskIds(Lists.newArrayList(taskId2ApplicationId.keySet()));

        // 包装VO
        return wrapVoList(
                toBeAuditTasks,
                taskId2Application,
                taskId2QuestionRelation, taskId2PaperRelation,
                page, pageSize, totalItem);
    }

    @Override
    public Page<RecordingTaskAuditVO> getMyRejectedTask(int page, int pageSize) {
        int userId = getUserId();
        // 查询审核员为当前用户的 驳回待修改（TO_BE_REVISED）的录题任务有序列表
        int totalItem = recordingTaskService.countRejectedTasksByAuditor(userId);
        List<RecordingTask> rejectedTasks = recordingTaskService.getRejectedTasksByAuditor(userId, page, pageSize);

        // 查询录题任务 关联的录题申请
        Map<Long, Long> taskId2ApplicationId = rejectedTasks.stream().collect(Collectors.toMap(
                RecordingTask::getId, RecordingTask::getApplicationId, Lambdas.pickFirst()));
        Map<Long, RecordingApplication> id2Application = recordingApplicationService
                .getByIds(taskId2ApplicationId.values());
        Map<Long, RecordingApplication> taskId2Application = MapUtil
                .convertValue(taskId2ApplicationId, id2Application::get);

        // 查询录题任务 关联的题目 & 试卷
        Map<Long, List<QuestionRecordingTask>> taskId2QuestionRelation = questionRecordingTaskService
                .getEffectiveRelationsByTaskIds(Lists.newArrayList(taskId2ApplicationId.keySet()));
        Map<Long, List<ExamPaperRecordingTask>> taskId2PaperRelation = examPaperRecordingTaskService
                .getEffectiveRelationsByTaskIds(Lists.newArrayList(taskId2ApplicationId.keySet()));

        // 包装VO
        return wrapVoList(rejectedTasks,
                taskId2Application,
                taskId2QuestionRelation, taskId2PaperRelation,
                page, pageSize, totalItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAuditTask(SaveAuditTaskRequestVO requestVO) {
        authorizeForAudit(requestVO.getTaskId());
        SaveAuditTaskRequest request = SaveAuditTaskRequestWrapper.wrap(requestVO);
        if (requestVO.getTargetType() == FeedbackTargetType.QUESTION.toInt()) {
            questionRecordingTaskService.saveAuditTask(request);
        }
        // 试卷
        else if (requestVO.getTargetType() == FeedbackTargetType.EXAM_PAPER.toInt()) {
            examPaperRecordingTaskService.saveAuditTask(request);
        }
        // 其他
        else {
            throw new BadRequestException("暂只支持题目和试卷审核!");
        }
    }

    @Override
    public void submitAuditTask(long taskId, SubmitAuditTaskRequestVO requestVO) {
        authorizeForAudit(taskId);
        RecordingTask task = checkTaskSubmittedAndGet(taskId);

        // 查询试卷 & 题目关联
        List<QuestionRecordingTask> questionRelations = questionRecordingTaskService
                .getEffectiveRelationsByTaskIds(Collections.singletonList(taskId))
                .getOrDefault(taskId, Lists.newArrayList());
        List<ExamPaperRecordingTask> paperRelations = examPaperRecordingTaskService
                .getEffectiveRelationsByTaskIds(Collections.singletonList(taskId))
                .getOrDefault(taskId, Lists.newArrayList());

        // 检查是否已经全部审核
        boolean allQuestionAudited = questionRelations.stream()
                .noneMatch(relation -> QuestionRecordingTaskStage.TO_BE_AUDITED.toInt() == relation.getStage());
        boolean allPaperAudited = paperRelations.stream()
                .noneMatch(relation -> ExamPaperRecordingTaskStage.TO_BE_AUDITED.toInt() == relation.getStage());
        if (!allQuestionAudited || !allPaperAudited) {
            throw new BadRequestException("当前审核任务下存在未完成审核的题目 / 试卷，请检查！");
        }

        // 计算是否需要前进任务状态
        boolean allQuestionPassed = questionRelations.stream().allMatch(RecordingStageUtil::auditApproved);
        boolean allPaperPassed = paperRelations.stream().allMatch(RecordingStageUtil::auditApproved);
        boolean taskApproved = allQuestionPassed && allPaperPassed && requestVO.isPassed();

        // 提交审核任务
        recordingTaskTxService.submitAuditTask(task, questionRelations,
                getUserId(), taskApproved, requestVO.getReason());

        // 更新索引
        recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(task.getApplicationId(), taskId));

        printAuditTaskSubmitCmsLog(requestVO.isPassed(), requestVO.getReason(), taskId, task.getApplicationId(), questionRelations, paperRelations);
    }

    private void printAuditTaskSubmitCmsLog(boolean passed, String reason, long taskId, long applicationId, List<QuestionRecordingTask> questionRelations, List<ExamPaperRecordingTask> paperRelations) {
        List<CmsLogEx> cmsLogExes = Lists.newArrayList();
        int userId = getUserId();
        String ldap = getLdapId();
        int userType = UserUtil.getUserType();
        long createTime = System.currentTimeMillis();

        // 录题任务
        String cmsLogContentForTask = getCmsLogContentForTask(passed, reason, questionRelations, paperRelations);
        CmsLogEx cmsLogForTask = new CmsLogEx(applicationId, RECORDING_APPLICATION_ID, userType, ldap, userId, cmsLogContentForTask, createTime);
        cmsLogExes.add(cmsLogForTask);

        Map<ImmutablePair<Long, Integer>, RecordingFeedback> feedbackMap = recordingFeedbackService.getAllTypeUnprocessedFeedBacksByTaskIds(Collections.singletonList(taskId))
                .getOrDefault(taskId, Lists.newArrayList())
                .stream().collect(Collectors.toMap(feedback -> ImmutablePair.of(feedback.getTargetId(), feedback.getTargetType()), feedback -> feedback, (feedback1, feedback2) -> feedback1));

        // 题目
        questionRelations.forEach(questionRelation -> {
            int questionId = questionRelation.getQuestionId();
            // 通过
            if (RecordingStageUtil.auditApproved(questionRelation)) {
                cmsLogExes.add(new CmsLogEx(questionId, getCmsLogIdTypeByQuestionId(questionId), userType, ldap, userId, "题目审核结果为:通过", createTime));
            }
            // 未通过
            else {
                RecordingFeedback feedback = feedbackMap.get(ImmutablePair.of((long) questionRelation.getQuestionId(), FeedbackTargetType.QUESTION.toInt()));
                List<String> feedbackReason = Optional.ofNullable(feedback).map(RecordingFeedback::getReason).orElse(Lists.newArrayList());
                String content = "题目审核结果为:不通过，原因:" + Joiner.on("、").join(feedbackReason)
                        + "，详细描述:" + Optional.ofNullable(feedback).map(RecordingFeedback::getDescription).orElse("");
                cmsLogExes.add(new CmsLogEx(questionId, getCmsLogIdTypeByQuestionId(questionId), userType, ldap, userId, content, createTime));
            }
        });

        // 试卷
        Map<Long, Long> paperIdToUnApprovedQuestionCountMap = questionRelations.stream().filter(questionRelation -> !RecordingStageUtil.auditApproved(questionRelation))
                .collect(Collectors.groupingBy(QuestionRecordingTask::getExamPaperId, Collectors.counting()));
        paperRelations.forEach(paperRelation -> {
            // 试卷通过 & 题目通过
            if (RecordingStageUtil.auditApproved(paperRelation) && !paperIdToUnApprovedQuestionCountMap.containsKey(paperRelation.getExamPaperId())) {
                cmsLogExes.add(new CmsLogEx(paperRelation.getExamPaperId(), TutorCmsLogConstants.EXAM_PAPER, userType, ldap, userId, "试卷审核结果为:通过", createTime));
            }
            // 未通过
            else {
                long count = paperIdToUnApprovedQuestionCountMap.getOrDefault(paperRelation.getExamPaperId(), 0L);
                RecordingFeedback feedback = feedbackMap.get(ImmutablePair.of(paperRelation.getExamPaperId(), FeedbackTargetType.EXAM_PAPER.toInt()));
                String description = Optional.ofNullable(feedback).map(RecordingFeedback::getDescription).orElse("");
                boolean failed = Optional.ofNullable(feedback).map(record -> FeedbackType.PAPER_AUDIT_FAILED.toInt() == record.getType()).orElse(false);;
                String content = "试卷审核结果为:不通过，原因:" + (count == 0L ? "" : count + "道题目内容错误") + (count != 0L && failed ? "、" : "") +
                        (failed ? "试卷信息错误:" + description : "");
                cmsLogExes.add(new CmsLogEx(paperRelation.getExamPaperId(), TutorCmsLogConstants.EXAM_PAPER, userType, ldap, userId, content, createTime));
            }
        });

        cmsLogService.batchCreateCmsLog(cmsLogExes);
    }

    private String getCmsLogContentForTask(boolean passed, String reason, List<QuestionRecordingTask> questionRelations, List<ExamPaperRecordingTask> paperRelations) {
        // 通过
        if (passed) {
            return "提交审核任务，审核结果为:通过";
        }

        // 题目反馈数
        long questionFeedbackCount = questionRelations.stream()
                .filter(relation -> relation.getStage() == QuestionRecordingTaskStage.TAG_FAILED.toInt() ||
                        relation.getStage() == QuestionRecordingTaskStage.AUDIT_FAILED.toInt())
                .count();

        // 试卷反馈数
        long paperFeedbackCount = paperRelations.stream()
                .filter(relation -> ExamPaperRecordingTaskStage.AUDIT_FAILED.toInt() == relation.getStage())
                .count();

        return "提交审核任务，审核结果为:不通过，原因: " +
                (paperFeedbackCount == 0L ? "" : paperFeedbackCount + "个试卷信息错误、") +
                (questionFeedbackCount == 0L ? "" : questionFeedbackCount + "道题目内容错误、") +
                "其他错误" + (StringUtils.isBlank(reason) ? "" : ":" + reason);
    }

    @Override
    public RecordingAuditResourceListVO getResourceList(long taskId) {
        RecordingTask recordingTask = recordingTaskService.getTaskById(taskId);
        if (recordingTask == null) {
            throw new BadRequestException(String.format("录题任务(%s)不存在,请确认!", taskId));
        }

        Optional<RecordingMode> recordingModeOptional = RecordingMode.findByInt(recordingTask.getRecordingMode());
        if (!recordingModeOptional.isPresent()) {
            throw new BadRequestException(String.format("录题模式(%s)不存在,请确认!", recordingTask.getRecordingMode()));
        }

        // 题目
        List<RecordingAuditResourceVO> resources;
        RecordingMode recordingMode = recordingModeOptional.get();
        if (recordingMode == SINGLE_QUESTION) {
            resources = singleQuestionTaskProcessService.getOrderedRecordingAuditResource(taskId)
                    .stream().map(RecordingAuditResourceWrapper::wrap).collect(Collectors.toList());
        }
        // 套卷
        else {
            List<RecordingAuditResource> examPaperResources = examPaperTaskProcessService.getOrderedRecordingAuditResource(taskId);
            resources = examPaperResources.stream().map(RecordingAuditResourceWrapper::wrap).collect(Collectors.toList());
        }
        return new RecordingAuditResourceListVO(resources);
    }

    @Override
    public RecordingAuditResourceDetailVO getResource(long taskId, long examPaperId, long targetId, int targetType) {
        // 题目
        if (targetType == FeedbackTargetType.QUESTION.toInt()) {
            Optional<QuestionRecordingTask> relationOp = questionRecordingTaskService
                    .getEffectiveRelationByTaskIdAndExamPaperId((int) targetId, taskId, examPaperId);
            if (!relationOp.isPresent()) {
                throw new BadRequestException(String.format(
                        "任务 id = %d 下不存在试卷 id = %d 的题目 id = %d ！",
                        taskId, examPaperId, targetId));
            }
            QuestionRecordingTask questionRecordingTask = relationOp.get();
            RecordingFeedBackVO recordingFeedBackVO = getRecordingFeedBackVO(questionRecordingTask.getQuestionId(),
                    FeedbackTargetType.QUESTION, questionRecordingTask.getExamPaperId(), questionRecordingTask.getTaskId());
            return QuestionRecordingTaskWrapper.wrapToRecordingAuditResourceDetailVO(questionRecordingTask, recordingFeedBackVO);
        }
        // 试卷
        else if (targetType == FeedbackTargetType.EXAM_PAPER.toInt()) {
            Optional<ExamPaperRecordingTask> relationOp = examPaperRecordingTaskService
                    .getEffectiveRelationByTaskIdAndExamPaperId(taskId, targetId);
            if (!relationOp.isPresent()) {
                throw new BadRequestException(String.format("任务 id = %d 下不存在试卷 id = %d！", taskId, targetId));
            }
            ExamPaperRecordingTask examPaperRecordingTask = relationOp.get();
            RecordingFeedBackVO recordingFeedBackVO = getRecordingFeedBackVO(examPaperRecordingTask.getExamPaperId(),
                    FeedbackTargetType.EXAM_PAPER, LONG_DEFAULT_ID, examPaperRecordingTask.getTaskId());
            return ExamPaperRecordingTaskWrapper.wrapToRecordingAuditResourceDetailVO(examPaperRecordingTask, recordingFeedBackVO);
        }
        // 其他
        else {
            throw new BadRequestException("暂只支持查看题目和试卷!");
        }
    }

    private RecordingFeedBackVO getRecordingFeedBackVO(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId) {
        List<RecordingFeedback> feedbacks = recordingFeedbackService.getFeedBacksOfSpecificTargetOfTask(targetId, targetType, examPaperId, taskId, true);
        if (CollectionUtils.isNotEmpty(feedbacks)) {
            if (feedbacks.size() > 1) {
                log.warn("未处理的feedback不唯一. targetId:{}, targetType:{}, taskId:{}", targetId, targetType, taskId);
            }
            RecordingFeedback feedback = feedbacks.get(0);
            Map<Integer, String> userNames = userService.getUserNames(Collections.singletonList(feedback.getCreator()));
            return RecordingFeedBackWrapper.wrap(feedback, userNames);
        }
        return null;
    }

    /**
     * 对一批审核任务 包装VO分页
     *
     * @param toBeAuditTasks           审核任务
     * @param taskId2Application       审核任务 - 录题申请 的映射
     * @param taskId2QuestionRelations 审核任务 - 题目列表 的映射
     * @param taskId2PaperRelations    审核任务 - 试卷列表 的映射
     * @param pageIndex
     * @param pageSize
     * @param totalItem
     * @return
     */
    private Page<RecordingTaskAuditVO> wrapVoList(
            List<RecordingTask> toBeAuditTasks,
            Map<Long, RecordingApplication> taskId2Application,
            Map<Long, List<QuestionRecordingTask>> taskId2QuestionRelations,
            Map<Long, List<ExamPaperRecordingTask>> taskId2PaperRelations,
            int pageIndex, int pageSize, int totalItem
    ) {
        List<RecordingTaskAuditVO> result = toBeAuditTasks.stream()
                .map(task -> wrap(
                        task,
                        taskId2Application.getOrDefault(task.getId(), new RecordingApplication()),
                        taskId2QuestionRelations.getOrDefault(task.getId(), Lists.newArrayList()),
                        taskId2PaperRelations.getOrDefault(task.getId(), Lists.newArrayList())
                ))
                .collect(Collectors.toList());

        Page<RecordingTaskAuditVO> page = new Page<>(totalItem, pageIndex, pageSize);
        page.setList(result);
        return page;
    }

    /**
     * 对单个审核任务 包装VO
     *
     * @param task              审核任务
     * @param application       审核任务 关联的录题申请
     * @param questionRelations 审核任务 关联的题目列表
     * @param paperRelations    审核任务 关联的试卷列表
     * @return VO
     */
    private RecordingTaskAuditVO wrap(RecordingTask task,
                                      RecordingApplication application,
                                      List<QuestionRecordingTask> questionRelations,
                                      List<ExamPaperRecordingTask> paperRelations) {
        RecordingTaskAuditVO vo = new RecordingTaskAuditVO();
        if (task != null) {
            vo.setId(task.getId());
            vo.setPhase(StudyPhase.findByInt(task.getPhaseId()).orElse(StudyPhase.UNKNOWN).toString());
            vo.setSubject(task.getSubjectId());
            vo.setQuestionNum(getOrEmpty(questionRelations).size());
            vo.setStage(task.getStage());
            vo.setApplicationId(task.getApplicationId());
            vo.setSubmitTimes(task.getSubmitTimes());

            // 计数：应审核数、待审核数、审核通过数
            Optional<RecordingMode> modeOp = RecordingMode.findByInt(task.getRecordingMode());
            if (modeOp.isPresent()) {
                RecordingMode mode = modeOp.get();
                int passedCount = RecordingTaskUtil.getAuditPassedCount(mode, questionRelations, paperRelations);
                int notPassedCount = RecordingTaskUtil.getNotAuditPassedCount(mode, questionRelations, paperRelations);
                int needAuditCount = RecordingTaskUtil.getNeedAuditCount(mode, questionRelations, paperRelations);
                int toBeAuditCount = RecordingTaskUtil.getToBeAuditCount(mode, questionRelations, paperRelations);
                int auditedCount = needAuditCount - toBeAuditCount;
                vo.setPassedCount(passedCount);
                vo.setNotPassedCount(notPassedCount);
                vo.setNeedAuditCount(needAuditCount);
                vo.setAuditedCount(auditedCount);
                wrapFeedbackInfo(vo, task, questionRelations, paperRelations);
            } else {
                log.error("Cannot find mode = {}, skip calculating counts!", task.getRecordingMode());
            }
        }
        if (application != null) {
            vo.setName(application.getName());
            vo.setQuestionSource(application.getQuestionSource());
            vo.setCreator(toUserName(application.getCreator()));
            vo.setSubmitTime(application.getSubmitTime());
        }

        return vo;
    }

    private void wrapFeedbackInfo(RecordingTaskAuditVO vo,
                                  RecordingTask task,
                                  List<QuestionRecordingTask> questionRelations,
                                  List<ExamPaperRecordingTask> paperRelations) {
        if (TO_BE_REVISED.toInt() == task.getStage()) {
            // 题目反馈数
            List<QuestionRecordingTask> feedBackedQuestions = getOrEmpty(questionRelations).stream()
                    .filter(relation -> relation.getStage() == QuestionRecordingTaskStage.TAG_FAILED.toInt() ||
                            relation.getStage() == QuestionRecordingTaskStage.AUDIT_FAILED.toInt())
                    .collect(Collectors.toList());
            int questionFeedbackCount = feedBackedQuestions.size();
            // 任务反馈数
            int unprocessedTaskFeedbackCount = recordingFeedbackService
                    .getUnprocessedFeedBacksOfRecordingTask(Collections.singletonList(task.getId()))
                    .getOrDefault(task.getId(), Lists.newArrayList())
                    .size();
            // 试卷反馈数
            int paperFeedbackCount = (int) getOrEmpty(paperRelations).stream()
                    .filter(relation -> ExamPaperRecordingTaskStage.AUDIT_FAILED.toInt() == relation.getStage())
                    .count();

            boolean anyFromTagStage = feedBackedQuestions.stream()
                    .anyMatch(relation -> relation.getStage() == QuestionRecordingTaskStage.TAG_FAILED.toInt());
            if (anyFromTagStage) {
                vo.setFeedbackStageName(TAG_STAGE_FEEDBACK);
            } else {
                vo.setFeedbackStageName(AUDIT_STAGE_FEEDBACK);
            }
            vo.setFeedbackType2Count(ImmutableMap.of(
                    FeedbackTargetType.QUESTION.toInt(), questionFeedbackCount,
                    FeedbackTargetType.EXAM_PAPER.toInt(), paperFeedbackCount,
                    FeedbackTargetType.RECORDING_TASK.toInt(), unprocessedTaskFeedbackCount));
        }
    }

    /**
     * 检查录题任务是否处于待审核状态
     *
     * @param taskId
     * @return
     */
    private RecordingTask checkTaskSubmittedAndGet(long taskId) {
        try {
            return recordingTaskService.checkTaskStageAndGet(taskId, SUBMITTED);
        } catch (CheckFailedException e) {
            throw new BadRequestException(String.format("检查录题任务状态失败！原因：%s", e.getMessage()), e);
        }
    }

    private String toUserName(int userId) {
        return userService.getUserNames(Lists.newArrayList(userId))
                .getOrDefault(userId, String.valueOf(userId));
    }

    private void authorizeForAudit(long taskId) {
        RecordingTask task = recordingTaskService.getTaskById(taskId);
        if (Objects.isNull(task)) {
            throw new BadRequestException("要审核的任务不存在");
        }
        authorizeFeature(task.getSubjectId(), task.getPhaseId(), task.getAuditor() == getUserId() ? QUESTION_AUDIT : QUESTION_AUDIT_OF_OTHERS);
    }
}
