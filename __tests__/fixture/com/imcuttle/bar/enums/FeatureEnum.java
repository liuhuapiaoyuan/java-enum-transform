/**
 * @(#)FeatureEnum.java, Apr 15, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.enums;

import lombok.Getter;

import java.util.Optional;

/**
 * @author duanou
 */
@Getter
public enum FeatureEnum {

    RECORDING_APPLICATION("RecordingApplication", "录题申请"),
    RECORDING_APPLICATION_OF_OTHERS("RecordingApplicationOfOthers", "其他人创建的录题申请"),
    RECORDING_APPLICATION_AUDIT("RecordingApplicationAudit", "录题申请审核"),
    RECORDING_APPLICATION_TASK_ASSIGN("RecordingApplicationTaskAssign", "录题申请任务分配"),
    QUESTION_RECORDING("QuestionRecording", "题目录入"),
    QUESTION_AUDIT("QuestionAudit", "题目审核"),
    QUESTION_TAG("QuestionTag", "题目标注"),
    QUESTION_RECORDING_OF_OTHERS("QuestionRecordingOfOthers", "题目录入(分配给其他人的)"),
    QUESTION_AUDIT_OF_OTHERS("QuestionAuditOfOthers", "题目审核(分配给其他人的)"),
    ALL_RECORDING_APPLICATIONS("AllRecordingApplications", "全部录题申请")
    ;

    private String key;

    private String name;

    FeatureEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public static Optional<FeatureEnum> findByKey(String key) {
        for (FeatureEnum featureEnum : FeatureEnum.values()) {
            if (featureEnum.key.equals(key)) {
                return Optional.of(featureEnum);
            }
        }
        return Optional.empty();
    }
}
