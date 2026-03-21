import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
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
  Modal,
} from 'antd';
import { useAuth } from '../../contexts/AuthContext';
import type { MenuProps } from 'antd';
import MemberManagement from '../user/MemberManagement';
import NotesPage from '../../pages/NotesPage';
import TeamsPage from '../../pages/TeamsPage';
import IntegrationsPage from '../../pages/IntegrationsPage';
import RemindersPage from '../../pages/RemindersPage';
import CalendarView from '../CalendarView';
import TaskListsSidebar from '../TaskList/TaskListsSidebar';
import TaskListView from '../TaskList/TaskListView';
import TaskModal from '../TaskList/TaskModal';
import TaskListModal from '../TaskList/TaskListModal';
import TaskDetailModal from '../TaskList/TaskDetailModal';
import TaskListDetailModal from '../TaskList/TaskListDetailModal';
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
  CommentOutlined,
  FormatPainterOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons';

const { Header, Sider, Content } = Layout;

interface DashboardLayoutProps {
  collapsed: boolean;
  onCollapse: (collapsed: boolean) => void;
  taskLists: TaskList[];
  taskListsLoading: boolean;
  tasksLoading: boolean;
  selectedListId: number | null;
  onSelectList: React.Dispatch<React.SetStateAction<number | null>>;
  onAddTaskList: () => void;
  onEditTaskList: (list: TaskList) => void;
  onDeleteTaskList: (taskListId: number) => void;
  tasks: Task[];
  refreshTasks: () => void;
  refreshAllTasks?: () => void;
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
  taskModalInitialValues: Record<string, unknown>;
  taskListModalOpen: boolean;
  onTaskListModalOk: (values: any) => void;
  onTaskListModalCancel: () => void;
  taskListModalMode: 'create' | 'edit';
  taskListModalInitialValues: Record<string, unknown>;
  memberModalOpen: boolean;
  onMemberModalClose: () => void;
  memberModalTaskListId: number | null;
  currentUserId: number;
  detailModalOpen: boolean;
  detailTaskId: number | null;
  onDetailModalClose: () => void;
  onCalendarAddTask: (task: Omit<Task, 'id' | 'isCompleted'> & { dueDate?: string }) => Promise<void>;
  onCalendarUpdateTask: (task: Task) => Promise<void>;
  taskListDetailOpen: boolean;
  taskListDetailId: number | null;
  onTaskListDetailClose: () => void;
  onShowTaskListDetail: (taskListId: number) => void;
}

const DashboardLayout: React.FC<DashboardLayoutProps> = ({
  collapsed,
  onCollapse,
  taskLists,
  taskListsLoading,
  tasksLoading,
  selectedListId,
  onSelectList,
  onAddTaskList,
  onEditTaskList,
  onDeleteTaskList,
  tasks,
  refreshTasks,
  refreshAllTasks,
  onAddTask,
  onEditTask,
  onDeleteTask,
  onToggleComplete,
  onShowDetail,
  showCalendar,
  onToggleView,
  taskModalOpen,
  onTaskModalOk,
  onTaskModalCancel,
  taskModalMode,
  taskModalInitialValues,
  taskListModalOpen,
  onTaskListModalOk,
  onTaskListModalCancel,
  taskListModalMode,
  taskListModalInitialValues,
  memberModalOpen,
  onMemberModalClose,
  memberModalTaskListId,
  currentUserId,
  detailModalOpen,
  detailTaskId,
  onDetailModalClose,
  onCalendarAddTask,
  onCalendarUpdateTask,
  taskListDetailOpen,
  taskListDetailId,
  onTaskListDetailClose,
  onShowTaskListDetail,
}) => {
  const { token } = theme.useToken();
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const getSelectedKeyFromPath = () => {
    const path = location.pathname;
    if (path.includes('calendar')) return 'calendar';
    if (path.includes('note')) return 'note';
    if (path.includes('teams')) return 'teams';
    if (path.includes('integration')) return 'integration';
    return 'dashboard';
  };
  const [selectedKey, setSelectedKey] = useState(getSelectedKeyFromPath);

  const isCalendarView = showCalendar || selectedKey === 'calendar';
  useEffect(() => {
    if (isCalendarView && refreshAllTasks) {
      refreshAllTasks();
    } else if (!isCalendarView) {
      refreshTasks();
    }
  }, [isCalendarView, refreshAllTasks, refreshTasks]);

  const calendarTasks = tasks.map((t) => {
    const raw = (t as { dueDate?: string; due_date?: string });
    const dueStr = raw.dueDate ?? raw.due_date;
    return {
      id: t.id,
      title: t.title ?? (t as { name?: string }).name ?? '',
      dueDate: dueStr ? String(dueStr) : '',
      priority: t.priority ?? 'MEDIUM',
      isCompleted: t.isCompleted ?? false,
      description: t.description,
    };
  });

  const dashboardWorkspace = (
    <div
      className="ui-dashboard-workspace"
      style={{ display: 'flex', height: '100%', background: token.colorBgLayout }}
    >
      <TaskListsSidebar
        taskLists={taskLists}
        selectedListId={selectedListId}
        onSelectList={(id) => onSelectList(id)}
        loading={taskListsLoading}
        onAddTaskList={onAddTaskList}
        onEditTaskList={onEditTaskList}
        onDeleteTaskList={onDeleteTaskList}
        onShowListDetail={onShowTaskListDetail}
      />
      <div style={{ flex: 1, padding: 24, overflow: 'auto' }}>
        <Space style={{ marginBottom: 12 }} size="middle">
          <Button
            type={showCalendar ? 'default' : 'primary'}
            className="ui-icon-btn"
            icon={<UnorderedListOutlined />}
            onClick={() => !showCalendar || onToggleView()}
          >
            Danh sách
          </Button>
          <Button
            type={showCalendar ? 'primary' : 'default'}
            className="ui-icon-btn"
            icon={<CalendarOutlined />}
            onClick={() => showCalendar || onToggleView()}
          >
            Lịch
          </Button>
        </Space>
        <div key={showCalendar ? 'cal' : 'list'} className="ui-view-crossfade" style={{ width: '100%' }}>
          {showCalendar ? (
            <CalendarView
              tasks={calendarTasks}
              onAddTask={onCalendarAddTask}
              onUpdateTask={onCalendarUpdateTask}
              onShowDetail={(t) => onShowDetail(t.id)}
            />
          ) : (
            <TaskListView
              tasks={tasks}
              loading={tasksLoading}
              taskListId={selectedListId}
              refreshTasks={refreshTasks}
              onAddTask={onAddTask}
              onShowDetail={onShowDetail}
              onDeleteTask={onDeleteTask}
              onEditTask={onEditTask}
              onToggleComplete={onToggleComplete}
            />
          )}
        </div>
      </div>
    </div>
  );

  const renderMainContent = () => {
    switch (selectedKey) {
      case 'calendar':
        return (
          <div style={{ padding: 24, height: '100%', boxSizing: 'border-box' }}>
            <CalendarView
              tasks={calendarTasks}
              onAddTask={onCalendarAddTask}
              onUpdateTask={onCalendarUpdateTask}
              onShowDetail={(t) => onShowDetail(t.id)}
            />
          </div>
        );
      case 'note':
        return <NotesPage />;
      case 'reminders':
        return <RemindersPage userId={currentUserId} />;
      case 'teams':
        return <TeamsPage />;
      case 'integration':
        return <IntegrationsPage />;
      default:
        return dashboardWorkspace;
    }
  };

  const menuItems: MenuProps['items'] = [
    { key: 'dashboard', icon: <ProjectOutlined style={{ fontSize: 18 }} />, label: 'Dashboard' },
    { key: 'calendar', icon: <CalendarOutlined style={{ fontSize: 18 }} />, label: 'My Calendar' },
    { key: 'note', icon: <CommentOutlined style={{ fontSize: 18 }} />, label: 'My Notes' },
    { key: 'reminders', icon: <NotificationOutlined style={{ fontSize: 18 }} />, label: 'Reminders' },
    { key: 'teams', icon: <TeamOutlined style={{ fontSize: 18 }} />, label: 'Teams' },
    { key: 'integration', icon: <FormatPainterOutlined style={{ fontSize: 18 }} />, label: 'Integrations' },
  ];

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key === 'hoso') {
      navigate('/profile');
    } else if (e.key === 'dangxuat') {
      logout();
      message.success('Đăng xuất thành công');
    }
  };

  const userMenuItems: MenuProps['items'] = [
    { key: 'hoso', label: 'Hồ sơ', icon: <UserOutlined /> },
    { key: 'caidat', label: 'Cài đặt', icon: <SettingOutlined /> },
    { type: 'divider' },
    { key: 'dangxuat', label: 'Đăng xuất', icon: <LogoutOutlined /> },
  ];

  return (
    <Layout
      style={{
        minHeight: '100vh',
        height: '100vh',
        width: '100%',
        position: 'fixed',
        inset: 0,
        overflow: 'hidden',
      }}
    >
      <Sider
        className="ui-sider-shell"
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
          zIndex: 1000,
          boxShadow: '4px 0 24px rgba(15, 23, 42, 0.04)',
        }}
      >
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: token.colorBgElevated,
            borderBottom: `1px solid ${token.colorBorderSecondary}`,
          }}
        >
          <Typography.Title
            level={4}
            style={{
              margin: 0,
              color: token.colorPrimary,
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              padding: '0 16px',
            }}
          >
            {collapsed ? 'TS' : 'Task Scheduler'}
          </Typography.Title>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          onClick={({ key }) => setSelectedKey(key)}
          style={{ height: 'calc(100% - 128px)', borderRight: 0, padding: '8px 10px' }}
          items={menuItems}
        />
        <div
          style={{
            position: 'absolute',
            bottom: 0,
            left: 0,
            right: 0,
            padding: 16,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            borderTop: `1px solid ${token.colorBorderSecondary}`,
            background: token.colorBgElevated,
          }}
        >
          <Dropdown menu={{ items: userMenuItems, onClick: handleMenuClick }} placement="topRight" trigger={['click']}>
            <Button type="text" icon={<UserOutlined />}>
              {!collapsed ? 'Tài khoản' : ''}
            </Button>
          </Dropdown>
        </div>
      </Sider>
      <Layout>
        <Header
          className="ui-header-shell"
          style={{
            padding: '0 24px',
            marginLeft: collapsed ? 80 : 250,
            background: token.colorBgContainer,
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderBottom: `1px solid ${token.colorBorderSecondary}`,
            boxShadow: '0 1px 0 rgba(15, 23, 42, 0.04)',
          }}
        >
          <Space size="middle">
            <Button
              type="text"
              className="ui-icon-btn"
              aria-label={collapsed ? 'Mở menu' : 'Thu menu'}
              icon={collapsed ? <MenuUnfoldOutlined style={{ fontSize: 18 }} /> : <MenuFoldOutlined style={{ fontSize: 18 }} />}
              onClick={() => onCollapse(!collapsed)}
            />
            <Input
              placeholder="Search..."
              prefix={<SearchOutlined style={{ color: token.colorTextPlaceholder }} />}
              style={{ width: 200, borderRadius: 20 }}
              variant="borderless"
            />
          </Space>
          <Space size="large">
            <Badge count={0} size="small" style={{ boxShadow: 'none' }}>
              <Button type="text" shape="circle" className="ui-icon-btn" icon={<BellOutlined style={{ fontSize: 18 }} />} />
            </Badge>
            <Dropdown menu={{ items: userMenuItems, onClick: handleMenuClick }} trigger={['click']}>
              <Avatar
                size={36}
                icon={<UserOutlined />}
                style={{
                  backgroundColor: token.colorPrimary,
                  color: '#fff',
                  cursor: 'pointer',
                }}
              />
            </Dropdown>
          </Space>
        </Header>
        <Content
          className="ui-content-shell"
          style={{
            marginLeft: collapsed ? 80 : 250,
            height: 'calc(100vh - 64px)',
            minHeight: 'calc(100vh - 64px)',
            minWidth: 900,
            background: token.colorBgLayout,
            overflow: 'auto',
          }}
        >
          <div
            key={selectedKey}
            className="ui-view-crossfade"
            style={{ height: '100%', minHeight: '100%', boxSizing: 'border-box' }}
          >
            {renderMainContent()}
          </div>
        </Content>
      </Layout>

      <TaskModal
        open={taskModalOpen}
        onOk={onTaskModalOk}
        onCancel={onTaskModalCancel}
        mode={taskModalMode}
        initialValues={taskModalInitialValues}
      />
      <TaskListModal
        open={taskListModalOpen}
        onOk={onTaskListModalOk}
        onCancel={onTaskListModalCancel}
        mode={taskListModalMode}
        initialValues={taskListModalInitialValues}
      />
      <TaskDetailModal
        open={detailModalOpen}
        taskId={detailTaskId}
        userId={currentUserId}
        onCancel={onDetailModalClose}
      />
      <TaskListDetailModal
        open={taskListDetailOpen}
        taskListId={taskListDetailId}
        onCancel={onTaskListDetailClose}
      />
      <Modal
        title="Thành viên task list"
        open={memberModalOpen}
        onCancel={onMemberModalClose}
        footer={null}
        width={720}
        destroyOnClose
      >
        {memberModalTaskListId ? (
          <MemberManagement taskListId={memberModalTaskListId} currentUserId={currentUserId} />
        ) : null}
      </Modal>
    </Layout>
  );
};

export default DashboardLayout;
