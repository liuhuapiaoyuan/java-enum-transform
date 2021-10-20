/**
 * @(#)RecordTaskAuditLogic.java, Apr 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.fenbi.commons.paging.Page;
import com.imcuttle.bar.web.data.RecordingAuditResourceDetailVO;
import com.imcuttle.bar.web.data.RecordingAuditResourceListVO;
import com.imcuttle.bar.web.data.RecordingTaskAuditVO;
import com.imcuttle.bar.web.data.SaveAuditTaskRequestVO;
import com.imcuttle.bar.web.data.SubmitAuditTaskRequestVO;

/**
 * @author chenkangbj
 */
public interface RecordTaskAuditLogic {

    Page<RecordingTaskAuditVO> getMyAuditTask(long taskId, int page, int pageSize);

    Page<RecordingTaskAuditVO> getMyRejectedTask(int page, int pageSize);

    void submitAuditTask(long taskId, SubmitAuditTaskRequestVO requestVO);

    void saveAuditTask(SaveAuditTaskRequestVO requestVO);

    RecordingAuditResourceListVO getResourceList(long taskId);

    RecordingAuditResourceDetailVO getResource(long taskId, long examPaperId, long targetId, int targetType);
}
