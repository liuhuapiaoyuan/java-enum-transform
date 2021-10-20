/**
 * @(#)RecordingTaskLogic.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.fenbi.commons.paging.Page;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.web.data.RecordingTaskCreateRequestVO;
import com.imcuttle.bar.web.data.RecordingTaskExamPaperQuestionsVO;
import com.imcuttle.bar.enums.RecordingTaskRoleEnum;
import com.imcuttle.bar.web.data.RecordingTaskUpdateRequestVO;
import com.imcuttle.bar.web.data.RecordingTaskVO;

import java.util.List;
import java.util.Map;

/**
 * @author xiechao01
 */
public interface RecordingTaskLogic {

    long create(RecordingTaskCreateRequestVO createRequestVO);

    boolean update(RecordingTaskUpdateRequestVO requestVO);

    Map<Integer, Integer> getRecordingTasksByRole(RecordingTaskRoleEnum taskRole);

    Page<RecordingTaskVO> getRecordingTaskByStatus(RecordingTaskStage taskStage, int page, int pageSize);

    RecordingTaskVO getRecordingTask(long taskId);

    RecordingTaskExamPaperQuestionsVO getRecordingTaskExamPaperQuestions(long taskId);

    boolean submit(long taskId);

    List<RecordingTaskVO> getRecordingTasksByApplicationId(long applicationId);
}
