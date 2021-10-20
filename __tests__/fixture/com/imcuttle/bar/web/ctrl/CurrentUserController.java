/**
 * @(#)UserController.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.ctrl;

import com.fenbi.commons.security.SecurityHelper;
import com.imcuttle.constant.TutorArmoryConstant;
import com.imcuttle.bar.web.data.FeatureVO;
import com.imcuttle.bar.web.data.UserVO;
import com.imcuttle.bar.web.logic.UserLogic;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author duanou
 */
@Slf4j
@RestController
@RequestMapping(value = TutorArmoryConstant.API + "/users/current")
public class CurrentUserController {

    @Autowired
    private UserLogic userLogic;

    @ApiOperation("查询当前用户的系统状态")
    @GetMapping
    public UserVO getCurrentUser() {
        int userId = SecurityHelper.getUserId();
        return userLogic.getUserByUserId(userId);
    }

    @ApiOperation("查询当前用户的功能权限")
    @GetMapping("/features")
    public List<FeatureVO> getCurrentUserFeatures() {
        int userId = SecurityHelper.getUserId();
        return userLogic.getUserFeatures(userId);
    }
}
