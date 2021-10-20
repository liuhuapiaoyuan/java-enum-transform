/**
 * @(#)RecordingFeedback.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.fenbi.commons.security.UserRole;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.bar.web.data.QuestionFeedbackCreateOrUpdateRequestVO;
import com.imcuttle.bar.web.data.QuestionFeedbackDeleteRequestVO;
import com.imcuttle.bar.web.data.RecordingFeedBackVO;
import com.imcuttle.bar.web.logic.RecordingFeedbackLogic;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.imcuttle.thrift.TutorArmoryConstants.LONG_ID_NO_LIMIT;

/**
 * @author linbonan
 */
@Slf4j
@RestController
@RequestMapping(value = TutorArmoryConstant.API + "/recording-feedbacks")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class RecordingFeedbackController {

    @Autowired
    private RecordingFeedbackLogic recordingFeedbackLogic;

    @ApiOperation("查询指定录题申请的反馈记录(按创建时间倒序)")
    @GetMapping("/applications/{applicationId}")
    public List<RecordingFeedBackVO> getFeedBacksOfSpecificApplication(@PathVariable long applicationId) {
        return recordingFeedbackLogic.getFeedBacksOfSpecificTarget(applicationId, FeedbackTargetType.APPLICATION);
    }

    @ApiOperation("查询指定录题任务某题的反馈记录(按创建时间倒序)")
    @GetMapping("/tasks/{taskId}/question/{questionId}")
    public List<RecordingFeedBackVO> getFeedBacksOfSpecificQuestion(
            @PathVariable long taskId,
            @PathVariable long questionId,
            @RequestParam(required = false, defaultValue = "0") long examPaperId
    ) {
        return recordingFeedbackLogic.getFeedBacksOfSpecificTargetOfTask(questionId, FeedbackTargetType.QUESTION, examPaperId, taskId);
    }

    @ApiOperation("查询指定录题任务某张试卷的反馈记录(按创建时间倒序)")
    @GetMapping("/tasks/{taskId}/examPaper/{examPaperId}")
    public List<RecordingFeedBackVO> getFeedBacksOfSpecificExamPaper(@PathVariable long taskId,
                                                                     @PathVariable long examPaperId) {
        return recordingFeedbackLogic.getFeedBacksOfSpecificTargetOfTask(examPaperId, FeedbackTargetType.EXAM_PAPER, LONG_ID_NO_LIMIT, taskId);
    }

    @ApiOperation("查询指定录题任务的反馈记录(按创建时间倒序)")
    @GetMapping("/tasks/{taskId}")
    public List<RecordingFeedBackVO> getFeedBacksOfSpecificTask(@PathVariable long taskId) {
        return recordingFeedbackLogic.getFeedBacksOfSpecificTargetOfTask(taskId, FeedbackTargetType.RECORDING_TASK, LONG_ID_NO_LIMIT, LONG_ID_NO_LIMIT);
    }

    @ApiOperation("标注阶段创建题目错题反馈")
    @PostMapping
    public long createOrUpdateWhenTag(@RequestBody QuestionFeedbackCreateOrUpdateRequestVO requestVO) {
        return recordingFeedbackLogic.createOrUpdateWhenTag(requestVO);
    }

    @ApiOperation("删除错题反馈")
    @DeleteMapping("/delete")
    public boolean deletedFeedback(@RequestParam long taskId,
                                   @RequestParam int questionId,
                                   @RequestParam long examPaperId) {
        QuestionFeedbackDeleteRequestVO vo = QuestionFeedbackDeleteRequestVO.builder()
                .taskId(taskId)
                .questionId(questionId)
                .examPaperId(examPaperId)
                .build();
        return recordingFeedbackLogic.deletedFeedback(vo);
    }
}
