/**
 * @(#)RecordingApplicationSearchParamWrapper.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.bar.data.RecordingApplicationSearchCondition;
import com.imcuttle.bar.web.data.RecordingApplicationBasicSearchParamVO;
import com.imcuttle.bar.web.data.RecordingTagSearchParamVO;
import org.springframework.beans.BeanUtils;

/**
 * @author linbonan
 */
public class RecordingApplicationSearchConditionWrapper {


    private RecordingApplicationSearchConditionWrapper() {}

    public static RecordingApplicationSearchCondition wrap(RecordingApplicationBasicSearchParamVO searchParamVO) {
        RecordingApplicationSearchCondition searchCondition = new RecordingApplicationSearchCondition();
        if (searchParamVO == null) {
            return searchCondition;
        }

        BeanUtils.copyProperties(searchParamVO, searchCondition);
        return searchCondition;
    }

    public static RecordingApplicationSearchCondition wrap(RecordingTagSearchParamVO searchParamVO) {
        RecordingApplicationSearchCondition searchCondition = new RecordingApplicationSearchCondition();
        if (searchParamVO == null) {
            return searchCondition;
        }

        BeanUtils.copyProperties(searchParamVO, searchCondition);
        return searchCondition;
    }
}
