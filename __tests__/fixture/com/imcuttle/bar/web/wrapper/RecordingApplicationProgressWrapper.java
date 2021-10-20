/**
 * @(#)RecordingApplicationProgressWrapper.java, 4月 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.bar.util.UserUtil;
import com.imcuttle.bar.web.data.RecordingApplicationProgressVO;
import com.imcuttle.bar.web.data.RecordingTaskVO;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingApplicationDetail;
import com.imcuttle.thrift.RecordingTask;
import com.fenbi.tutor.crmaccount.thrift.UserInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.imcuttle.bar.util.UserUtil.formatToName;

/**
 * @author linbonan
 */
public class RecordingApplicationProgressWrapper {

    public static RecordingApplicationProgressVO wrap(RecordingApplicationDetail applicationDetail, Map<Integer, UserInfo> userInfoMap) {
        if (Objects.isNull(applicationDetail)) {
            return null;
        }

        RecordingApplicationProgressVO progressVO = new RecordingApplicationProgressVO();
        // 基础属性
        RecordingApplication application = Preconditions.checkNotNull(applicationDetail.getApplication());
        BeanUtils.copyProperties(application, progressVO);
        if (MapUtils.isNotEmpty(userInfoMap)) {
            String creatorName = Optional.ofNullable(userInfoMap.get(application.getCreator()))
                    .map(UserUtil::formatToName)
                    .orElse(String.valueOf(application.getCreator()));
            progressVO.setCreatorName(creatorName);
        }

        // 进度属性
        int recordedQuestionCount = (int) Optional.ofNullable(applicationDetail.getContainedQuestions())
                .orElse(Lists.newArrayList())
                .stream()
                .map(QuestionRecordingTask::getQuestionId)
                .count();
        progressVO.setRecordedQuestionCount(recordedQuestionCount);

        int recordedExamPaperCount = (int) Optional.ofNullable(applicationDetail.getContainedExamPapers())
                .orElse(Lists.newArrayList())
                .stream()
                .map(ExamPaperRecordingTask::getExamPaperId)
                .count();
        progressVO.setRecordedExamPaperCount(recordedExamPaperCount);

        int maxTaskSubmitTimes = Optional.ofNullable(applicationDetail.getTaskList())
                .orElse(Lists.newArrayList())
                .stream()
                .map(RecordingTask::getSubmitTimes)
                .max(Integer::compareTo)
                .orElse(0);
        progressVO.setTaskSubmitTimes(maxTaskSubmitTimes);

        if (RecordingApplicationStage.PUBLISHED.toInt() == application.getStage()) {
            progressVO.setCompleteDurationInMilliseconds(application.getPublishTime() - application.getSubmitTime());
        }

        // 该申请下的任务
        progressVO.setTasks(wrapTasks(applicationDetail, userInfoMap));

        // 「应录题数」有特殊的逻辑：如果当前录题申请已经被分配出任务, 则以录题任务的应录题数代替创建申请时填写的数值, 作为该申请的应录题数
        Optional.ofNullable(applicationDetail.getTaskList())
                .orElse(Lists.newArrayList())
                .stream()
                .map(RecordingTask::getEstimatedQuestionNum)
                .reduce(Integer::sum)
                .ifPresent(progressVO::setEstimatedQuestionNum);

        return progressVO;
    }

    private static List<RecordingTaskVO> wrapTasks(RecordingApplicationDetail applicationDetail, Map<Integer, UserInfo> userInfoMap) {
        Map<Long, RecordingApplication> taskIdToApplication = Maps.newHashMap();
        Optional.ofNullable(applicationDetail.getTaskList())
                .orElse(Lists.newArrayList())
                .forEach(task -> taskIdToApplication.put(task.getId(), applicationDetail.getApplication()));

        Map<Long, List<QuestionRecordingTask>> questionGroupByTask = Optional.ofNullable(applicationDetail.getContainedQuestions())
                .orElse(Lists.newArrayList())
                .stream()
                .collect(Collectors.groupingBy(QuestionRecordingTask::getTaskId));

        Map<Long, List<ExamPaperRecordingTask>> examPaperGroupByTask = Optional.ofNullable(applicationDetail.getContainedExamPapers())
                .orElse(Lists.newArrayList())
                .stream()
                .collect(Collectors.groupingBy(ExamPaperRecordingTask::getTaskId));

        return RecordingTaskVOWrapper.wrap(applicationDetail.getTaskList(), taskIdToApplication, userInfoMap, questionGroupByTask, examPaperGroupByTask, Maps.newHashMap());
    }

    private RecordingApplicationProgressWrapper() {
    }
}
