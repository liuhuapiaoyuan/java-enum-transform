/**
 * @(#)RecordingTagLogic.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.fenbi.commons.paging.Page;
import com.imcuttle.bar.web.data.RecordingQuestionVO;
import com.imcuttle.bar.web.data.RecordingTagSearchParamVO;
import com.imcuttle.bar.web.data.RecordingTagVO;
import com.imcuttle.bar.web.data.TaggerUpdateRequestVO;

import java.util.List;

/**
 * @author chenkangbj
 */
public interface RecordingTagLogic {

    Page<RecordingTagVO> searchRecordingTagTask(RecordingTagSearchParamVO param);

    List<RecordingQuestionVO> getQuestionTagInfos(long applicationId);

    void rejectTagTask(long taskId);

    RecordingTagVO updateTaggerAndGet(TaggerUpdateRequestVO requestVO);

    RecordingQuestionVO getQuestionTagInfo(long taskId, int questionId, long examPaperId);
}
