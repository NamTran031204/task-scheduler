import React, { useEffect, useState } from 'react';
import { Form, Input, Button, Upload, Avatar, message, Card, Space, Popconfirm } from 'antd';
import { UploadOutlined, UserOutlined, DeleteOutlined } from '@ant-design/icons';
import {
  getUserById,
  updateUser,
  updateUserAvatar,
  deleteUser,
} from '../../api/user';
import type { UserResponse } from '../../api/user';
import { useNavigate } from 'react-router-dom';

const UserProfile: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState<UserResponse | null>(null);
  const navigate = useNavigate();

  const userId = Number(localStorage.getItem('userId'));

  useEffect(() => {
    if (!userId) {
      navigate('/login');
      return;
    }
    (async () => {
      try {
        const data = await getUserById(userId);
        setUser(data);
        form.setFieldsValue({ username: data.username, fullname: data.fullName });
      } catch (err) {
        message.error('Không lấy được thông tin người dùng');
      }
    })();
  }, [userId]);

  const onFinish = async (values: any) => {
    try {
      setLoading(true);
      await updateUser(userId, { username: values.username, fullname: values.fullname });
      message.success('Cập nhật thành công');
    } catch (err) {
      message.error('Cập nhật thất bại');
    } finally {
      setLoading(false);
    }
  };

  const beforeUpload = async (file: File) => {
    try {
      await updateUserAvatar(userId, file);
      message.success('Cập nhật avatar thành công');
      setUser((prev) => (prev ? { ...prev, avatarUrl: URL.createObjectURL(file) } : prev));
    } catch (err) {
      message.error('Upload thất bại');
    }
    return false;
  };

  const handleDelete = async () => {
    try {
      await deleteUser(userId);
      message.success('Đã xoá tài khoản');
      localStorage.removeItem('userId');
      navigate('/register');
    } catch (err) {
      message.error('Xoá tài khoản thất bại');
    }
  };

  return (
    <Card title="Quản lý thông tin người dùng" style={{ maxWidth: 600, margin: '24px auto' }}>
      <Space direction="vertical" style={{ width: '100%' }}>
        <Avatar size={80} icon={<UserOutlined />} src={user?.avatarUrl} />
        <Upload beforeUpload={beforeUpload} showUploadList={false}>
          <Button icon={<UploadOutlined />}>Thay đổi Avatar</Button>
        </Upload>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item name="username" label="Tên đăng nhập" rules={[{ required: true }]}> 
            <Input />
          </Form.Item>
          <Form.Item name="fullname" label="Họ tên"> 
            <Input />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading}>Lưu thay đổi</Button>
          </Form.Item>
        </Form>
        <Popconfirm
          title="Xoá tài khoản?"
          description="Hành động này không thể hoàn tác"
          onConfirm={handleDelete}
          okText="Xoá"
          cancelText="Huỷ"
        >
          <Button danger icon={<DeleteOutlined />}>Xoá tài khoản</Button>
        </Popconfirm>
      </Space>
    </Card>
  );
};

export default UserProfile;
