import React from 'react';
import { List, Checkbox, Tag, Spin, Empty, Button } from 'antd';
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
      <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
        <input placeholder="Tìm kiếm..." value={keyword} onChange={e => setKeyword(e.target.value)} style={{ width: 180, padding: 4 }} />
        <select value={status} onChange={e => setStatus(e.target.value as any)} style={{ padding: 4 }}>
          <option value="all">Tất cả trạng thái</option>
          <option value="completed">Đã hoàn thành</option>
          <option value="incomplete">Chưa hoàn thành</option>
        </select>
        <select value={priority} onChange={e => setPriority(e.target.value as any)} style={{ padding: 4 }}>
          <option value="all">Tất cả ưu tiên</option>
          <option value="LOW">Thấp</option>
          <option value="MEDIUM">Trung bình</option>
          <option value="HIGH">Cao</option>
          <option value="URGENT">Khẩn cấp</option>
        </select>
        <input type="date" value={dueDate ? dueDate.format('YYYY-MM-DD') : ''} onChange={e => setDueDate(e.target.value ? dayjs(e.target.value) : null)} style={{ padding: 4 }} />
        <Button onClick={() => { setKeyword(''); setStatus('all'); setPriority('all'); setDueDate(null); }}>Xóa lọc</Button>
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div style={{ fontWeight: 600, fontSize: 20 }}>Công việc</div>
        <Button type="primary" disabled={!taskListId} onClick={onAddTask}>+ Thêm Task</Button>
      </div>
      <Spin spinning={loading}>
        {filteredTasks.length === 0 ? (
          <Empty description="Không có công việc nào" />
        ) : (
          <List
            itemLayout="horizontal"
            dataSource={filteredTasks}
            renderItem={task => (
              <List.Item actions={[
  <Button type="link" onClick={() => onShowDetail(task.id)}>Chi tiết</Button>,
  <Button type="link" danger onClick={() => onDeleteTask(task.id)}>Xóa</Button>,
  <Button type="link" onClick={() => onEditTask(task)}>Sửa</Button>
]}> 
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
                    <span title={task.description} style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', display: 'inline-block', maxWidth: 220 }}>
                      {task.description}
                    </span>
                  }
                />
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 6 }}>
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
              </List.Item>
            )}
          />
        )}
      </Spin>
    </div>
  );
};

export default TaskListView;
