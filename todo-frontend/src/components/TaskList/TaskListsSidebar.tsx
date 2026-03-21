import React from 'react';
import { Menu, Button, Spin, Typography, theme } from 'antd';
import type { MenuProps } from 'antd';

declare global {
  interface Window {
    memberModalOpen?: (listId: number) => void;
  }
}

interface TaskListsSidebarProps {
  taskLists: any[];
  selectedListId: number | null;
  onSelectList: (id: number) => void;
  loading: boolean;
  onAddTaskList: () => void;
  onEditTaskList: (taskList: any) => void;
  onDeleteTaskList: (taskListId: number) => void;
  onShowListDetail?: (taskListId: number) => void;
}

const TaskListsSidebar: React.FC<TaskListsSidebarProps> = ({
  taskLists,
  selectedListId,
  onSelectList,
  loading,
  onAddTaskList,
  onEditTaskList,
  onDeleteTaskList,
  onShowListDetail,
}) => {
  const { token } = theme.useToken();
  const items: MenuProps['items'] = taskLists.map((list) => ({
    key: list.id.toString(),
    label: (
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
        <span>{list.name}</span>
        <span style={{ opacity: 0.7, marginLeft: 8, display: 'flex', gap: 4 }}>
          <Button size="small" type="link" onClick={e => { e.stopPropagation(); onEditTaskList(list); }}>Sửa</Button>
          {onShowListDetail ? (
            <Button size="small" type="link" onClick={e => { e.stopPropagation(); onShowListDetail(list.id); }}>Chi tiết</Button>
          ) : null}
          <Button size="small" type="link" onClick={e => { e.stopPropagation(); if (window.memberModalOpen) window.memberModalOpen(list.id); }}>Thành viên</Button>
          <Button size="small" type="link" danger onClick={e => { e.stopPropagation(); onDeleteTaskList(list.id); }}>Xóa</Button>
        </span>
      </div>
    ),
    style: { color: list.color || '#3b82f6', fontWeight: 500 },
  }));

  return (
    <div
      className="ui-sidebar-panel"
      style={{
        width: 260,
        background: token.colorBgContainer,
        borderRight: `1px solid ${token.colorBorderSecondary}`,
        padding: 16,
        boxShadow: '2px 0 16px rgba(15, 23, 42, 0.03)',
      }}
    >
      <Typography.Title level={5} style={{ marginTop: 0, marginBottom: 16 }}>
        Danh sách Task List
      </Typography.Title>
      <Button type="primary" block className="ui-icon-btn" style={{ marginBottom: 16 }} onClick={onAddTaskList}>
        + Tạo Task List
      </Button>
      <Spin spinning={loading}>
        <Menu
          mode="inline"
          selectedKeys={selectedListId ? [selectedListId.toString()] : []}
          onClick={({ key }) => onSelectList(Number(key))}
          items={items}
        />
      </Spin>
    </div>
  );
};

export default TaskListsSidebar;
