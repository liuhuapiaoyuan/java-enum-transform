/**
 * @(#)RecordingApplicationStorage.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db;

import com.imcuttle.thrift.RecordingApplication;

import java.util.Collection;
import java.util.List;

/**
 * @author linbonan
 */
public interface RecordingApplicationStorage {

    List<RecordingApplication> getByIds(Collection<Long> ids);

    long create(RecordingApplication recordingApplication);

    boolean updateStage(long id, int fromStage, int toStage);

    boolean updateBasicInfo(RecordingApplication recordingApplication);

    List<Long> scanId(long cursorIdExclusive, int batchSize);

    void updatePublishTime(RecordingApplication application);

    List<Long> batchCreate(Collection<RecordingApplication> recordingApplications);
}
