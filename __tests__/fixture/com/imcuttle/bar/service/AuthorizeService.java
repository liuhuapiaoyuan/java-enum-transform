/**
 * @(#)AuthorizeService.java, 4月 22, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import com.imcuttle.bar.data.SubjectPhasePair;
import com.imcuttle.bar.enums.FeatureEnum;
import com.imcuttle.bar.enums.TableEnum;

import java.util.List;

/**
 * @author linbonan
 */
public interface AuthorizeService {

    boolean featureAuthorized(int userId, int subjectId, int phaseId, FeatureEnum featureEnum);

    List<SubjectPhasePair> getAuthorizedSubjectPhase(int userId, TableEnum tableEnum);
}
