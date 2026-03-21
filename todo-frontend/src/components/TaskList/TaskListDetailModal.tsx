import React, { useEffect, useState } from 'react';
import { Modal, Descriptions, Spin, message } from 'antd';
import { getTaskListById } from '../../api/taskList';

interface TaskListDetailModalProps {
  open: boolean;
  taskListId: number | null;
  onCancel: () => void;
}

const TaskListDetailModal: React.FC<TaskListDetailModalProps> = ({ open, taskListId, onCancel }) => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open || !taskListId) {
      setData(null);
      return;
    }
    setLoading(true);
    getTaskListById(taskListId)
      .then(setData)
      .catch(() => message.error('Không tải được chi tiết task list'))
      .finally(() => setLoading(false));
  }, [open, taskListId]);

  return (
    <Modal open={open} onCancel={onCancel} footer={null} title="Chi tiết task list" destroyOnClose>
      <Spin spinning={loading}>
        {data ? (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="ID">{data.id}</Descriptions.Item>
            <Descriptions.Item label="Tên">{data.name}</Descriptions.Item>
            <Descriptions.Item label="Mô tả">{data.description ?? '—'}</Descriptions.Item>
            <Descriptions.Item label="Màu">{data.color ?? '—'}</Descriptions.Item>
            <Descriptions.Item label="Mã tham gia">{data.joinCode ?? data.code ?? '—'}</Descriptions.Item>
          </Descriptions>
        ) : null}
      </Spin>
    </Modal>
  );
};

export default TaskListDetailModal;
