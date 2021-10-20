/**
 * @(#)QuestionVerifyController.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.fenbi.commons.paging.Page;
import com.fenbi.commons.security.UserRole;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.imcuttle.bar.web.data.RecordingAuditResourceDetailVO;
import com.imcuttle.bar.web.data.RecordingAuditResourceListVO;
import com.imcuttle.bar.web.data.RecordingTaskAuditVO;
import com.imcuttle.bar.web.data.SaveAuditTaskRequestVO;
import com.imcuttle.bar.web.data.SubmitAuditTaskRequestVO;
import com.imcuttle.bar.web.logic.RecordTaskAuditLogic;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenkangbj
 */
@RestController
@RequestMapping(TutorArmoryConstant.API + "/recording-tasks/audit")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class RecordingTaskAuditController {

    @Autowired
    private RecordTaskAuditLogic recordTaskAuditLogic;

    @GetMapping("/my-tasks")
    @ApiOperation("查询状态为待审核的 我的录题任务")
    public Page<RecordingTaskAuditVO> getMyAuditTask(@RequestParam(name = "taskId", required = false, defaultValue = "0") long taskId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
        return recordTaskAuditLogic.getMyAuditTask(taskId, page, pageSize);
    }

    @GetMapping("/my-rejected-tasks")
    @ApiOperation("查询状态为待修改错题的 我的录题任务")
    public Page<RecordingTaskAuditVO> getMyRejectedTask(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return recordTaskAuditLogic.getMyRejectedTask(page, pageSize);
    }

    @PostMapping("/{taskId}/submit")
    @ApiOperation("提交审核完毕的录题任务")
    public void submitAuditTask(@PathVariable("taskId") long taskId,
                                @RequestBody SubmitAuditTaskRequestVO requestVO) {
        recordTaskAuditLogic.submitAuditTask(taskId, requestVO);
    }

    @PostMapping("/save-audit-task")
    @ApiOperation("保存指定录题任务下 某道题目/试卷的审核状态")
    public void saveAuditTask(@RequestBody SaveAuditTaskRequestVO requestVO) {
        recordTaskAuditLogic.saveAuditTask(requestVO);
    }

    @GetMapping("/{taskId}/resources")
    @ApiOperation("获取审核任务下资源列表")
    public RecordingAuditResourceListVO getResourceList(@PathVariable long taskId) {
        // checkAuth (userId, taskId)
        return recordTaskAuditLogic.getResourceList(taskId);
    }

    @GetMapping("/{taskId}/single-resource")
    @ApiOperation("获取审核任务下 单个题 / 试卷的审核状态")
    public RecordingAuditResourceDetailVO getResourceDetail(
            @ApiParam("任务id") @PathVariable long taskId,
            @ApiParam("试卷id") @RequestParam(required = false, defaultValue = "0") long examPaperId,
            @ApiParam("目标id") @RequestParam long targetId,
            @ApiParam("目标类型 2 题目 3 试卷") @RequestParam int targetType
    ) {
        return recordTaskAuditLogic.getResource(taskId, examPaperId, targetId, targetType);
    }
}
