import React from 'react';
import { Menu, Button, Spin } from 'antd';
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
}

const TaskListsSidebar: React.FC<TaskListsSidebarProps> = ({ taskLists, selectedListId, onSelectList, loading, onAddTaskList, onEditTaskList, onDeleteTaskList }) => {
  const items: MenuProps['items'] = taskLists.map((list) => ({
    key: list.id.toString(),
    label: (
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
        <span>{list.name}</span>
        <span style={{ opacity: 0.7, marginLeft: 8, display: 'flex', gap: 4 }}>
          <Button size="small" type="link" onClick={e => { e.stopPropagation(); onEditTaskList(list); }}>Sửa</Button>
          <Button size="small" type="link" onClick={e => { e.stopPropagation(); if (window.memberModalOpen) window.memberModalOpen(list.id); }}>Thành viên</Button>
          <Button size="small" type="link" danger onClick={e => { e.stopPropagation(); onDeleteTaskList(list.id); }}>Xóa</Button>
        </span>
      </div>
    ),
    style: { color: list.color || '#3b82f6', fontWeight: 500 },
  }));

  return (
    <div style={{ width: 240, background: '#f6f8fa', borderRight: '1px solid #eee', padding: 16 }}>
      <div style={{ marginBottom: 16, fontWeight: 600, fontSize: 18 }}>Danh sách Task List</div>
      <Button type="primary" block style={{ marginBottom: 16 }} onClick={onAddTaskList}>
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
