/**
 * @(#)RecordingApplicationDocumentField.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.foo.es;

/**
 * @author linbonan
 */
public class RecordingApplicationDocument {

    public static final String RECORDING_APPLICATION_INDEX_NAME = "recording-application";

    public static final String WILDCARD_CODE = "*";
    public static final int MAX_KEYWORD_SIZE = 100;
    public static final int MAX_WINDOW_SIZE = 10000;

    public static final String FIELD_ID = "id";
    public static final String FIELD_PHASE_ID = "phaseId";
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_QUESTION_SOURCE = "questionSource";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SUBMIT_TIME = "submitTime";
    public static final String FIELD_STAGE = "stage";
    public static final String FIELD_CREATOR = "creator";
    public static final String FIELD_CREATOR_NAME = "creatorName";
    public static final String FIELD_PUBLISH_TIME = "publishTime";
    public static final String FIELD_MAX_TASK_SUBMIT_TIMES = "maxTaskSubmitTimes";
    public static final String FIELD_RECORDED_QUESTION = "recordedQuestion";
    public static final String FIELD_COMPLETE_DURATION = "completeDuration";
    public static final String NESTED_PATH_TAGGER_NAME = "tasks";
    public static final String NESTED_FIELD_TAGGER_NAME = "tasks.taggerName";
    public static final String NESTED_FIELD_TAG_PROCESS = "tasks.tagProgressComplete";
    public static final String NESTED_FIELD_TASK_ID = "tasks.taskId";
    public static final String NESTED_FIELD_RECORDER_NAME = "tasks.recorderName";
    public static final String NESTED_FIELD_AUDITOR_NAME = "tasks.auditorName";
    public static final String NESTED_FIELD_TASK_STAGE = "tasks.taskStage";

}
