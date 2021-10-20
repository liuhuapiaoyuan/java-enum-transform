/**
 * @(#)ExamPaperRecordingTaskStatus.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.foo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linbonan
 */
public enum ExamPaperRecordingTaskStage {

    DELETED(-1, "被废弃"),
    RECORDED(1, "已录入"),
    TO_BE_AUDITED(2, "待审核"),
    AUDIT_APPROVED(3, "审核通过"),
    AUDIT_FAILED(4, "审核不通过"),
    PUBLISHED(5, "已被发布");

    private int value;

    private String name;

    ExamPaperRecordingTaskStage(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private static List<Pair<ExamPaperRecordingTaskStage, ExamPaperRecordingTaskStage>> DIRECTED_EDGES_OF_STAGES = Lists.newArrayList();

    public static Optional<ExamPaperRecordingTaskStage> findByInt(int value) {
        for (ExamPaperRecordingTaskStage item : ExamPaperRecordingTaskStage.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<ExamPaperRecordingTaskStage> findByString(String name) {
        for (ExamPaperRecordingTaskStage item : ExamPaperRecordingTaskStage.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static ExamPaperRecordingTaskStage findNullableByString(String name) {
        for (ExamPaperRecordingTaskStage item : ExamPaperRecordingTaskStage.values()) {
            if (item.name.equals(name)) {
                return item;
            }
        }

        return null;
    }

    @JsonValue
    public String toString() {
        return this.name;
    }

    public int toInt() {
        return this.value;
    }

    public static Set<ExamPaperRecordingTaskStage> getAvailableSourceStages(ExamPaperRecordingTaskStage toStage) {
        if (Objects.isNull(toStage)) {
            return Sets.newHashSet();
        }

        return DIRECTED_EDGES_OF_STAGES.stream()
                .filter(edge -> edge.getRight() == toStage)
                .map(Pair::getLeft)
                .collect(Collectors.toSet());
    }

    static { // edge: from => to
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(RECORDED, DELETED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(RECORDED, TO_BE_AUDITED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TO_BE_AUDITED, AUDIT_APPROVED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TO_BE_AUDITED, AUDIT_FAILED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_APPROVED, AUDIT_FAILED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_FAILED, AUDIT_APPROVED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_FAILED, TO_BE_AUDITED)); // 修改后重新提审
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_APPROVED, PUBLISHED));
    }
}
