/**
 * @(#)LogLogicImpl.java, 5月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic.impl;

import com.fenbi.commons.paging.Page;
import com.fenbi.commons.paging.PageConfig;
import com.fenbi.commons2.rest.exception.BadRequestException;
import com.imcuttle.bar.enums.LogIdType;
import com.imcuttle.bar.enums.LogOrderBy;
import com.imcuttle.bar.enums.UserStatusEnum;
import com.imcuttle.bar.enums.UserType;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.web.data.CmsLogVO;
import com.imcuttle.bar.web.data.UserInfoVO;
import com.imcuttle.bar.web.logic.LogLogic;
import com.fenbi.tutor.cmslog.client.proxy.TutorCmsLogProxy;
import com.fenbi.tutor.cmslog.thrift.CmsLogEx;
import com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants;
import com.fenbi.tutor.crmaccount.client.proxy.TutorCrmAccountProxy;
import com.fenbi.tutor.crmaccount.thrift.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fenbi.commons.security.SecurityHelper.getUserId;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class LogLogicImpl implements LogLogic {

    @Autowired
    private UserService userService;

    @Autowired
    private TutorCmsLogProxy tutorCmsLogProxy;

    @Autowired
    private TutorCrmAccountProxy tutorCrmAccountProxy;

    @Override
    public List<CmsLogVO> getLogs(int id, String idTypeStr) {
        userService.getUserByUserId(getUserId())
                .filter(user -> user.getStatus() == UserStatusEnum.VALID)
                .orElseThrow(BadRequestException::new);
        int idType = LogIdType.findByString(idTypeStr).map(LogIdType::toInt).orElseThrow(BadRequestException::new);

        List<CmsLogEx> cmsLogs = tutorCmsLogProxy.getLogsById(id, idType);
        return wrap(cmsLogs);
    }

    private List<CmsLogVO> wrap(List<CmsLogEx> cmsLogs) {
        if (CollectionUtils.isEmpty(cmsLogs)) {
            return Collections.emptyList();
        }

        List<Integer> userIds = cmsLogs.stream().map((cmsLog) -> (int) cmsLog.getUserId())
                .filter(id -> id != 0).distinct().collect(Collectors.toList());
        Map<Integer, UserInfo> userInfoMap = tutorCrmAccountProxy.getUserInfosByIds(userIds);

        return cmsLogs.stream()
                .map(cmsLog ->
                        wrap(cmsLog, TutorCmsLogConstants.UNUSED_USERID == cmsLog.getUserId() ?
                                null : userInfoMap.get((int) cmsLog.getUserId())))
                .collect(Collectors.toList());
    }

    private CmsLogVO wrap(CmsLogEx cmsLog, UserInfo userInfo) {
        CmsLogVO cmsLogVO = new CmsLogVO();

        cmsLogVO.setId(cmsLog.getId());
        cmsLogVO.setIdType(LogIdType.findByInt(cmsLog.getIdType()).orElseThrow(BadRequestException::new));
        try {
            cmsLogVO.setUserType(UserType.findByInt(cmsLog.getUserType()));
        } catch (IllegalArgumentException e) {
            cmsLogVO.setUserType(UserType.UNKNOWN);
        }
        cmsLogVO.setLdap(cmsLog.getLdap());
        cmsLogVO.setUserInfo(userInfo == null ? null : new UserInfoVO(userInfo));
        cmsLogVO.setContent(cmsLog.getContent());
        cmsLogVO.setCreatedTime(cmsLog.getCreatedTime());

        return cmsLogVO;
    }
}
