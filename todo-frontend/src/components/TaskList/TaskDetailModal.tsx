import React, { useEffect, useState } from 'react';
import { Modal, Tag, Descriptions, Spin, message } from 'antd';
import AttachmentSection from '../AttachmentSection';
import ReminderSection from '../ReminderSection';
import { getTaskById, getTaskRecurrence } from '../../api/task';

interface TaskDetailModalProps {
  open: boolean;
  taskId: number | null;
  onCancel: () => void;
  userId: number;
}

const TaskDetailModal: React.FC<TaskDetailModalProps> = ({ open, taskId, onCancel, userId }) => {
  const [task, setTask] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [recurrence, setRecurrence] = useState<any>(null);

  useEffect(() => {
    if (open && taskId) {
      setLoading(true);
      setRecurrence(null);
      Promise.all([
        getTaskById(userId, taskId).catch(() => null),
        getTaskRecurrence(taskId).catch(() => null),
      ])
        .then(([t, r]) => {
          if (t) setTask(t);
          else message.error('Không lấy được chi tiết task');
          setRecurrence(r && typeof r === 'object' && 'content' in r ? (r as any).content : r);
        })
        .finally(() => setLoading(false));
    }
  }, [open, taskId, userId]);

  return (
    <Modal open={open} onCancel={onCancel} footer={null} title="Chi tiết công việc">
      <Spin spinning={loading}>
        {task ? (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="Tên công việc">{task.title}</Descriptions.Item>
            <Descriptions.Item label="Mô tả">{task.description}</Descriptions.Item>
            <Descriptions.Item label="Mức độ ưu tiên">
              <Tag color={task.priority === 'HIGH' ? 'red' : task.priority === 'MEDIUM' ? 'orange' : 'blue'}>{task.priority}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Hạn chót">{task.dueDate ? new Date(task.dueDate).toLocaleString() : 'Không có'}</Descriptions.Item>
            <Descriptions.Item label="Trạng thái">{task.isCompleted ? 'Đã hoàn thành' : 'Chưa hoàn thành'}</Descriptions.Item>
            <Descriptions.Item label="Lặp lại">
              {recurrence?.type ?? recurrence?.recurrenceType ?? '—'}
            </Descriptions.Item>
          </Descriptions>
        ) : null}
      </Spin>
      {/* File đính kèm */}
      {task && (
        <AttachmentSection taskId={task.id} />
      )}
      {/* Nhắc nhở */}
      {task && (
        <ReminderSection taskId={task.id} />
      )}
    </Modal>
  );
};

export default TaskDetailModal;
