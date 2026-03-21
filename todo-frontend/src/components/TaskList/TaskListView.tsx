import React from 'react';
import { List, Checkbox, Tag, Spin, Empty, Button, Input, Select, DatePicker, Space } from 'antd';
import dayjs from 'dayjs';

interface TaskListViewProps {
  tasks: any[];
  loading: boolean;
  taskListId: number | null;
  refreshTasks: () => void;
  onAddTask: () => void;
  onShowDetail: (taskId: number) => void;
  onDeleteTask: (taskId: number) => void;
  onEditTask: (task: any) => void;
  onToggleComplete: (task: any) => void;
}

const TaskListView: React.FC<TaskListViewProps> = ({ tasks, loading, taskListId, onAddTask, onShowDetail, onDeleteTask, onEditTask, onToggleComplete }) => {
  const [keyword, setKeyword] = React.useState('');
  const [status, setStatus] = React.useState<'all' | 'completed' | 'incomplete'>('all');
  const [priority, setPriority] = React.useState<'all' | 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'>('all');
  const [dueDate, setDueDate] = React.useState<any>(null);

  const filteredTasks = tasks.filter(task => {
    const matchKeyword = keyword === '' || task.title.toLowerCase().includes(keyword.toLowerCase()) || (task.description || '').toLowerCase().includes(keyword.toLowerCase());
    const matchStatus = status === 'all' || (status === 'completed' ? task.isCompleted : !task.isCompleted);
    const matchPriority = priority === 'all' || task.priority === priority;
    const matchDueDate = !dueDate || (task.dueDate && dayjs(task.dueDate).format('YYYY-MM-DD') === dueDate.format('YYYY-MM-DD'));
    return matchKeyword && matchStatus && matchPriority && matchDueDate;
  });

  return (
    <div style={{ minHeight: 500 }}>
      <Space wrap className="ui-filter-bar" size="middle" style={{ marginBottom: 16, width: '100%' }}>
        <Input
          allowClear
          placeholder="Tìm kiếm..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          style={{ width: 200 }}
        />
        <Select
          value={status}
          onChange={(v) => setStatus(v)}
          style={{ width: 180 }}
          options={[
            { value: 'all', label: 'Tất cả trạng thái' },
            { value: 'completed', label: 'Đã hoàn thành' },
            { value: 'incomplete', label: 'Chưa hoàn thành' },
          ]}
        />
        <Select
          value={priority}
          onChange={(v) => setPriority(v)}
          style={{ width: 160 }}
          options={[
            { value: 'all', label: 'Tất cả ưu tiên' },
            { value: 'LOW', label: 'Thấp' },
            { value: 'MEDIUM', label: 'Trung bình' },
            { value: 'HIGH', label: 'Cao' },
            { value: 'URGENT', label: 'Khẩn cấp' },
          ]}
        />
        <DatePicker
          value={dueDate}
          onChange={(d) => setDueDate(d)}
          placeholder="Lọc theo hạn"
          style={{ width: 160 }}
        />
        <Button onClick={() => { setKeyword(''); setStatus('all'); setPriority('all'); setDueDate(null); }}>
          Xóa lọc
        </Button>
      </Space>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div style={{ fontWeight: 600, fontSize: 20 }}>Công việc</div>
        <Button type="primary" className="ui-icon-btn" disabled={!taskListId} onClick={onAddTask}>
          + Thêm Task
        </Button>
      </div>
      <Spin spinning={loading}>
        {filteredTasks.length === 0 ? (
          <Empty description="Không có công việc nào" />
        ) : (
          <List
            style={{overflowX: 'auto',}}
            itemLayout="horizontal"
            dataSource={filteredTasks}
            renderItem={(task, index) => (
              <List.Item
                className="ui-task-row ui-stagger-item"
                style={{ overflowX: 'visible', animationDelay: `${Math.min(index, 20) * 42}ms`, position: 'relative' }}
              >
                <div style={{ display: 'flex', width: '100%', alignItems: 'center', gap: 16, paddingRight: 250 }}>
                  <List.Item.Meta
                    title={
                      <span style={{ textDecoration: task.isCompleted ? 'line-through' : 'none', fontWeight: 500 }}>
                        {task.title}
                        <Tag color={task.isCompleted ? 'green' : 'gold'} style={{ marginLeft: 8 }}>
                          {task.isCompleted ? 'Hoàn thành' : 'Chưa xong'}
                        </Tag>
                      </span>
                    }
                    description={
                      <span title={task.description} style={{ overflow: 'hidden', textOverflow: 'ellipsis', display: 'inline-block' }}>
                        {task.description}
                      </span>
                    }
                  />
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 6, flexShrink: 0 }}>
                    <Checkbox checked={task.isCompleted} onChange={() => onToggleComplete(task)} style={{ marginBottom: 2 }} />
                    <Tag color={task.priority === 'HIGH' ? 'red' : task.priority === 'MEDIUM' ? 'orange' : task.priority === 'URGENT' ? 'volcano' : 'blue'}>
                      {task.priority}
                    </Tag>
                    {task.dueDate && (
                      <Tag color={dayjs(task.dueDate).isBefore(dayjs(), 'day') && !task.isCompleted ? 'red' : 'default'} style={{ marginTop: 2 }}>
                        Hạn: {dayjs(task.dueDate).format('DD/MM/YYYY')}
                      </Tag>
                    )}
                  </div>
                </div>
                <div style={{ position: 'absolute', right: 0, top: 0, height: '100%', display: 'flex', alignItems: 'center', gap: 8, background: 'white', paddingLeft: 16 }}>
                  <Button type="link" onClick={() => onShowDetail(task.id)}>Chi tiết</Button>
                  <Button type="link" danger onClick={() => onDeleteTask(task.id)}>Xóa</Button>
                  <Button type="link" onClick={() => onEditTask(task)}>Sửa</Button>
                </div>
              </List.Item>
            )}
          />
        )}
      </Spin>
    </div>
  );
};

export default TaskListView;
