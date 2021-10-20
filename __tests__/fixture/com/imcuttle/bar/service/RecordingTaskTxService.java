/**
 * @(#)RecordingTaskTxService.java, Apr 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.thrift.QuestionRecordingTask;
import com.imcuttle.thrift.RecordingTask;

import java.util.List;

/**
 * @author chenkangbj
 */
public interface RecordingTaskTxService {

    void submitAuditTask(RecordingTask task, List<QuestionRecordingTask> questionRelations,
                         int feedBackCreatorId, boolean taskApproved, String feedbackDescription);

    void publishTask(RecordingTask task);
}
