/**
 * @(#)UserServiceImpl.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.common.util.PartitionUtils;
import com.fenbi.commons.security.SecurityHelper;
import com.imcuttle.bar.message.producer.CmsLogGeneratedEventProducer;
import com.imcuttle.bar.service.CmsLogService;
import com.fenbi.tutor.cmslog.client.proxy.TutorCmsLogProxy;
import com.fenbi.tutor.cmslog.thrift.CmsLogEx;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.imcuttle.bar.util.UserUtil.getUserType;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.UNUSED_LDAP;

/**
 * @author lukebj
 */
@Service
public class CmsLogServiceImpl implements CmsLogService {

    private static final int BATCH_PRODUCE_SIZE = 10;

    @Autowired
    private CmsLogGeneratedEventProducer cmsLogGeneratedEventProducer;

    @Autowired
    private TutorCmsLogProxy tutorCmsLogProxy;

    @Override
    public void batchCreateCmsLog(List<CmsLogEx> logs) {
        PartitionUtils.partitionConsume(logs, BATCH_PRODUCE_SIZE, batch ->
                cmsLogGeneratedEventProducer.publish(Lists.newArrayList(batch)));
    }

    @Override
    public void log(long id, int idType, String content) {
        int userType = getUserType();
        String ldap = Optional.ofNullable(SecurityHelper.getLdapId()).orElse(UNUSED_LDAP);
        int userId = SecurityHelper.getUserId();
        CmsLogEx cmsLog = new CmsLogEx(id, idType, userType, ldap, userId, content, System.currentTimeMillis());
        tutorCmsLogProxy.createCmsLog(cmsLog);
    }


}
