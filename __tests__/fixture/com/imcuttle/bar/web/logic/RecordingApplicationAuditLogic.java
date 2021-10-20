/**
 * @(#)RecordingApplicationLogic.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.fenbi.commons.paging.Page;
import com.imcuttle.bar.web.data.RecordingApplicationAuditResultVO;
import com.imcuttle.bar.web.data.RecordingApplicationAuditSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationAuditVO;

/**
 * @author lukebj
 */
public interface RecordingApplicationAuditLogic {

    Page<RecordingApplicationAuditVO> searchSubmittedApplications(RecordingApplicationAuditSearchParamVO searchParamVO);

    void audit(RecordingApplicationAuditResultVO auditResultVO, int auditorId);
}
