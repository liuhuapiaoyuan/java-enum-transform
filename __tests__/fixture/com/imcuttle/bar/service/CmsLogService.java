/**
 * @(#)PaperService.java, Apr 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.fenbi.tutor.cmslog.thrift.CmsLogEx;

import java.util.List;

/**
 * @author lukebj
 */
public interface CmsLogService {

    void batchCreateCmsLog(List<CmsLogEx> logs);

    void log(long id, int idType, String content);
}
