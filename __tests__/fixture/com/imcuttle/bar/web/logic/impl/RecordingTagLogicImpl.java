/**
 * @(#)RecordingTagLogicImpl.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.common.lambda.Lambdas;
import com.fenbi.commons.paging.Page;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.fenbi.commons2.rest.exception.NotFoundException;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.RecordingMode;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.data.RecordingApplicationSearchCondition;
import com.imcuttle.bar.enums.TableEnum;
import com.imcuttle.bar.exceptions.CheckFailedException;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.CmsLogService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingApplicationSearchService;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.bar.service.TaskProcessService;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.util.MapUtil;
import com.imcuttle.bar.web.data.RecordingQuestionVO;
import com.imcuttle.bar.web.data.RecordingTagSearchParamVO;
import com.imcuttle.bar.web.data.RecordingTagVO;
import com.imcuttle.bar.web.data.TaggerUpdateRequestVO;
import com.imcuttle.bar.web.logic.RecordingTagLogic;
import com.imcuttle.bar.web.wrapper.RecordingQuestionWrapper;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingFeedback;
import com.imcuttle.thrift.RecordingTask;
import com.fenbi.tutor.common.constant.StudyPhase;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.enums.QuestionRecordingTaskStage.TAG_FAILED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.TAG_FINISHED;
import static com.imcuttle.enums.QuestionRecordingTaskStage.TO_BE_TAGGED;
import static com.imcuttle.enums.RecordingTaskStage.AUDITED;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_TAG;
import static com.imcuttle.bar.util.IdUtil.isDefaultId;
import static com.imcuttle.bar.web.wrapper.RecordingApplicationSearchConditionWrapper.wrap;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.RECORDING_APPLICATION_ID;

/**
 * @author chenkangbj
 */
@Service
@Slf4j
public class RecordingTagLogicImpl extends BaseLogic implements RecordingTagLogic {

    @Autowired
    private RecordingApplicationService recordingApplicationService;

    @Autowired
    private RecordingApplicationSearchService applicationSearchService;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    @Qualifier("examPaperTaskProcessService")
    private TaskProcessService examPaperTaskProcessService;

    @Autowired
    @Qualifier("singleQuestionTaskProcessService")
    private TaskProcessService singleQuestionTaskProcessService;

    @Autowired
    private CmsLogService cmsLogService;

    @Override
    public Page<RecordingTagVO> searchRecordingTagTask(RecordingTagSearchParamVO param) {
        // 搜索申请ids
        RecordingApplicationSearchCondition searchCondition = wrap(param);
        searchCondition.setTaskStage(RecordingTaskStage.AUDITED.toInt());
        searchCondition.setAuthorizedSubjectPhases(getAuthorizedSubjectPhases(TableEnum.RECORDING_TASK));
        Page<Long> matchedIdPage = applicationSearchService.searchIdList(searchCondition);
        List<RecordingTagVO> results = Lists.newLinkedList();

        // 查询申请
        List<Long> orderedApplicationIds = matchedIdPage.getList();
        Map<Long, RecordingApplication> id2Application = recordingApplicationService.getByIds(orderedApplicationIds);

        // 查询申请关联的任务
        Map<Long, List<RecordingTask>> applicationId2Task = recordingTaskService.getByApplicationIds(orderedApplicationIds);
        List<RecordingTask> matchedTasks = applicationId2Task.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 查询申请 关联的任务 的题目
        Map<Long, RecordingTask> id2Task = matchedTasks.stream().collect(Collectors.toMap(
                RecordingTask::getId, Function.identity(), Lambdas.pickFirst()));
        List<Long> taskIds = id2Task.values().stream()
                .map(RecordingTask::getId)
                .collect(Collectors.toList());
        Map<Long, List<QuestionRecordingTask>> taskId2QuestionRelations = questionRecordingTaskService
                .getEffectiveRelationsByTaskIds(taskIds);

        // 包装VO
        Map<Long, RecordingApplication> taskId2Application = MapUtil
                .convertValue(id2Task, task -> id2Application.get(task.getApplicationId()));
        for (Long applicationId : orderedApplicationIds) {
            applicationId2Task.getOrDefault(applicationId, Lists.newArrayList())
                    .forEach(task -> {
                        long taskId = task.getId();
                        List<QuestionRecordingTask> relations = taskId2QuestionRelations.getOrDefault(taskId, Lists.newArrayList());
                        RecordingApplication application = taskId2Application.get(taskId);
                        RecordingTagVO vo = wrapTagVo(task, application, relations);
                        results.add(vo);
                    });
        }

        Page<RecordingTagVO> page = new Page<>(
                matchedIdPage.getPageInfo().getTotalItem(),
                matchedIdPage.getPageInfo().getCurrentPage(),
                matchedIdPage.getPageInfo().getPageSize()
        );
        page.setList(results);
        return page;
    }

    @Override
    public List<RecordingQuestionVO> getQuestionTagInfos(long applicationId) {
        List<RecordingTask> recordingTasks = recordingTaskService.getByApplicationIds(Collections.singletonList(applicationId))
                .getOrDefault(applicationId, Lists.newArrayList());
        if (CollectionUtils.isEmpty(recordingTasks)) {
            throw new NotFoundException();
        }
        List<RecordingQuestionVO> result = new ArrayList<>();
        recordingTasks.forEach(recordingTask -> {
            long taskId = recordingTask.getId();
            RecordingMode recordingMode = RecordingMode.findByInt(recordingTask.getRecordingMode())
                    .orElseThrow(() -> new RuntimeException("录题任务的 录入方式mode 不合法"));
            List<QuestionRecordingTask> questionRecordingTasks = new ArrayList<>();
            if (recordingMode.equals(RecordingMode.EXAM_PAPER)) {
                questionRecordingTasks = examPaperTaskProcessService.getOrderedQuestionRecordingTask(taskId);
            } else if (recordingMode.equals(RecordingMode.SINGLE_QUESTION)) {
                questionRecordingTasks = singleQuestionTaskProcessService.getOrderedQuestionRecordingTask(taskId);
            }
            List<Long> questionIds = questionRecordingTasks.stream().map(QuestionRecordingTask::getQuestionId).map(Long::valueOf).collect(Collectors.toList());
            Map<Long, List<RecordingFeedback>> questionId2Feedbacks = recordingFeedbackService.batchGetFeedBacksByTargetId(questionIds, FeedbackTargetType.QUESTION, taskId, true);
            List<RecordingQuestionVO> recordingQuestionVOS = RecordingQuestionWrapper.wrap(questionRecordingTasks, questionId2Feedbacks);
            result.addAll(recordingQuestionVOS);
        });

        return result;
    }

    @Override
    public void rejectTagTask(long taskId) {
        RecordingTask task = checkTaskAuditedAndGet(taskId);
        authorizeFeature(task.getSubjectId(), task.getPhaseId(), QUESTION_TAG);

        List<QuestionRecordingTask> questionRelations = questionRecordingTaskService.getQuestionRecordingTasksByTaskId(taskId);
        boolean noQuestionToBeTagged = questionRelations.stream()
                .noneMatch(relation -> TO_BE_TAGGED.toInt() == relation.getStage());
        if (!noQuestionToBeTagged) {
            throw new BadRequestException("标注任务下仍存在未标注题目，不能打回录题员，请检查！");
        }

        // 不去重
        long tagFailedCount = questionRelations.stream()
                .filter(relation -> TAG_FAILED.toInt() == relation.getStage()).count();
        if (tagFailedCount <= 0L) {
            throw new BadRequestException("标注任务下没有要纠错的题目，不能打回录题员，请检查！");
        }

        recordingTaskService.updateTaskStage(taskId, RecordingTaskStage.TO_BE_REVISED.toInt());
        recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(task.getApplicationId(), task.getId()));
        cmsLogService.log(task.getApplicationId(), RECORDING_APPLICATION_ID, "打回给录题人修改，" + tagFailedCount + " 个题目被纠错");
    }

    @Override
    public RecordingTagVO updateTaggerAndGet(TaggerUpdateRequestVO requestVO) {
        RecordingTask task = checkTaskAuditedAndGet(requestVO.getTaskId());
        authorizeFeature(task.getSubjectId(), task.getPhaseId(), QUESTION_TAG);
        checkTaggerUpdateRequest(requestVO, task);

        int userId = getUserId();
        int newTaggerId = requestVO.isCancelTagger() ? 0 : userId;
        log.info("Update tagger, taskId = {}, isCancelTagger = {}, oldTaggerId = {}, newTaggerId = {}",
                requestVO.getTaskId(), requestVO.isCancelTagger(), task.getTagger(), newTaggerId);
        boolean updated = recordingTaskService.updateTagger(task.getId(), newTaggerId);
        if (updated) {
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(task.getApplicationId(), task.getId()));
            if (requestVO.isCancelTagger()) {
                cmsLogService.log(task.getApplicationId(), RECORDING_APPLICATION_ID, "退领标注任务");
            } else {
                cmsLogService.log(task.getApplicationId(), RECORDING_APPLICATION_ID, "领取标注任务");
            }
        }

        // 现查最新的标注任务返回前端 局部刷新
        task = recordingTaskService.getTaskById(requestVO.getTaskId());
        List<QuestionRecordingTask> questionRelations = questionRecordingTaskService
                .getEffectiveRelationsByTaskIds(Collections.singletonList(task.getId()))
                .getOrDefault(task.getId(), Lists.newArrayList());
        RecordingApplication application = recordingApplicationService
                .getByIds(Collections.singletonList(task.getApplicationId()))
                .get(task.getApplicationId());
        return wrapTagVo(task, application, questionRelations);
    }

    private void checkTaggerUpdateRequest(TaggerUpdateRequestVO requestVO, RecordingTask task) {
        if (requestVO.isCancelTagger()) {
            if (isDefaultId(task.getTagger())) {
                throw new BadRequestException(String.format("当前标注任务 id = %d 没有标注人，不能退领！", task.getId()));
            }
        } else {
            if (!isDefaultId(task.getTagger())) {
                throw new BadRequestException(String.format("当前标注任务 id = %d 已有标注人 id = %d，不能再次领取！", task.getId(), task.getTagger()));
            }
        }
    }

    @Override
    public RecordingQuestionVO getQuestionTagInfo(long taskId, int questionId, long examPaperId) {
        Optional<QuestionRecordingTask> taskOptional = questionRecordingTaskService.get(taskId, questionId, examPaperId);
        if (!taskOptional.isPresent()) {
            throw new NotFoundException();
        }
        List<RecordingFeedback> recordingFeedbacks = recordingFeedbackService.getFeedBacksOfSpecificTargetOfTask(questionId, FeedbackTargetType.QUESTION, examPaperId, taskId, true);
        return RecordingQuestionWrapper.wrap(taskOptional.get(), recordingFeedbacks);
    }

    private RecordingTagVO wrapTagVo(RecordingTask recordingTask,
                                     RecordingApplication application,
                                     List<QuestionRecordingTask> relations) {
        // 计算 已标注数 = 标注完成数 + 放弃标注数（需纠错数）
        int finishedCount = (int) relations.stream().filter(task -> TAG_FINISHED.toInt() == task.getStage()).count();
        int failedCount = (int) relations.stream().filter(task -> TAG_FAILED.toInt() == task.getStage()).count();
        int taggedCount = finishedCount + failedCount;

        RecordingTagVO vo = new RecordingTagVO();
        vo.setTaggedCount(taggedCount);
        vo.setFeedbackCount(failedCount);
        vo.setShouldTagCount(relations.size());
        wrapBasicInfo(vo, application, recordingTask);
        return vo;
    }

    private void wrapBasicInfo(RecordingTagVO vo, RecordingApplication application, RecordingTask task) {
        if (task != null) {
            vo.setId(task.getId());
            vo.setPhase(StudyPhase.findByInt(task.getPhaseId()).orElse(StudyPhase.UNKNOWN).toString());
            vo.setSubject(task.getSubjectId());
            if (!isDefaultId(task.getTagger())) {
                vo.setTagger(toUserName(task.getTagger()));
            }
            vo.setApplicationId(task.getApplicationId());
        }
        if (application != null) {
            vo.setName(application.getName());
            vo.setQuestionSource(application.getQuestionSource());
            vo.setCreator(toUserName(application.getCreator()));
            vo.setSubmitTime(application.getSubmitTime());
        }
    }

    private String toUserName(int userId) {
        return userService.getUserNames(Collections.singletonList(userId)).getOrDefault(userId, "" + userId);
    }

    /**
     * 检查录题任务是否处于 已审核待标注状态
     *
     * @param taskId
     * @return
     */
    private RecordingTask checkTaskAuditedAndGet(long taskId) {
        try {
            return recordingTaskService.checkTaskStageAndGet(taskId, AUDITED);
        } catch (CheckFailedException e) {
            throw new BadRequestException(String.format("检查录题任务状态失败！原因：%s", e.getMessage()), e);
        }
    }
}
