/**
 * @(#)RecordingApplicationDetailService.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.thrift.RecordingApplicationDetail;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author linbonan
 */
public interface RecordingApplicationDetailService {

    Map<Long, RecordingApplicationDetail> getByIds(Collection<Long> ids);

    Map<Long, RecordingApplicationDetail> getByIdsIncludeAllRelations(Collection<Long> ids);

    Set<Integer> getAllUserIdsInvolvedWithApplication(RecordingApplicationDetail detail);
}
