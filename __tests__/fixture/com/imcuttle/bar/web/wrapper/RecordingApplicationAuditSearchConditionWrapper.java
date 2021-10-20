/**
 * @(#)RecordingApplicationSearchParamWrapper.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.bar.data.RecordingApplicationSearchCondition;
import com.imcuttle.bar.web.data.RecordingApplicationAuditSearchParamVO;
import org.springframework.beans.BeanUtils;

/**
 * @author lukebj
 */
public class RecordingApplicationAuditSearchConditionWrapper {

    public static RecordingApplicationSearchCondition wrap(RecordingApplicationAuditSearchParamVO searchParamVO) {
        RecordingApplicationSearchCondition searchCondition = new RecordingApplicationSearchCondition();
        if (searchParamVO != null) {
            BeanUtils.copyProperties(searchParamVO, searchCondition);
            searchCondition.setStage(searchParamVO.getStage() == 0 ? RecordingApplicationStage.SUBMITTED.toInt() : searchParamVO.getStage());
        }
        return searchCondition;
    }
}
