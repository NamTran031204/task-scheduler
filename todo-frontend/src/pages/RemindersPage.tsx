import { useEffect, useState } from 'react';
import { Card, List, Button, Space, Input, message, Popconfirm, Tag, Typography } from 'antd';
import dayjs from 'dayjs';
import {
  getRemindersByUser,
  getPendingRemindersByUser,
  updateReminder,
  deleteReminder,
  sendDueReminders,
} from '../api/reminder';

interface ReminderItem {
  id: number;
  message?: string;
  remindAt?: string;
  remind_at?: string;
  dueDate?: string;
  createdAt?: string;
  status?: string;
  taskId?: number;
}

interface RemindersPageProps {
  userId: number;
}

const RemindersPage = ({ userId }: RemindersPageProps) => {
  const [allReminders, setAllReminders] = useState<ReminderItem[]>([]);
  const [pendingReminders, setPendingReminders] = useState<ReminderItem[]>([]);
  const [loadingAll, setLoadingAll] = useState(false);
  const [loadingPending, setLoadingPending] = useState(false);
  const [sending, setSending] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingMessage, setEditingMessage] = useState('');

  const getTimeLabel = (item: ReminderItem) => {
    const time = item.remindAt || item.remind_at || item.dueDate || item.createdAt;
    if (!time) return '--';
    return dayjs(time).format('DD/MM/YYYY HH:mm');
  };

  const loadAll = async () => {
    if (!userId) return;
    setLoadingAll(true);
    try {
      const data = await getRemindersByUser(userId);
      setAllReminders(data.content || []);
    } catch {
      message.error('Không lấy được danh sách nhắc nhở');
    } finally {
      setLoadingAll(false);
    }
  };

  const loadPending = async () => {
    if (!userId) return;
    setLoadingPending(true);
    try {
      const data = await getPendingRemindersByUser(userId);
      setPendingReminders(data.content || []);
    } catch {
      message.error('Không lấy được danh sách nhắc nhở');
    } finally {
      setLoadingPending(false);
    }
  };

  useEffect(() => {
    loadAll();
    loadPending();
  }, [userId]);

  const handleEdit = (item: ReminderItem) => {
    setEditingId(item.id);
    setEditingMessage(item.message || '');
  };

  const handleSave = async (id: number) => {
    if (!userId) return;
    try {
      await updateReminder(userId, id, { message: editingMessage });
      message.success('Đã cập nhật nhắc nhở');
      setEditingId(null);
      setEditingMessage('');
      loadAll();
      loadPending();
    } catch {
      message.error('Cập nhật nhắc nhở thất bại');
    }
  };

  const handleDelete = async (id: number) => {
    if (!userId) return;
    try {
      await deleteReminder(userId, id);
      message.success('Đã xóa nhắc nhở');
      loadAll();
      loadPending();
    } catch {
      message.error('Xóa nhắc nhở thất bại');
    }
  };

  const handleSendDue = async () => {
    setSending(true);
    try {
      await sendDueReminders();
      message.success('Đã gửi nhắc nhở');
      loadPending();
    } catch {
      message.error('Gửi nhắc nhở thất bại');
    } finally {
      setSending(false);
    }
  };

  const renderItem = (item: ReminderItem) => (
    <List.Item
      actions={[
        editingId === item.id ? (
          <Button size="small" type="primary" onClick={() => handleSave(item.id)}>
            LÆ°u
          </Button>
        ) : (
          <Button size="small" onClick={() => handleEdit(item)}>
            Sá»­a
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
          <Space size={8}>
            <span>{getTimeLabel(item)}</span>
            {item.status ? <Tag>{item.status}</Tag> : null}
          </Space>
        }
        description={
          editingId === item.id ? (
            <Input
              value={editingMessage}
              onChange={e => setEditingMessage(e.target.value)}
              placeholder="Nội dung nhắc nhở"
            />
          ) : (
            item.message || 'Không có nội dung'
          )
        }
      />
    </List.Item>
  );

  return (
    <div style={{ padding: 24 }}>
      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <Card>
          <Space direction="vertical" size={12} style={{ width: '100%' }}>
            <Typography.Title level={4} style={{ margin: 0 }}>
              Reminder Center
            </Typography.Title>
            <Space>
              <Button onClick={loadAll}>Tải lại tất cả</Button>
              <Button onClick={loadPending}>Tải lại pending</Button>
              <Button type="primary" loading={sending} onClick={handleSendDue}>
                Gửi nhắc nhở
              </Button>
            </Space>
          </Space>
        </Card>

        <Card title="Tất cả cập nhật" variant={'borderless'}>
          <List
            loading={loadingAll}
            dataSource={allReminders}
            locale={{ emptyText: 'Không cần nhắc nhở' }}
            renderItem={renderItem}
          />
        </Card>

        <Card title="Nhắc nhở chưa gửi" bordered>
          <List
            loading={loadingPending}
            dataSource={pendingReminders}
            locale={{ emptyText: 'Không có nhắc nhở chưa gửi' }}
            renderItem={renderItem}
          />
        </Card>
      </Space>
    </div>
  );
};

export default RemindersPage;
