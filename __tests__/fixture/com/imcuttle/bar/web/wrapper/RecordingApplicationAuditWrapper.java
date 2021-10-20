/**
 * @(#)RecordingApplicationWrapper.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.bar.web.data.RecordingApplicationAuditVO;
import com.imcuttle.thrift.RecordingApplication;
import org.springframework.beans.BeanUtils;

/**
 * @author lukebj
 */
public class RecordingApplicationAuditWrapper {

    public static RecordingApplicationAuditVO wrap2VO(RecordingApplication application, String userName) {
        RecordingApplicationAuditVO applicationAuditVO = new RecordingApplicationAuditVO();
        if (application != null) {
            BeanUtils.copyProperties(application, applicationAuditVO);
            applicationAuditVO.setCreatorName(userName);
        }
        return applicationAuditVO;
    }
}
