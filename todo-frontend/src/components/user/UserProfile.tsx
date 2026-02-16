import { useEffect, useState } from 'react';
import { Form, Input, Button, Upload, Avatar, message, Card, Space, Popconfirm, Alert } from 'antd';
import { UploadOutlined, UserOutlined, DeleteOutlined } from '@ant-design/icons';
import styled from 'styled-components';
import {
  getUserById,
  updateUser,
  updateUserAvatar,
  deleteUser,
} from '../../api/user';
import type { UserResponse } from '../../api/user';
import { useNavigate } from 'react-router-dom';

const Page = styled.div`
  min-height: 100vh;
  padding: 40px 200px 64px;
  display: flex;
  justify-content: center;
  align-items: center;
  align-self: center;
  justify-self: center;
  font-family: 'Manrope', 'Segoe UI', system-ui, sans-serif;
`;

const ProfileCard = styled(Card)`
  width: 100%;
  max-width: 980px;
  border-radius: 24px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
  /* border: 1px solid rgba(148, 163, 184, 0.2); */

  .ant-card-head {
    /* border-bottom: 1px solid rgba(148, 163, 184, 0.2); */
    /* border-top-left-radius: 24px; */
    /* border-top-right-radius: 24px; */
  }

  .ant-card-head-title {
    padding: 18px 24px;
  }

  .ant-card-body {
    padding: 28px 28px 32px;
  }
`;

const TitleWrap = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #f8fafc;
`;

const TitleText = styled.div`
  justify-self: center !important;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.2px;
  color: black;
`;

const HeaderRow = styled.div`
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 24px;
  align-items: center;
  margin-bottom: 28px;

  @media (max-width: 720px) {
    grid-template-columns: 1fr;
  }
`;

const AvatarStack = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  border: 1px dashed rgba(148, 163, 184, 0.35);
`;

const NameBlock = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;

  .name {
    font-size: 22px;
    font-weight: 700;
    color: #0f172a;
  }

  .meta {
    font-size: 14px;
    color: #64748b;
  }
`;

const FormGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 20px;

  @media (max-width: 720px) {
    grid-template-columns: 1fr;
  }
`;

const ActionsRow = styled.div`
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
`;

const DangerZone = styled.div`
  margin-top: 26px;
  padding-top: 20px;
  /* border-top: 1px solid rgba(148, 163, 184, 0.2); */
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;

  .danger-title {
    font-weight: 600;
    color: #b91c1c;
  }

  .danger-desc {
    font-size: 13px;
    color: #64748b;
  }
`;

const UserProfile = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [profileLoading, setProfileLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [errorText, setErrorText] = useState<string | null>(null);
  const [user, setUser] = useState<UserResponse | null>(null);
  const navigate = useNavigate();

  const userId = Number(localStorage.getItem('userId'));

  useEffect(() => {
    if (!userId) {
      return;
    }
    (async () => {
      setProfileLoading(true);
      try {
        const data = await getUserById(userId);
        setUser(data);
        form.setFieldsValue({ username: data.username, fullname: data.fullName });
        setErrorText(null);
      } catch (err) {
        setErrorText('Failed to load profile data.');
      } finally {
        setProfileLoading(false);
      }
    })();
  }, [userId]);

  const onFinish = async (values: any) => {
    try {
      if (!userId) {
        return;
      }
      setLoading(true);
      await updateUser(userId, { username: values.username, fullname: values.fullname });
      message.success('Thành công');
      setErrorText(null);
    } catch (err) {
      message.error('Thất bại');
      setErrorText('Failed to update profile.');
    } finally {
      setLoading(false);
    }
  };

  const beforeUpload = async (file: File) => {
    try {
      if (!userId) {
        message.error('Cần đăng nhập');
        return false;
      }
      setUploading(true);
      await updateUserAvatar(userId, file);
      message.success('Thành công');
      setUser((prev) => (prev ? { ...prev, avatarUrl: URL.createObjectURL(file) } : prev));
      setErrorText(null);
    } catch (err) {
      message.error('Upload thất bại');
      setErrorText('Failed to upload avatar.');
    } finally {
      setUploading(false);
    }
    return false;
  };

  const handleDelete = async () => {
    try {
      if (!userId) {
        message.error('Chưa đăng nhập');
        return;
      }
      setDeleting(true);
      await deleteUser(userId);
      message.success('Đã xóa tài khoản');
      localStorage.removeItem('userId');
      navigate('/register');
      setErrorText(null);
    } catch (err) {
      message.error('Xóa thất bại');
      setErrorText('Failed to delete account.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <Page>
      <ProfileCard
        title={
          <TitleWrap>
            <TitleText>Thông tin người dùng</TitleText>
          </TitleWrap>
        }
        variant={'borderless'}
        loading={profileLoading}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          {errorText ? <Alert type="error" message={errorText} showIcon /> : null}
          <HeaderRow>
            <AvatarStack>
              <Avatar size={88} icon={<UserOutlined />} src={user?.avatarUrl} />
              <Upload beforeUpload={beforeUpload} showUploadList={false}>
                <Button icon={<UploadOutlined />} loading={uploading}>Thay Avatar</Button>
              </Upload>
            </AvatarStack>
            <NameBlock>
              <div className="name">
                {user?.fullName || user?.username || 'User'}
              </div>
              <div className="meta">
                {user?.username ? `@${user.username}` : ''}
              </div>
            </NameBlock>
          </HeaderRow>

          <Form form={form} layout="vertical" onFinish={onFinish}>
            <FormGrid>
              <Form.Item name="username" label="Tên đăng nhập" rules={[{ required: false }]}>
                <Input placeholder="username" disabled={loading || uploading || deleting} />
              </Form.Item>
              <Form.Item name="fullname" label="Tên đầy đủ">
                <Input placeholder="Full name" disabled={loading || uploading || deleting} />
              </Form.Item>
            </FormGrid>
            <ActionsRow>
              <Button type="primary" htmlType="submit" loading={loading} disabled={uploading || deleting}>
                Lưu thay đổi
              </Button>
            </ActionsRow>
          </Form>

          <DangerZone>
            <div>
              <div className="danger-title">Danger zone</div>
              <div className="danger-desc">Delete your account and remove all data.</div>
            </div>
            <Popconfirm
              title="Xóa tài khoản"
              description="Bạn có chắc chắn muốn xóa tài khoản này?"
              onConfirm={handleDelete}
              okText="Xóa"
              cancelText="Hủy"
            >
              <Button danger icon={<DeleteOutlined />} loading={deleting} disabled={loading || uploading}>
                Xóa tài khoản
              </Button>
            </Popconfirm>
          </DangerZone>
        </Space>
      </ProfileCard>
    </Page>
  );
};

export default UserProfile;
