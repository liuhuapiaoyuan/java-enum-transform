/**
 * @(#)RecordingTaskVOWrapper.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.enums.ExamPaperRecordingTaskStage;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.util.UserUtil;
import com.imcuttle.bar.web.data.AuditorVO;
import com.imcuttle.bar.web.data.RecorderVO;
import com.imcuttle.bar.web.data.RecordingTaskVO;
import com.imcuttle.bar.web.data.TaggerVO;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingFeedback;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.utils.RecordingStageUtil;
import com.fenbi.tutor.crmaccount.thrift.UserInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author xiechao01
 */
public class RecordingTaskVOWrapper {

    private static final String AUDIT_STAGE_FEEDBACK = "审核阶段纠错";

    private static final String TAG_STAGE_FEEDBACK = "标注阶段纠错";

    public static RecordingTaskVO wrap(RecordingTask recordingTask,
                                       RecordingApplication recordingApplication,
                                       Map<Integer, UserInfo> userInfoMap,
                                       List<QuestionRecordingTask> effectiveQuestionRecordingTasks,
                                       List<ExamPaperRecordingTask> effectiveExamPaperQuestionRecordingTasks,
                                       int examPaperFeedbackCount,
                                       int taskFeedbackCount) {
        RecordingTaskVO vo = new RecordingTaskVO();
        vo.setTaskId(recordingTask.getId());
        vo.setPhaseId(recordingTask.getPhaseId());
        vo.setSubjectId(recordingTask.getSubjectId());
        vo.setStage(recordingTask.getStage());
        vo.setEstimatedQuestionNum(recordingTask.getEstimatedQuestionNum());
        vo.setRecordingMode(recordingTask.getRecordingMode());
        vo.setApplicationId(recordingTask.getApplicationId());
        vo.setSubmitTimes(recordingTask.getSubmitTimes());

        RecorderVO recorderVO = new RecorderVO();
        int recorderId = recordingTask.getRecorder();
        recorderVO.setRecorderId(recorderId);
        Optional.ofNullable(userInfoMap.get(recorderId))
                .ifPresent(userInfo -> {
                    recorderVO.setRecorderName(userInfo.getNickname());
                    recorderVO.setRecorderPhone(userInfo.getPhone());
                });
        vo.setRecorder(recorderVO);

        AuditorVO auditorVO = new AuditorVO();
        int auditorId = recordingTask.getAuditor();
        auditorVO.setAuditorId(recordingTask.getAuditor());
        Optional.ofNullable(userInfoMap.get(auditorId))
                .ifPresent(userInfo -> {
                    auditorVO.setAuditorName(userInfo.getNickname());
                    auditorVO.setAuditorPhone(userInfo.getPhone());
                });
        vo.setAuditor(auditorVO);

        TaggerVO taggerVO = new TaggerVO();
        int taggerId = recordingTask.getTagger();
        taggerVO.setTaggerId(recordingTask.getTagger());
        Optional.ofNullable(userInfoMap.get(taggerId))
                .ifPresent(userInfo -> {
                    taggerVO.setTaggerName(userInfo.getNickname());
                    taggerVO.setTaggerPhone(userInfo.getPhone());
                });
        vo.setTagger(taggerVO);

        vo.setName(recordingApplication.getName());
        vo.setQuestionSource(recordingApplication.getQuestionSource());
        int creatorId = recordingApplication.getCreator();
        String creator = Optional.ofNullable(userInfoMap.get(creatorId))
                .map(UserUtil::formatToName)
                .orElse(String.valueOf(creatorId));
        vo.setCreator(creator);
        vo.setSubmitTime(recordingApplication.getSubmitTime());

        int questionNum = effectiveQuestionRecordingTasks.size();
        int examPaperNum = effectiveExamPaperQuestionRecordingTasks.size();
        int auditProcessedQuestionNum = 0;
        int tagProcessedQuestionNum = 0;
        int auditProcessedExamPaperNum = 0;
        auditProcessedQuestionNum = (int) effectiveQuestionRecordingTasks.stream().filter(RecordingStageUtil::hasBeenProcessedByAuditor).count();
        tagProcessedQuestionNum = (int) effectiveQuestionRecordingTasks.stream().filter(RecordingStageUtil::hasBeenProcessedByTagger).count();
        auditProcessedExamPaperNum = (int) effectiveExamPaperQuestionRecordingTasks.stream().filter(RecordingStageUtil::hasBeenProcessedByAuditor).count();

        vo.setQuestionNum(questionNum);
        vo.setExamPaperNum(examPaperNum);
        vo.setAuditProcessedQuestionNum(auditProcessedQuestionNum);
        vo.setTagProcessedQuestionNum(tagProcessedQuestionNum);
        vo.setAuditProcessedExamPaperNum(auditProcessedExamPaperNum);
        vo.setFeedbackStageName(StringUtils.EMPTY);
        vo.setFeedbackType2Count(new HashMap<>());

        if (RecordingTaskStage.TO_BE_REVISED.toInt() == recordingTask.getStage()
                || RecordingTaskStage.AUDITED.toInt() == recordingTask.getStage()) {
            //题目反馈
            List<QuestionRecordingTask> questionFeedbacks = new ArrayList<>(effectiveQuestionRecordingTasks.stream()
                    .filter(task -> task.getStage() == QuestionRecordingTaskStage.TAG_FAILED.toInt() ||
                            task.getStage() == QuestionRecordingTaskStage.AUDIT_FAILED.toInt())
                    .collect(Collectors.toList()));

            int questionFeedbackCount = questionFeedbacks.size();
            if (questionFeedbacks.stream().allMatch(task -> task.getStage() == QuestionRecordingTaskStage.AUDIT_FAILED.toInt())) {
                vo.setFeedbackStageName(AUDIT_STAGE_FEEDBACK);
            } else {
                vo.setFeedbackStageName(TAG_STAGE_FEEDBACK);
            }
            vo.setFeedbackType2Count(ImmutableMap.of(FeedbackTargetType.QUESTION.toInt(), questionFeedbackCount,
                    FeedbackTargetType.EXAM_PAPER.toInt(), examPaperFeedbackCount,
                    FeedbackTargetType.RECORDING_TASK.toInt(), taskFeedbackCount));
        }

        return vo;
    }

    public static List<RecordingTaskVO> wrap(List<RecordingTask> recordingTaskList,
                                             Map<Long, RecordingApplication> taskId2Application,
                                             Map<Integer, UserInfo> userInfoMap,
                                             Map<Long, List<QuestionRecordingTask>> effectiveQuestionRelationsByTaskIds,
                                             Map<Long, List<ExamPaperRecordingTask>> effectiveExamPaperRelationsByTaskIds,
                                             Map<Long, List<RecordingFeedback>> unprocessedRecordingTaskFeedback) {
        List<RecordingTaskVO> vos = new ArrayList<>();
        if (Objects.isNull(recordingTaskList)) {
            return vos;
        }
        recordingTaskList.forEach(recordingTask -> {
            long taskId = recordingTask.getId();
            RecordingApplication recordingApplication = taskId2Application.get(taskId);
            List<QuestionRecordingTask> questionRecordingTasks = effectiveQuestionRelationsByTaskIds.getOrDefault(taskId, Lists.newArrayList());
            List<ExamPaperRecordingTask> examPaperRecordingTasks = effectiveExamPaperRelationsByTaskIds.getOrDefault(taskId, Lists.newArrayList());

            //试卷反馈
            int examPaperFeedbackCount = (int) effectiveExamPaperRelationsByTaskIds
                    .getOrDefault(taskId, Lists.newArrayList())
                    .stream()
                    .filter(relation -> ExamPaperRecordingTaskStage.AUDIT_FAILED.toInt() == relation.getStage())
                    .count();

            //其他反馈
            int taskFeedbackCount = unprocessedRecordingTaskFeedback.getOrDefault(taskId, Lists.newArrayList()).size();

            RecordingTaskVO vo = wrap(recordingTask, recordingApplication, userInfoMap, questionRecordingTasks, examPaperRecordingTasks, examPaperFeedbackCount, taskFeedbackCount);
            vos.add(vo);
        });
        return vos;
    }
}
