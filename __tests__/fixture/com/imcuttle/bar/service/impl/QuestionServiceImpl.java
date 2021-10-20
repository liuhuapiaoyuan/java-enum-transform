/**
 * @(#)QuestionServiceImpl.java, Apr 18, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.service.impl;

import com.fenbi.common.util.JsonUtils;
import com.imcuttle.bar.service.QuestionService;
import com.imcuttle.bar.exceptions.CheckFailedException;
import com.fenbi.tutor.cyberjungle.client.proxy.TutorCyberJungleProxy;
import com.fenbi.tutor.cyberjungle.thrift.ExamPointCheckResultEnum;
import com.fenbi.tutor.neoquestion.client.proxy.spi.TutorNeoQuestionProxy;
import com.fenbi.tutor.neoquestion.thrift.Question;
import com.fenbi.tutor.neoquestion.thrift.Vignette;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.imcuttle.bar.util.MapUtil.getOrEmpty;
import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.CONTENT_TYPE_UBB;
import static com.fenbi.tutor.neoquestion.thrift.TutorNeoQuestionConstants.QUESTION_VIGNETTE_ID_DIVISION_NUMBER;

/**
 * @author chenkangbj
 */
@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private TutorNeoQuestionProxy neoQuestionProxy;

    @Autowired
    private TutorCyberJungleProxy tutorCyberJungleProxy;

    @Override
    public void checkWholeQuestions(List<Integer> wholeQuestionIds) {
        List<Integer> vignetteIds = filterVignetteIds(wholeQuestionIds);
        List<Integer> questionIds = filterQuestionIds(wholeQuestionIds);

        // 查询所有大题 + 小题
        Map<Integer, Vignette> id2Vignette = neoQuestionProxy.getVignettes(vignetteIds, CONTENT_TYPE_UBB);
        List<Integer> subQuestionIds = id2Vignette.values().stream()
                .map(Vignette::getQuestionIds)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        questionIds.addAll(subQuestionIds);
        Map<Integer, Question> id2Question = neoQuestionProxy.getQuestions(questionIds, CONTENT_TYPE_UBB);
        List<Vignette> vignettes = Lists.newArrayList(id2Vignette.values());
        List<Question> questions = Lists.newLinkedList(id2Question.values());

        // 查询所有考点
        Map<Long, List<Integer>> examPointId2QuestionIds = groupQuestionsByExamPointId(questions);
        Map<Long, List<Integer>> examPointId2VignetteIds = groupVignettesByExamPointId(vignettes);
        Map<Long, List<Integer>> examPointId2WholeQuestionIds = mergeExamPointRelations(examPointId2QuestionIds, examPointId2VignetteIds);
        log.info("mergeExamPointRelations examPointId2QuestionIds = {}, examPointId2VignetteIds = {}, result = {}",
                examPointId2QuestionIds, examPointId2VignetteIds, examPointId2WholeQuestionIds);

        // 检查题目难度 是否全部设置
        checkDifficultyMarked(questions);

        // 检查题目使用的考点 是否有效
        checkExamPoints(examPointId2WholeQuestionIds);
    }

    @Override
    public boolean publishWholeQuestions(List<Integer> wholeQuestionIds) {
        List<Integer> vignetteIds = filterVignetteIds(wholeQuestionIds);
        List<Integer> questionIds = filterQuestionIds(wholeQuestionIds);

        boolean published = neoQuestionProxy.publishWholeQuestions(ListUtils.union(vignetteIds, questionIds));
        log.info("publishWholeQuestions published = {}, vignetteIds = {}, questionIds = {}",
                published, vignetteIds, questionIds);
        return published;
    }

    private void checkDifficultyMarked(List<Question> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }
        List<Integer> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        log.info("checkDifficultyMarked questionIds = {}", questionIds);
        List<Question> unmarkedQuestions = questions.stream()
                .filter(question -> question.getDifficulty() <= 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(unmarkedQuestions)) {
            return;
        }

        Set<Integer> illegalIds = new HashSet<>();
        unmarkedQuestions.forEach(question -> {
            if (question.getVignetteId() != 0) {
                illegalIds.add(question.getVignetteId());
            } else {
                illegalIds.add(question.getId());
            }
        });
        if (CollectionUtils.isNotEmpty(illegalIds)) {
            throw new CheckFailedException("存在未标记难度的题目：" + JsonUtils.writeValue(illegalIds));
        }
    }

    /**
     * 检查考点是否为有效
     *
     * @param examPointId2QuestionIdsMap
     * @return
     */
    private void checkExamPoints(Map<Long, List<Integer>> examPointId2QuestionIdsMap) {
        if (MapUtils.isEmpty(examPointId2QuestionIdsMap)) {
            return;
        }
        Map<Long, ExamPointCheckResultEnum> examPointId2Result = getOrEmpty(tutorCyberJungleProxy
                .checkExamPoints(examPointId2QuestionIdsMap.keySet()));
        log.info("checkExamPoints result = {}", examPointId2Result);
        if (examPointId2Result.values().stream().allMatch(resultEnum -> resultEnum == ExamPointCheckResultEnum.NORMAL)) {
            return;
        }

        StringBuilder msg = new StringBuilder("发布题目失败，");
        List<Integer> deprecatedMsgs = Lists.newArrayList();
        List<Integer> notExistedMsgs = Lists.newArrayList();
        for (long id : examPointId2Result.keySet()) {
            if (examPointId2Result.get(id) == ExamPointCheckResultEnum.DEPRECATED &&
                    examPointId2QuestionIdsMap.containsKey(id)) {
                deprecatedMsgs.addAll(examPointId2QuestionIdsMap.get(id));
            } else if (examPointId2Result.get(id) == ExamPointCheckResultEnum.NOT_EXISTED &&
                    examPointId2QuestionIdsMap.containsKey(id)) {
                notExistedMsgs.addAll(examPointId2QuestionIdsMap.get(id));
            }
        }

        deprecatedMsgs = deprecatedMsgs.stream().distinct().collect(Collectors.toList());
        notExistedMsgs = notExistedMsgs.stream().distinct().collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(deprecatedMsgs)) {
            msg.append("以下题目有考点或知识点被作废: ").append(Joiner.on(",").skipNulls().join(deprecatedMsgs)).append(" ");
        }
        if (!CollectionUtils.isEmpty(notExistedMsgs)) {
            msg.append("以下题目有考点或知识点不存在: ").append(Joiner.on(",").skipNulls().join(notExistedMsgs)).append(" ");
        }
        throw new CheckFailedException(msg.toString());
    }

    private Map<Long, List<Integer>> groupQuestionsByExamPointId(List<Question> questions) {
        Map<Long, List<Integer>> examPointId2QuestionIdsMap = Maps.newHashMap();
        for (Question question : questions) {
            if (CollectionUtils.isNotEmpty(question.getExamPointIds())) {
                question.getExamPointIds().forEach(examPointId ->
                        examPointId2QuestionIdsMap.computeIfAbsent(examPointId, k -> Lists.newArrayList()).add(question.getId()));
            }

            if (CollectionUtils.isNotEmpty(question.getBoutiqueExamPointIds())) {
                question.getBoutiqueExamPointIds().forEach(examPointId ->
                        examPointId2QuestionIdsMap.computeIfAbsent(examPointId, k -> Lists.newArrayList()).add(question.getId()));
            }
        }
        return examPointId2QuestionIdsMap;
    }

    private Map<Long, List<Integer>> groupVignettesByExamPointId(List<Vignette> vignettes) {
        Map<Long, List<Integer>> examPointId2VignetteIdsMap = Maps.newHashMap();
        for (Vignette question : vignettes) {
            if (CollectionUtils.isNotEmpty(question.getExamPointIds())) {
                question.getExamPointIds().forEach(examPointId ->
                        examPointId2VignetteIdsMap.computeIfAbsent(examPointId, k -> Lists.newArrayList()).add(question.getId()));
            }

            if (CollectionUtils.isNotEmpty(question.getBoutiqueExamPointIds())) {
                question.getBoutiqueExamPointIds().forEach(examPointId ->
                        examPointId2VignetteIdsMap.computeIfAbsent(examPointId, k -> Lists.newArrayList()).add(question.getId()));
            }
        }
        return examPointId2VignetteIdsMap;
    }

    /**
     * 合并考点 - 题目映射
     *
     * @param examPointId2QuestionIds
     * @param examPointId2VignetteIds
     * @return
     */
    private Map<Long, List<Integer>> mergeExamPointRelations(Map<Long, List<Integer>> examPointId2QuestionIds,
                                                             Map<Long, List<Integer>> examPointId2VignetteIds) {
        Map<Long, List<Integer>> result = Maps.newHashMap();
        result.putAll(examPointId2QuestionIds);

        examPointId2VignetteIds.forEach((examPointId, vignetteIds) ->
                result.merge(examPointId, vignetteIds, (oldExamPointId, oldIds) -> {
                    oldIds.addAll(vignetteIds);
                    return oldIds;
                })
        );
        return result;
    }

    private List<Integer> filterVignetteIds(List<Integer> wholeQuestionIds) {
        return wholeQuestionIds.stream()
                .filter(id -> id >= QUESTION_VIGNETTE_ID_DIVISION_NUMBER)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Integer> filterQuestionIds(List<Integer> wholeQuestionIds) {
        return wholeQuestionIds.stream()
                .filter(id -> id < QUESTION_VIGNETTE_ID_DIVISION_NUMBER)
                .distinct()
                .collect(Collectors.toList());
    }
}
