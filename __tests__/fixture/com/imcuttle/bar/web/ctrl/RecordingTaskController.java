/**
 * @(#)RecordingTaskController.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.fenbi.commons.paging.Page;
import com.fenbi.commons.security.UserRole;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.enums.RecordingTaskRoleEnum;
import com.imcuttle.bar.web.data.RecordingTaskCreateRequestVO;
import com.imcuttle.bar.web.data.RecordingTaskExamPaperQuestionsVO;
import com.imcuttle.bar.web.data.RecordingTaskUpdateRequestVO;
import com.imcuttle.bar.web.data.RecordingTaskVO;
import com.imcuttle.bar.web.logic.RecordingTaskLogic;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author xiechao01
 */
@RestController
@Slf4j
@RequestMapping(value = TutorArmoryConstant.API + "/recording-tasks")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class RecordingTaskController {

    @Autowired
    private RecordingTaskLogic recordingTaskLogic;

    @ApiOperation("创建录题任务")
    @PostMapping
    public long create(@RequestBody RecordingTaskCreateRequestVO requestVO) {
        return recordingTaskLogic.create(requestVO);
    }

    @ApiOperation("修改录题任务")
    @PutMapping
    public boolean update(@RequestBody RecordingTaskUpdateRequestVO requestVO) {
        return recordingTaskLogic.update(requestVO);
    }

    @ApiOperation("查询指定角色录题任务数量接口")
    @GetMapping("/task-count")
    public Map<Integer, Integer> getRecordingTasks(@RequestParam @ApiParam("recorder 录题员，auditor 审核员") String taskRole) {
        Optional<RecordingTaskRoleEnum> roleEnumOptional = RecordingTaskRoleEnum.findByString(taskRole);
        if (!roleEnumOptional.isPresent()) {
            throw new BadRequestException("taskRole 不合法");
        }
        return recordingTaskLogic.getRecordingTasksByRole(roleEnumOptional.get());
    }

    @ApiOperation("查询录题任务列表")
    @GetMapping("/recording-task-list")
    public Page<RecordingTaskVO> getRecordingTaskByStatus(@RequestParam @ApiParam("1 录题中，3 驳回待修改") int taskStage,
                                                          @RequestParam(defaultValue = "0", required = false) int page,
                                                          @RequestParam(defaultValue = "20", required = false) int pageSize) {
        Optional<RecordingTaskStage> stageOptional = RecordingTaskStage.findByInt(taskStage);
        if (!stageOptional.isPresent()) {
            throw new BadRequestException("taskStage 不合法");
        }
        return recordingTaskLogic.getRecordingTaskByStatus(stageOptional.get(), page, pageSize);
    }

    @ApiOperation("查询录题任务")
    @GetMapping("/{taskId}")
    public RecordingTaskVO getRecordingTasks(@PathVariable long taskId) {
        return recordingTaskLogic.getRecordingTask(taskId);
    }

    @ApiOperation("提交录题任务")
    @PutMapping("/submit-recording-task/{taskId}")
    public boolean submitRecordingTask(@PathVariable("taskId") long taskId) {
        return recordingTaskLogic.submit(taskId);
    }

    @ApiOperation("查询录题任务下试卷题目信息")
    @GetMapping("/examPaperQuestions/{taskId}")
    public RecordingTaskExamPaperQuestionsVO getRecordingTaskExamPaperQuestions(@PathVariable("taskId") long taskId) {
        return recordingTaskLogic.getRecordingTaskExamPaperQuestions(taskId);
    }

    @ApiOperation("查询录题申请下的录题任务列表")
    @GetMapping("/by-application/{applicationId}")
    public List<RecordingTaskVO> getRecordingTasksOfApplication(@PathVariable("applicationId") long applicationId) {
        return recordingTaskLogic.getRecordingTasksByApplicationId(applicationId);
    }
}
