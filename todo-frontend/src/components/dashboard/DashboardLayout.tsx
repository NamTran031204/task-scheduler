import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Layout, 
  Menu, 
  Input, 
  Avatar, 
  Badge, 
  Button, 
  Typography, 
  theme,
  Space,
  Dropdown,
  message,
} from 'antd';
import { useAuth } from '../../contexts/AuthContext';
import type { MenuProps } from 'antd';
import MemberManagement from "../user/MemberManagement"
import NotesPage from '../../pages/NotesPage';
import TeamsPage from '../../pages/TeamsPage';
import IntegrationsPage from '../../pages/IntegrationsPage';
import RemindersPage from '../../pages/RemindersPage';
import CalendarView from '../CalendarView';
import type { Task, TaskList } from '../../types/task';
import { 
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SearchOutlined,
  BellOutlined,
  UserOutlined,
  ProjectOutlined,
  CalendarOutlined,
  SettingOutlined,
  LogoutOutlined,
  TeamOutlined,
  NotificationOutlined,
  // FileTextOutlined,
  CommentOutlined,
  FormatPainterOutlined,
} from '@ant-design/icons';

type CustomMenuItem = {
  key: string;
  icon: React.ReactNode;
  label: string;
  component: React.ReactNode;
};

const { Header, Sider, Content } = Layout;
const { Title, } = Typography;

interface DashboardLayoutProps {
  collapsed: boolean;
  onCollapse: (collapsed: boolean) => void;
  taskLists: TaskList[];
  selectedListId: number | null;
  onSelectList: React.Dispatch<React.SetStateAction<number | null>>;
  onAddTaskList: () => void;
  tasks: Task[];
  onAddTask: () => void;
  onEditTask: (task: Task) => void;
  onDeleteTask: (taskId: number) => void;
  onToggleComplete: (task: Task) => void;
  onShowDetail: (taskId: number) => void;
  showCalendar: boolean;
  onToggleView: () => void;
  taskModalOpen: boolean;
  onTaskModalOk: (values: any) => void;
  onTaskModalCancel: () => void;
  taskModalMode: 'create' | 'edit';
  editingTask: Task | null;
  taskListModalOpen: boolean;
  onTaskListModalOk: (values: any) => void;
  onTaskListModalCancel: () => void;
  memberModalOpen: boolean;
  onMemberModalClose: () => void;
  memberModalTaskListId: number | null;
  currentUserId: number;
}

const DashboardLayout: React.FC<DashboardLayoutProps> = ({
  collapsed,
  onCollapse,
  selectedListId,
  memberModalTaskListId,
  currentUserId,
}) => {
  const { token } = theme.useToken();
  const { logout } = useAuth();
  const navigate = useNavigate();
  const taskListId = memberModalTaskListId ?? selectedListId ?? 0;
  
  const getSelectedKey = () => {
    const path = location.pathname;
    if (path.includes('calendar')) return 'calendar';
    if (path.includes('note')) return 'note';
    if (path.includes('teams')) return 'teams';
    if (path.includes('integration')) return 'integration';
    return 'dashboard';
  };
  
  const [selectedKey, setSelectedKey] = useState(getSelectedKey());

  const customMenuItems: CustomMenuItem[] = [
    {
      key: 'dashboard',
      icon: <ProjectOutlined style={{ fontSize: '18px' }} />,
      label: 'Dashboard',
      component: <MemberManagement taskListId={taskListId} currentUserId={currentUserId} />
    },
    {
      key: 'calendar',
      icon: <CalendarOutlined style={{ fontSize: '18px' }} />,
      label: 'My Calendar',
      component: <CalendarView 
        tasks={[]} 
        onShowDetail={() => {}} 
        onAddTask={async () => {}} 
      />
    },
    {
      key: 'note',
      icon: <CommentOutlined style={{ fontSize: '18px' }} />,
      label: 'My Notes',
      component: <NotesPage />
    },
    {
      key: 'reminders',
      icon: <NotificationOutlined style={{ fontSize: '18px' }} />,
      label: 'Reminders',
      component: <RemindersPage userId={currentUserId} />
    },
    {
      key: 'teams',
      icon: <TeamOutlined style={{ fontSize: '18px' }} />,
      label: 'Teams',
      component: <TeamsPage />
    },
    {
      key: 'integration',
      icon: <FormatPainterOutlined style={{ fontSize: '18px' }} />,
      label: 'Integrations',
      component: <IntegrationsPage />
    },
  ];

  const [selectedComponent, setSelectedComponent] = useState(
    customMenuItems[0].component
  );

  const menuItems: MenuProps['items'] = customMenuItems.map(item => ({
    key: item.key,
    icon: item.icon,
    label: item.label,
    onClick: () => {
      setSelectedKey(item.key);
      setSelectedComponent(item.component);
    }
  }));

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key === 'hoso') {
      navigate('/profile');
    } else if (e.key === 'dangxuat') {
      logout();
      message.success('Đăng xuất thành công');
    }
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'hoso',
      label: 'Hồ sơ',
      icon: <UserOutlined />,
    },
    {
      key: 'caidat',
      label: 'Cài đặt',
      icon: <SettingOutlined />,
      // disabled: true,
    },
    {
      type: 'divider',
    },
    {
      key: 'dangxuat',
      label: 'Đăng xuất',
      icon: <LogoutOutlined />,
      // danger: true,
    },
  ];

  

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed} 
        width={250}
        style={{
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          background: token.colorBgContainer,
          borderRight: `1px solid ${token.colorBorderSecondary}`,
          zIndex: 1000
        }}
      >
        <div style={{
          height: '64px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: token.colorBgElevated,
          borderBottom: `1px solid ${token.colorBorderSecondary}`
        }}>
          <Title level={4} style={{
            margin: 0,
            color: token.colorPrimary,
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            padding: '0 16px'
          }}>
            {collapsed ? 'TS' : 'Task Scheduler'}
          </Title>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          onClick={({ key }) => {
            setSelectedKey(key);
            const menuItem = customMenuItems.find(item => item.key === key);
            if (menuItem) {
              setSelectedComponent(menuItem.component);
            }
          }}
          style={{ height: '100%', borderRight: 0 }}
          items={menuItems}
        />
        <div style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          padding: '16px',
          display: 'flex',
          alignItems: 'center',
          borderTop: `1px solid ${token.colorBorderSecondary}`,
          background: token.colorBgElevated
        }}>
          <Dropdown menu={{ items: userMenuItems, onClick: handleMenuClick }} placement="topRight" trigger={['click']}>
            
          </Dropdown>
        </div>
      </Sider>
      <Layout>
        <Header style={{
          padding: '0 24px',
          background: token.colorBgContainer,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: `1px solid ${token.colorBorderSecondary}`,
          
        }}>
          <Space size="middle">
            {React.createElement(collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
              style: { fontSize: '18px', cursor: 'pointer' },
              onClick: () => onCollapse(!collapsed),
            })}
            <Input
              placeholder="Search..."
              prefix={<SearchOutlined style={{ color: token.colorTextPlaceholder }} />}
              style={{ width: 200, borderRadius: '20px' }}
              variant={'borderless'}
            />
          </Space>
          <Space size="large">
            <Badge count={5} size="small" style={{ boxShadow: 'none' }}>
              <Button 
                type="text" 
                shape="circle" 
                icon={<BellOutlined style={{ fontSize: '18px' }} />} 
              />
            </Badge>
            <Dropdown menu={{ items: userMenuItems, onClick: handleMenuClick }} trigger={['click']}>
              <Avatar 
                size={36} 
                icon={<UserOutlined />} 
                style={{ 
                  backgroundColor: token.colorPrimary,
                  color: '#fff',
                  cursor: 'pointer'
                }} 
              />
            </Dropdown>
          </Space>
        </Header>
        <Content
          style={{
            marginLeft: collapsed ? 80 : 250,
            minHeight: '100vh',
            transition: 'all 0.2s',
            minWidth: "1100px"
          }}
        >
          {selectedComponent}
        </Content>
      </Layout>
    </Layout>
  );
};

export default DashboardLayout;
