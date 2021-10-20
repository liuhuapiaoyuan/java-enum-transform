/**
 * @(#)QuestionRecordingTaskStatus.java, 4月 12, 2021.
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
public enum QuestionRecordingTaskStage {

    INACTIVATED(-2,"暂不可用"),
    DELETED(-1,"被废弃"),
    RECORDED(1,"已录入"),
    TO_BE_AUDITED(2,"待审核"),
    AUDIT_APPROVED(3,"审核通过"),
    AUDIT_FAILED(4,"审核不通过"),
    TO_BE_TAGGED(5,"待标注"),
    TAG_FINISHED(6,"已被标注"),
    TAG_FAILED(7,"标注阶段纠错"),
    PUBLISHED(8,"已被发布");

    private int value;

    private String name;

    QuestionRecordingTaskStage(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private static List<Pair<QuestionRecordingTaskStage, QuestionRecordingTaskStage>> DIRECTED_EDGES_OF_STAGES = Lists.newArrayList();

    public static Optional<QuestionRecordingTaskStage> findByInt(int value) {
        for (QuestionRecordingTaskStage item : QuestionRecordingTaskStage.values()) {
            if (item.value == value) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public static Optional<QuestionRecordingTaskStage> findByString(String name) {
        for (QuestionRecordingTaskStage item : QuestionRecordingTaskStage.values()) {
            if (item.name.equals(name)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @JsonCreator
    public static QuestionRecordingTaskStage findNullableByString(String name) {
        for (QuestionRecordingTaskStage item : QuestionRecordingTaskStage.values()) {
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

    public static Set<QuestionRecordingTaskStage> getAvailableSourceStages(QuestionRecordingTaskStage toStage) {
        if (Objects.isNull(toStage)) {
            return Sets.newHashSet();
        }

        return DIRECTED_EDGES_OF_STAGES.stream()
                .filter(edge -> edge.getRight() == toStage)
                .map(Pair::getLeft)
                .collect(Collectors.toSet());
    }

    static { // edge: from => to
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(INACTIVATED, DELETED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(RECORDED, DELETED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TO_BE_AUDITED, DELETED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_APPROVED, DELETED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_FAILED, DELETED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TO_BE_TAGGED, DELETED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TAG_FINISHED, DELETED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TAG_FAILED, DELETED));

        DIRECTED_EDGES_OF_STAGES.add(Pair.of(INACTIVATED, RECORDED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(RECORDED, TO_BE_AUDITED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TO_BE_AUDITED, AUDIT_APPROVED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TO_BE_AUDITED, AUDIT_FAILED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_APPROVED, AUDIT_FAILED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_FAILED, AUDIT_APPROVED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_FAILED, TO_BE_AUDITED)); // 修改后重新提审
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(AUDIT_APPROVED, TO_BE_TAGGED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TO_BE_TAGGED, TAG_FINISHED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TO_BE_TAGGED, TAG_FAILED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TAG_FAILED, TO_BE_TAGGED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TAG_FINISHED, TAG_FAILED));
        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TAG_FAILED, TO_BE_AUDITED)); // 修改后重新提审

        DIRECTED_EDGES_OF_STAGES.add(Pair.of(TAG_FINISHED, PUBLISHED));
    }
}
