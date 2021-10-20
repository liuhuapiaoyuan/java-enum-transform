/**
 * @(#)RecordingApplicationSearchParamCondition.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.server.data;

import lombok.Data;

import java.util.List;

/**
 * @author linbonan
 */
@Data
public class RecordingApplicationSearchCondition {

    /* 基础属性 */
    private int phaseId;

    private int subjectId;

    private int questionSource;

    private String keyword;

    private long id;

    private long submitStartTime;

    private long submitEndTime;

    private int stage;

    private int creator;

    private String creatorName;

    /* 下属的任务的属性 */
    private long taskId;

    private int taskStage;

    private String recorderName;

    private String auditorName;

    private String taggerName;

    private int tagProcess;

    /* 分页属性 */
    private int page = 0;

    private int pageSize = 20;

    private String sort;

    /* 权限属性 */
    private int currentUserId;
    private List<SubjectPhasePair> authorizedSubjectPhases;
}
