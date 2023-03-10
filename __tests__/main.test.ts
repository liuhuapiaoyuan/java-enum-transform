/**
 * @file main
 * @author imcuttle
 * @date 2018/4/4
 */
import { javaEnumTransformByDir, resultToCode } from '../src'
import { fixture } from './helper'
describe('javaEnumTransform', function () {
  it('should spec', async function () {
    const data = await javaEnumTransformByDir(fixture(''), {
      ignore: ['**/example/**']
    })
    expect(resultToCode(data)).toMatchInlineSnapshot(`"export const enum ChangeTypeEnum {
  /**
   * 未变化
   */
  UNCHANGED = 1,
  /**
   * 新增
   */
  ADD = 2,
  /**
   * 变更
   */
  UPDATE = 3,
}
export const ChangeTypeEnumOptions = [
  { label: \\"未变化\\", value: ChangeTypeEnum[\\"UNCHANGED\\"] },
  { label: \\"新增\\", value: ChangeTypeEnum[\\"ADD\\"] },
  { label: \\"变更\\", value: ChangeTypeEnum[\\"UPDATE\\"] },
]
export const enum LockPrefixEnum {
  /**
   * paper
   */
  PAPER_PREFIX = 1,
  /**
   * question
   */
  QUESTION_PREFIX = 2,
  /**
   * recordingTask
   */
  RECORDING_TASK = 3,
}
export const LockPrefixEnumOptions = [
  { label: \\"paper\\", value: LockPrefixEnum[\\"PAPER_PREFIX\\"] },
  { label: \\"question\\", value: LockPrefixEnum[\\"QUESTION_PREFIX\\"] },
  { label: \\"recordingTask\\", value: LockPrefixEnum[\\"RECORDING_TASK\\"] },
]
export const enum LogOrderByEnum {
  /**
   * createdTimeDesc
   */
  CREATED_TIME_DESC = \\"CREATED_TIME_DESC\\",
  /**
   * createdTimeAsc
   */
  CREATED_TIME_ASC = \\"CREATED_TIME_ASC\\",
}
export const LogOrderByEnumOptions = [
  { label: \\"createdTimeDesc\\", value: LogOrderByEnum[\\"CREATED_TIME_DESC\\"] },
  { label: \\"createdTimeAsc\\", value: LogOrderByEnum[\\"CREATED_TIME_ASC\\"] },
]
export const enum RecordingTaskRoleEnum {
  /**
   * recorder
   */
  RECORDING_TASK_RECORDER = 1,
  /**
   * auditor
   */
  RECORDING_TASK_AUDITOR = 2,
}
export const RecordingTaskRoleEnumOptions = [
  { label: \\"recorder\\", value: RecordingTaskRoleEnum[\\"RECORDING_TASK_RECORDER\\"] },
  { label: \\"auditor\\", value: RecordingTaskRoleEnum[\\"RECORDING_TASK_AUDITOR\\"] },
]
export const enum RowRuleEnum {
  /**
   * 所有学科学段
   */
  ALL_PHASE_SUBJECT = \\"all\\",
  /**
   * 与角色相同的学科学段
   */
  PHASE_SUBJECT_OF_ROLE = \\"same-phase-subject\\",
}
export const RowRuleEnumOptions = [
  { label: \\"所有学科学段\\", value: RowRuleEnum[\\"ALL_PHASE_SUBJECT\\"] },
  { label: \\"与角色相同的学科学段\\", value: RowRuleEnum[\\"PHASE_SUBJECT_OF_ROLE\\"] },
]
export const enum SortTypeEnum {
  /**
   * submitTimeAsc
   */
  SUBMIT_TIME_ASC = 1,
  /**
   * submitTimeDesc
   */
  SUBMIT_TIME_DESC = 2,
  /**
   * publishTimeAsc
   */
  PUBLISH_TIME_ASC = 3,
  /**
   * publishTimeDesc
   */
  PUBLISH_TIME_DESC = 4,
  /**
   * taskSubmitTimesAsc
   */
  TASK_SUBMIT_TIMES_ASC = 5,
  /**
   * taskSubmitTimesDesc
   */
  TASK_SUBMIT_TIMES_DESC = 6,
  /**
   * recordedQuestionCountAsc
   */
  RECORDED_QUESTION_COUNT_ASC = 7,
  /**
   * recordedQuestionCountDesc
   */
  RECORDED_QUESTION_COUNT_DESC = 8,
  /**
   * completeDurationInMillisecondsAsc
   */
  COMPLETE_DURATION_ASC = 9,
  /**
   * completeDurationInMillisecondsDesc
   */
  COMPLETE_DURATION_DESC = 10,
}
export const SortTypeEnumOptions = [
  { label: \\"submitTimeAsc\\", value: SortTypeEnum[\\"SUBMIT_TIME_ASC\\"] },
  { label: \\"submitTimeDesc\\", value: SortTypeEnum[\\"SUBMIT_TIME_DESC\\"] },
  { label: \\"publishTimeAsc\\", value: SortTypeEnum[\\"PUBLISH_TIME_ASC\\"] },
  { label: \\"publishTimeDesc\\", value: SortTypeEnum[\\"PUBLISH_TIME_DESC\\"] },
  { label: \\"taskSubmitTimesAsc\\", value: SortTypeEnum[\\"TASK_SUBMIT_TIMES_ASC\\"] },
  { label: \\"taskSubmitTimesDesc\\", value: SortTypeEnum[\\"TASK_SUBMIT_TIMES_DESC\\"] },
  { label: \\"recordedQuestionCountAsc\\", value: SortTypeEnum[\\"RECORDED_QUESTION_COUNT_ASC\\"] },
  { label: \\"recordedQuestionCountDesc\\", value: SortTypeEnum[\\"RECORDED_QUESTION_COUNT_DESC\\"] },
  { label: \\"completeDurationInMillisecondsAsc\\", value: SortTypeEnum[\\"COMPLETE_DURATION_ASC\\"] },
  { label: \\"completeDurationInMillisecondsDesc\\", value: SortTypeEnum[\\"COMPLETE_DURATION_DESC\\"] },
]
export const enum TableEnum {
  /**
   * 录题申请
   */
  RECORDING_APPLICATION = \\"application\\",
  /**
   * 录题任务
   */
  RECORDING_TASK = \\"task\\",
}
export const TableEnumOptions = [
  { label: \\"录题申请\\", value: TableEnum[\\"RECORDING_APPLICATION\\"] },
  { label: \\"录题任务\\", value: TableEnum[\\"RECORDING_TASK\\"] },
]
export const enum UserStatusEnum {
  /**
   * 有效
   */
  VALID = 1,
  /**
   * 失效
   */
  INVALID = -1,
}
export const UserStatusEnumOptions = [
  { label: \\"有效\\", value: UserStatusEnum[\\"VALID\\"] },
  { label: \\"失效\\", value: UserStatusEnum[\\"INVALID\\"] },
]
export const enum RecordingTagProcessEnum {
  /**
   * 全部
   */
  ALL = 1,
  /**
   * 未完成
   */
  NOT_FINISHED = 2,
  /**
   * 已完成
   */
  FINISHED = 3,
}
export const RecordingTagProcessEnumOptions = [
  { label: \\"全部\\", value: RecordingTagProcessEnum[\\"ALL\\"] },
  { label: \\"未完成\\", value: RecordingTagProcessEnum[\\"NOT_FINISHED\\"] },
  { label: \\"已完成\\", value: RecordingTagProcessEnum[\\"FINISHED\\"] },
]
export const enum ExamPaperRecordingTaskStageEnum {
  /**
   * 被废弃
   */
  DELETED = -1,
  /**
   * 已录入
   */
  RECORDED = 1,
  /**
   * 待审核
   */
  TO_BE_AUDITED = 2,
  /**
   * 审核通过
   */
  AUDIT_APPROVED = 3,
  /**
   * 审核不通过
   */
  AUDIT_FAILED = 4,
  /**
   * 已被发布
   */
  PUBLISHED = 5,
}
export const ExamPaperRecordingTaskStageEnumOptions = [
  { label: \\"被废弃\\", value: ExamPaperRecordingTaskStageEnum[\\"DELETED\\"] },
  { label: \\"已录入\\", value: ExamPaperRecordingTaskStageEnum[\\"RECORDED\\"] },
  { label: \\"待审核\\", value: ExamPaperRecordingTaskStageEnum[\\"TO_BE_AUDITED\\"] },
  { label: \\"审核通过\\", value: ExamPaperRecordingTaskStageEnum[\\"AUDIT_APPROVED\\"] },
  { label: \\"审核不通过\\", value: ExamPaperRecordingTaskStageEnum[\\"AUDIT_FAILED\\"] },
  { label: \\"已被发布\\", value: ExamPaperRecordingTaskStageEnum[\\"PUBLISHED\\"] },
]
export const enum FeedbackTargetTypeEnum {
  /**
   * 录题申请
   */
  APPLICATION = 1,
  /**
   * 题目
   */
  QUESTION = 2,
  /**
   * 试卷
   */
  EXAM_PAPER = 3,
  /**
   * 录入任务
   */
  RECORDING_TASK = 4,
}
export const FeedbackTargetTypeEnumOptions = [
  { label: \\"录题申请\\", value: FeedbackTargetTypeEnum[\\"APPLICATION\\"] },
  { label: \\"题目\\", value: FeedbackTargetTypeEnum[\\"QUESTION\\"] },
  { label: \\"试卷\\", value: FeedbackTargetTypeEnum[\\"EXAM_PAPER\\"] },
  { label: \\"录入任务\\", value: FeedbackTargetTypeEnum[\\"RECORDING_TASK\\"] },
]
export const enum FeedbackTypeEnum {
  APPLICATION_NEED_REVISED = \\"APPLICATION_NEED_REVISED\\",
  APPLICATION_REJECTED = \\"APPLICATION_REJECTED\\",
  APPLICATION_APPROVED = \\"APPLICATION_APPROVED\\",
  APPLICATION_CANCELED = \\"APPLICATION_CANCELED\\",
  QUESTION_APPROVED = \\"QUESTION_APPROVED\\",
  QUESTION_AUDIT_FAILED = \\"QUESTION_AUDIT_FAILED\\",
  QUESTION_NEED_REVISED = \\"QUESTION_NEED_REVISED\\",
  PAPER_AUDIT_APPROVED = \\"PAPER_AUDIT_APPROVED\\",
  PAPER_AUDIT_FAILED = \\"PAPER_AUDIT_FAILED\\",
  TASK_AUDIT_FAILED = \\"TASK_AUDIT_FAILED\\",
}
export const enum QuestionRecordingTaskStageEnum {
  /**
   * 暂不可用
   */
  INACTIVATED = -2,
  /**
   * 被废弃
   */
  DELETED = -1,
  /**
   * 已录入
   */
  RECORDED = 1,
  /**
   * 待审核
   */
  TO_BE_AUDITED = 2,
  /**
   * 审核通过
   */
  AUDIT_APPROVED = 3,
  /**
   * 审核不通过
   */
  AUDIT_FAILED = 4,
  /**
   * 待标注
   */
  TO_BE_TAGGED = 5,
  /**
   * 已被标注
   */
  TAG_FINISHED = 6,
  /**
   * 标注阶段纠错
   */
  TAG_FAILED = 7,
  /**
   * 已被发布
   */
  PUBLISHED = 8,
}
export const QuestionRecordingTaskStageEnumOptions = [
  { label: \\"暂不可用\\", value: QuestionRecordingTaskStageEnum[\\"INACTIVATED\\"] },
  { label: \\"被废弃\\", value: QuestionRecordingTaskStageEnum[\\"DELETED\\"] },
  { label: \\"已录入\\", value: QuestionRecordingTaskStageEnum[\\"RECORDED\\"] },
  { label: \\"待审核\\", value: QuestionRecordingTaskStageEnum[\\"TO_BE_AUDITED\\"] },
  { label: \\"审核通过\\", value: QuestionRecordingTaskStageEnum[\\"AUDIT_APPROVED\\"] },
  { label: \\"审核不通过\\", value: QuestionRecordingTaskStageEnum[\\"AUDIT_FAILED\\"] },
  { label: \\"待标注\\", value: QuestionRecordingTaskStageEnum[\\"TO_BE_TAGGED\\"] },
  { label: \\"已被标注\\", value: QuestionRecordingTaskStageEnum[\\"TAG_FINISHED\\"] },
  { label: \\"标注阶段纠错\\", value: QuestionRecordingTaskStageEnum[\\"TAG_FAILED\\"] },
  { label: \\"已被发布\\", value: QuestionRecordingTaskStageEnum[\\"PUBLISHED\\"] },
]
export const enum RecordingApplicationQuestionSourceEnum {
  /**
   * 外部题源
   */
  EXTERNAL = 1,
  /**
   * 辅导自编
   */
  ORIGINAL = 2,
}
export const RecordingApplicationQuestionSourceEnumOptions = [
  { label: \\"外部题源\\", value: RecordingApplicationQuestionSourceEnum[\\"EXTERNAL\\"] },
  { label: \\"辅导自编\\", value: RecordingApplicationQuestionSourceEnum[\\"ORIGINAL\\"] },
]
export const enum RecordingApplicationStageEnum {
  /**
   * 待审核
   */
  SUBMITTED = 1,
  /**
   * 驳回待修改
   */
  TO_BE_REVISED = 2,
  /**
   * 审核不通过
   */
  REJECTED = 3,
  /**
   * 待分配
   */
  TO_BE_ASSIGNED = 4,
  /**
   * 任务进行中
   */
  PROCESSING = 5,
  /**
   * 已发布
   */
  PUBLISHED = 6,
  /**
   * 已取消
   */
  CANCELED = 7,
}
export const RecordingApplicationStageEnumOptions = [
  { label: \\"待审核\\", value: RecordingApplicationStageEnum[\\"SUBMITTED\\"] },
  { label: \\"驳回待修改\\", value: RecordingApplicationStageEnum[\\"TO_BE_REVISED\\"] },
  { label: \\"审核不通过\\", value: RecordingApplicationStageEnum[\\"REJECTED\\"] },
  { label: \\"待分配\\", value: RecordingApplicationStageEnum[\\"TO_BE_ASSIGNED\\"] },
  { label: \\"任务进行中\\", value: RecordingApplicationStageEnum[\\"PROCESSING\\"] },
  { label: \\"已发布\\", value: RecordingApplicationStageEnum[\\"PUBLISHED\\"] },
  { label: \\"已取消\\", value: RecordingApplicationStageEnum[\\"CANCELED\\"] },
]
export const enum RecordingModeEnum {
  /**
   * 套卷
   */
  EXAM_PAPER = 1,
  /**
   * 散题
   */
  SINGLE_QUESTION = 2,
}
export const RecordingModeEnumOptions = [
  { label: \\"套卷\\", value: RecordingModeEnum[\\"EXAM_PAPER\\"] },
  { label: \\"散题\\", value: RecordingModeEnum[\\"SINGLE_QUESTION\\"] },
]
export const enum RecordingQuestionOriginEnum {
  /**
   * 新录入
   */
  RECORDED = 1,
  /**
   * 已有题目
   */
  RELEASED = 2,
}
export const RecordingQuestionOriginEnumOptions = [
  { label: \\"新录入\\", value: RecordingQuestionOriginEnum[\\"RECORDED\\"] },
  { label: \\"已有题目\\", value: RecordingQuestionOriginEnum[\\"RELEASED\\"] },
]
export const enum RecordingTaskStageEnum {
  /**
   * 录题中
   */
  RECORDING = 1,
  /**
   * 待审核
   */
  SUBMITTED = 2,
  /**
   * 驳回待修改
   */
  TO_BE_REVISED = 3,
  /**
   * 审核通过待标注
   */
  AUDITED = 4,
  /**
   * 已发布
   */
  PUBLISHED = 5,
  /**
   * 已取消
   */
  CANCELED = 6,
}
export const RecordingTaskStageEnumOptions = [
  { label: \\"录题中\\", value: RecordingTaskStageEnum[\\"RECORDING\\"] },
  { label: \\"待审核\\", value: RecordingTaskStageEnum[\\"SUBMITTED\\"] },
  { label: \\"驳回待修改\\", value: RecordingTaskStageEnum[\\"TO_BE_REVISED\\"] },
  { label: \\"审核通过待标注\\", value: RecordingTaskStageEnum[\\"AUDITED\\"] },
  { label: \\"已发布\\", value: RecordingTaskStageEnum[\\"PUBLISHED\\"] },
  { label: \\"已取消\\", value: RecordingTaskStageEnum[\\"CANCELED\\"] },
]"`)
  })
})
