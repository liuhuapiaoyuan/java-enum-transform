/**
 * @(#)QuestionService.java, Apr 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service;

import java.util.List;

/**
 * @author chenkangbj
 */
public interface QuestionService {

    void checkWholeQuestions(List<Integer> wholeQuestionIds);

    boolean publishWholeQuestions(List<Integer> wholeQuestionIds);
}
