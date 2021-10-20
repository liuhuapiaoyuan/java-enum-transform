/**
 * @(#)QuestionRecordingTaskWrapper.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.bar.data.RecordingAuditResource;
import com.imcuttle.bar.web.data.RecordingAuditResourceVO;
import org.springframework.beans.BeanUtils;

/**
 * @author chenyibo
 */
public class RecordingAuditResourceWrapper {

    public static RecordingAuditResourceVO wrap(RecordingAuditResource recordingAuditResource) {
        if (recordingAuditResource == null) {
            return null;
        }

        RecordingAuditResourceVO recordingAuditResourceVO = new RecordingAuditResourceVO();
        BeanUtils.copyProperties(recordingAuditResource, recordingAuditResourceVO);

        return recordingAuditResourceVO;
    }
}
