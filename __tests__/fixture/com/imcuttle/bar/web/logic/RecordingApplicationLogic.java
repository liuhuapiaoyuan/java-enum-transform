/**
 * @(#)RecordingApplicationLogic.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.web.logic;

import com.fenbi.commons.paging.Page;
import com.imcuttle.bar.web.data.AuditorVO;
import com.imcuttle.bar.web.data.RecorderVO;
import com.imcuttle.bar.web.data.RecordingApplicationBasicSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationBasicVO;
import com.imcuttle.bar.web.data.RecordingApplicationBriefVO;
import com.imcuttle.bar.web.data.RecordingApplicationCancelParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationManageSearchParamVO;
import com.imcuttle.bar.web.data.RecordingApplicationProgressVO;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author linbonan
 */
public interface RecordingApplicationLogic {

    Page<RecordingApplicationBasicVO> searchBasicInfoList(RecordingApplicationBasicSearchParamVO searchParamVO);

    long save(RecordingApplicationBasicVO applicationBasicVO);

    void cancel(long id, RecordingApplicationCancelParamVO cancelParamVO);

    Map<Long, RecordingApplicationBriefVO> batchGetRecordingApplicationBriefInfo(Collection<Long> ids);

    Page<RecordingApplicationProgressVO> searchApplicationsConditionally(RecordingApplicationManageSearchParamVO searchParamVO);

    void publishApplication(long applicationId);

    List<RecorderVO> getRecorders(long applicationId);

    List<AuditorVO> getAuditors(long applicationId);
}
