/**
 * @(#)RecordingApplicationDiffServiceImpl.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.enums.RecordingApplicationQuestionSource;
import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.bar.service.diff.DefaultFormatter;
import com.imcuttle.bar.service.diff.LongDateFormatter;
import com.imcuttle.bar.service.diff.PropertyFormatter;
import com.imcuttle.thrift.RecordingApplication;
import com.fenbi.tutor.common.constant.StudyPhase;
import com.fenbi.tutor.common.constant.Subject;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * @author chenkangbj
 */
@Service
@Qualifier("recordingApplicationDiffService")
public class RecordingApplicationDiffService extends AbstractLogDiffService<RecordingApplication> {

    private static final Map<String, String> NEED_DIFF_PROPERTY_NAMES_MAP = ImmutableMap.<String, String>builder()
            .put("phaseId", "阶段")
            .put("subjectId", "科目")
            .put("name", "名称")
            .put("questionSource", "题目来源")
            .put("secrecyEndTime", "保密时间")
            .put("estimatedQuestionNum", "应录题数")
            .put("resourceId", "资源id")
            .put("fileName", "题目原件")
            .put("stage", "状态")
            .put("remark", "备注")
            .build();


    private Map<String, PropertyFormatter> property2Formatter;

    @PostConstruct
    private void initFormatterMap() {
        property2Formatter = ImmutableMap.of(
                "secrecyEndTime", new LongDateFormatter(),
                "phaseId", phaseId -> StudyPhase.findByInt((int) phaseId).orElse(StudyPhase.UNKNOWN).toChinese(),
                "subjectId", subjectId -> Subject.findById((int) subjectId).map(Subject::getName).orElse("未知"),
                "questionSource", questionSource -> RecordingApplicationQuestionSource.findByInt((int) questionSource)
                        .map(RecordingApplicationQuestionSource::toString)
                        .orElse("未知"),
                "stage", stage -> RecordingApplicationStage.findByInt((int) stage)
                        .map(RecordingApplicationStage::toString)
                        .orElse("未知")
        );
    }

    @Override
    public Set<String> getAllPropertyNames() {
        return NEED_DIFF_PROPERTY_NAMES_MAP.keySet();
    }

    @Override
    public PropertyFormatter getFormatterForProperty(String propertyName) {
        return property2Formatter.getOrDefault(propertyName, new DefaultFormatter());
    }

    @Override
    protected Class<RecordingApplication> getBeanClass() {
        return RecordingApplication.class;
    }

    @Override
    public String getFieldName(String propertyName) {
        return NEED_DIFF_PROPERTY_NAMES_MAP.getOrDefault(propertyName, "未知");
    }
}
