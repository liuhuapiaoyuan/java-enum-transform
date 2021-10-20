/**
 * @(#)RecordingApplicationLogicImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.commons.paging.Page;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.FeedbackType;
import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.bar.data.RecordingApplicationAuditResult;
import com.imcuttle.bar.data.RecordingApplicationSearchCondition;
import com.imcuttle.bar.data.SubjectPhasePair;
import com.imcuttle.bar.enums.TableEnum;
import com.imcuttle.bar.service.CmsLogService;
import com.imcuttle.bar.service.RecordingApplicationAuditService;
import com.imcuttle.bar.service.RecordingApplicationSearchService;
import com.imcuttle.bar.service.RecordingApplicationService;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.util.RecordingApplicationStageUtil;
import com.imcuttle.bar.web.data.RecordingApplicationAuditResultVO;
import com.imcuttle.bar.web.data.RecordingApplicationAuditSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationAuditVO;
import com.imcuttle.bar.web.logic.RecordingApplicationAuditLogic;
import com.imcuttle.bar.web.wrapper.RecordingApplicationAuditResultWrapper;
import com.imcuttle.bar.web.wrapper.RecordingApplicationAuditSearchConditionWrapper;
import com.imcuttle.thrift.RecordingApplication;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.imcuttle.bar.enums.FeatureEnum.RECORDING_APPLICATION_AUDIT;
import static com.imcuttle.bar.web.wrapper.RecordingApplicationAuditWrapper.wrap2VO;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.RECORDING_APPLICATION_ID;

/**
 * @author lukebj
 */
@Slf4j
@Service
public class RecordingApplicationAuditLogicImpl extends BaseLogic implements RecordingApplicationAuditLogic {

    @Autowired
    private CmsLogService cmsLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecordingApplicationService recordingApplicationService;

    @Autowired
    private RecordingApplicationAuditService recordingApplicationAuditService;

    @Autowired
    private RecordingApplicationSearchService recordingApplicationSearchService;

    @Override
    public Page<RecordingApplicationAuditVO> searchSubmittedApplications(RecordingApplicationAuditSearchParamVO searchParamVO) {
        RecordingApplicationSearchCondition searchCondition = RecordingApplicationAuditSearchConditionWrapper.wrap(searchParamVO);
        List<SubjectPhasePair> authorizedSubjectPhases = getAuthorizedSubjectPhases(TableEnum.RECORDING_APPLICATION);
        searchCondition.setAuthorizedSubjectPhases(authorizedSubjectPhases);
        Page<Long> pageIdResult = recordingApplicationSearchService.searchIdList(searchCondition);

        Page<RecordingApplicationAuditVO> pageResult = new Page<>(pageIdResult.getPageInfo(), Lists.newArrayList());
        if (CollectionUtils.isEmpty(pageIdResult.getList())) {
            return pageResult;
        }

        List<Long> ids = pageIdResult.getList();
        Map<Long, RecordingApplication> recordingApplications = recordingApplicationService.getByIds(ids);
        List<Integer> creatorList = recordingApplications.values().stream().map(RecordingApplication::getCreator).collect(Collectors.toList());
        Map<Integer, String> creatorNameMap = userService.getUserNames(creatorList);
        List<RecordingApplicationAuditVO> basicVOS = ids.stream()
                .filter(recordingApplications::containsKey)
                .map(recordingApplications::get)
                .map(application -> wrap2VO(application, creatorNameMap.getOrDefault(application.getCreator(), "" + application.getCreator())))
                .collect(Collectors.toList());
        pageResult.setList(basicVOS);

        return pageResult;
    }

    @Override
    public void audit(RecordingApplicationAuditResultVO auditResultVO, int auditorId) {
        validAuditResult(auditResultVO);

        Map<Long, RecordingApplication> applicationMap = recordingApplicationService.getByIds(Collections.singletonList(auditResultVO.getRecordingApplicationId()));
        if (MapUtils.isEmpty(applicationMap) || !applicationMap.containsKey(auditResultVO.getRecordingApplicationId())) {
            throw new BadRequestException(String.format("录题申请(%s)不存在,请确认!", auditResultVO.getRecordingApplicationId()));
        }
        RecordingApplication recordingApplication = applicationMap.get(auditResultVO.getRecordingApplicationId());
        authorizeFeature(recordingApplication.getSubjectId(), recordingApplication.getPhaseId(), RECORDING_APPLICATION_AUDIT);
        if (recordingApplication.getStage() != RecordingApplicationStage.SUBMITTED.toInt()) {
            throw new BadRequestException(String.format("录题申请(%s)状态(%s)非待审核状态,请确认!",
                    auditResultVO.getRecordingApplicationId(), recordingApplication.getStage()));
        }
        RecordingApplicationAuditResult auditResult = RecordingApplicationAuditResultWrapper.wrap(auditResultVO, auditorId);
        recordingApplicationAuditService.audit(auditResult);
        String message = "审核录题申请，审核结果为：" +
                formatApplicationAuditResult(auditResult.getFeedbackType()) +
                (StringUtils.isBlank(auditResult.getDescription()) ? "" : "，原因：" + auditResult.getDescription());
        cmsLogService.log(auditResultVO.getRecordingApplicationId(), RECORDING_APPLICATION_ID, message);
    }

    private String formatApplicationAuditResult(int feedbackType) {
        return FeedbackType.findByInt(feedbackType).map(type -> {
            switch (type) {
                case APPLICATION_NEED_REVISED:
                    return "驳回待修改";
                case APPLICATION_REJECTED:
                    return "不通过";
                case APPLICATION_APPROVED:
                    return "通过";
                default:
                    return "";
            }
        }).orElse("");
    }

    private void validAuditResult(RecordingApplicationAuditResultVO auditResultVO) {
        if (auditResultVO == null) {
            throw new BadRequestException("审核结果不能为空!");
        }
        Optional<FeedbackType> feedbackTypeOptional = FeedbackType.findByInt(auditResultVO.getFeedbackType());
        if (!feedbackTypeOptional.isPresent()) {
            throw new BadRequestException(String.format("审核结果(%s)不存在，请确认!", auditResultVO.getFeedbackType()));
        }
        FeedbackType feedbackType = feedbackTypeOptional.get();
        if (feedbackType.getRequiredTargetType() != FeedbackTargetType.APPLICATION.toInt()) {
            throw new BadRequestException(String.format("审核结果(%s)非录题申请类型，请确认!", auditResultVO.getFeedbackType()));
        }
        RecordingApplicationStage stage = RecordingApplicationStageUtil.getStageByFeedbackType(auditResultVO.getFeedbackType());
        if (stage == null) {
            throw new BadRequestException(String.format("审核结果(%s)无对应等录题审核进度，请确认!", auditResultVO.getFeedbackType()));
        }

        // 驳回待修改或不通过,需要对原因做必填校验
        if (needReason(feedbackType) && StringUtils.isBlank(auditResultVO.getDescription())) {
            throw new BadRequestException(String.format("审核结果(%s)原因不能为空!", auditResultVO.getFeedbackType()));
        }
    }

    private boolean needReason(FeedbackType feedbackType) {
        return feedbackType == FeedbackType.APPLICATION_NEED_REVISED || feedbackType == FeedbackType.APPLICATION_REJECTED;
    }
}
