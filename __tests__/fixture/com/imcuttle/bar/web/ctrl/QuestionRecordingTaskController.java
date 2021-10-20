/**
 * @(#)QuestionRecordingTaskController.java, 4月 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.fenbi.commons.security.UserRole;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.imcuttle.bar.web.data.BindBatchQuestionRequest;
import com.imcuttle.bar.web.data.BindSingleQuestionRequest;
import com.imcuttle.bar.web.logic.QuestionRecordingTaskLogic;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chenyibo
 */
@RestController
@RequestMapping(value = TutorArmoryConstant.API + "/questionRecordingTask")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class QuestionRecordingTaskController {

    @Autowired
    private QuestionRecordingTaskLogic questionRecordingTaskLogic;

    @ApiOperation("将已有题目加入录题任务")
    @PostMapping("/bindSingleQuestion")
    public boolean bindSingleQuestion2Task(@RequestBody BindSingleQuestionRequest request) {
        return questionRecordingTaskLogic.bindSingleQuestion2RecordingTask(request);
    }

    @ApiOperation("将已有散题移出录题任务")
    @DeleteMapping("/{taskId}/question/{questionId}")
    public boolean deleteSingleQuestionFromTask(@PathVariable long taskId, @PathVariable int questionId) {
        return questionRecordingTaskLogic.removeQuestionFromTask(taskId, questionId);
    }

    @ApiOperation("调整录题任务里的题目顺序")
    @PutMapping("/{taskId}/questions/ordinal")
    public boolean updateQuestionOrdinalOfTask(@PathVariable long taskId, @RequestBody List<Integer> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return true;
        }
        return questionRecordingTaskLogic.reorderQuestions(taskId, questionIds);
    }

    @ApiOperation("批量将已有题目加入录题任务")
    @PostMapping("/questions/bind")
    public boolean bindBatchQuestion2Task(@RequestBody BindBatchQuestionRequest request) {
        return questionRecordingTaskLogic.bindBatchQuestion2RecordingTask(request);
    }
}
