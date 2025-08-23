import React, { useEffect, useState } from 'react';
import { List, Button, DatePicker, TimePicker, Input, Space, message, Popconfirm, Spin } from 'antd';
import dayjs from 'dayjs';
import {
  getTaskReminders,
  createTaskReminder,
  updateTaskReminder,
  deleteTaskReminder,
} from '../api/task';

interface ReminderSectionProps {
  taskId: number;
}

const ReminderSection: React.FC<ReminderSectionProps> = ({ taskId }) => {
  const [reminders, setReminders] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [newDate, setNewDate] = useState<any>(null);
  const [newTime, setNewTime] = useState<any>(null);
  const [note, setNote] = useState('');

  const fetchReminders = async () => {
    setLoading(true);
    try {
      const data = await getTaskReminders(taskId);
      setReminders(data.content || []);
    } catch {
      message.error('Không lấy được nhắc nhở');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (taskId) fetchReminders();
  }, [taskId]);

  const handleCreate = async () => {
    if (!newDate || !newTime) return message.warning('Chọn ngày và giờ!');
    setCreating(true);
    try {
      const dateTime = dayjs(newDate).hour(dayjs(newTime).hour()).minute(dayjs(newTime).minute()).second(0).toISOString();
      await createTaskReminder(taskId, { remindAt: dateTime, note });
      message.success('Đã thêm nhắc nhở!');
      setNewDate(null); setNewTime(null); setNote('');
      fetchReminders();
    } catch {
      message.error('Tạo nhắc nhở thất bại!');
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (reminderId: number) => {
    setLoading(true);
    try {
      await deleteTaskReminder(reminderId);
      message.success('Đã xóa nhắc nhở!');
      fetchReminders();
    } catch {
      message.error('Xóa nhắc nhở thất bại!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ marginTop: 32 }}>
      <b>Nhắc nhở</b>
      <Space style={{ marginTop: 8, marginBottom: 16 }}>
        <DatePicker value={newDate} onChange={setNewDate} />
        <TimePicker value={newTime} onChange={setNewTime} format="HH:mm" />
        <Input placeholder="Ghi chú" value={note} onChange={e => setNote(e.target.value)} style={{ width: 160 }} />
        <Button type="primary" onClick={handleCreate} loading={creating}>Thêm</Button>
      </Space>
      <Spin spinning={loading}>
        <List
          dataSource={reminders}
          locale={{ emptyText: 'Không có nhắc nhở' }}
          renderItem={item => (
            <List.Item
              actions={[
                <Popconfirm title="Xóa nhắc nhở này?" onConfirm={() => handleDelete(item.id)} okText="Xóa" cancelText="Hủy">
                  <Button danger size="small">Xóa</Button>
                </Popconfirm>
              ]}
            >
              <List.Item.Meta
                title={dayjs(item.remindAt).format('DD/MM/YYYY HH:mm')}
                description={item.note}
              />
            </List.Item>
          )}
        />
      </Spin>
    </div>
  );
};

export default ReminderSection;
