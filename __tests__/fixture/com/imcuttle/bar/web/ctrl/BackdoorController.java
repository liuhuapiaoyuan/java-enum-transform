/**
 * @(#)BaseController.java, Dec 28, 2015.
 * <p/>
 * Copyright 2015 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.fenbi.commons.security.UserRole;
import com.imcuttle.foo.constant.TutorArmoryConstant;
import com.imcuttle.bar.exceptions.CheckFailedException;
import com.imcuttle.bar.exceptions.TaskValidateException;
import com.fenbi.tutor.atmsfilter.TutorUserRole;
import com.fenbi.tutor.trfilter.TrUserRole;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linbonan
 */
@Slf4j
@RestController
@RequestMapping(value = TutorArmoryConstant.API + "/backdoor")
@Secured({TrUserRole.ARMORY_USER, UserRole.STAFF, TutorUserRole.TEACHER, TutorUserRole.MENTOR, TutorUserRole.ASSISTANT})
public class BackdoorController {

    @ApiOperation("服务健康检查接口")
    @GetMapping("/health")
    public boolean checkIfHealthy() {
        return true;
    }

    @ApiOperation("测试sentry")
    @GetMapping("/testSentry1")
    public void testSentry1() {
        throw new TaskValidateException("测试sentry1");
    }

    @ApiOperation("测试sentry")
    @GetMapping("/testSentry2")
    public void testSentry2() {
        throw new CheckFailedException("测试sentry2");
    }

    @ApiOperation("测试过滤通用log")
    @GetMapping("/testSentryLog1")
    public void testSentrLog1() {
        log.error("serviceName not set: 需要过滤");
    }

    @ApiOperation("测试过滤项目内自定义log")
    @GetMapping("/testSentryLog2")
    public void testSentrLog2() {
        log.error("testFilterLog: 需要过滤");
    }

    @ApiOperation("测试正常报警log")
    @GetMapping("/testSentryLog3")
    public void testSentrLog3() {
        log.error("common error log: 发送邮件");
    }

    @ApiOperation("测试录入的log")
    @GetMapping("/testSentryLog4")
    public void testSentrLog4(@RequestParam String text) {
        log.error(text);
    }
}
