/**
 * @(#)RecordingTaskStorageImpl.java, Apr 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fenbi.common.data.Pair;
import com.fenbi.common.util.JsonUtils;
import com.imcuttle.enums.RecordingTaskStage;
import com.imcuttle.bar.storage.db.BaseDbStorage;
import com.imcuttle.bar.storage.db.RecordingTaskStorage;
import com.imcuttle.thrift.RecordingTask;
import com.imcuttle.thrift.TaskAuditSubmitDetail;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author chenkangbj
 */
@Repository
@Slf4j
public class RecordingTaskStorageImpl extends BaseDbStorage implements RecordingTaskStorage {

    private static final String TABLE_NAME = "`recording_task`";

    private static final String COLUMNS_WITHOUT_ID = "`phaseId`, `subjectId`, `applicationId`, `estimatedQuestionNum`, `recordingMode`, `recorder`, `auditor`, `tagger`, `stage`, `submitTimes`, `applicationSubmitTime`, `taskAuditSubmitDetails`";

    private static final String ALL_COLUMNS = "`id`, " + COLUMNS_WITHOUT_ID;

    private static final RowMapper<RecordingTask> ROW_MAPPER = ((rs, rowNum) -> {
        RecordingTask recordingTask = new RecordingTask();

        recordingTask.setId(rs.getLong("id"));
        recordingTask.setPhaseId(rs.getInt("phaseId"));
        recordingTask.setSubjectId(rs.getInt("subjectId"));
        recordingTask.setApplicationId(rs.getLong("applicationId"));
        recordingTask.setEstimatedQuestionNum(rs.getInt("estimatedQuestionNum"));
        recordingTask.setRecordingMode(rs.getInt("recordingMode"));
        recordingTask.setRecorder(rs.getInt("recorder"));
        recordingTask.setAuditor(rs.getInt("auditor"));
        recordingTask.setTagger(rs.getInt("tagger"));
        recordingTask.setStage(rs.getInt("stage"));
        recordingTask.setSubmitTimes(rs.getInt("submitTimes"));
        recordingTask.setApplicationSubmitTime(rs.getLong("applicationSubmitTime"));

        String submitterJson = rs.getString("taskAuditSubmitDetails");
        if (StringUtils.isNotBlank(submitterJson)) {
            recordingTask.setTaskAuditSubmitDetails(JsonUtils.readValue(submitterJson, new TypeReference<List<TaskAuditSubmitDetail>>() {
            }));
        } else {
            recordingTask.setTaskAuditSubmitDetails(Lists.newArrayList());
        }

        return recordingTask;
    });

    private MapSqlParameterSource getSqlParamSource(RecordingTask recordingTask) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("id", recordingTask.getId());
        source.addValue("phaseId", recordingTask.getPhaseId());
        source.addValue("subjectId", recordingTask.getSubjectId());
        source.addValue("applicationId", recordingTask.getApplicationId());
        source.addValue("estimatedQuestionNum", recordingTask.getEstimatedQuestionNum());
        source.addValue("recordingMode", recordingTask.getRecordingMode());
        source.addValue("recorder", recordingTask.getRecorder());
        source.addValue("auditor", recordingTask.getAuditor());
        source.addValue("tagger", recordingTask.getTagger());
        source.addValue("stage", recordingTask.getStage());
        source.addValue("submitTimes", recordingTask.getSubmitTimes());
        source.addValue("applicationSubmitTime", recordingTask.getApplicationSubmitTime());
        source.addValue("taskAuditSubmitDetails", JsonUtils.writeValue(CollectionUtils.isEmpty(recordingTask.getTaskAuditSubmitDetails()) ? Lists.newArrayList() : recordingTask.getTaskAuditSubmitDetails()));
        return source;
    }

    @Override
    public boolean update(long recordingTaskId, int estimatedQuestionNum, int recorderId, int auditorId) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                " estimatedQuestionNum = :estimatedQuestionNum, " +
                " recorder = :recorder, " +
                " auditor = :auditor " +
                " WHERE id = :id";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("id", recordingTaskId);
        source.addValue("estimatedQuestionNum", estimatedQuestionNum);
        source.addValue("recorder", recorderId);
        source.addValue("auditor", auditorId);
        return dbClient.update(sql, source) > 0;
    }

    @Override
    public List<Long> batchCreate(List<RecordingTask> recordingTasks) {
        if (CollectionUtils.isEmpty(recordingTasks)) {
            return new ArrayList<>();
        }
        String sql = "INSERT INTO " + TABLE_NAME + " SET " +
                " phaseId = :phaseId, " +
                " subjectId = :subjectId, " +
                " applicationId = :applicationId, " +
                " estimatedQuestionNum = :estimatedQuestionNum, " +
                " recordingMode = :recordingMode, " +
                " recorder = :recorder, " +
                " auditor = :auditor, " +
                " tagger = :tagger, " +
                " stage = :stage, " +
                " taskAuditSubmitDetails = :taskAuditSubmitDetails, " +
                " submitTimes = :submitTimes, " +
                " applicationSubmitTime = :applicationSubmitTime";
        List<SqlParameterSource> sourceList = recordingTasks.stream().map(task -> getSqlParamSource(task)).collect(Collectors.toList());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int[] results = dbClient.batchUpdateWithSqlParamSource(sql, sourceList, keyHolder);

        List<Long> ids = keyHolder.getKeyList().stream().map(fieldValueMap -> fieldValueMap.values().iterator().next())
                .filter(key -> key instanceof Number).map(key -> ((Number) key).longValue())
                .collect(toList());

        for (int index = 0; index < recordingTasks.size(); ++index) {
            recordingTasks.get(index).setId(ids.get(index));
        }
        return ids;
    }

    @Override
    public Map<Integer, Integer> countTasksByAuditor(int auditorId, List<Integer> stages) {
        String sql = String.format("SELECT stage, COUNT(*) AS count FROM %s " +
                "WHERE `auditor` = :auditor AND `stage` IN (:stages) " +
                "GROUP BY `stage`", TABLE_NAME);
        return dbClient.queryForMap(sql,
                ImmutableMap.of("auditor", auditorId, "stages", stages),
                (rs, rowNum) -> {
                    Pair<Integer, Integer> pair = new Pair<>();
                    pair.setFirst(rs.getInt("stage"));
                    pair.setSecond(rs.getInt("count"));
                    return pair;
                });
    }

    @Override
    public RecordingTask getById(long taskId) {
        String sql = String.format("SELECT %s FROM %s WHERE `id` = :id", ALL_COLUMNS, TABLE_NAME);
        try {
            return dbClient.queryForObject(sql, ImmutableMap.of("id", taskId), ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<RecordingTask> getByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id in (:ids)";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("ids", ids);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public Map<Integer, Integer> countByRecorder(int userId, Collection<Integer> stageList) {
        String sql = "SELECT stage, COUNT(*) AS num FROM " + TABLE_NAME + " WHERE `recorder` = :userId AND stage IN (:stageList) GROUP BY stage";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("userId", userId);
        source.addValue("stageList", stageList);
        return dbClient.queryForMap(sql, source, (rs, rowNum) -> {
            Pair<Integer, Integer> pair = new Pair<>();
            pair.setFirst(rs.getInt("stage"));
            pair.setSecond(rs.getInt("num"));
            return pair;
        });
    }

    @Override
    public List<RecordingTask> getByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Lists.newArrayList();
        }

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE applicationId in (:applicationIds)";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("applicationIds", applicationIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public List<RecordingTask> getByRecorderAndStage(RecordingTaskStage taskStage, int userId, int page, int pageSize) {
        String sql = String.format("SELECT %s FROM %s " +
                "WHERE `recorder` = :userId AND `stage` = :stage " +
                "ORDER BY applicationSubmitTime ASC " +
                "LIMIT :offset, :batchSize", ALL_COLUMNS, TABLE_NAME);
        return dbClient.query(sql, ImmutableMap.of(
                "userId", userId,
                "stage", taskStage.toInt(),
                "offset", page * pageSize,
                "batchSize", pageSize
        ), ROW_MAPPER);
    }

    @Override
    public List<RecordingTask> getByAuditorAndStage(int auditorId, int page, int pageSize, int stage) {
        String sql = String.format("SELECT %s FROM %s " +
                "WHERE `auditor` = :auditor AND `stage` = :stage " +
                "ORDER BY applicationSubmitTime ASC " +
                "LIMIT :offset, :batchSize", ALL_COLUMNS, TABLE_NAME);
        return dbClient.query(sql, ImmutableMap.of(
                "auditor", auditorId,
                "stage", stage,
                "offset", page * pageSize,
                "batchSize", pageSize
        ), ROW_MAPPER);
    }

    @Override
    public boolean submit(long taskId) {
        String sql = String.format("UPDATE %s SET `stage` = :targetStage, `submitTimes` = `submitTimes` + 1 WHERE `id` = :taskId", TABLE_NAME);
        return dbClient.update(sql, new MapSqlParameterSource("targetStage", RecordingTaskStage.SUBMITTED.toInt())
                .addValue("taskId", taskId)) > 0;
    }

    @Override
    public boolean updateTaskStage(long taskId, int stage) {
        String sql = String.format("UPDATE %s SET stage = :stage WHERE id = :taskId", TABLE_NAME);
        int result = dbClient.update(sql, ImmutableMap.of("stage", stage, "taskId", taskId));
        return result > 0;
    }

    @Override
    public boolean updateTaskStageAndTaskAuditSubmitDetails(long taskId, int stage, List<TaskAuditSubmitDetail> taskAuditSubmitDetails) {
        String sql = String.format("UPDATE %s SET stage = :stage, taskAuditSubmitDetails = :taskAuditSubmitDetails WHERE id = :taskId", TABLE_NAME);
        int result = dbClient.update(sql, ImmutableMap.of("stage", stage, "taskAuditSubmitDetails", JsonUtils.writeValue(CollectionUtils.isEmpty(taskAuditSubmitDetails) ? Lists.newArrayList() : taskAuditSubmitDetails), "taskId", taskId));
        return result > 0;
    }

    @Override
    public boolean updateTagger(long taskId, int userId) {
        String sql = String.format("UPDATE %s SET " +
                "tagger = :tagger " +
                "WHERE id = :taskId", TABLE_NAME);
        int result = dbClient.update(sql, ImmutableMap.of(
                "tagger", userId,
                "taskId", taskId));
        return result > 0;
    }

    @Override
    public boolean updateTaskStageByApplicationId(long applicationId, int stage) {
        String sql = String.format("UPDATE %s SET stage = :stage WHERE applicationId = :applicationId", TABLE_NAME);
        int result = dbClient.update(sql, ImmutableMap.of("stage", stage, "applicationId", applicationId));
        return result > 0;
    }
}
