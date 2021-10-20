/**
 * @(#)RecordingTaskUpdatedEvent.java, 4月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.message.event;

import com.fenbi.tutor.cmslog.thrift.CmsLogEx;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

import static com.imcuttle.bar.util.CollectionUtil.getOrEmpty;

/**
 * @author chenyibo
 */
@Data
public class CmsLogGeneratedEvent {

    public static final String TAGS = "CMS_LOG_GENERATED";

    private List<CmsLogEx> cmsLogList;

    public CmsLogGeneratedEvent() {}

    public CmsLogGeneratedEvent(CmsLogEx cmsLog) {
        this.cmsLogList = Lists.newArrayList();
        cmsLogList.add(cmsLog);
    }

    public CmsLogGeneratedEvent(List<CmsLogEx> cmsLogList) {
        this.cmsLogList = Lists.newArrayList();
        this.cmsLogList.addAll(getOrEmpty(cmsLogList));
    }
}
