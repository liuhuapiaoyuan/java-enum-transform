/**
 * @(#)BaseLogic.java, 4月 22, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.commons2.rest.exception.ForbiddenException;
import com.fenbi.commons2.rest.exception.ServerErrorException;
import com.imcuttle.bar.data.SubjectPhasePair;
import com.imcuttle.bar.enums.FeatureEnum;
import com.imcuttle.bar.enums.TableEnum;
import com.imcuttle.bar.service.AuthorizeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.fenbi.commons.security.SecurityHelper.getUserId;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class BaseLogic {

    private static final String DEFAULT_MSG_OF_FEATURE_AUTH_FAILED = "没有执行当前操作的权限";

    @Autowired
    private AuthorizeService authorizeService;

    protected void authorizeFeature(int subjectId, int phaseId, FeatureEnum featureEnum) {
        authorizeFeature(subjectId, phaseId, featureEnum, DEFAULT_MSG_OF_FEATURE_AUTH_FAILED);
    }

    protected void authorizeFeature(int subjectId, int phaseId, FeatureEnum featureEnum, String msgWhenAuthFailed) {
        boolean authorized = false;
        try {
            authorized = authorizeService.featureAuthorized(getUserId(), subjectId, phaseId, featureEnum);
        } catch (Exception e) {
            log.error("鉴权服务处理失败, subjectId = {}, phaseId = {}, feature = {}", subjectId, phaseId, featureEnum, e);
            throw new ServerErrorException("鉴权失败, 请刷新后重试");
        }

        if (!authorized) {
            throw new ForbiddenException(msgWhenAuthFailed);
        }
    }

    public List<SubjectPhasePair> getAuthorizedSubjectPhases(TableEnum tableEnum) {
        List<SubjectPhasePair> authorizedSubjectPhases;

        try {
            authorizedSubjectPhases = authorizeService.getAuthorizedSubjectPhase(getUserId(), tableEnum);
        } catch (Exception e) {
            log.error("从鉴权服务查询当前用户的数据权限失败, tableEnum = {}", tableEnum, e);
            throw new ServerErrorException("鉴权失败, 请刷新后重试");
        }

        if (CollectionUtils.isEmpty(authorizedSubjectPhases)) {
            throw new ForbiddenException("没有查看当前页面数据的权限");
        }

        return authorizedSubjectPhases;
    }
}
