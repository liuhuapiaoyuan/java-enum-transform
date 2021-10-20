/**
 * @(#)RecordingApplicationService.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.thrift.RecordingApplication;
import com.imcuttle.thrift.RecordingTask;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author linbonan
 */
public interface RecordingApplicationService {

    List<Long> createRecordingTasksForApplication(long applicationId, List<RecordingTask> tasks);

    Map<Long, RecordingApplication> getByIds(Collection<Long> ids);

    long create(RecordingApplication recordingApplication);

    boolean updateStage(long applicationId, RecordingApplicationStage fromStage, RecordingApplicationStage targetStage);

    void updateBasicInfo(RecordingApplication recordingApplication);

    void updatePublishTime(RecordingApplication application);

    List<Long> scanId(long cursorIdExclusive, int batchSize);
}
