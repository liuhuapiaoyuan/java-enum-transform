/**
 * @(#)CollectionUtil.java, 四月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.util;

import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.MATERIAL_ID;
import static com.fenbi.tutor.cmslog.thrift.TutorCmsLogConstants.QUESTION_ID;
import static com.fenbi.tutor.cybercommon.util.QuestionIdUtil.isQuestionId;

/**
 * @author lukebj
 */
public class CmsLogUtil {

    public static int getCmsLogIdTypeByQuestionId(int questionId) {
        if (isQuestionId(questionId)) {
            return QUESTION_ID;
        }
        return MATERIAL_ID;
    }

    private CmsLogUtil() {
    }
}
