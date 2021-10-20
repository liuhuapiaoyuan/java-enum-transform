/**
 * @(#)RecordingApplicationSearchService.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.fenbi.commons.paging.Page;
import com.imcuttle.bar.data.RecordingApplicationSearchCondition;

/**
 * @author linbonan
 */
public interface RecordingApplicationSearchService {

    Page<Long> searchIdList(RecordingApplicationSearchCondition searchCondition);
}
