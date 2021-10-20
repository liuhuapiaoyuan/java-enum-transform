/**
 * @(#)QuestionRecordingTaskPair.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.data;

import com.imcuttle.thrift.QuestionRecordingTask;
import lombok.Data;

/**
 * @author chenyibo
 */
@Data
public class QuestionRecordingTaskPair {

    private int questionId;

    private long recordingTaskId;

    private long examPaperId;

    public static QuestionRecordingTaskPair fromQuestionRecordingTask(QuestionRecordingTask questionRecordingTask) {
        if (questionRecordingTask == null) {
            return null;
        }

        QuestionRecordingTaskPair pair = new QuestionRecordingTaskPair();
        pair.setQuestionId(questionRecordingTask.getQuestionId());
        pair.setRecordingTaskId(questionRecordingTask.getTaskId());
        pair.setExamPaperId(questionRecordingTask.getExamPaperId());

        return pair;
    }
}
