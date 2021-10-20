/**
 * @(#)RecordingTaskUpdatedEvent.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.message.event;

import lombok.Data;

import java.util.List;

/**
 * @author lukebj
 */
@Data
public class RecordingApplicationUpdateEvent {

    public static final String TAG_RECORDING_APPLICATION_UPDATED = "RECORDING_APPLICATION_UPDATED";

    private long recordingApplicationId;

    private List<String> needUpdateFields;
}
