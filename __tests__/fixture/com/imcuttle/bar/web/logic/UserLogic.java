/**
 * @(#)UserLogic.java, Apr 09, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.imcuttle.bar.web.data.FeatureVO;
import com.imcuttle.bar.web.data.UserVO;

import java.util.List;

/**
 * @author duanou
 */
public interface UserLogic {

    UserVO getUserByUserId(int userId);

    List<FeatureVO> getUserFeatures(int userId);
}
