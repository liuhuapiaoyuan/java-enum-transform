/**
 * @(#)ExamPaperRecordingTaskStorageImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fenbi.common.util.JsonUtils;
import com.imcuttle.bar.storage.db.BaseDbStorage;
import com.imcuttle.bar.storage.db.ExamPaperRecordingTaskStorage;
import com.imcuttle.thrift.AuditDetail;
import com.imcuttle.thrift.ExamPaperRecordingTask;
import com.imcuttle.thrift.RecordingTask;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author chenyibo
 */
@Repository
public class ExamPaperRecordingTaskStorageImpl extends BaseDbStorage implements ExamPaperRecordingTaskStorage {

    private static final String TABLE_NAME = " exam_paper_recording_task ";

    private static final RowMapper<ExamPaperRecordingTask> ROW_MAPPER = ((rs, rowNum) -> {
        ExamPaperRecordingTask examPaperRecordingTask = new ExamPaperRecordingTask();

        examPaperRecordingTask.setId(rs.getLong("id"));
        examPaperRecordingTask.setApplicationId(rs.getLong("applicationId"));
        examPaperRecordingTask.setTaskId(rs.getLong("taskId"));
        examPaperRecordingTask.setExamPaperId(rs.getLong("examPaperId"));
        examPaperRecordingTask.setStage(rs.getInt("stage"));
        examPaperRecordingTask.setOrdinal(rs.getInt("ordinal"));
        examPaperRecordingTask.setCreator(rs.getInt("creator"));
        examPaperRecordingTask.setCreatedTime(rs.getLong("createdTime"));

        if (StringUtils.isNotEmpty(rs.getString("auditDetails"))) {
            examPaperRecordingTask.setAuditDetails(JsonUtils.readValue(rs.getString("auditDetails"), new TypeReference<List<AuditDetail>>() {}));
        } else {
            examPaperRecordingTask.setAuditDetails(Collections.emptyList());
        }
        return examPaperRecordingTask;
    });

    private MapSqlParameterSource getParameterSource(ExamPaperRecordingTask examPaperRecordingTask) {
        MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue("taskId", examPaperRecordingTask.getTaskId());
        source.addValue("examPaperId", examPaperRecordingTask.getExamPaperId());
        source.addValue("stage", examPaperRecordingTask.getStage());
        source.addValue("applicationId", examPaperRecordingTask.getApplicationId());
        source.addValue("ordinal", examPaperRecordingTask.getOrdinal());
        source.addValue("creator", examPaperRecordingTask.getCreator());
        source.addValue("createdTime", examPaperRecordingTask.getCreatedTime());
        source.addValue("auditDetails", JsonUtils.writeValue(CollectionUtils.isEmpty(examPaperRecordingTask.getAuditDetails()) ? Lists.newArrayList() : examPaperRecordingTask.getAuditDetails()));
        return source;
    }

    @Override
    public boolean updateExamPaperRecordingTaskStage(long taskId, long examPaperId, int stage) {
        String sql = "UPDATE " + TABLE_NAME + " SET stage = :stage WHERE taskId = :taskId AND examPaperId = :examPaperId";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);
        source.addValue("examPaperId", examPaperId);
        source.addValue("stage", stage);

        return dbClient.update(sql, source) > 0;
    }

    @Override
    public boolean updateExamPaperRecordingTaskStageAndAuditDetails(long taskId, long examPaperId, int stage, List<AuditDetail> auditDetails) {
        String sql = "UPDATE " + TABLE_NAME + " SET stage = :stage, auditDetails = :auditDetails WHERE taskId = :taskId AND examPaperId = :examPaperId";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);
        source.addValue("examPaperId", examPaperId);
        source.addValue("stage", stage);
        source.addValue("auditDetails", JsonUtils.writeValue(CollectionUtils.isEmpty(auditDetails) ? Lists.newArrayList() : auditDetails));

        return dbClient.update(sql, source) > 0;
    }

    @Override
    public List<ExamPaperRecordingTask> getExamPaperRecordingTaskByTaskIdAndExamPaperId(long taskId, long examPaperId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId = :taskId AND examPaperId = :examPaperId";

        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);
        source.addValue("examPaperId", examPaperId);

        try {
            return dbClient.getNamedWriter().query(sql, source, ROW_MAPPER);
        } catch (DataAccessException e) {
            return Lists.newArrayList();
        }
    }

    @Override
    public List<ExamPaperRecordingTask> getEffectiveRelationsByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE applicationId in (:applicationIds) AND stage>0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("applicationIds", applicationIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public List<ExamPaperRecordingTask> getAllRelationsByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE applicationId in (:applicationIds) ";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("applicationIds", applicationIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public List<ExamPaperRecordingTask> getExamPaperRecordingTaskByTaskId(long taskId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId = :taskId";

        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);

        try {
            return dbClient.getNamedWriter().query(sql, source, ROW_MAPPER);
        } catch (DataAccessException e) {
            return Lists.newArrayList();
        }
    }

    @Override
    public List<ExamPaperRecordingTask> getEffectiveRelationsByTaskIds(Collection<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId in (:taskIds) AND stage > 0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("taskIds", taskIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public boolean create(ExamPaperRecordingTask examPaperRecordingTask) {
        String sql = "INSERT INTO " + TABLE_NAME + " SET " +
                "applicationId = :applicationId, " +
                "taskId = :taskId, " +
                "examPaperId = :examPaperId, " +
                "ordinal = :ordinal, " +
                "stage = :stage," +
                "creator = :creator," +
                "auditDetails = :auditDetails," +
                "createdTime = :createdTime";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        dbClient.getNamedWriter().update(sql, getParameterSource(examPaperRecordingTask), keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue() > 0;
    }

    @Override
    public List<Long> batchCreate(List<ExamPaperRecordingTask> examPaperRecordingTasks) {
        if (CollectionUtils.isEmpty(examPaperRecordingTasks)) {
            return new ArrayList<>();
        }
        String sql = "INSERT INTO " + TABLE_NAME + " SET " +
                "applicationId = :applicationId, " +
                "taskId = :taskId, " +
                "examPaperId = :examPaperId, " +
                "ordinal = :ordinal, " +
                "stage = :stage," +
                "creator = :creator," +
                "auditDetails = :auditDetails," +
                "createdTime = :createdTime";

        List<SqlParameterSource> sourceList = examPaperRecordingTasks.stream().map(task -> getParameterSource(task)).collect(Collectors.toList());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int[] results = dbClient.batchUpdateWithSqlParamSource(sql, sourceList, keyHolder);

        List<Long> ids = keyHolder.getKeyList().stream().map(fieldValueMap -> fieldValueMap.values().iterator().next())
                .filter(key -> key instanceof Number).map(key -> ((Number) key).longValue())
                .collect(toList());

        for (int index = 0; index < examPaperRecordingTasks.size(); ++index) {
            examPaperRecordingTasks.get(index).setId(ids.get(index));
        }
        return ids;
    }

    @Override
    public List<ExamPaperRecordingTask> getEffectiveRelationsByRecordingTaskIds(Collection<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId in (:taskIds) AND stage > 0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("taskIds", taskIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public boolean updateStageById(long id, int originStage, int targetStage) {
        String sql = String.format("UPDATE %s SET `stage` = :targetStage WHERE `id` = :id AND `stage` = :originStage", TABLE_NAME);
        return dbClient.update(sql, new MapSqlParameterSource("targetStage", targetStage)
                .addValue("id", id)
                .addValue("originStage", originStage)) > 0;
    }

    @Override
    public Optional<ExamPaperRecordingTask> getExamPaperRecordingTaskById(long id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = :id";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("id", id);

        try {
            ExamPaperRecordingTask result = dbClient.getNamedWriter().queryForObject(sql, source, ROW_MAPPER);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean updateStage(long taskId, Set<Integer> originStages, int targetStage) {
        if (CollectionUtils.isEmpty(originStages)) {
            return true;
        }
        String sql = String.format("UPDATE %s SET `stage` = :targetStage WHERE `taskId` = :taskId AND `stage` in (:originStages)", TABLE_NAME);
        return dbClient.update(sql, new MapSqlParameterSource("targetStage", targetStage)
                .addValue("taskId", taskId)
                .addValue("originStages", originStages)) > 0;
    }
}
