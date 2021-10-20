/**
 * @(#)AuthorizeServiceImpl.java, 4月 22, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.bar.data.SubjectPhasePair;
import com.imcuttle.bar.enums.FeatureEnum;
import com.imcuttle.bar.enums.RowRuleEnum;
import com.imcuttle.bar.enums.TableEnum;
import com.imcuttle.bar.service.AuthorizeService;
import com.fenbi.tutor.patronus.client.proxy.spi.TutorPatronusProxy;
import com.fenbi.tutor.patronus.thrift.QueryUserTableRulesRequest;
import com.fenbi.tutor.patronus.thrift.QueryUserTableRulesResponse;
import com.fenbi.tutor.patronus.thrift.UserFeatureAuthRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.imcuttle.bar.constant.PatronusConstant.PROJECT_KEY;
import static com.imcuttle.thrift.TutorArmoryConstants.INT_ID_NO_LIMIT;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class AuthorizeServiceImpl implements AuthorizeService {

    @Autowired
    private TutorPatronusProxy tutorPatronusProxy;

    @Override
    public boolean featureAuthorized(int userId, int subjectId, int phaseId, FeatureEnum featureEnum) {
        UserFeatureAuthRequest authRequest = new UserFeatureAuthRequest();
        authRequest.setSubjectId(subjectId);
        authRequest.setPhaseId(phaseId);
        authRequest.setProjectKey(PROJECT_KEY);
        authRequest.setFeatureKey(featureEnum.getKey());
        authRequest.setUserId(userId);
        return tutorPatronusProxy.authorizeFeature(authRequest);
    }

    @Override
    public List<SubjectPhasePair> getAuthorizedSubjectPhase(int userId, TableEnum tableEnum) {
        QueryUserTableRulesRequest request = new QueryUserTableRulesRequest();
        request.setProjectKey(PROJECT_KEY);
        request.setUserId(userId);
        request.setTableKey(tableEnum.toKey());
        QueryUserTableRulesResponse response = tutorPatronusProxy.queryUserTableRules(request);

        Set<SubjectPhasePair> subjectPhasePairs = Sets.newHashSet();
        response.getTableRowRuleRolesList()
                .forEach(tableRowRuleRoles -> {
                    String roleRuleKey = tableRowRuleRoles.getTableRowRule().getKey();
                    RowRuleEnum.findByKey(roleRuleKey)
                            .ifPresent(rowRule -> {
                                if (RowRuleEnum.ALL_PHASE_SUBJECT == rowRule) {
                                    subjectPhasePairs.add(new SubjectPhasePair(INT_ID_NO_LIMIT, INT_ID_NO_LIMIT));
                                } else {
                                    tableRowRuleRoles.getRoles()
                                            .stream()
                                            .map(role -> new SubjectPhasePair(role.getSubjectId(), role.getPhaseId()))
                                            .forEach(subjectPhasePairs::add);
                                }
                            });
                });

        return Lists.newArrayList(subjectPhasePairs);
    }

}
