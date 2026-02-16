import { useEffect, useState } from 'react';
import { List, Button, Input, message, Tag, Popconfirm, Typography, Space } from 'antd';
import {
  getTaskListMembers,
  shareTaskList,
  removeTaskListMember,
  updateTaskListMemberRole,
  joinTaskList,
  leaveTaskList,
} from "../../api/taskList";

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

const MemberManagement = ({ taskListId, currentUserId }: MemberManagementProps) => {
  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(false);
  const [inviteUserId, setInviteUserId] = useState('');
  const [joinCode, setJoinCode] = useState('');
  const [inviting, setInviting] = useState(false);
  const [joining, setJoining] = useState(false);
  const [leaving, setLeaving] = useState(false);
  const [transferring, setTransferring] = useState<number | null>(null);

  const fetchMembers = async () => {
    if (!taskListId) {
      setMembers([]);
      return;
    }
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
    const targetUserId = Number(inviteUserId);
    if (!targetUserId || !taskListId) return;
    setInviting(true);
    try {
      await shareTaskList(taskListId, targetUserId);
      message.success('Đã chia sẻ task list!');
      setInviteUserId('');
      fetchMembers();
    } catch {
      message.error('Chia sẻ thất bại!');
    } finally {
      setInviting(false);
    }
  };

  const handleRemove = async (userId: number) => {
    if (!taskListId) return;
    setLoading(true);
    try {
      await removeTaskListMember(taskListId, userId, currentUserId);
      message.success('Đã xóa thành viên!');
      fetchMembers();
    } catch {
      message.error('Xóa thành viên thất bại!');
    } finally {
      setLoading(false);
    }
  };

  const handleTransferHost = async (userId: number) => {
    if (!taskListId) return;
    setTransferring(userId);
    try {
      await updateTaskListMemberRole(taskListId, userId, currentUserId, 'HOST');
      message.success('Đã chuyển vai trò thành Host!');
      fetchMembers();
    } catch {
      message.error('Chuyển vai trò thất bại!');
    } finally {
      setTransferring(null);
    }
  };

  const host = members.find(m => m.role === 'HOST');
  const isHost = host?.id === currentUserId;

  return (
    <div style={{ padding: '16px' }}>
      <Typography.Title level={4}>Quản lý thành viên</Typography.Title>
      <Space direction="vertical" size={12} style={{ width: '100%', marginBottom: 16 }}>
        <Input.Search
          placeholder="Nhập UserId để chia sẻ"
          value={inviteUserId}
          onChange={e => setInviteUserId(e.target.value)}
          onSearch={handleInvite}
          enterButton="Chia sẻ"
          loading={inviting}
          disabled={!isHost}
          style={{ maxWidth: 420 }}
        />
        <Input.Search
          placeholder="Nhập mã task list tham gia"
          value={joinCode}
          onChange={e => setJoinCode(e.target.value)}
          onSearch={async () => {
            if (!joinCode.trim() || !currentUserId) return;
            setJoining(true);
            try {
              await joinTaskList(joinCode.trim(), currentUserId);
              message.success('Đã tham gia task list!');
              setJoinCode('');
            } catch {
              message.error('Tham gia thất bại!');
            } finally {
              setJoining(false);
            }
          }}
          enterButton="Tham gia"
          loading={joining}
          style={{ maxWidth: 420 }}
        />
        {!isHost && taskListId ? (
          <Popconfirm
            title="Rời khỏi task list này?"
            onConfirm={async () => {
              setLeaving(true);
              try {
                await leaveTaskList(taskListId, currentUserId);
                message.success('Đã rời khỏi task list!');
                fetchMembers();
              } catch {
                message.error('Rời task list thất bại!');
              } finally {
                setLeaving(false);
              }
            }}
            okText="Rời đi"
            cancelText="Hủyy"
          >
            <Button danger loading={leaving} style={{ maxWidth: 200 }}>
              Rời task list
            </Button>
          </Popconfirm>
        ) : null}
      </Space>
      <List
        loading={loading}
        dataSource={members}
        renderItem={member => (
          <List.Item
            actions={[
              member.role === 'MEMBER' && isHost ? (
                <Popconfirm
                  title="Xóa thành viên này?"
                  onConfirm={() => handleRemove(member.id)}
                  okText="Xóa"
                  cancelText="Hủy"
                >
                  <Button size="small" danger>XÃ³a</Button>
                </Popconfirm>
              ) : null,
              member.role === 'MEMBER' && isHost ? (
                <Button
                  size="small"
                  loading={transferring === member.id}
                  onClick={() => handleTransferHost(member.id)}
                >
                  Chuyển vai trò
                </Button>
              ) : null,
              member.role === 'HOST' ? <Tag color="gold">Chấp nhận</Tag> : null
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
