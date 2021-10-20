/**
 * @(#)PaperServiceImpl.java, Apr 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.bar.service.PaperService;
import com.fenbi.tutor.neoquestion.client.proxy.spi.TutorNeoQuestionProxy;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chenkangbj
 */
@Service
@Slf4j
public class PaperServiceImpl implements PaperService {

    @Autowired
    private TutorNeoQuestionProxy neoQuestionProxy;

    @Override
    public boolean publishArmoryPapers(List<Long> paperIds) {
        log.info("publishArmoryPapers paperIds = {}", paperIds);
        return neoQuestionProxy.batchPublishExamPapers(Lists.newArrayList(paperIds));
    }
}
