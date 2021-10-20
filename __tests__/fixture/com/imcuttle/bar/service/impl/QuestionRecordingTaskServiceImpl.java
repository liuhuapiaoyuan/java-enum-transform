/**
 * @(#)QuestionRecordingTaskServiceImpl.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.common.lambda.Lambdas;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.FeedbackType;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.enums.RecordingMode;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.constant.LockKeyFieldConstant;
import com.imcuttle.bar.data.QuestionRecordingTaskPair;
import com.imcuttle.bar.data.SaveAuditTaskRequest;
import com.imcuttle.bar.enums.LockPrefixEnum;
import com.imcuttle.bar.message.event.RecordingTaskUpdatedEvent;
import com.imcuttle.bar.message.producer.RecordingTaskUpdatedEventProducer;
import com.imcuttle.bar.service.AuthorizeService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.storage.db.QuestionRecordingTaskStorage;
import com.imcuttle.bar.storage.db.RecordingTaskStorage;
import com.imcuttle.bar.util.lock.DistributedLock;
import com.imcuttle.bar.util.lock.DistributedLockKey;
import com.imcuttle.thrift.ArmoryBadRequestException;
import com.imcuttle.thrift.ArmoryUnAuthorizedException;
import com.imcuttle.thrift.AuditDetail;
import com.imcuttle.thrift.BindBatchQuestionReq;
import com.imcuttle.thrift.BindQuestionReq;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingFeedback;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.utils.RecordingStageUtil;
import com.fenbi.tutor.neoquestion.client.proxy.spi.TutorNeoQuestionProxy;
import com.fenbi.tutor.neoquestion.thrift.Question;
import com.fenbi.tutor.neoquestion.thrift.Vignette;
import com.fenbi.tutor.neoquestion.util.QuestionImageUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_RECORDING;
import static com.imcuttle.bar.enums.FeatureEnum.QUESTION_RECORDING_OF_OTHERS;
import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.CONTENT_TYPE_HTML;
import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.CONTENT_TYPE_UBB;
import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.QUESTION_VIGNETTE_ID_DIVISION_NUMBER;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class QuestionRecordingTaskServiceImpl implements QuestionRecordingTaskService {

    @Autowired
    private QuestionRecordingTaskStorage questionRecordingTaskStorage;

    @Autowired
    private RecordingTaskStorage recordingTaskStorage;

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private RecordingTaskUpdatedEventProducer recordingTaskUpdatedEventProducer;

    @Autowired
    private AuthorizeService authorizeService;

    @Autowired
    private TutorNeoQuestionProxy tutorNeoQuestionProxy;

    private static final Set<Integer> allowRecordingTaskStage = new HashSet<>(Arrays.asList(RecordingTaskStage.RECORDING.toInt(), RecordingTaskStage.TO_BE_REVISED.toInt()));

    @Override
    public Map<Long, List<QuestionRecordingTask>> getEffectiveRelationsByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Maps.newHashMap();
        }

        return questionRecordingTaskStorage.getEffectiveRelationsByRecordingApplicationIds(applicationIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionRecordingTask::getApplicationId));
    }

    @Override
    public Map<Long, List<QuestionRecordingTask>> getAllRelationsByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Maps.newHashMap();
        }

        return questionRecordingTaskStorage.getAllRelationsByRecordingApplicationIds(applicationIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionRecordingTask::getApplicationId));
    }

    @Override
    public Map<Long, List<QuestionRecordingTask>> getEffectiveRelationsByTaskIds(List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Maps.newHashMap();
        }

        return questionRecordingTaskStorage.getEffectiveRelationsByTaskIds(taskIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionRecordingTask::getTaskId,
                        collectingAndThen(toList(), tasks -> tasks.stream().sorted(Comparator.comparing(QuestionRecordingTask::getOrdinal)).collect(toList()))));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createOrUpdateQuestionRecordingTasks(List<QuestionRecordingTask> allExistQuestionRecordingTasks, List<QuestionRecordingTask> latestQuestionRecordingTasks) {
        if (CollectionUtils.isEmpty(allExistQuestionRecordingTasks) && CollectionUtils.isEmpty(latestQuestionRecordingTasks)) {
            return true;
        }

        Map<QuestionRecordingTaskPair, QuestionRecordingTask> existedEffectiveRelations = allExistQuestionRecordingTasks.stream()
                .filter(relation -> relation.getStage() != QuestionRecordingTaskStage.DELETED.toInt())
                .collect(Collectors.toMap(QuestionRecordingTaskPair::fromQuestionRecordingTask, Function.identity(), (former, latter) -> latter));

        Map<QuestionRecordingTaskPair, QuestionRecordingTask> existedAllRelations = allExistQuestionRecordingTasks.stream()
                .collect(Collectors.toMap(QuestionRecordingTaskPair::fromQuestionRecordingTask, Function.identity(), (former, latter) -> latter));

        Map<QuestionRecordingTaskPair, QuestionRecordingTask> latestRelations = latestQuestionRecordingTasks.stream()
                .collect(Collectors.toMap(QuestionRecordingTaskPair::fromQuestionRecordingTask, Function.identity(), (former, latter) -> latter));

        List<QuestionRecordingTask> deletedRelations = Sets.difference(existedEffectiveRelations.keySet(), latestRelations.keySet())
                .stream()
                .map(relationKey -> {
                    QuestionRecordingTask relation = existedEffectiveRelations.get(relationKey);
                    relation.setStage(QuestionRecordingTaskStage.DELETED.toInt());
                    return relation;
                })
                .collect(Collectors.toList());

        List<QuestionRecordingTask> newRelations = Sets.difference(latestRelations.keySet(), existedAllRelations.keySet())
                .stream()
                .map(latestRelations::get)
                .collect(Collectors.toList());

        List<QuestionRecordingTask> updatedRelations = Sets.intersection(existedAllRelations.keySet(), latestRelations.keySet())
                .stream()
                .map(relationKey -> {
                    QuestionRecordingTask latestRelation = latestRelations.get(relationKey);
                    QuestionRecordingTask existedRelation = existedAllRelations.get(relationKey);
                    if (!sameQuestionRecordingTask(latestRelation, existedRelation)) {
                        latestRelation.setId(existedRelation.getId());
                        latestRelation.setAuditDetails(CollectionUtils.isEmpty(existedRelation.getAuditDetails()) ? Lists.newArrayList() : existedRelation.getAuditDetails());
                        return latestRelation;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(updatedRelations)) {
            boolean updated = questionRecordingTaskStorage.batchUpdate(updatedRelations);
            Preconditions.checkState(updated, "更新 QuestionRecordingTasks 失败");
        }

        if (CollectionUtils.isNotEmpty(newRelations)) {
            boolean created = questionRecordingTaskStorage.batchInsert(newRelations);
            Preconditions.checkState(created, "更新 QuestionRecordingTasks 失败");
        }

        if (CollectionUtils.isNotEmpty(deletedRelations)) {
            boolean deleted = deleteQuestionsAndCleanFeedback(deletedRelations);
            Preconditions.checkState(deleted, "删除题目并清理反馈记录 失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributedLock(prefix = LockPrefixEnum.RECORDING_TASK)
    public boolean bindQuestion2RecordingTask(@DistributedLockKey(field = "taskId") BindQuestionReq req) throws ArmoryBadRequestException, ArmoryUnAuthorizedException {
        long taskId = req.getTaskId();
        RecordingTask task = recordingTaskStorage.getById(taskId);
        checkTask(task, taskId, req.getOperatorUserId());
        QuestionRecordingTask questionRecordingTask = buildQuestionRecordingTask(task, req);
        QuestionRecordingTask existsRecord = questionRecordingTaskStorage.get(questionRecordingTask.getTaskId(), questionRecordingTask.getQuestionId(), questionRecordingTask.getExamPaperId()).orElse(null);
        if (existsRecord != null) {
            if (existsRecord.getStage() == QuestionRecordingTaskStage.DELETED.toInt()) {
                questionRecordingTaskStorage.resume(existsRecord.getId(), questionRecordingTask);
            } else if (questionRecordingTask.getStage() < 0) {
                //约定小于0的都是草稿态，如果已经有存在的记录，说明曾经加入过任务，不进行处理
                return true;
            } else {
                log.error("find duplicate questionRecordingTask: {}, {}", existsRecord, req);
                throw new ArmoryBadRequestException("该题目已被加入到录题任务中，不能重复添加");
            }
        } else {
            questionRecordingTaskStorage.insert(questionRecordingTask);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributedLock(prefix = LockPrefixEnum.RECORDING_TASK)
    public boolean bindBatchQuestion2RecordingTask(@DistributedLockKey(field = "taskId") BindBatchQuestionReq req) throws ArmoryBadRequestException, ArmoryUnAuthorizedException {
        long taskId = req.getTaskId();
        RecordingTask task = recordingTaskStorage.getById(taskId);
        checkTask(task, taskId, req.getOperatorUserId());
        List<QuestionRecordingTask> questionRecordingTasks = buildQuestionRecordingTasks(task, req);
        List<QuestionRecordingTask> existsTasks = questionRecordingTaskStorage.getTasks(req.getTaskId(), req.getQuestionIds(), req.getExamPaperId());
        Map<Integer, QuestionRecordingTask> questionId2RecordingTask = questionRecordingTasks.stream().collect(Collectors.toMap(QuestionRecordingTask::getQuestionId, Function.identity(), Lambdas.pickFirst()));
        //数据库内的task列表
        List<Long> deletedIds = new ArrayList<>();
        //入参的task列表
        List<QuestionRecordingTask> needResumeTasks = new ArrayList<>();
        List<QuestionRecordingTask> draftTasks = new ArrayList<>();
        List<QuestionRecordingTask> repeatTasks = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(existsTasks)) {
            existsTasks.forEach(existsTask -> {
                if (existsTask.getStage() == QuestionRecordingTaskStage.DELETED.toInt()) {
                    deletedIds.add(existsTask.getId());
                    needResumeTasks.add(questionId2RecordingTask.get(existsTask.getQuestionId()));
                } else if (questionId2RecordingTask.get(existsTask.getQuestionId()).getStage() < 0) {
                    draftTasks.add(existsTask);
                } else {
                    repeatTasks.add(existsTask);
                }
            });
            if (CollectionUtils.isNotEmpty(repeatTasks)) {
                log.error("find duplicate questionRecordingTasks: {}, {}", repeatTasks, req);
                throw new ArmoryBadRequestException("存在已被加入到录题任务中的题目，不能重复添加" + repeatTasks.stream().map(QuestionRecordingTask::getQuestionId).collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(draftTasks)) {
                //约定小于0的都是草稿态，如果已经有存在的记录，说明曾经加入过任务，不进行处理
                return true;
            }
            questionRecordingTaskStorage.batchResume(deletedIds, needResumeTasks);
            List<QuestionRecordingTask> newTasks = new ArrayList<>();
            for (QuestionRecordingTask questionRecordingTask : questionRecordingTasks) {
                boolean isNewQuestion = needResumeTasks.stream().allMatch(oldTask -> oldTask.getQuestionId() != questionRecordingTask.getQuestionId());
                if (isNewQuestion) {
                    newTasks.add(questionRecordingTask);
                }
            }
            if (CollectionUtils.isNotEmpty(newTasks)) {
                questionRecordingTaskStorage.batchInsert(newTasks);
            }
        } else {
            questionRecordingTaskStorage.batchInsert(questionRecordingTasks);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributedLock(prefix = LockPrefixEnum.RECORDING_TASK)
    public boolean replaceQuestionAndFeedback(@DistributedLockKey(field = LockKeyFieldConstant.PARAMETER_IDENTITY) long taskId, long examPaperId, int needReplaceId, int replacedId) {
        Optional<QuestionRecordingTask> questionRecordingTaskOp = questionRecordingTaskStorage.get(taskId, needReplaceId, examPaperId);
        if (!questionRecordingTaskOp.isPresent()) {
            log.error("替换题目失败， 题目与任务关联关系不存在, taskId:{}, examPaperId:{}, questionId:{}", taskId, examPaperId, needReplaceId);
            return false;
        }
        QuestionRecordingTask needReplaceQuestionRecordingTask = questionRecordingTaskOp.get();
        QuestionRecordingTask needDeleteQuestionRecordingTask = needReplaceQuestionRecordingTask.deepCopy();
        needDeleteQuestionRecordingTask.setStage(QuestionRecordingTaskStage.DELETED.toInt());
        boolean questionsDeleted = questionRecordingTaskStorage.batchUpdate(Collections.singletonList(needDeleteQuestionRecordingTask));
        Preconditions.checkState(questionsDeleted, "删除题目关联失败");
        QuestionRecordingTask replacedQuestionRecordingTask = needReplaceQuestionRecordingTask.deepCopy();
        replacedQuestionRecordingTask.setOriginWholeQuestionId(needReplaceId);
        replacedQuestionRecordingTask.setQuestionId(replacedId);
        if (CollectionUtils.isNotEmpty(replacedQuestionRecordingTask.getAuditDetails())) {
            AuditDetail firstAuditDetail = replacedQuestionRecordingTask.getAuditDetails().get(0);
            if (firstAuditDetail.getOriginStage() == QuestionRecordingTaskStage.INACTIVATED.toInt() && firstAuditDetail.getLatestStage() == QuestionRecordingTaskStage.RECORDED.toInt()) {
                List<String> imageIds = new ArrayList<>();
                List<String> audioIds = new ArrayList<>();

                if (replacedId > QUESTION_VIGNETTE_ID_DIVISION_NUMBER) {
                    Vignette vignette = tutorNeoQuestionProxy.getVignettes(Collections.singletonList(replacedId), CONTENT_TYPE_UBB).get(replacedId);
                    if (Objects.isNull(vignette)) {
                        throw new RuntimeException("将题目保存到录题任务，题目不存在, 题目id:" + replacedId);
                    }
                    imageIds.addAll(QuestionImageUtil.getVignetteAllImageIds(vignette));

                    if (StringUtils.isNotBlank(vignette.getAudioId())) {
                        audioIds.add(vignette.getAudioId());
                    }
                    if (CollectionUtils.isNotEmpty(vignette.getQuestionIds())) {
                        Map<Integer, Question> questionId2question = tutorNeoQuestionProxy.getQuestions(vignette.getQuestionIds(), CONTENT_TYPE_UBB);
                        questionId2question.values().forEach(question -> {
                            imageIds.addAll(QuestionImageUtil.getQuestionAllImageIds(question));

                            if (StringUtils.isNotBlank(question.getAudioId())) {
                                audioIds.add(question.getAudioId());
                            }
                            if (StringUtils.isNotBlank(question.getTtsAudioId())) {
                                audioIds.add(question.getTtsAudioId());
                            }
                        });
                    }
                } else {
                    Question question = tutorNeoQuestionProxy.getQuestions(Collections.singletonList(replacedId), CONTENT_TYPE_UBB).get(replacedId);
                    if (Objects.isNull(question)) {
                        throw new RuntimeException("将题目保存到录题任务，题目不存在, 题目id:" + replacedId);
                    }
                    imageIds.addAll(QuestionImageUtil.getQuestionAllImageIds(question));

                    if (StringUtils.isNotBlank(question.getAudioId())) {
                        audioIds.add(question.getAudioId());
                    }
                    if (StringUtils.isNotBlank(question.getTtsAudioId())) {
                        audioIds.add(question.getTtsAudioId());
                    }
                }
                firstAuditDetail.setAudioIds(audioIds);
                firstAuditDetail.setImageIds(imageIds);
            }
        }
        questionRecordingTaskStorage.insert(replacedQuestionRecordingTask);
        log.info("replace needReplaceQuestionRecordingTask {} to replacedQuestionRecordingTask {}", needReplaceQuestionRecordingTask, replacedQuestionRecordingTask);

        //替换错题反馈
        QuestionRecordingTaskPair questionRecordingTaskPair = QuestionRecordingTaskPair.fromQuestionRecordingTask(needReplaceQuestionRecordingTask);
        List<RecordingFeedback> needReplaceRecordingFeedbacks = recordingFeedbackService.getFeedBacksOfSpecificTargetOfTask(needReplaceId, FeedbackTargetType.QUESTION, examPaperId, taskId, false);
        recordingFeedbackService.deleteFeedBackOfQuestions(Collections.singletonList(questionRecordingTaskPair));
        Set<RecordingFeedback> replacedRecordingFeedbacks = needReplaceRecordingFeedbacks.stream()
                .peek(recordingFeedback -> recordingFeedback.setTargetId(replacedId))
                .collect(Collectors.toSet());
        recordingFeedbackService.batchCreateWithProcessedAndCreatedTime(replacedRecordingFeedbacks);
        log.debug("replace recordingFeedbacks {} to replacedRecordingFeedbacks {}", needReplaceRecordingFeedbacks, replacedRecordingFeedbacks);

        return true;
    }

    @Override
    public void saveAuditTask(SaveAuditTaskRequest request) {
        Optional<QuestionRecordingTask> taskOptional = getEffectiveRelationByTaskIdAndExamPaperId(
                (int) request.getTargetId(), request.getTaskId(), request.getExamPaperId());
        if (!taskOptional.isPresent()) {
            throw new BadRequestException(String.format(
                    "任务 id = %d 下不存在属于试卷 id = %d 的题目 id = %d ！",
                    request.getTaskId(), request.getExamPaperId(), request.getTargetId()));
        }
        QuestionRecordingTask questionRecordingTask = taskOptional.get();

        if (!RecordingStageUtil.canAudit(questionRecordingTask)) {
            throw new BadRequestException(String.format("题目-任务关联 id = %d 状态 stage = %d，不允许审核!",
                    questionRecordingTask.getId(), questionRecordingTask.getStage()));
        }

        RecordingTask recordingTask = recordingTaskStorage.getById(request.getTaskId());
        if (Objects.isNull(recordingTask)) {
            throw new BadRequestException(String.format("题目-任务关联 id = %d 状态 stage = %d，题目所属任务不存在!",
                    questionRecordingTask.getId(), questionRecordingTask.getStage()));
        }

        int originStage = questionRecordingTask.getStage();
        int targetStage = request.isPassed() ?
                QuestionRecordingTaskStage.AUDIT_APPROVED.toInt() :
                QuestionRecordingTaskStage.AUDIT_FAILED.toInt();

        AuditDetail auditDetail = new AuditDetail();
        if (needUpdate(originStage, targetStage)) {
            questionRecordingTask.setTaskId(request.getTaskId());
            questionRecordingTask.setExamPaperId(request.getExamPaperId());
            questionRecordingTask.setQuestionId((int) request.getTargetId());
            questionRecordingTask.setStage(targetStage);

            auditDetail.setLatestStage(targetStage);
        } else {
            auditDetail.setLatestStage(originStage);
        }
        auditDetail.setAuditTimes(recordingTask.getSubmitTimes());
        auditDetail.setOriginStage(originStage);
        auditDetail.setAuditor(getUserId());
        auditDetail.setAuditTime(System.currentTimeMillis());

        List<AuditDetail> auditDetails = CollectionUtils.isEmpty(questionRecordingTask.getAuditDetails()) ? Lists.newArrayList() : questionRecordingTask.getAuditDetails();
        auditDetails.add(auditDetail);
        questionRecordingTask.setAuditDetails(auditDetails);
        boolean success = questionRecordingTaskStorage.batchUpdate(Collections.singletonList(questionRecordingTask));
        if (!success) {
            throw new BadRequestException("其他人已修改题目录入任务状态,请刷新页面再试!");
        }

        // 处理反馈
        RecordingFeedback feedback = buildFeedback(request, questionRecordingTask);
        boolean notBeenAudited = QuestionRecordingTaskStage.TO_BE_AUDITED.toInt() == originStage;
        boolean everBeenApproved = RecordingStageUtil.auditApproved(questionRecordingTask);
        boolean approved = request.isPassed();
        recordingFeedbackService.processFeedbackForAudit(feedback, notBeenAudited, everBeenApproved, approved);
    }

    private boolean needUpdate(int originStage, int targetStage) {
        // stage无变化
        if (originStage == targetStage) {
            return false;
        }

        // 任何状态 -> 不通过
        if (targetStage == QuestionRecordingTaskStage.AUDIT_FAILED.toInt()) {
            return true;
        }

        // 不通过 -> 通过
        if ((originStage == QuestionRecordingTaskStage.AUDIT_FAILED.toInt()) && (targetStage == QuestionRecordingTaskStage.AUDIT_APPROVED.toInt())) {
            return true;
        }

        // 待审核 -> 通过
        if ((originStage == QuestionRecordingTaskStage.TO_BE_AUDITED.toInt()) && (targetStage == QuestionRecordingTaskStage.AUDIT_APPROVED.toInt())) {
            return true;
        }

        // 其他状态 -> 通过
        return false;
    }

    private RecordingFeedback buildFeedback(SaveAuditTaskRequest request, QuestionRecordingTask questionRecordingTask) {
        RecordingFeedback feedback = new RecordingFeedback();
        feedback.setApplicationId(questionRecordingTask.getApplicationId());
        feedback.setTaskId(questionRecordingTask.getTaskId());
        feedback.setTargetId(questionRecordingTask.getQuestionId());
        feedback.setTargetType(FeedbackTargetType.QUESTION.toInt());
        feedback.setExamPaperId(questionRecordingTask.getExamPaperId());
        feedback.setType(request.isPassed() ? FeedbackType.QUESTION_APPROVED.toInt() : FeedbackType.QUESTION_AUDIT_FAILED.toInt());
        feedback.setReason(request.getReason());
        feedback.setDescription(request.getDescription());
        feedback.setAttachments(request.getAttachments());
        feedback.setCreator(getUserId());
        return feedback;
    }

    @Override
    public List<QuestionRecordingTask> getQuestionRecordingTasksByTaskId(long taskId) {
        return questionRecordingTaskStorage.getQuestionRecordingTasksByTaskId(taskId);
    }

    @Override
    public boolean submit(long taskId) {
        Set<Integer> fromStages = QuestionRecordingTaskStage.getAvailableSourceStages(QuestionRecordingTaskStage.TO_BE_AUDITED)
                .stream()
                .map(QuestionRecordingTaskStage::toInt)
                .collect(Collectors.toSet());
        return questionRecordingTaskStorage.updateStage(taskId, fromStages, QuestionRecordingTaskStage.TO_BE_AUDITED.toInt());
    }

    @Override
    public boolean updateQuestionRecordingTaskSnapshots(long taskId, long examPaperId, int questionId, List<String> snapshots) {
        Optional<QuestionRecordingTask> questionRecordingTaskOp = questionRecordingTaskStorage.get(taskId, questionId, examPaperId);
        if (!questionRecordingTaskOp.isPresent()) {
            log.error("更新题目原图， 题目与任务关联关系不存在, taskId:{}, examPaperId:{}, questionId:{}", taskId, examPaperId, questionId);
            return false;
        }

        QuestionRecordingTask questionRecordingTask = questionRecordingTaskOp.get();
        if (questionRecordingTask.getStage() == QuestionRecordingTaskStage.DELETED.toInt()) {
            log.error("更新题目原图， 题目与任务关联关系已删除, taskId:{}, examPaperId:{}, questionId:{}", taskId, examPaperId, questionId);
            return false;
        }

        if (ListUtils.isEqualList(questionRecordingTask.getSnapshots(), snapshots)) {
            return true;
        }
        return questionRecordingTaskStorage.updateQuestionRecordingTaskSnapshots(taskId, examPaperId, questionId, snapshots);
    }

    @Override
    public Optional<QuestionRecordingTask> get(long taskId, int questionId, long examPaperId) {
        return questionRecordingTaskStorage.get(taskId, questionId, examPaperId);
    }

    @Override
    public boolean updateStage(long taskId, long examPaperId, int questionId, int stage) {
        return questionRecordingTaskStorage.updateQuestionRecordingTaskStage(taskId, examPaperId, questionId, stage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean tagFinish(long taskId, int questionId, long examPaperId) {
        RecordingTask recordingTask = recordingTaskStorage.getById(taskId);
        if (recordingTask == null) {
            log.error("taskId {} not exist", taskId);
            return false;
        }
        boolean res = questionRecordingTaskStorage.updateQuestionRecordingTaskStage(taskId, examPaperId, questionId, QuestionRecordingTaskStage.TAG_FINISHED.toInt());
        if (res) {
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(recordingTask.getApplicationId(), taskId));
        } else {
            log.error("tagFinish failed, taskId {}, questionId {}", taskId, questionId);
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributedLock(prefix = LockPrefixEnum.RECORDING_TASK)
    public boolean deleteAndCleanFeedback(@DistributedLockKey(field = "taskId") QuestionRecordingTask questionRecordingTask) {
        boolean deleted = deleteQuestionsAndCleanFeedback(Lists.newArrayList(questionRecordingTask));
        if (deleted) {
            questionRecordingTaskStorage.decreaseOrdinal(questionRecordingTask.getTaskId(), questionRecordingTask.getExamPaperId(), questionRecordingTask.getOrdinal(), 1);
            recordingTaskUpdatedEventProducer.publish(new RecordingTaskUpdatedEvent(questionRecordingTask.getApplicationId(), questionRecordingTask.getTaskId()));
        }
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteQuestionsAndCleanFeedback(List<QuestionRecordingTask> questionRecordingTasks) {
        if (CollectionUtils.isEmpty(questionRecordingTasks)) {
            return true;
        }
        boolean allLegalRelations = questionRecordingTasks.stream().allMatch(relation -> relation.getId() > 0L);
        if (!allLegalRelations) {
            log.error("要删除的题目信息不完整, questionRecordingTasks = {}", questionRecordingTasks);
            return false;
        }

        List<QuestionRecordingTask> relationsToBeDeleted = questionRecordingTasks
                .stream()
                .map(relation -> {
                    QuestionRecordingTask param = relation.deepCopy();
                    param.setStage(QuestionRecordingTaskStage.DELETED.toInt());
                    return param;
                })
                .collect(Collectors.toList());
        boolean questionsDeleted = questionRecordingTaskStorage.batchUpdate(relationsToBeDeleted);
        Preconditions.checkState(questionsDeleted, "删除题目关联失败");

        Set<QuestionRecordingTaskPair> questionIdentifiers = questionRecordingTasks
                .stream()
                .map(QuestionRecordingTaskPair::fromQuestionRecordingTask)
                .collect(Collectors.toSet());
        recordingFeedbackService.deleteFeedBackOfQuestions(questionIdentifiers);
        return true;
    }

    @Override
    public boolean batchUpdate(List<QuestionRecordingTask> questionRecordingTasks) {
        if (CollectionUtils.isEmpty(questionRecordingTasks)) {
            return true;
        }

        boolean updated = questionRecordingTaskStorage.batchUpdate(questionRecordingTasks);
        if (updated) {
            questionRecordingTasks.stream()
                    .map(relation -> new RecordingTaskUpdatedEvent(relation.getApplicationId(), relation.getTaskId()))
                    .distinct()
                    .forEach(event -> recordingTaskUpdatedEventProducer.publish(event));
        }
        return updated;
    }

    @Override
    public Optional<QuestionRecordingTask> getEffectiveRelationByTaskIdAndExamPaperId(int questionId, long taskId, long examPaperId) {
        return questionRecordingTaskStorage.get(taskId, questionId, examPaperId).filter(relation -> relation.getStage() > 0);
    }

    private QuestionRecordingTask buildQuestionRecordingTask(RecordingTask task, BindQuestionReq bindQuestionReq) throws ArmoryBadRequestException {
        QuestionRecordingTask result = new QuestionRecordingTask();
        result.setApplicationId(task.getApplicationId());
        result.setTaskId(task.getId());

        int questionId = bindQuestionReq.getQuestionId();
        result.setQuestionId(questionId);
        result.setQuestionOrigin(bindQuestionReq.getQuestionOrigin());
        result.setSnapshots(bindQuestionReq.getSnapshotImgIds());
        result.setCreator(bindQuestionReq.getOperatorUserId());
        result.setCreatedTime(System.currentTimeMillis());
        if (task.getRecordingMode() == RecordingMode.EXAM_PAPER.toInt()) {
            if (bindQuestionReq.getExamPaperId() <= 0) {
                throw new ArmoryBadRequestException("将题目加入到套卷，套卷不存在");
            }
            result.setExamPaperId(bindQuestionReq.getExamPaperId());
            result.setStage(QuestionRecordingTaskStage.INACTIVATED.toInt());
        } else if (task.getRecordingMode() == RecordingMode.SINGLE_QUESTION.toInt()) {
            result.setStage(QuestionRecordingTaskStage.RECORDED.toInt());
            int maxOrdinal = questionRecordingTaskStorage.getMaxOrdinal(task.getId());
            result.setOrdinal(maxOrdinal + 1);
        }

        List<String> imageIds = new ArrayList<>();
        List<String> audioIds = new ArrayList<>();

        if (questionId > QUESTION_VIGNETTE_ID_DIVISION_NUMBER) {
            Vignette vignette = tutorNeoQuestionProxy.getVignettes(Collections.singletonList(questionId), CONTENT_TYPE_UBB).get(questionId);
            if (Objects.isNull(vignette)) {
                throw new ArmoryBadRequestException("将题目保存到录题任务，题目不存在, 题目id:" + questionId);
            }
            imageIds.addAll(QuestionImageUtil.getVignetteAllImageIds(vignette));

            if (StringUtils.isNotBlank(vignette.getAudioId())) {
                audioIds.add(vignette.getAudioId());
            }
            if (CollectionUtils.isNotEmpty(vignette.getQuestionIds())) {
                Map<Integer, Question> questionId2question = tutorNeoQuestionProxy.getQuestions(vignette.getQuestionIds(), CONTENT_TYPE_UBB);
                questionId2question.values().forEach(question -> {
                    imageIds.addAll(QuestionImageUtil.getQuestionAllImageIds(question));

                    if (StringUtils.isNotBlank(question.getAudioId())) {
                        audioIds.add(question.getAudioId());
                    }
                    if (StringUtils.isNotBlank(question.getTtsAudioId())) {
                        audioIds.add(question.getTtsAudioId());
                    }
                });
            }
        } else {
            Question question = tutorNeoQuestionProxy.getQuestions(Collections.singletonList(questionId), CONTENT_TYPE_UBB).get(questionId);
            if (Objects.isNull(question)) {
                throw new ArmoryBadRequestException("将题目保存到录题任务，题目不存在, 题目id:" + questionId);
            }
            imageIds.addAll(QuestionImageUtil.getQuestionAllImageIds(question));

            if (StringUtils.isNotBlank(question.getAudioId())) {
                audioIds.add(question.getAudioId());
            }
            if (StringUtils.isNotBlank(question.getTtsAudioId())) {
                audioIds.add(question.getTtsAudioId());
            }
        }

        AuditDetail auditDetail = new AuditDetail();
        auditDetail.setAuditTimes(task.getSubmitTimes());
        auditDetail.setOriginStage(QuestionRecordingTaskStage.INACTIVATED.toInt());
        auditDetail.setLatestStage(QuestionRecordingTaskStage.RECORDED.toInt());
        auditDetail.setAuditor(bindQuestionReq.getOperatorUserId());
        auditDetail.setAuditTime(System.currentTimeMillis());
        auditDetail.setAudioIds(audioIds);
        auditDetail.setImageIds(imageIds);

        result.setAuditDetails(Collections.singletonList(auditDetail));
        return result;
    }

    private List<QuestionRecordingTask> buildQuestionRecordingTasks(RecordingTask task, BindBatchQuestionReq bindBatchQuestionReq) throws ArmoryBadRequestException {
        if (CollectionUtils.isEmpty(bindBatchQuestionReq.getQuestionIds())) {
            return Lists.newArrayList();
        }
        List<Integer> questionIds = bindBatchQuestionReq.getQuestionIds().stream().filter(questionId -> questionId < QUESTION_VIGNETTE_ID_DIVISION_NUMBER).collect(toList());
        Map<Integer, Question> questionId2question = tutorNeoQuestionProxy.getQuestions(questionIds, CONTENT_TYPE_UBB);

        List<Integer> vignetteIds = bindBatchQuestionReq.getQuestionIds().stream().filter(questionId -> questionId >= QUESTION_VIGNETTE_ID_DIVISION_NUMBER).collect(toList());
        Map<Integer, Vignette> vignetteId2vignette = tutorNeoQuestionProxy.getVignettes(vignetteIds, CONTENT_TYPE_UBB);

        List<QuestionRecordingTask> result = new ArrayList<>();
        for (int questionId : bindBatchQuestionReq.getQuestionIds()) {
            QuestionRecordingTask questionRecordingTask = new QuestionRecordingTask();
            questionRecordingTask.setApplicationId(task.getApplicationId());
            questionRecordingTask.setTaskId(task.getId());
            questionRecordingTask.setQuestionId(questionId);
            questionRecordingTask.setQuestionOrigin(bindBatchQuestionReq.getQuestionOrigin());
            questionRecordingTask.setSnapshots(bindBatchQuestionReq.getQuestionId2SnapshotImgIds().get(questionId));
            questionRecordingTask.setCreator(bindBatchQuestionReq.getOperatorUserId());
            questionRecordingTask.setCreatedTime(System.currentTimeMillis());

            List<String> imageIds = new ArrayList<>();
            List<String> audioIds = new ArrayList<>();
            if (questionId > QUESTION_VIGNETTE_ID_DIVISION_NUMBER) {
                Vignette vignette = vignetteId2vignette.get(questionId);
                if (Objects.isNull(vignette)) {
                    throw new ArmoryBadRequestException("将题目保存到录题任务，题目不存在, 题目id:" + questionId);
                }
                imageIds.addAll(QuestionImageUtil.getVignetteAllImageIds(vignette));

                if (StringUtils.isNotBlank(vignette.getAudioId())) {
                    audioIds.add(vignette.getAudioId());
                }
                if (CollectionUtils.isNotEmpty(vignette.getQuestionIds())) {
                    Map<Integer, Question> questionIdMap = tutorNeoQuestionProxy.getQuestions(vignette.getQuestionIds(), CONTENT_TYPE_UBB);
                    questionIdMap.values().forEach(question -> {
                        imageIds.addAll(QuestionImageUtil.getQuestionAllImageIds(question));

                        if (StringUtils.isNotBlank(question.getAudioId())) {
                            audioIds.add(question.getAudioId());
                        }
                        if (StringUtils.isNotBlank(question.getTtsAudioId())) {
                            audioIds.add(question.getTtsAudioId());
                        }
                    });
                }
            } else {
                Question question = questionId2question.get(questionId);
                if (Objects.isNull(question)) {
                    throw new ArmoryBadRequestException("将题目保存到录题任务，题目不存在, 题目id:" + questionId);
                }
                imageIds.addAll(QuestionImageUtil.getQuestionAllImageIds(question));

                if (StringUtils.isNotBlank(question.getAudioId())) {
                    audioIds.add(question.getAudioId());
                }
                if (StringUtils.isNotBlank(question.getTtsAudioId())) {
                    audioIds.add(question.getTtsAudioId());
                }
            }

            AuditDetail auditDetail = new AuditDetail();
            auditDetail.setAuditTimes(task.getSubmitTimes());
            auditDetail.setOriginStage(QuestionRecordingTaskStage.INACTIVATED.toInt());
            auditDetail.setLatestStage(QuestionRecordingTaskStage.RECORDED.toInt());
            auditDetail.setAuditor(bindBatchQuestionReq.getOperatorUserId());
            auditDetail.setAuditTime(System.currentTimeMillis());
            auditDetail.setAudioIds(audioIds);
            auditDetail.setImageIds(imageIds);

            questionRecordingTask.setAuditDetails(Collections.singletonList(auditDetail));
            result.add(questionRecordingTask);
        }

        if (task.getRecordingMode() == RecordingMode.EXAM_PAPER.toInt()) {
            if (bindBatchQuestionReq.getExamPaperId() <= 0) {
                throw new ArmoryBadRequestException("将题目加入到套卷，套卷不存在");
            }
            result.forEach(questionRecordingTask -> {
                questionRecordingTask.setExamPaperId(bindBatchQuestionReq.getExamPaperId());
                questionRecordingTask.setStage(QuestionRecordingTaskStage.INACTIVATED.toInt());
            });
        } else if (task.getRecordingMode() == RecordingMode.SINGLE_QUESTION.toInt()) {
            int startOrdinal = questionRecordingTaskStorage.getMaxOrdinal(task.getId()) + 1;
            for (int i = 0; i < result.size(); i++) {
                result.get(i).setStage(QuestionRecordingTaskStage.RECORDED.toInt());
                result.get(i).setOrdinal(startOrdinal + i);
            }
        }
        return result;
    }

    private boolean sameQuestionRecordingTask(QuestionRecordingTask existQuestionRecordingTask, QuestionRecordingTask latestExistQuestionRecordingTask) {
        return (existQuestionRecordingTask.getExamPaperId() == latestExistQuestionRecordingTask.getExamPaperId())
                && (existQuestionRecordingTask.getApplicationId() == latestExistQuestionRecordingTask.getApplicationId())
                && (existQuestionRecordingTask.getTaskId() == latestExistQuestionRecordingTask.getTaskId())
                && (existQuestionRecordingTask.getStage() == latestExistQuestionRecordingTask.getStage())
                && (existQuestionRecordingTask.getOrdinal() == latestExistQuestionRecordingTask.getOrdinal());
    }

    private void authorizeForUpdateQuestions(RecordingTask task, int userId) throws ArmoryUnAuthorizedException {
        boolean authorized = authorizeService.featureAuthorized(userId, task.getSubjectId(), task.getPhaseId(),
                task.getRecorder() == userId ? QUESTION_RECORDING : QUESTION_RECORDING_OF_OTHERS);
        if (!authorized) {
            throw new ArmoryUnAuthorizedException("没有权限执行当前操作");
        }
    }

    private void checkTask(RecordingTask task, long taskId, int operatorId) throws ArmoryBadRequestException, ArmoryUnAuthorizedException {

        if (task == null) {
            log.error("task not exists: {}", taskId);
            throw new ArmoryBadRequestException("task not exists: " + taskId);
        }

        if (operatorId <= 0) {
            log.error("cannot find userId :{}", operatorId);
            throw new RuntimeException();
        }

        authorizeForUpdateQuestions(task, operatorId);
        RecordingTaskStage stage = RecordingTaskStage.findByInt(task.getStage()).orElse(null);
        if (stage == null) {
            log.error("unsupported stage value: {}", task.getStage());
            throw new RuntimeException();
        }
        if (!allowRecordingTaskStage.contains(task.getStage())) {
            log.warn("task {}, stage: {}, not allow record question", taskId, task.getStage());
            throw new ArmoryBadRequestException(String.format("录题任务状态: %s, 不再允许加入题目", stage.toString()));
        }
    }
}
