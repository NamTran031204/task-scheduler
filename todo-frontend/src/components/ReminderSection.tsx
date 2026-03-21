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
  const [states, setStates] = useState<{
    reminders: any[];
    loading: boolean;
    creating: boolean;
    newDate: any;
    newTime: any;
    note: string;
    editingId: number | null;
    editNote: string;
    editDate: any;
    editTime: any;
    savingEdit: boolean;
  }>({
    reminders: [],
    loading: false,
    creating: false,
    newDate: null,
    newTime: null,
    note: '',
    editingId: null,
    editNote: '',
    editDate: null,
    editTime: null,
    savingEdit: false,
  })

  const fetchReminders = async () => {
    setStates((s) => ({ ...s, loading: true }));
    try {
      const data = await getTaskReminders(taskId);
      setStates((s) => ({ ...s, reminders: data.content || [] }));
    } catch {
      message.error('Không lấy được nhắc nhở');
    } finally {
      setStates((s) => ({ ...s, loading: false }));
    }
  };

  useEffect(() => {
    if (taskId) fetchReminders();
  }, [taskId]);

  const handleCreate = async () => {
    if (!states.newDate || !states.newTime) return message.warning('Chọn ngày và giờ!');
    setStates((s) => ({ ...s, creating: true }));
    try {
      const dateTime = 
        dayjs(states.newDate)
        .hour(dayjs(states.newTime)
        .hour()).minute(dayjs(states.newTime)
        .minute()).second(0).toISOString();
      await createTaskReminder(taskId, { remindAt: dateTime, note: states.note });
      message.success('Đã thêm nhắc nhở!');
      setStates((s) => ({ ...s, newDate: null, newTime: null, note: '' }));
      fetchReminders();
    } catch {
      message.error('Tạo nhắc nhở thất bại!');
    } finally {
      setStates((s) => ({ ...s, creating: false }));
    }
  };

  const handleDelete = async (reminderId: number) => {
    setStates((s) => ({ ...s, loading: true }));
    try {
      await deleteTaskReminder(reminderId);
      message.success('Đã xóa nhắc nhở!');
      fetchReminders();
    } catch {
      message.error('Xóa nhắc nhở thất bại!');
    } finally {
      setStates((s) => ({ ...s, loading: false }));
    }
  };

  const startEdit = (item: any) => {
    // setEditingId(item.id);
    // setEditNote(item.note ?? '');
    setStates((s) => ({...s, editingId: item.id, editNote: item.note ?? ''}))
    const at = item.remindAt ? dayjs(item.remindAt) : dayjs();
    setStates((s) => ({...s, editTime: at, editDate: at}));
  };

  const saveEdit = async (reminderId: number) => {
    if (!states.editDate || !states.editTime) {
      message.warning('Chọn ngày và giờ');
      return;
    }
    setStates((s) => ({ ...s, savingEdit: true }));
    try {
      const dateTime = 
        dayjs(states.editDate)
        .hour(dayjs(states.editTime)
        .hour()).minute(dayjs(states.editTime)
        .minute()).second(0).toISOString();
      await updateTaskReminder(reminderId, { remindAt: dateTime, note: states.editNote });
      message.success('Đã cập nhật nhắc nhở');
      setStates((s) => ({ ...s, editingId: null }));
      fetchReminders();
    } catch {
      message.error('Cập nhật thất bại');
    } finally {
      // setSavingEdit(false);
      setStates((s) => ({ ...s, savingEdit: false }));
    }
  };

  return (
    <div style={{ marginTop: 32 }}>
      <b>Nhắc nhở</b>
      <Space style={{ marginTop: 8, marginBottom: 16 }}>
        <DatePicker value={states.newDate} 
        onChange={v => setStates(s => ({...s, newDate: v}))} />
        <TimePicker value={states.newTime} onChange={v => setStates(s => ({...s, newTime: v}))} format="HH:mm" />
        <Input placeholder="Ghi chú" 
              value={states.note} 
              onChange={e => setStates(s => ({...s, note: e.target.value}))} 
              style={{ width: 160 }} 
        />
        <Button type="primary" onClick={handleCreate} loading={states.creating}>Thêm</Button>
      </Space>
      <Spin spinning={states.loading}>
        <List
          dataSource={states.reminders}
          locale={{ emptyText: 'Không có nhắc nhở' }}
          renderItem={item => (
            <List.Item
              actions={[
                states.editingId === item.id ? (
                  <Button size="small" 
                          type="primary" 
                          loading={states.savingEdit} 
                          onClick={() => saveEdit(item.id)}
                  >
                    Lưu
                  </Button>
                ) : (
                  <Button size="small" onClick={() => startEdit(item)}>
                    Sửa
                  </Button>
                ),
                <Popconfirm 
                  title="Xóa nhắc nhở này?" 
                  onConfirm={() => handleDelete(item.id)} 
                  okText="Xóa" 
                  cancelText="Hủy"
                >
                  <Button danger size="small">Xóa</Button>
                </Popconfirm>
              ]}
            >
              <List.Item.Meta
                title={
                  states.editingId === item.id ? (
                    <Space wrap>
                      <DatePicker 
                        value={states.editDate} 
                        onChange={v => setStates(s => ({...s, editDate: v}))} 
                      />
                      <TimePicker 
                        value={states.editTime} 
                        onChange={v => setStates(s => ({...s, editTime: v}))} format="HH:mm" 
                      />
                    </Space>
                  ) : (
                    dayjs(item.remindAt).format('DD/MM/YYYY HH:mm')
                  )
                }
                description={
                  states.editingId === item.id ? (
                    <Input 
                      value={states.editNote} 
                      onChange={e => setStates(s => ({...s, editNote: e.target.value}))} 
                      placeholder="Ghi chú" 
                      style={{ marginTop: 8 }} 
                    />
                  ) : (
                    item.note
                  )
                }
              />
            </List.Item>
          )}
        />
      </Spin>
    </div>
  );
};

export default ReminderSection;
