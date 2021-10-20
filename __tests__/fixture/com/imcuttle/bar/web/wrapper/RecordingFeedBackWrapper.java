/**
 * @(#)RecordingFeedBackWrapper.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.wrapper;

import com.imcuttle.bar.web.data.RecordingFeedBackVO;
import com.imcuttle.thrift.RecordingFeedback;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;

import java.util.Map;
import java.util.Optional;

/**
 * @author linbonan
 */
public class RecordingFeedBackWrapper {

    public static RecordingFeedBackVO wrap(RecordingFeedback recordingFeedback, Map<Integer, String> userNames) {
        if (recordingFeedback == null) {
            return null;
        }

        RecordingFeedBackVO feedBackVO = new RecordingFeedBackVO();
        BeanUtils.copyProperties(recordingFeedback, feedBackVO);

        feedBackVO.setCreatorName(String.valueOf(recordingFeedback.getCreator()));
        if (MapUtils.isNotEmpty(userNames)) {
            Optional.ofNullable(userNames.get(recordingFeedback.getCreator()))
                    .ifPresent(feedBackVO::setCreatorName);
        }

        return feedBackVO;
    }

    private RecordingFeedBackWrapper() {
    }
}
