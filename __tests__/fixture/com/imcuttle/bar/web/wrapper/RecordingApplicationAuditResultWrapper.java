/**
 * @(#)RecordingApplicationSearchParamWrapper.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.bar.data.RecordingApplicationAuditResult;
import com.imcuttle.bar.util.RecordingApplicationStageUtil;
import com.imcuttle.bar.web.data.RecordingApplicationAuditResultVO;
import org.springframework.beans.BeanUtils;

/**
 * @author lukebj
 */
public class RecordingApplicationAuditResultWrapper {

    public static RecordingApplicationAuditResult wrap(RecordingApplicationAuditResultVO resultVO, int auditorId) {
        RecordingApplicationAuditResult result = new RecordingApplicationAuditResult();
        result.setAuditorId(auditorId);
        if (resultVO != null) {
            BeanUtils.copyProperties(resultVO, result);
            RecordingApplicationStage stage = RecordingApplicationStageUtil.getStageByFeedbackType(resultVO.getFeedbackType());
            result.setStage(stage);
        }
        return result;
    }
}
