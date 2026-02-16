import React, { useEffect, useState } from 'react';
import { Modal, Tag, Descriptions, Spin, message } from 'antd';
import AttachmentSection from '../AttachmentSection';
import ReminderSection from '../ReminderSection';
import { getTaskById } from '../../api/task'; 

interface TaskDetailModalProps {
  open: boolean;
  taskId: number | null;
  onCancel: () => void;
  userId: number;
}

const TaskDetailModal: React.FC<TaskDetailModalProps> = ({ open, taskId, onCancel, userId }) => {
  const [task, setTask] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open && taskId) {
      setLoading(true);
      getTaskById(userId, taskId)
        .then(setTask)
        .catch(() => message.error('Không lấy được chi tiết task'))
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
