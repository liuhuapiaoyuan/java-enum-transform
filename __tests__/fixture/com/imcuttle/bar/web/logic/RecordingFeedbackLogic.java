/**
 * @(#)FeedbackLogic.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.imcuttle.enums.FeedbackTargetType;
import com.imcuttle.bar.web.data.QuestionFeedbackCreateOrUpdateRequestVO;
import com.imcuttle.bar.web.data.QuestionFeedbackDeleteRequestVO;
import com.imcuttle.bar.web.data.RecordingFeedBackVO;

import java.util.List;

/**
 * @author linbonan
 */
public interface RecordingFeedbackLogic {

    List<RecordingFeedBackVO> getFeedBacksOfSpecificTarget(long targetId, FeedbackTargetType targetType);

    List<RecordingFeedBackVO> getFeedBacksOfSpecificTargetOfTask(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId);

    List<RecordingFeedBackVO> getFeedBacksOfSpecificTargetOfTask(long targetId, FeedbackTargetType targetType, long examPaperId, long taskId, boolean onlyUnProcessed);

    long createOrUpdateWhenTag(QuestionFeedbackCreateOrUpdateRequestVO requestVO);

    boolean deletedFeedback(QuestionFeedbackDeleteRequestVO requestVO);
}
