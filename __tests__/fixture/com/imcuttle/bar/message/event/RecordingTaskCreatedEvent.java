/**
 * @(#)RecordingTaskCreateEvent.java, Apr 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.message.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tangjucai
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordingTaskCreatedEvent {

    public static final String TAG_RECORDING_TASK_CREATED = "RECORDING_TASK_CREATED";

    private long recordingApplicationId;

    private long recordingTaskId;
}
