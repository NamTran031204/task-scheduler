import { useEffect, useState } from 'react';
import { Card, Table, Typography, Space, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { getAllUsers, type UserResponse } from '../api/user';

const TeamsPage: React.FC = () => {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [totalPage, setTotalPage] = useState(0);
  const [page, setPage] = useState(0);
  const [record] = useState(20);
  const [loading, setLoading] = useState(false);

  const load = async (p: number) => {
    setLoading(true);
    try {
      const data = await getAllUsers(record, p);
      setUsers(data.userResponses ?? []);
      setTotalPage(data.totalPage ?? 0);
    } catch {
      message.error('Không tải được danh sách người dùng');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(page);
  }, [page, record]);

  const columns: ColumnsType<UserResponse> = [
    { title: 'ID', dataIndex: 'id', width: 72 },
    { title: 'Username', dataIndex: 'username' },
    { title: 'Email', dataIndex: 'email' },
    { title: 'Họ tên', dataIndex: 'fullName', render: (v) => v || '—' },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <Typography.Title level={4} style={{ margin: 0 }}>
            Người dùng
          </Typography.Title>
          <Typography.Text type="secondary">
            Dùng User ID khi chia sẻ task list trong Dashboard → Thành viên.
          </Typography.Text>
          <Table
            rowKey="id"
            loading={loading}
            columns={columns}
            dataSource={users}
            pagination={{
              current: page + 1,
              pageSize: record,
              total: totalPage * record,
              onChange: (p) => setPage(p - 1),
              showSizeChanger: false,
            }}
          />
        </Space>
      </Card>
    </div>
  );
};

export default TeamsPage;
