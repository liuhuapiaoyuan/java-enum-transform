/**
 * @(#)QuestionRecordingTaskStorageImpl.java, 4月 12, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.imcuttle.bar.storage.db.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fenbi.common.db.mapper.IntegerRowMapper;
import com.fenbi.common.util.JsonUtils;
import com.imcuttle.enums.QuestionRecordingTaskStage;
import com.imcuttle.bar.storage.db.BaseDbStorage;
import com.imcuttle.bar.storage.db.QuestionRecordingTaskStorage;
import com.imcuttle.thrift.AuditDetail;
import com.imcuttle.thrift.QuestionRecordingTask;
import com.fenbi.tutor.cybermultimedia.thrift.VideoSegment;
import com.google.common.collect.Lists;
import joptsimple.internal.Strings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.imcuttle.thrift.TutorArmoryConstants.INT_ID_NO_LIMIT;
import static com.imcuttle.thrift.TutorArmoryConstants.LONG_ID_NO_LIMIT;

/**
 * @author chenyibo
 */
@Repository
public class QuestionRecordingTaskStorageImpl extends BaseDbStorage implements QuestionRecordingTaskStorage {

    private static final String TABLE_NAME = " question_recording_task ";

    private static final String ALL_COLUMNS_WITHOUT_ID = "`applicationId`, `taskId`, `examPaperId`, `questionId`, `questionOrigin`, `ordinal`, `stage`, `snapshots`, `creator`, `createdTime`, `originWholeQuestionId`, `auditDetails`";

    private static final String ALL_COLUMNS = "id, " + ALL_COLUMNS_WITHOUT_ID;

    private static final RowMapper<QuestionRecordingTask> ROW_MAPPER = ((rs, rowNum) -> {
        QuestionRecordingTask questionRecordingTask = new QuestionRecordingTask();

        questionRecordingTask.setId(rs.getLong("id"));
        questionRecordingTask.setApplicationId(rs.getLong("applicationId"));
        questionRecordingTask.setExamPaperId(rs.getLong("examPaperId"));
        questionRecordingTask.setTaskId(rs.getLong("taskId"));
        questionRecordingTask.setStage(rs.getInt("stage"));
        questionRecordingTask.setOrdinal(rs.getInt("ordinal"));
        questionRecordingTask.setQuestionId(rs.getInt("questionId"));
        questionRecordingTask.setQuestionOrigin(rs.getInt("questionOrigin"));
        String snapshotsStr = rs.getString("snapshots");
        questionRecordingTask.setSnapshots(StringUtils.isEmpty(snapshotsStr) ? new ArrayList<>() : JsonUtils.readValue(snapshotsStr, new TypeReference<List<String>>() {
        }));
        questionRecordingTask.setCreator(rs.getInt("creator"));
        questionRecordingTask.setCreatedTime(rs.getLong("createdTime"));
        questionRecordingTask.setOriginWholeQuestionId(rs.getInt("originWholeQuestionId"));

        if (StringUtils.isNotEmpty(rs.getString("auditDetails"))) {
            questionRecordingTask.setAuditDetails(JsonUtils.readValue(rs.getString("auditDetails"), new TypeReference<List<AuditDetail>>() {}));
        } else {
            questionRecordingTask.setAuditDetails(Collections.emptyList());
        }
        return questionRecordingTask;
    });

    private MapSqlParameterSource getSqlSource(QuestionRecordingTask questionRecordingTask) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("id", questionRecordingTask.getId());

        source.addValue("applicationId", questionRecordingTask.getApplicationId());
        source.addValue("taskId", questionRecordingTask.getTaskId());
        source.addValue("examPaperId", questionRecordingTask.getExamPaperId());
        source.addValue("questionId", questionRecordingTask.getQuestionId());
        source.addValue("questionOrigin", questionRecordingTask.getQuestionOrigin());
        source.addValue("ordinal", questionRecordingTask.getOrdinal());
        source.addValue("stage", questionRecordingTask.getStage());
        source.addValue("snapshots", CollectionUtils.isEmpty(questionRecordingTask.getSnapshots()) ? "" : JsonUtils.writeValue(questionRecordingTask.getSnapshots()));
        source.addValue("creator", questionRecordingTask.getCreator());
        source.addValue("createdTime", questionRecordingTask.getCreatedTime());
        source.addValue("originWholeQuestionId", questionRecordingTask.getOriginWholeQuestionId());
        source.addValue("auditDetails", JsonUtils.writeValue(CollectionUtils.isEmpty(questionRecordingTask.getAuditDetails()) ? Lists.newArrayList() : questionRecordingTask.getAuditDetails()));
        return source;
    }

    @Override
    public boolean updateQuestionRecordingTaskStage(long taskId, long examPaperId, int questionId, int stage) {
        String sql = "UPDATE " + TABLE_NAME + " SET stage = :stage WHERE taskId = :taskId";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);
        if (examPaperId != LONG_ID_NO_LIMIT) {
            sql += " AND examPaperId = :examPaperId";
            source.addValue("examPaperId", examPaperId);
        }

        source.addValue("stage", stage);
        if (questionId != INT_ID_NO_LIMIT) {
            sql += " AND questionId = :questionId";
            source.addValue("questionId", questionId);
        }

        return dbClient.update(sql, source) > 0;
    }

    @Override
    public long insert(QuestionRecordingTask questionRecordingTask) {
        String sql = "INSERT INTO " + TABLE_NAME +
                " SET applicationId = :applicationId, " +
                " taskId = :taskId, " +
                " examPaperId = :examPaperId, " +
                " questionId = :questionId, " +
                " questionOrigin = :questionOrigin, " +
                " snapshots = :snapshots, " +
                " ordinal = :ordinal, " +
                " stage = :stage," +
                " auditDetails = :auditDetails," +
                " creator = :creator," +
                " createdTime = :createdTime, " +
                " originWholeQuestionId = :originWholeQuestionId";
        ;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        dbClient.update(sql, getSqlSource(questionRecordingTask), keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<QuestionRecordingTask> get(long taskId, int questionId, long examPaperId) {
        String sql = "SELECT " + ALL_COLUMNS + " FROM " + TABLE_NAME + " WHERE taskId = :taskId AND questionId = :questionId AND examPaperId = :examPaperId";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);
        source.addValue("examPaperId", examPaperId);
        source.addValue("questionId", questionId);

        try {
            QuestionRecordingTask result = dbClient.getNamedWriter().queryForObject(sql, source, ROW_MAPPER);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<QuestionRecordingTask> getTasks(long taskId, List<Integer> questionIds, long examPaperId) {
        String sql = "SELECT " + ALL_COLUMNS + " FROM " + TABLE_NAME + " WHERE taskId = :taskId AND examPaperId = :examPaperId AND questionId in (:questionIds)";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);
        source.addValue("examPaperId", examPaperId);
        source.addValue("questionIds", questionIds);
        return dbClient.query(sql, source, ROW_MAPPER);
    }

    @Override
    public boolean resume(long id, QuestionRecordingTask questionRecordingTask) {
        String sql = "UPDATE " + TABLE_NAME + " SET stage = :stage, ordinal = :ordinal, questionOrigin = :questionOrigin, auditDetails = :auditDetails, snapshots = :snapshots WHERE id = :id AND stage = :deletedStage";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("stage", questionRecordingTask.getStage());
        source.addValue("ordinal", questionRecordingTask.getOrdinal());
        source.addValue("questionOrigin", questionRecordingTask.getQuestionOrigin());
        source.addValue("id", id);
        source.addValue("deletedStage", QuestionRecordingTaskStage.DELETED.toInt());
        source.addValue("snapshots", CollectionUtils.isEmpty(questionRecordingTask.getSnapshots()) ? "" : JsonUtils.writeValue(questionRecordingTask.getSnapshots()));
        source.addValue("auditDetails", JsonUtils.writeValue(CollectionUtils.isEmpty(questionRecordingTask.getAuditDetails()) ? Lists.newArrayList() : questionRecordingTask.getAuditDetails()));
        return dbClient.update(sql, source) > 0;
    }

    @Override
    public boolean batchResume(List<Long> ids, List<QuestionRecordingTask> questionRecordingTasks) {
        String sql = "UPDATE " + TABLE_NAME + " SET stage = :stage, ordinal = :ordinal, questionOrigin = :questionOrigin, auditDetails = :auditDetails, snapshots = :snapshots WHERE id = :id AND stage = :deletedStage";
        List<MapSqlParameterSource> sourceList = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            QuestionRecordingTask questionRecordingTask = questionRecordingTasks.get(i);
            MapSqlParameterSource source = new MapSqlParameterSource();
            source.addValue("stage", questionRecordingTask.getStage());
            source.addValue("ordinal", questionRecordingTask.getOrdinal());
            source.addValue("questionOrigin", questionRecordingTask.getQuestionOrigin());
            source.addValue("id", ids.get(i));
            source.addValue("deletedStage", QuestionRecordingTaskStage.DELETED.toInt());
            source.addValue("snapshots", CollectionUtils.isEmpty(questionRecordingTask.getSnapshots()) ? "" : JsonUtils.writeValue(questionRecordingTask.getSnapshots()));
            source.addValue("auditDetails", JsonUtils.writeValue(CollectionUtils.isEmpty(questionRecordingTask.getAuditDetails()) ? Lists.newArrayList() : questionRecordingTask.getAuditDetails()));
            sourceList.add(source);
        }
        int[] results = dbClient.batchUpdate(sql, sourceList.toArray(new SqlParameterSource[0]));
        return Arrays.stream(results).allMatch(result -> result > 0 || result == Statement.SUCCESS_NO_INFO);
    }

    @Override
    public int getMaxOrdinal(long taskId) {
        //stage > 0 只关心有效关联的题目的最大ordinal
        String sql = "SELECT max(ordinal) FROM " + TABLE_NAME + " WHERE taskId = :taskId AND stage > 0";
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);
        return dbClient.getNamedWriter().queryForObject(sql, source, IntegerRowMapper.INSTANCE);
    }

    @Override
    public List<QuestionRecordingTask> getQuestionRecordingTasksByTaskIdAndExamPaperId(long taskId, long examPaperId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId = :taskId AND examPaperId = :examPaperId";

        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);
        source.addValue("examPaperId", examPaperId);

        return dbClient.getNamedWriter().query(sql, source, ROW_MAPPER);
    }

    @Override
    public List<QuestionRecordingTask> getQuestionRecordingTasksByTaskId(long taskId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId = :taskId";

        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("taskId", taskId);

        return dbClient.getNamedWriter().query(sql, source, ROW_MAPPER);
    }

    @Override
    public boolean batchInsert(List<QuestionRecordingTask> questionRecordingTasks) {
        if (CollectionUtils.isEmpty(questionRecordingTasks)) {
            return true;
        }
        String sql = "INSERT IGNORE INTO " + TABLE_NAME + " SET " +
                "applicationId = :applicationId, " +
                "questionId = :questionId, " +
                "taskId = :taskId, " +
                "examPaperId = :examPaperId, " +
                "questionOrigin = :questionOrigin, " +
                "snapshots = :snapshots, " +
                "ordinal = :ordinal, " +
                "stage = :stage," +
                "creator = :creator," +
                " auditDetails = :auditDetails," +
                "createdTime = :createdTime, " +
                "originWholeQuestionId = :originWholeQuestionId";
        List<SqlParameterSource> sourceList = questionRecordingTasks.stream().map(task -> getSqlSource(task)).collect(Collectors.toList());
        int[] results = dbClient.batchUpdate(sql, sourceList.toArray(new SqlParameterSource[0]));
        return Arrays.stream(results).allMatch(result -> result > 0 || result == Statement.SUCCESS_NO_INFO);
    }

    @Override
    public boolean batchUpdate(List<QuestionRecordingTask> questionRecordingTasks) {
        if (CollectionUtils.isEmpty(questionRecordingTasks)) {
            return true;
        }

        String sql = "UPDATE " + TABLE_NAME + " SET " +
                "applicationId = :applicationId, " +
                "examPaperId = :examPaperId, " +
                "questionOrigin = :questionOrigin, " +
                "ordinal = :ordinal, " +
                "stage = :stage, " +
                "auditDetails = :auditDetails " +
                "WHERE id = :id";

        SqlParameterSource[] paras = questionRecordingTasks.stream()
                .map(questionRecordingTask -> {
                    MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
                    sqlParameterSource.addValue("applicationId", questionRecordingTask.getApplicationId());
                    sqlParameterSource.addValue("examPaperId", questionRecordingTask.getExamPaperId());
                    sqlParameterSource.addValue("questionOrigin", questionRecordingTask.getQuestionOrigin());
                    sqlParameterSource.addValue("ordinal", questionRecordingTask.getOrdinal());
                    sqlParameterSource.addValue("stage", questionRecordingTask.getStage());
                    sqlParameterSource.addValue("id", questionRecordingTask.getId());
                    sqlParameterSource.addValue("auditDetails", JsonUtils.writeValue(CollectionUtils.isEmpty(questionRecordingTask.getAuditDetails()) ? Lists.newArrayList() : questionRecordingTask.getAuditDetails()));
                    return sqlParameterSource;
                }).toArray(SqlParameterSource[]::new);

        int[] results = dbClient.batchUpdate(sql, paras);
        return Arrays.stream(results).allMatch(result -> result > 0 || result == Statement.SUCCESS_NO_INFO);
    }

    @Override
    public List<QuestionRecordingTask> getEffectiveRelationsByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE applicationId in (:applicationIds) AND stage > 0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("applicationIds", applicationIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public List<QuestionRecordingTask> getAllRelationsByRecordingApplicationIds(Collection<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE applicationId in (:applicationIds) ";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("applicationIds", applicationIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
    }

    @Override
    public List<QuestionRecordingTask> getEffectiveRelationsByTaskIds(Collection<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Lists.newArrayList();
        }
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE taskId in (:taskIds) AND stage > 0";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("taskIds", taskIds);
        return dbClient.query(sql, parameterSource, ROW_MAPPER);
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

    @Override
    public boolean updateQuestionRecordingTaskSnapshots(long taskId, long examPaperId, int questionId, List<String> snapshots) {
        String sql = String.format("UPDATE %s SET `snapshots` = :snapshots WHERE `taskId` = :taskId AND `examPaperId` = :examPaperId AND `questionId` = :questionId", TABLE_NAME);
        return dbClient.update(sql, new MapSqlParameterSource("snapshots", CollectionUtils.isEmpty(snapshots) ? Strings.EMPTY : JsonUtils.writeValue(snapshots))
                .addValue("taskId", taskId)
                .addValue("questionId", questionId)
                .addValue("examPaperId", examPaperId)) > 0;
    }

    @Override
    public boolean decreaseOrdinal(long taskId, long examPaperId, int startOrdinalExclusive, int count) {
        String sql = "UPDATE " + TABLE_NAME + " SET `ordinal` = `ordinal` - :count " +
                "WHERE `taskId` = :taskId " +
                "AND `examPaperId` = :examPaperId " +
                "AND `ordinal` > :startOrdinal " +
                "AND `stage` > 0";

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("count", count);
        parameterSource.addValue("taskId", taskId);
        parameterSource.addValue("examPaperId", examPaperId);
        parameterSource.addValue("startOrdinal", startOrdinalExclusive);
        return dbClient.update(sql, parameterSource) > 0;
    }
}
