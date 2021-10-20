package com.imcuttle.bar.util;

import com.imcuttle.enums.FeedbackType;
import com.imcuttle.enums.RecordingApplicationStage;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author luke
 */
public class RecordingApplicationStageUtil {

    private static final Map<Integer, RecordingApplicationStage> FEEDBACK_TYPE_TO_STAGE_MAP = ImmutableMap.of(
            FeedbackType.APPLICATION_APPROVED.toInt(), RecordingApplicationStage.TO_BE_ASSIGNED,
            FeedbackType.APPLICATION_REJECTED.toInt(), RecordingApplicationStage.REJECTED,
            FeedbackType.APPLICATION_NEED_REVISED.toInt(), RecordingApplicationStage.TO_BE_REVISED
    );

    public static RecordingApplicationStage getStageByFeedbackType(int feedbackType) {
        return FEEDBACK_TYPE_TO_STAGE_MAP.get(feedbackType);
    }
}
