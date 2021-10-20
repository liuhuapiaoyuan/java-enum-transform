/**
 * @(#)LogLogic.java, 5月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.imcuttle.bar.web.data.CmsLogVO;

import java.util.List;

/**
 * @author linbonan
 */
public interface LogLogic {

    List<CmsLogVO> getLogs(int id, String idTypeStr);
}
