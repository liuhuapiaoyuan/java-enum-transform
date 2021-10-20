/**
 * @(#)RecordingTaskLogDiffService.java, Apr 21, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.enums.RecordingMode;
import com.imcuttle.bar.service.UserService;
import com.imcuttle.bar.service.diff.DefaultFormatter;
import com.imcuttle.bar.service.diff.LongDateFormatter;
import com.imcuttle.bar.service.diff.PropertyFormatter;
import com.imcuttle.bar.service.diff.UserNameFormatter;
import com.imcuttle.thrift.RecordingTask;
import com.fenbi.tutor.common.constant.StudyPhase;
import com.fenbi.tutor.common.constant.Subject;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * @author chenkangbj
 */
@Service
@Qualifier("recordingTaskLogDiffService")
public class RecordingTaskLogDiffService extends AbstractLogDiffService<RecordingTask> {

    @Autowired
    private UserService userService;

    private static final Map<String, String> NEED_DIFF_PROPERTY_NAMES_MAP = ImmutableMap.<String, String>builder()
            .put("phaseId", "阶段")
            .put("subjectId", "科目")
            .put("estimatedQuestionNum", "应录题数")
            .put("recordingMode", "录入模式")
            .put("stage", "状态")
            .put("submitTimes", "审核次数")
            .put("recorder", "录题人")
            .put("auditor", "审核人")
            .put("tagger", "标注人")
            .build();

    private Map<String, PropertyFormatter> property2Formatter = ImmutableMap.of();

    @PostConstruct
    private void initFormatterMap() {
        property2Formatter = ImmutableMap.<String, PropertyFormatter>builder()
                .put("phaseId", phaseId -> StudyPhase.findByInt((int) phaseId).orElse(StudyPhase.UNKNOWN).toChinese())
                .put("subjectId", subjectId -> Subject.findById((int) subjectId).map(Subject::getName).orElse("未知"))
                .put("recordingMode", mode -> RecordingMode.findByInt((int) mode)
                        .map(RecordingMode::toString)
                        .orElse("未知"))
                .put("stage", stage -> RecordingApplicationStage.findByInt((int) stage)
                        .map(RecordingApplicationStage::toString)
                        .orElse("未知"))
                .put("recorder", new UserNameFormatter(userService))
                .put("auditor", new UserNameFormatter(userService))
                .put("tagger", new UserNameFormatter(userService))
                .put("applicationSubmitTime", new LongDateFormatter())
                .build();
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
    protected Class<RecordingTask> getBeanClass() {
        return RecordingTask.class;
    }

    @Override
    public String getFieldName(String propertyName) {
        return NEED_DIFF_PROPERTY_NAMES_MAP.getOrDefault(propertyName, "未知");
    }
}
