/**
 * @(#)QuestionRecordingTaskLogic.java, 4月 14, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.imcuttle.bar.web.data.BindBatchQuestionRequest;
import com.imcuttle.bar.web.data.BindSingleQuestionRequest;

import java.util.List;

/**
 * @author chenyibo
 */
public interface QuestionRecordingTaskLogic {

    boolean bindSingleQuestion2RecordingTask(BindSingleQuestionRequest request);

    boolean removeQuestionFromTask(long taskId, int questionId);

    boolean reorderQuestions(long taskId, List<Integer> questionIds);

    boolean bindBatchQuestion2RecordingTask(BindBatchQuestionRequest request);
}
