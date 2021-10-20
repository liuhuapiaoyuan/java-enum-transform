/**
 * @(#)RecordingApplicationController.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.fenbi.commons.paging.Page;
import com.fenbi.commons.security.UserRole;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.imcuttle.bar.web.data.RecordingQuestionVO;
import com.imcuttle.bar.web.data.RecordingTagSearchParamVO;
import com.imcuttle.bar.web.data.RecordingTagVO;
import com.imcuttle.bar.web.data.TaggerUpdateRequestVO;
import com.imcuttle.bar.web.logic.RecordingTagLogic;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.imcuttle.bar.util.IdUtil.isDefaultId;

/**
 * @author linbonan
 */
@Slf4j
@RestController
@RequestMapping(value = TutorArmoryConstant.API + "/recording-tasks/tag")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class RecordingTagController {

    @Autowired
    private RecordingTagLogic recordingTagLogic;

    @ApiOperation("更新标注任务归属人 并返回更新后的标注任务")
    @PutMapping("/tagger")
    public RecordingTagVO updateTaggerAndGet(@RequestBody TaggerUpdateRequestVO requestVO) {
        return recordingTagLogic.updateTaggerAndGet(requestVO);
    }

    @ApiOperation("搜索题目标注任务")
    @GetMapping("/search")
    public Page<RecordingTagVO> searchRecordingTagTask(
            @ApiParam(value = "阶段") @RequestParam(defaultValue = "-1") int phaseId,
            @ApiParam(value = "科目") @RequestParam(defaultValue = "-1") int subjectId,
            @ApiParam(value = "题目来源") @RequestParam(defaultValue = "0") int questionSource,
            @ApiParam(value = "关键词") @RequestParam(defaultValue = "") String keyword,
            @ApiParam(value = "录题申请id") @RequestParam(defaultValue = "0") long id,
            @ApiParam(value = "标注人") @RequestParam(defaultValue = "") String tagger,
            @ApiParam(value = "标注进度") @RequestParam(defaultValue = "0") int tagProcess,
            @ApiParam(value = "申请起始时间") @RequestParam(defaultValue = "0") long submitStartTime,
            @ApiParam(value = "申请结束时间") @RequestParam(defaultValue = "0") long submitEndTime,
            @ApiParam(value = "申请人") @RequestParam(defaultValue = "") String creator,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @ApiParam(value = "排序规则 取值：submitTimeAsc submitTimeDesc")
            @RequestParam(value = "sort", defaultValue = "") String sortBy) {
        RecordingTagSearchParamVO param = new RecordingTagSearchParamVO();
        param.setPhaseId(phaseId);
        param.setSubjectId(subjectId);
        param.setQuestionSource(questionSource);
        param.setKeyword(keyword);
        if (!isDefaultId(id)) {
            param.setId(id);
        }
        param.setTaggerName(tagger);
        param.setTagProcess(tagProcess);
        param.setSubmitStartTime(submitStartTime);
        param.setSubmitEndTime(submitEndTime);
        param.setPage(page);
        param.setPageSize(pageSize);
        if (StringUtils.isNotEmpty(sortBy)) {
            param.setSort(sortBy);
        }

        param.setCreatorName(creator);
        param.setTaggerName(tagger);
        param.setTagProcess(tagProcess);

        return recordingTagLogic.searchRecordingTagTask(param);
    }

    @ApiOperation("查询标注任务题目信息")
    @GetMapping("/questions/{applicationId}")
    public List<RecordingQuestionVO> getQuestionTagInfos(@PathVariable("applicationId") long applicationId) {
        return recordingTagLogic.getQuestionTagInfos(applicationId);
    }

    @ApiOperation("单个查询标注任务题目信息")
    @GetMapping("/questionTagInfo")
    public RecordingQuestionVO getQuestionTagInfos(@RequestParam long taskId,
                                                   @RequestParam int questionId,
                                                   @RequestParam long examPaperId) {
        return recordingTagLogic.getQuestionTagInfo(taskId, questionId, examPaperId);
    }

    @ApiOperation("将标注任务打回录题员")
    @PutMapping("/reject/{taskId}")
    public void rejectTagTask(@PathVariable long taskId) {
        recordingTagLogic.rejectTagTask(taskId);
    }
}
