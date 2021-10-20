/**
 * @(#)RecordingApplicationController.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import com.fenbi.commons.paging.Page;
import com.fenbi.commons.security.UserRole;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.imcuttle.bar.enums.SortTypeEnum;
import com.imcuttle.bar.web.data.AuditorVO;
import com.imcuttle.bar.web.data.RecorderVO;
import com.imcuttle.bar.web.data.RecordingApplicationBasicSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationBasicVO;
import com.imcuttle.bar.web.data.RecordingApplicationBriefVO;
import com.imcuttle.bar.web.data.RecordingApplicationCancelParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationManageSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationProgressVO;
import com.imcuttle.bar.web.logic.RecordingApplicationLogic;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
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

import static com.fenbi.commons.security.SecurityHelper.getUserId;

/**
 * @author linbonan
 */
@Slf4j
@RestController
@RequestMapping(value = TutorArmoryConstant.API + "/recording-applications")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class RecordingApplicationController {

    @Autowired
    private RecordingApplicationLogic recordingApplicationLogic;

    @ApiOperation("我的录题申请")
    @PostMapping("/my-applications")
    public Page<RecordingApplicationBasicVO> searchApplicationsOfCurrentUser(@RequestBody RecordingApplicationBasicSearchParamVO searchParamVO) {
        int currentUser = getUserId();
        searchParamVO.setCreator(currentUser);
        if (StringUtils.isBlank(searchParamVO.getSort())) {
            searchParamVO.setSort(SortTypeEnum.SUBMIT_TIME_DESC.toString());
        }
        return recordingApplicationLogic.searchBasicInfoList(searchParamVO);
    }

    @ApiOperation("取消录题申请")
    @PutMapping("/{applicationId}/cancel")
    public void cancelApplicationById(@PathVariable long applicationId, @RequestBody RecordingApplicationCancelParamVO cancelParamVO) {
        recordingApplicationLogic.cancel(applicationId, cancelParamVO);
    }

    @ApiOperation("创建录题申请")
    @PostMapping
    public long saveApplication(@RequestBody RecordingApplicationBasicVO applicationBasicVO) {
        return recordingApplicationLogic.save(applicationBasicVO);
    }

    @ApiOperation("获取录题申请概况信息")
    @GetMapping("/{id}")
    public RecordingApplicationBriefVO getRecordingApplicationBasicInfo(@PathVariable long id) {
        return recordingApplicationLogic
                .batchGetRecordingApplicationBriefInfo(Lists.newArrayList(id))
                .get(id);
    }

    @ApiOperation("批量获取录题申请概况信息(包含题目和试卷数量)")
    @GetMapping("/batch")
    public Map<Long, RecordingApplicationBriefVO> getRecordingApplicationBasicInfo(@RequestParam List<Long> ids) {
        return recordingApplicationLogic.batchGetRecordingApplicationBriefInfo(ids);
    }

    @ApiOperation("全部录题申请列表")
    @PostMapping("/manage/search")
    public Page<RecordingApplicationProgressVO> searchApplicationsConditionally(@RequestBody RecordingApplicationManageSearchParamVO searchParamVO) {
        if (StringUtils.isBlank(searchParamVO.getSort())) {
            searchParamVO.setSort(SortTypeEnum.SUBMIT_TIME_DESC.toString());
        }
        return recordingApplicationLogic.searchApplicationsConditionally(searchParamVO);
    }

    @ApiOperation("查询录题申请可选的录题人列表")
    @GetMapping("/{applicationId}/recorders")
    public List<RecorderVO> getRecorder(@PathVariable long applicationId) {
        return recordingApplicationLogic.getRecorders(applicationId);
    }

    @ApiOperation("查询录题申请可选的审核人列表")
    @GetMapping("/{applicationId}/auditors")
    public List<AuditorVO> getAuditors(@PathVariable long applicationId) {
        return recordingApplicationLogic.getAuditors(applicationId);
    }

    @ApiOperation("发布录题申请")
    @PostMapping("/{applicationId}/publish")
    public void publishApplication(@PathVariable long applicationId) {
        recordingApplicationLogic.publishApplication(applicationId);
    }
}
