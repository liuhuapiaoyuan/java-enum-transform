/**
 * @(#)RecordingApplicationSearchServiceImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.common.elastic6.client.config.EsSearchParam;
import com.fenbi.commons.paging.Page;
import com.fenbi.commons.paging.PageConfig;
import com.imcuttle.enums.RecordingApplicationQuestionSource;
import com.imcuttle.enums.RecordingApplicationStage;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.data.RecordingApplicationSearchCondition;
import com.imcuttle.bar.data.SortRegistry;
import com.imcuttle.bar.enums.SortTypeEnum;
import com.imcuttle.bar.exceptions.FailedEsQueryException;
import com.imcuttle.bar.service.RecordingApplicationSearchService;
import com.imcuttle.bar.web.data.RecordingTagProcess;
import com.fenbi.tutor.common.constant.Subject;
import com.fenbi.tutor.common.util.StudyPhaseUtils;
import com.fenbi.tutor.cybersearch.client.proxy.TutorCyberSearchProxy;
import com.fenbi.tutor.cybersearch.thrift.SearchHit;
import com.fenbi.tutor.cybersearch.thrift.SearchResult;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.imcuttle.es.RecordingApplicationDocument.FIELD_CREATOR;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_CREATOR_NAME;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_ID;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_NAME;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_PHASE_ID;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_QUESTION_SOURCE;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_STAGE;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_SUBJECT_ID;
import static com.imcuttle.es.RecordingApplicationDocument.FIELD_SUBMIT_TIME;
import static com.imcuttle.es.RecordingApplicationDocument.MAX_KEYWORD_SIZE;
import static com.imcuttle.es.RecordingApplicationDocument.MAX_WINDOW_SIZE;
import static com.imcuttle.es.RecordingApplicationDocument.NESTED_FIELD_AUDITOR_NAME;
import static com.imcuttle.es.RecordingApplicationDocument.NESTED_FIELD_RECORDER_NAME;
import static com.imcuttle.es.RecordingApplicationDocument.NESTED_FIELD_TAGGER_NAME;
import static com.imcuttle.es.RecordingApplicationDocument.NESTED_FIELD_TAG_PROCESS;
import static com.imcuttle.es.RecordingApplicationDocument.NESTED_FIELD_TASK_ID;
import static com.imcuttle.es.RecordingApplicationDocument.NESTED_FIELD_TASK_STAGE;
import static com.imcuttle.es.RecordingApplicationDocument.NESTED_PATH_TAGGER_NAME;
import static com.imcuttle.es.RecordingApplicationDocument.RECORDING_APPLICATION_INDEX_NAME;
import static com.imcuttle.es.RecordingApplicationDocument.WILDCARD_CODE;
import static com.imcuttle.thrift.TutorArmoryConstants.INT_ID_NO_LIMIT;

/**
 * @author linbonan
 */
@Slf4j
@Service
public class RecordingApplicationSearchServiceImpl implements RecordingApplicationSearchService {

    @Autowired
    private TutorCyberSearchProxy tutorCyberSearchProxy;

    @Override
    public Page<Long> searchIdList(RecordingApplicationSearchCondition searchCondition) {
        EsSearchParam searchParam = buildEsSearchParam(searchCondition);
        log.debug("ES查询语句: {}", searchParam.toString());
        try {
            return doSearch(searchCondition.getPage(), searchCondition.getPageSize(), searchParam);
        } catch (Exception e) {
            log.error("从ES查询录题申请ID失败, searchCondition = {}, searchParam = {}", searchCondition, searchParam, e);
            throw new FailedEsQueryException("从ES查询录题申请ID失败");
        }
    }

    private Page<Long> doSearch(int page, int pageSize, EsSearchParam searchParam) throws Exception {
        PageConfig pageConfig = new PageConfig(page, pageSize);
        Page<Long> pageResult = new Page<>(0, pageConfig);
        pageResult.setList(Lists.newArrayList());

        if (pageConfig.getPageSize() * (pageConfig.getCurrentPage() + 1) > MAX_WINDOW_SIZE) {
            //避免ES的深分页问题
            return pageResult;
        }

        SearchResult searchResult = tutorCyberSearchProxy.searchByEsSearchParam(searchParam, pageConfig.getCurrentPage(), pageConfig.getPageSize());
        if (CollectionUtils.isEmpty(searchResult.getSearchHits())) {
            return pageResult;
        }

        List<Long> hitIds = searchResult.getSearchHits().stream().map(SearchHit::getId).collect(Collectors.toList());
        pageResult.setList(hitIds);
        pageResult.getPageInfo().setTotalPage(searchResult.getPageInfo().getTotalPage());
        pageResult.getPageInfo().setTotalItem((int) searchResult.getPageInfo().getTotalItem());
        return pageResult;
    }

    /**
     * 构造录题申请的 ES 搜索语句
     *
     * @param searchCondition
     * @return
     */
    private EsSearchParam buildEsSearchParam(RecordingApplicationSearchCondition searchCondition) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        wrapBasicCondition(query, searchCondition);
        wrapTaskCondition(query, searchCondition);
        wrapAuthCondition(query, searchCondition);

        if (StringUtils.isBlank(searchCondition.getSort())) {
            searchCondition.setSort(SortTypeEnum.SUBMIT_TIME_ASC.toString());
        }

        return new EsSearchParam.Builder()
                .setIndex(RECORDING_APPLICATION_INDEX_NAME)
                .setSearchSourceBuilder(fromCondition(searchCondition))
                .setQueryBuilder(query)
                .build();
    }

    /**
     * 从搜索条件 构造分页条件 & 排序条件
     *
     * @param searchCondition
     * @return
     */
    private SearchSourceBuilder fromCondition(RecordingApplicationSearchCondition searchCondition) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .from(searchCondition.getPage() * searchCondition.getPageSize())
                .size(searchCondition.getPageSize())
                .fetchSource("id", null);
        SortTypeEnum.findByString(searchCondition.getSort())
                .map(SortRegistry.sortImplMap::get)
                .ifPresent(sortList -> sortList.forEach(sourceBuilder::sort));
        return sourceBuilder;
    }

    /**
     * 包装基本的录题申请搜索条件
     *
     * @param query
     * @param searchCondition
     */
    private void wrapBasicCondition(BoolQueryBuilder query, RecordingApplicationSearchCondition searchCondition) {
        if (StudyPhaseUtils.isValidStudyPhase(searchCondition.getPhaseId())) {
            query.must(QueryBuilders.termQuery(FIELD_PHASE_ID, searchCondition.getPhaseId()));
        }
        Subject.findById(searchCondition.getSubjectId()).ifPresent(subject -> query.must(new TermQueryBuilder(FIELD_SUBJECT_ID, subject.getId())));
        RecordingApplicationQuestionSource.findByInt(searchCondition.getQuestionSource()).ifPresent(source -> query.must(new TermQueryBuilder(FIELD_QUESTION_SOURCE, source.toInt())));
        RecordingApplicationStage.findByInt(searchCondition.getStage()).ifPresent(stage -> query.must(new TermQueryBuilder(FIELD_STAGE, stage.toInt())));

        if (searchCondition.getId() > 0L) {
            query.must(QueryBuilders.termQuery(FIELD_ID, searchCondition.getId()));
        }

        if (searchCondition.getCreator() > 0) {
            query.must(QueryBuilders.termQuery(FIELD_CREATOR, searchCondition.getCreator()));
        }

        if (StringUtils.isNotBlank(searchCondition.getCreatorName())) {
            String creatorName = searchCondition.getCreatorName();
            if (creatorName.length() > MAX_KEYWORD_SIZE) {
                creatorName = creatorName.substring(0, MAX_KEYWORD_SIZE);
            }
            query.must(QueryBuilders.termQuery(FIELD_CREATOR_NAME, creatorName));
        }

        if (searchCondition.getSubmitStartTime() > 0L
                && searchCondition.getSubmitEndTime() > 0L
                && searchCondition.getSubmitStartTime() <= searchCondition.getSubmitEndTime()) {
            query.must(new RangeQueryBuilder(FIELD_SUBMIT_TIME).gte(searchCondition.getSubmitStartTime()).lte(searchCondition.getSubmitEndTime()));
        }

        if (StringUtils.isNotBlank(searchCondition.getKeyword())) {
            String name = searchCondition.getKeyword();
            if (name.length() > MAX_KEYWORD_SIZE) {
                name = name.substring(0, MAX_KEYWORD_SIZE);
            }
            query.must(new WildcardQueryBuilder(FIELD_NAME, WILDCARD_CODE + name + WILDCARD_CODE));
        }
    }

    private boolean isTagFinished(int tagProcess) {
        return RecordingTagProcess.FINISHED.toInt() == tagProcess;
    }

    private void wrapTaskCondition(BoolQueryBuilder query, RecordingApplicationSearchCondition searchCondition) {
        BoolQueryBuilder nestedQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(searchCondition.getRecorderName())) {
            nestedQuery.must(QueryBuilders.termQuery(NESTED_FIELD_RECORDER_NAME, searchCondition.getRecorderName()));
        }
        if (StringUtils.isNotBlank(searchCondition.getAuditorName())) {
            nestedQuery.must(QueryBuilders.termQuery(NESTED_FIELD_AUDITOR_NAME, searchCondition.getAuditorName()));
        }
        if (StringUtils.isNotBlank(searchCondition.getTaggerName())) {
            nestedQuery.must(QueryBuilders.termQuery(NESTED_FIELD_TAGGER_NAME, searchCondition.getTaggerName()));
        }
        if (searchCondition.getTaskId() > 0) {
            nestedQuery.must(QueryBuilders.termQuery(NESTED_FIELD_TASK_ID, searchCondition.getTaskId()));
        }
        RecordingTagProcess.findByInt(searchCondition.getTagProcess()).ifPresent(tagProcess -> {
            nestedQuery.must(QueryBuilders.termQuery(NESTED_FIELD_TAG_PROCESS, isTagFinished(searchCondition.getTagProcess())));
        });
        RecordingTaskStage.findByInt(searchCondition.getTaskStage())
                .ifPresent(taskStage -> {
                    nestedQuery.must(QueryBuilders.termQuery(NESTED_FIELD_TASK_STAGE, taskStage.toInt()));
                });

        if (nestedQuery.hasClauses()) {
            QueryBuilder outerQuery = QueryBuilders.nestedQuery(NESTED_PATH_TAGGER_NAME, nestedQuery, ScoreMode.None);
            query.must(outerQuery);
        }
    }

    private void wrapAuthCondition(BoolQueryBuilder query, RecordingApplicationSearchCondition searchCondition) {
        BoolQueryBuilder filterQuery = QueryBuilders.boolQuery();
        if (CollectionUtils.isNotEmpty(searchCondition.getAuthorizedSubjectPhases())) {
            searchCondition.getAuthorizedSubjectPhases()
                    .forEach(pair -> {
                        BoolQueryBuilder subjectPhaseQuery = QueryBuilders.boolQuery();
                        if (pair.getSubjectId() == INT_ID_NO_LIMIT) {
                            subjectPhaseQuery.must(QueryBuilders.rangeQuery(FIELD_SUBJECT_ID).gt(INT_ID_NO_LIMIT));
                        } else if (Subject.findById(pair.getSubjectId()).isPresent()) {
                            subjectPhaseQuery.must(QueryBuilders.termQuery(FIELD_SUBJECT_ID, pair.getSubjectId()));
                        }

                        if (pair.getPhaseId() == INT_ID_NO_LIMIT) {
                            subjectPhaseQuery.must(QueryBuilders.rangeQuery(FIELD_PHASE_ID).gt(INT_ID_NO_LIMIT));
                        } else if (StudyPhaseUtils.isValidStudyPhase(pair.getPhaseId())) {
                            subjectPhaseQuery.must(QueryBuilders.termQuery(FIELD_PHASE_ID, pair.getPhaseId()));
                        }

                        if (subjectPhaseQuery.hasClauses()) {
                            filterQuery.should(subjectPhaseQuery);
                        }
                    });
        }

        if (filterQuery.hasClauses()) {
            query.filter(filterQuery);
        }
    }
}
