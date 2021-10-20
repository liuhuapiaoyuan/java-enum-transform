/**
 * @(#)RecordingApplicationBasicVO.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.data;

import com.imcuttle.enums.RecordingApplicationStage;
import lombok.Data;

import java.util.List;

/**
 * @author lukebj
 */
@Data
public class RecordingApplicationAuditResult {

    private int auditorId;

    private long recordingApplicationId;

    /**
     * @see com.imcuttle.enums.FeedbackType
     */
    private int feedbackType;

    private RecordingApplicationStage stage;

    private String description;

    private List<String> attachments;
}
