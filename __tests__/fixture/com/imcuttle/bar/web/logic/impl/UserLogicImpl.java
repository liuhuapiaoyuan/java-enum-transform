/**
 * @(#)UserLogicImpl.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.commons2.rest.exception.BadRequestException;
import com.fenbi.commons2.rest.exception.ServerErrorException;
import com.imcuttle.bar.constant.PatronusConstant;
import com.imcuttle.bar.data.UserBO;
import com.imcuttle.bar.enums.FeatureEnum;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.web.data.FeatureVO;
import com.imcuttle.bar.web.data.UserVO;
import com.imcuttle.bar.web.logic.UserLogic;
import com.imcuttle.bar.web.wrapper.FeatureVOWrapper;
import com.fenbi.tutor.patronus.client.proxy.spi.TutorPatronusProxy;
import com.fenbi.tutor.patronus.thrift.FeatureRoles;
import com.fenbi.tutor.patronus.thrift.QueryUserFeatureRolesRequest;
import com.fenbi.tutor.patronus.thrift.QueryUserFeatureRolesResponse;
import com.fenbi.tutor.patronus.thrift.UserStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author duanou
 */
@Slf4j
@Service
public class UserLogicImpl implements UserLogic {

    @Autowired
    private UserService userService;

    @Autowired
    private TutorPatronusProxy patronusProxy;

    @Override
    public UserVO getUserByUserId(int userId) {
        if (userId <= 0) {
            return UserVO.newInvalidUser();
        }

        Optional<UserBO> userOpt = userService.getUserByUserId(userId);
        if (!userOpt.isPresent()) {
            return UserVO.newInvalidUser();
        }

        UserBO userBO = userOpt.get();
        UserVO result = new UserVO();
        result.setUserId(userBO.getUserId());
        result.setStatus(userBO.getStatus().toInt());
        return result;
    }

    @Override
    public List<FeatureVO> getUserFeatures(int userId) {
        QueryUserFeatureRolesRequest request = new QueryUserFeatureRolesRequest();
        request.setUserId(userId);
        request.setProjectKey(PatronusConstant.PROJECT_KEY);

        QueryUserFeatureRolesResponse response;
        try {
            response = patronusProxy.queryUserFeatureRoles(request);
        } catch (Exception e) {
            log.error("调用 patronus 的 queryUserFeatureRoles 异常", e);
            throw new ServerErrorException("调用 patronus 的 queryUserFeatureRoles 异常：" + e.getMessage());
        }

        if (response.getUser() == null || response.getUser().getStatus() != UserStatusEnum.VALID) {
            throw new BadRequestException("当前用户不是权限系统有效用户");
        }

        List<FeatureRoles> featureRolesList = response.getFeatureRoles();
        List<FeatureVO> result = new ArrayList<>();
        featureRolesList.forEach(featureRoles -> {
            String featureKey = featureRoles.getFeature().getKey();
            if (FeatureEnum.findByKey(featureKey).isPresent()) {
                FeatureVO featureVO = FeatureVOWrapper.wrap(featureRoles);
                result.add(featureVO);
            }
        });
        return result;
    }
}
