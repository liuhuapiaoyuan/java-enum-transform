/**
 * @(#)SortRegistry.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.data;

import com.imcuttle.bar.enums.SortTypeEnum;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.imcuttle.es.RecordingApplicationDocument.FIELD_COMPLETE_DURATION;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_MAX_TASK_SUBMIT_TIMES;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_PUBLISH_TIME;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_RECORDED_QUESTION;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_SUBMIT_TIME;
import static com.imcuttle.bar.enums.SortTypeEnum.COMPLETE_DURATION_ASC;
import static com.imcuttle.bar.enums.SortTypeEnum.COMPLETE_DURATION_DESC;
import static com.imcuttle.bar.enums.SortTypeEnum.PUBLISH_TIME_ASC;
import static com.imcuttle.bar.enums.SortTypeEnum.PUBLISH_TIME_DESC;
import static com.imcuttle.bar.enums.SortTypeEnum.RECORDED_QUESTION_COUNT_ASC;
import static com.imcuttle.bar.enums.SortTypeEnum.RECORDED_QUESTION_COUNT_DESC;
import static com.imcuttle.bar.enums.SortTypeEnum.SUBMIT_TIME_ASC;
import static com.imcuttle.bar.enums.SortTypeEnum.SUBMIT_TIME_DESC;
import static com.imcuttle.bar.enums.SortTypeEnum.TASK_SUBMIT_TIMES_ASC;
import static com.imcuttle.bar.enums.SortTypeEnum.TASK_SUBMIT_TIMES_DESC;

/**
 * @author linbonan
 */
public class SortRegistry {

    public static final Map<SortTypeEnum, List<SortBuilder<?>>> sortImplMap = Maps.newHashMap();

    static {
        List<SortBuilder<?>> submitTimeAscSorts = new ArrayList<>();
        sortImplMap.put(SUBMIT_TIME_ASC, submitTimeAscSorts);
        submitTimeAscSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.ASC));

        List<SortBuilder<?>> submitTimeDescSorts = new ArrayList<>();
        sortImplMap.put(SUBMIT_TIME_DESC, submitTimeDescSorts);
        submitTimeDescSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));

        List<SortBuilder<?>> publishTimeAscSorts = new ArrayList<>();
        sortImplMap.put(PUBLISH_TIME_ASC, publishTimeAscSorts);
        publishTimeAscSorts.add(SortBuilders.fieldSort(FIELD_PUBLISH_TIME).order(SortOrder.ASC).missing("_last"));
        publishTimeAscSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));

        List<SortBuilder<?>> publishTimeDescSorts = new ArrayList<>();
        sortImplMap.put(PUBLISH_TIME_DESC, publishTimeDescSorts);
        publishTimeDescSorts.add(SortBuilders.fieldSort(FIELD_PUBLISH_TIME).order(SortOrder.DESC).missing("_last"));
        publishTimeDescSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));

        List<SortBuilder<?>> taskSubmitTimesAscSorts = new ArrayList<>();
        sortImplMap.put(TASK_SUBMIT_TIMES_ASC, taskSubmitTimesAscSorts);
        taskSubmitTimesAscSorts.add(SortBuilders.fieldSort(FIELD_MAX_TASK_SUBMIT_TIMES).order(SortOrder.ASC));
        taskSubmitTimesAscSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));

        List<SortBuilder<?>> taskSubmitTimesDescSorts = new ArrayList<>();
        sortImplMap.put(TASK_SUBMIT_TIMES_DESC, taskSubmitTimesDescSorts);
        taskSubmitTimesDescSorts.add(SortBuilders.fieldSort(FIELD_MAX_TASK_SUBMIT_TIMES).order(SortOrder.DESC));
        taskSubmitTimesDescSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));

        List<SortBuilder<?>> recordedQuestionAscSorts = new ArrayList<>();
        sortImplMap.put(RECORDED_QUESTION_COUNT_ASC, recordedQuestionAscSorts);
        recordedQuestionAscSorts.add(SortBuilders.fieldSort(FIELD_RECORDED_QUESTION).order(SortOrder.ASC));
        recordedQuestionAscSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));

        List<SortBuilder<?>> recordedQuestionDescSorts = new ArrayList<>();
        sortImplMap.put(RECORDED_QUESTION_COUNT_DESC, recordedQuestionDescSorts);
        recordedQuestionDescSorts.add(SortBuilders.fieldSort(FIELD_RECORDED_QUESTION).order(SortOrder.DESC));
        recordedQuestionDescSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));

        List<SortBuilder<?>> completeDurationAscSorts = new ArrayList<>();
        sortImplMap.put(COMPLETE_DURATION_ASC, completeDurationAscSorts);
        completeDurationAscSorts.add(SortBuilders.fieldSort(FIELD_COMPLETE_DURATION).order(SortOrder.ASC).missing("_last"));
        completeDurationAscSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));

        List<SortBuilder<?>> completeDurationDescSorts = new ArrayList<>();
        sortImplMap.put(COMPLETE_DURATION_DESC, completeDurationDescSorts);
        completeDurationDescSorts.add(SortBuilders.fieldSort(FIELD_COMPLETE_DURATION).order(SortOrder.DESC).missing("_last"));
        completeDurationDescSorts.add(SortBuilders.fieldSort(FIELD_SUBMIT_TIME).order(SortOrder.DESC));
    }
}
