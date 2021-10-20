/**
 * @(#)PaperService.java, Apr 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import java.util.List;

/**
 * @author chenkangbj
 */
public interface PaperService {

    boolean publishArmoryPapers(List<Long> paperIds);
}
