import React, { useEffect, useState } from 'react';
import { List, Button, Input, message, Tag, Popconfirm, Typography } from 'antd';
import { getTaskListMembers, shareTaskList, removeTaskListMember, transferTaskListHost } from "../../api/taskList"

interface Member {
  id: number;
  email: string;
  name: string;
  role: 'HOST' | 'MEMBER';
}

interface MemberManagementProps {
  taskListId: number;
  currentUserId: number;
}

const MemberManagement: React.FC<MemberManagementProps> = ({ taskListId, currentUserId }) => {
  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviting, setInviting] = useState(false);
  const [transferring, setTransferring] = useState<number | null>(null);

  const fetchMembers = async () => {
    setLoading(true);
    try {
      const data = await getTaskListMembers(taskListId);
      setMembers(data.content || []);
    } catch {
      message.error('Không lấy được danh sách thành viên');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMembers();
  }, [taskListId]);

  const handleInvite = async () => {
    if (!inviteEmail) return;
    setInviting(true);
    try {
      await shareTaskList(taskListId, inviteEmail);
      message.success('Đã gửi lời mời!');
      setInviteEmail('');
      fetchMembers();
    } catch {
      message.error('Mời thất bại!');
    } finally {
      setInviting(false);
    }
  };

  const handleRemove = async (userId: number) => {
    setLoading(true);
    try {
      await removeTaskListMember(taskListId, userId);
      message.success('Đã xóa thành viên!');
      fetchMembers();
    } catch {
      message.error('Xóa thành viên thất bại!');
    } finally {
      setLoading(false);
    }
  };

  const handleTransferHost = async (userId: number) => {
    setTransferring(userId);
    try {
      await transferTaskListHost(taskListId, userId);
      message.success('Đã chuyển quyền chủ nhóm!');
      fetchMembers();
    } catch {
      message.error('Chuyển quyền thất bại!');
    } finally {
      setTransferring(null);
    }
  };

  const host = members.find(m => m.role === 'HOST');
  const isHost = host?.id === currentUserId;

  return (
    <div style={{ padding: '16px' }}>
      <Typography.Title level={4}>Quản lý thành viên</Typography.Title>
      <div style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="Nhập email để mời thành viên"
          value={inviteEmail}
          onChange={e => setInviteEmail(e.target.value)}
          onSearch={handleInvite}
          enterButton="Mời"
          loading={inviting}
          disabled={!isHost}
          style={{ maxWidth: 400 }}
        />
      </div>
      <List
        loading={loading}
        dataSource={members}
        renderItem={member => (
          <List.Item
            actions={[
              member.role === 'MEMBER' && isHost ? (
                <Popconfirm title="Xóa thành viên này?" onConfirm={() => handleRemove(member.id)} okText="Xóa" cancelText="Hủy">
                  <Button size="small" danger>Xóa</Button>
                </Popconfirm>
              ) : null,
              member.role === 'MEMBER' && isHost ? (
                <Button size="small" loading={transferring === member.id} onClick={() => handleTransferHost(member.id)}>
                  Chuyển quyền chủ nhóm
                </Button>
              ) : null,
              member.role === 'HOST' ? <Tag color="gold">Chủ nhóm</Tag> : null
            ].filter(Boolean)}
          >
            <List.Item.Meta title={member.name || member.email} description={member.email} />
          </List.Item>
        )}
      />
    </div>
  );
};

export default MemberManagement;
