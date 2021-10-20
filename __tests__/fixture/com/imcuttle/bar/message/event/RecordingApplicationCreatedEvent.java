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
 * @author linbonan
 */
@Data
public class RecordingApplicationCreatedEvent {

    public static final String TAG_RECORDING_APPLICATION_CREATED = "RECORDING_APPLICATION_CREATED";

    private long recordingApplicationId;

    private List<String> needUpdateFields;

    public RecordingApplicationCreatedEvent() {
    }

    public RecordingApplicationCreatedEvent(long recordingApplicationId) {
        this.recordingApplicationId = recordingApplicationId;
        this.needUpdateFields = Lists.newArrayList();
    }

    public RecordingApplicationCreatedEvent(long recordingApplicationId, List<String> needUpdateFields) {
        this.recordingApplicationId = recordingApplicationId;
        this.needUpdateFields = needUpdateFields;
    }
}
