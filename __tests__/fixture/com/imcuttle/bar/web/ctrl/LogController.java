/**
 * @(#)CmsLogController.java, 5月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.fenbi.commons.security.UserRole;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.imcuttle.bar.web.data.CmsLogVO;
import com.imcuttle.bar.web.logic.LogLogic;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author linbonan
 */
@RestController
@RequestMapping(value = TutorArmoryConstant.API + "/logs")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class LogController {

    @Autowired
    private LogLogic logLogic;

    @ApiOperation("获取操作日志，如果不指定 orderBy，默认按时间降序")
    @GetMapping
    public List<CmsLogVO> getCmsLogs(@RequestParam int id,
                                     @RequestParam(value = "idType") String idTypeStr) {
        try {
            return logLogic.getLogs(id, idTypeStr);
        } catch (BadRequestException e) {
            return Lists.newArrayList();
        }
    }
}
