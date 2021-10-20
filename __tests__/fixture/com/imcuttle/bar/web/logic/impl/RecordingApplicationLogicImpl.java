/**
 * @(#)RecordingApplicationLogicImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import com.fenbi.commons.paging.Page;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.fenbi.commons2.rest.exception.ServerErrorException;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.FeedbackType;
import com.imcuttle.enums.RecordingApplicationQuestionSource;
import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.constant.PatronusConstant;
import com.imcuttle.bar.data.Change;
import com.imcuttle.bar.data.RecordingApplicationSearchCondition;
import com.imcuttle.bar.enums.FeatureEnum;
import com.imcuttle.bar.enums.TableEnum;
import com.imcuttle.bar.exceptions.CheckFailedException;
import com.imcuttle.bar.message.producer.RecordingApplicationUpdateEventProducer;
import com.imcuttle.bar.service.CmsLogService;
import com.imcuttle.bar.service.ExamPaperRecordingTaskService;
import com.imcuttle.bar.service.LogDiffService;
import com.imcuttle.bar.service.QuestionRecordingTaskService;
import com.imcuttle.bar.service.RecordingApplicationDetailService;
import com.imcuttle.bar.service.RecordingApplicationSearchService;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.RecordingFeedbackService;
import com.imcuttle.bar.service.RecordingTaskService;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.web.data.AuditorVO;
import com.imcuttle.bar.web.data.RecorderVO;
import com.imcuttle.bar.web.data.RecordingApplicationBasicSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationBasicVO;
import com.imcuttle.bar.web.data.RecordingApplicationBriefVO;
import com.imcuttle.bar.web.data.RecordingApplicationCancelParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationManageSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationProgressVO;
import com.imcuttle.bar.web.logic.RecordingApplicationLogic;
import com.imcuttle.bar.web.wrapper.RecordingApplicationProgressWrapper;
import com.imcuttle.bar.web.wrapper.RecordingApplicationWrapper;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingApplicationDetail;
import com.imcuttle.thrift.RecordingFeedback;
import com.fenbi.tutor.common.constant.Subject;
import com.fenbi.tutor.common.util.StudyPhaseUtils;
import com.fenbi.tutor.crmaccount.thrift.UserInfo;
import com.fenbi.tutor.patronus.client.proxy.spi.TutorPatronusProxy;
import com.fenbi.tutor.patronus.thrift.QueryFeatureUsersRequest;
import com.fenbi.tutor.patronus.thrift.QueryFeatureUsersResponse;
import com.fenbi.tutor.patronus.thrift.User;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;
import static com.imcuttle.enums.RecordingApplicationStage.PROCESSING;
import static com.imcuttle.enums.RecordingApplicationStage.PUBLISHED;
import static com.imcuttle.enums.RecordingApplicationStage.TO_BE_REVISED;
import static com.imcuttle.bar.enums.FeatureEnum.RECORDING_APPLICATION;
import static com.imcuttle.bar.enums.FeatureEnum.RECORDING_APPLICATION_OF_OTHERS;
import static com.imcuttle.bar.web.wrapper.RecordingApplicationSearchConditionWrapper.wrap;
import static com.imcuttle.bar.web.wrapper.RecordingApplicationWrapper.wrapBriefInfo;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.RECORDING_APPLICATION_ID;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class RecordingApplicationLogicImpl extends BaseLogic implements RecordingApplicationLogic {

    @Autowired
    private RecordingApplicationSearchService recordingApplicationSearchService;

    @Autowired
    private RecordingApplicationService recordingApplicationService;

    @Autowired
    private CmsLogService cmsLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecordingFeedbackService recordingFeedbackService;

    @Autowired
    private RecordingApplicationUpdateEventProducer recordingApplicationUpdateEventProducer;

    @Autowired
    private QuestionRecordingTaskService questionRecordingTaskService;

    @Autowired
    private ExamPaperRecordingTaskService examPaperRecordingTaskService;

    @Autowired
    private RecordingApplicationDetailService recordingApplicationDetailService;

    @Autowired
    private RecordingTaskService recordingTaskService;

    @Autowired
    private TutorPatronusProxy patronusProxy;

    @Autowired
    @Qualifier("recordingApplicationDiffService")
    private LogDiffService<RecordingApplication> recordingApplicationDiffService;

    @Override
    public Page<RecordingApplicationBasicVO> searchBasicInfoList(RecordingApplicationBasicSearchParamVO searchParamVO) {
        RecordingApplicationSearchCondition searchCondition = wrap(searchParamVO);
        searchCondition.setAuthorizedSubjectPhases(getAuthorizedSubjectPhases(TableEnum.RECORDING_APPLICATION));
        Page<Long> pageIdResult = recordingApplicationSearchService.searchIdList(searchCondition);

        Page<RecordingApplicationBasicVO> pageResult = new Page<>(pageIdResult.getPageInfo(), Lists.newArrayList());
        if (CollectionUtils.isEmpty(pageIdResult.getList())) {
            return pageResult;
        }

        List<Long> ids = pageIdResult.getList();
        Map<Long, RecordingApplication> recordingApplications = recordingApplicationService.getByIds(ids);
        List<RecordingApplicationBasicVO> basicVOS = ids.stream()
                .filter(recordingApplications::containsKey)
                .map(recordingApplications::get)
                .map(RecordingApplicationWrapper::wrapBasic)
                .collect(Collectors.toList());
        pageResult.setList(basicVOS);

        return pageResult;
    }

    @Override
    public long save(RecordingApplicationBasicVO applicationBasicVO) {
        if (applicationBasicVO.getSubjectId() <= 0 || !Subject.findById(applicationBasicVO.getSubjectId()).isPresent()) {
            throw new BadRequestException("必须指定系统支持的学科");
        }

        if (!StudyPhaseUtils.isValidStudyPhase(applicationBasicVO.getPhaseId())) {
            throw new BadRequestException("必须指定系统支持的学段");
        }

        if (!RecordingApplicationQuestionSource.isValidQuestionSource(applicationBasicVO.getQuestionSource())) {
            throw new BadRequestException("必须指定系统支持的题目来源");
        }

        if (StringUtils.isBlank(applicationBasicVO.getName())) {
            throw new BadRequestException("必须指定名称");
        }

        if (StringUtils.isBlank(applicationBasicVO.getResourceId())) {
            throw new BadRequestException("必须提供题目原件");
        }

        if (StringUtils.isBlank(applicationBasicVO.getFileName())) {
            throw new BadRequestException("没有设置题目原件的文件名");
        }

        RecordingApplication recordingApplication = RecordingApplicationWrapper.unwrap(applicationBasicVO);
        Preconditions.checkNotNull(recordingApplication, "创建录题申请的入参为空");

        recordingApplication.setCreator(getUserId());
        if (recordingApplication.getId() <= 0L) {
            authorizeFeature(recordingApplication.getSubjectId(), recordingApplication.getPhaseId(), RECORDING_APPLICATION, "您无该科目阶段的录题申请权限, 请联系题库中台教研");

            recordingApplication.setSubmitTime(System.currentTimeMillis());
            long id = recordingApplicationService.create(recordingApplication);
            cmsLogService.log(id, RECORDING_APPLICATION_ID, "创建录题申请");
            return id;
        } else {
            long applicationId = recordingApplication.getId();
            RecordingApplication oldRecordingApplication = recordingApplicationService
                    .getByIds(Collections.singletonList(applicationId))
                    .get(applicationId);
            if (oldRecordingApplication == null) {
                throw new BadRequestException(String.format("要更新的录题申请不存在！id = %d", applicationId));
            }

            if (RecordingApplicationStage.PROCESSING.toInt() == oldRecordingApplication.getStage()) {
                boolean subjectChanged = oldRecordingApplication.getSubjectId() != recordingApplication.getSubjectId();
                boolean phaseChanged = oldRecordingApplication.getPhaseId() != recordingApplication.getPhaseId();
                boolean questionSourceChanged = oldRecordingApplication.getQuestionSource() != recordingApplication.getQuestionSource();
                if (subjectChanged || phaseChanged || questionSourceChanged) {
                    throw new BadRequestException("任务进行中的录题申请, 不允许再修改科目阶段或题目来源");
                }
            }

            authorizeFeature(recordingApplication.getSubjectId(), recordingApplication.getPhaseId(),
                    oldRecordingApplication.getCreator() == getUserId() ? RECORDING_APPLICATION : RECORDING_APPLICATION_OF_OTHERS);

            recordingApplicationService.updateBasicInfo(recordingApplication);
            if (recordingApplication.getStage() == RecordingApplicationStage.TO_BE_REVISED.toInt()) {
                recordingFeedbackService.processAllByApplicationId(applicationId);
            }
            cmsLogAfterUpdate(oldRecordingApplication);
            return applicationId;
        }
    }

    private void cmsLogAfterUpdate(RecordingApplication oldRecordingApplication) {
        RecordingApplication newRecordingApplication = recordingApplicationService
                .getByIds(Collections.singletonList(oldRecordingApplication.getId()))
                .get(oldRecordingApplication.getId());
        if (newRecordingApplication == null) {
            log.error("recordingApplication is null after update! id = {}", oldRecordingApplication.getId());
            return;
        }

        String logStr = (oldRecordingApplication.getStage() == TO_BE_REVISED.toInt()) ?
                "修改录题申请并提交审核" : "修改录题申请";
        long applicationId = newRecordingApplication.getId();
        List<Change> changes = recordingApplicationDiffService.diff(oldRecordingApplication, newRecordingApplication);
        String changeStr = Joiner.on("，").skipNulls().join(changes);
        changeStr = StringUtils.isEmpty(changeStr) ? changeStr : "，" + changeStr;
        cmsLogService.log(applicationId, RECORDING_APPLICATION_ID, logStr + changeStr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(long id, RecordingApplicationCancelParamVO cancelParamVO) {
        String reasonDescription = cancelParamVO.getDescription();
        if (StringUtils.isBlank(reasonDescription)) {
            throw new BadRequestException("必须填写取消原因");
        }

        RecordingApplication application = recordingApplicationService.getByIds(Lists.newArrayList(id)).get(id);

        if (application == null) {
            throw new BadRequestException("录题申请不存在");
        }

        authorizeFeature(application.getSubjectId(), application.getPhaseId(),
                application.getCreator() == getUserId() ? RECORDING_APPLICATION : RECORDING_APPLICATION_OF_OTHERS);

        if (application.getStage() == RecordingApplicationStage.CANCELED.toInt()) {
            log.info("录题申请已经处于取消状态, 结束流程");
            return;
        }

        RecordingApplicationStage currentStage = RecordingApplicationStage.findByInt(application.getStage())
                .orElseThrow(() -> new BadRequestException("无法识别录题申请的当前状态"));

        boolean canceled = recordingApplicationService.updateStage(id, currentStage, RecordingApplicationStage.CANCELED);
        if (!canceled) {
            throw new ServerErrorException("取消录题申请失败, 请刷新重试");
        }
        // 可能没有录题任务，此时不抛出异常
        canceled = recordingTaskService.updateTaskStageByApplicationId(id, RecordingTaskStage.CANCELED.toInt());
        if (!canceled) {
            log.warn("取消录题任务失败, applicationId:{}", id);
        }

        RecordingFeedback feedback = new RecordingFeedback();
        feedback.setCreator(getUserId());
        feedback.setApplicationId(id);
        feedback.setTargetId(id);
        feedback.setTargetType(FeedbackTargetType.APPLICATION.toInt());
        feedback.setType(FeedbackType.APPLICATION_CANCELED.toInt());
        feedback.setDescription(reasonDescription);
        long feedbackId = recordingFeedbackService.create(feedback);
        if (feedbackId <= 0) {
            throw new ServerErrorException("取消录题申请时保存取消原因失败, 请刷新重试");
        }
        cmsLogService.log(id, RECORDING_APPLICATION_ID, String.format("取消录题申请，原因：%s", reasonDescription));
        recordingApplicationUpdateEventProducer.publish(id);
    }

    @Override
    public Map<Long, RecordingApplicationBriefVO> batchGetRecordingApplicationBriefInfo(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Maps.newHashMap();
        }

        Map<Long, RecordingApplication> applications = recordingApplicationService.getByIds(ids);
        Set<Integer> userIds = applications.values().stream().map(RecordingApplication::getCreator).collect(Collectors.toSet());
        Map<Integer, String> userIdToName = userService.getUserNames(userIds);
        Map<Long, List<QuestionRecordingTask>> applicationToQuestions = questionRecordingTaskService.getEffectiveRelationsByRecordingApplicationIds(ids);
        Map<Long, List<ExamPaperRecordingTask>> applicationToExamPapers = examPaperRecordingTaskService.getEffectiveRelationsByRecordingApplicationIds(ids);

        Map<Long, RecordingApplicationBriefVO> result = Maps.newHashMap();
        for (Long applicationId : ids) {
            RecordingApplication application = applications.get(applicationId);
            if (Objects.isNull(application)) {
                throw new BadRequestException(String.format("录题申请(%s)不存在,请确认!", applicationId));
            }

            RecordingApplicationBriefVO briefVO = wrapBriefInfo(application,
                    userIdToName.getOrDefault(application.getCreator(), String.valueOf(application.getCreator())),
                    applicationToQuestions.get(applicationId),
                    applicationToExamPapers.get(applicationId)
            );
            result.put(applicationId, briefVO);
        }
        return result;
    }

    @Override
    public Page<RecordingApplicationProgressVO> searchApplicationsConditionally(RecordingApplicationManageSearchParamVO searchParamVO) {
        RecordingApplicationSearchCondition searchCondition = wrap(searchParamVO);
        searchCondition.setAuthorizedSubjectPhases(getAuthorizedSubjectPhases(TableEnum.RECORDING_APPLICATION));
        Page<Long> pageIdResult = recordingApplicationSearchService.searchIdList(searchCondition);

        Page<RecordingApplicationProgressVO> pageResult = new Page<>(pageIdResult.getPageInfo(), Lists.newArrayList());
        if (CollectionUtils.isEmpty(pageIdResult.getList())) {
            return pageResult;
        }

        List<Long> ids = pageIdResult.getList();
        Map<Long, RecordingApplicationDetail> applicationDetails = recordingApplicationDetailService.getByIds(ids);
        Set<Integer> userIds = applicationDetails.values()
                .stream()
                .map(detail -> recordingApplicationDetailService.getAllUserIdsInvolvedWithApplication(detail))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Map<Integer, UserInfo> userInfoMap = userService.getUserInfo(userIds);

        List<RecordingApplicationProgressVO> resultRows = Lists.newArrayList();
        for (long applicationId : ids) {
            RecordingApplicationDetail applicationDetail = applicationDetails.get(applicationId);
            if (Objects.isNull(applicationDetail)) {
                throw new ServerErrorException("录题申请不存在, id = " + applicationId);
            }
            RecordingApplicationProgressVO progressVO = RecordingApplicationProgressWrapper.wrap(applicationDetail, userInfoMap);
            resultRows.add(progressVO);
        }
        pageResult.setList(resultRows);

        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishApplication(long applicationId) {
        RecordingApplication application = recordingApplicationService
                .getByIds(Collections.singletonList(applicationId))
                .get(applicationId);
        if (application == null) {
            throw new BadRequestException(String.format("请求的录题申请不存在！id = %d", applicationId));
        }

        authorizeFeature(application.getSubjectId(), application.getPhaseId(), RECORDING_APPLICATION);

        if (RecordingApplicationStage.PROCESSING.toInt() != application.getStage()) {
            throw new BadRequestException(String.format("要发布的录题申请状态 stage = %d 不正确！id = %d", application.getStage(), applicationId));
        }

        // 发布录题申请下各任务
        recordingTaskService.getByApplicationIds(Collections.singletonList(applicationId))
                .getOrDefault(applicationId, Lists.newArrayList())
                .forEach(task -> {
                    try {
                        log.info("publishTask taskId = {}", task.getId());
                        recordingTaskService.publishTask(task);
                    } catch (CheckFailedException e) {
                        throw new BadRequestException("发布录题申请时检查失败！原因：" + e.getMessage());
                    }
                });

        // 更新录题申请发布时间
        application.setPublishTime(System.currentTimeMillis());
        recordingApplicationService.updatePublishTime(application);

        // 更新录题申请发布状态
        if (!recordingApplicationService.updateStage(applicationId, PROCESSING, PUBLISHED)) {
            throw new BadRequestException(String.format("发布录题申请失败！id = %d", applicationId));
        }

        // 消息 & 日志
        recordingApplicationUpdateEventProducer.publish(applicationId);
        cmsLogService.log(applicationId, RECORDING_APPLICATION_ID, "发布录题申请");
    }

    @Override
    public List<RecorderVO> getRecorders(long applicationId) {
        Map<Long, RecordingApplication> recordingApplicationMap = recordingApplicationService.getByIds(Collections.singletonList(applicationId));
        if (MapUtils.isEmpty(recordingApplicationMap) || recordingApplicationMap.get(applicationId) == null) {
            throw new BadRequestException("录题需求不存在, applicationId=" + applicationId);
        }

        RecordingApplication application = recordingApplicationMap.get(applicationId);
        List<User> users = queryFeatureUsers(FeatureEnum.QUESTION_RECORDING, application.getPhaseId(), application.getSubjectId());
        return buildRecorderVOList(users);
    }

    @Override
    public List<AuditorVO> getAuditors(long applicationId) {
        Map<Long, RecordingApplication> recordingApplicationMap = recordingApplicationService.getByIds(Collections.singletonList(applicationId));
        if (MapUtils.isEmpty(recordingApplicationMap) || recordingApplicationMap.get(applicationId) == null) {
            throw new BadRequestException("录题需求不存在, applicationId=" + applicationId);
        }

        RecordingApplication application = recordingApplicationMap.get(applicationId);
        List<User> users = queryFeatureUsers(FeatureEnum.QUESTION_AUDIT, application.getPhaseId(), application.getSubjectId());
        return buildAuditorVOList(users);
    }

    private List<User> queryFeatureUsers(FeatureEnum feature, int phaseId, int subjectId) {
        QueryFeatureUsersRequest request = new QueryFeatureUsersRequest();
        request.setProjectKey(PatronusConstant.PROJECT_KEY);
        request.setFeatureKey(feature.getKey());
        request.setPhaseId(phaseId);
        request.setSubjectId(subjectId);
        QueryFeatureUsersResponse response = patronusProxy.queryFeatureUsers(request);
        if (response == null || CollectionUtils.isEmpty(response.getUsers())) {
            return new ArrayList<>();
        }

        return response.getUsers();
    }

    private List<RecorderVO> buildRecorderVOList(List<User> users) {
        if (CollectionUtils.isEmpty(users)) {
            return new ArrayList<>();
        }

        List<Integer> userIds = users.stream().map(user -> (int) user.getUserId()).collect(Collectors.toList());
        Map<Integer, UserInfo> userInfoMap = userService.getUserInfo(userIds);
        List<RecorderVO> result = new ArrayList<>();
        userIds.forEach(userId -> {
            UserInfo userInfo = userInfoMap.get(userId);
            if (userInfo == null) {
                log.error("查询不到用户信息, userId={}", userId);
                return;
            }

            RecorderVO recorderVO = new RecorderVO();
            recorderVO.setRecorderId(userId);
            recorderVO.setRecorderName(userInfo.getNickname());
            recorderVO.setRecorderPhone(userInfo.getPhone());
            result.add(recorderVO);
        });
        return result;
    }

    private List<AuditorVO> buildAuditorVOList(List<User> users) {
        if (CollectionUtils.isEmpty(users)) {
            return new ArrayList<>();
        }

        List<Integer> userIds = users.stream().map(user -> (int) user.getUserId()).collect(Collectors.toList());
        Map<Integer, UserInfo> userInfoMap = userService.getUserInfo(userIds);
        List<AuditorVO> result = new ArrayList<>();
        userIds.forEach(userId -> {
            UserInfo userInfo = userInfoMap.get(userId);
            if (userInfo == null) {
                log.error("查询不到用户信息, userId={}", userId);
                return;
            }

            AuditorVO auditorVO = new AuditorVO();
            auditorVO.setAuditorId(userId);
            auditorVO.setAuditorName(userInfo.getNickname());
            auditorVO.setAuditorPhone(userInfo.getPhone());
            result.add(auditorVO);
        });
        return result;
    }
}
