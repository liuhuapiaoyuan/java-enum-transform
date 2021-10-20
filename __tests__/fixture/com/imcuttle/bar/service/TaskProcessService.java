/**
 * @(#)ProcessService.java, Apr 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.bar.data.RecordingAuditResource;
import com.imcuttle.thrift.QuestionRecordingTask;

import java.util.List;

/**
 * @author xiechao01
 */
public interface TaskProcessService {

    List<String> validateForSubmit(long taskId);

    List<QuestionRecordingTask> getOrderedQuestionRecordingTask(long taskId);

    List<RecordingAuditResource> getOrderedRecordingAuditResource(long taskId);
}
