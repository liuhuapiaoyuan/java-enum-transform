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
import com.imcuttle.bar.web.data.RecordingApplicationAuditResultVO;
import com.imcuttle.bar.web.data.RecordingApplicationAuditSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationAuditVO;
import com.imcuttle.bar.web.logic.RecordingApplicationAuditLogic;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fenbi.commons.security.SecurityHelper.getUserId;

/**
 * @author lukebj
 */
@Slf4j
@RestController
@RequestMapping(value = TutorArmoryConstant.API + "/recording-applications/audit")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class RecordingApplicationAuditController {

    @Autowired
    private RecordingApplicationAuditLogic recordingApplicationAuditLogic;

    @ApiOperation("搜索待审核录题申请")
    @PostMapping("/search")
    public Page<RecordingApplicationAuditVO> searchSubmittedApplications(@RequestBody RecordingApplicationAuditSearchParamVO searchParamVO) {
       return recordingApplicationAuditLogic.searchSubmittedApplications(searchParamVO);
    }

    @ApiOperation("审核")
    @PostMapping
    public void audit(@RequestBody RecordingApplicationAuditResultVO auditResultVO) {
        recordingApplicationAuditLogic.audit(auditResultVO, getUserId());
    }
}
