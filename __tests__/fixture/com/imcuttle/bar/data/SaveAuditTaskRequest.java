/**
 * @(#)QuestionRecordingTaskPair.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.data;

import lombok.Data;

import java.util.List;

/**
 * @author chenyibo
 */
@Data
public class SaveAuditTaskRequest {

    // 审核相关

    private long taskId;

    private long examPaperId;

    private long targetId;

    private int targetType;

    private boolean passed;

    // 反馈相关

    private List<String> reason;

    private String description;

    private List<String> attachments;
}
