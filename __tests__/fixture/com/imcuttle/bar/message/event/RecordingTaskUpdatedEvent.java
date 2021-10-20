/**
 * @(#)RecordingTaskUpdatedEvent.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.message.event;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @author chenyibo
 */
@Data
public class RecordingTaskUpdatedEvent {

    public static final String TAG_RECORDING_TASK_UPDATED = "RECORDING_TASK_UPDATED";

    private long recordingApplicationId;

    private long recordingTaskId;

    private List<String> needUpdateFields;

    public RecordingTaskUpdatedEvent() {
    }

    public RecordingTaskUpdatedEvent(long recordingApplicationId, long recordingTaskId) {
        this.recordingApplicationId = recordingApplicationId;
        this.recordingTaskId = recordingTaskId;
        this.needUpdateFields = Lists.newArrayList();
    }

    public RecordingTaskUpdatedEvent(long recordingApplicationId, long recordingTaskId, List<String> needUpdateFields) {
        this.recordingApplicationId = recordingApplicationId;
        this.recordingTaskId = recordingTaskId;
        this.needUpdateFields = needUpdateFields;
    }
}
