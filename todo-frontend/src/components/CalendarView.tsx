import React, { useState } from 'react';
import { Calendar, Button, List, Tag, Space, message } from 'antd';
import { PlusOutlined, CheckCircleOutlined, ClockCircleOutlined, LeftOutlined, RightOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import 'dayjs/locale/vi';
import viVN from 'antd/es/calendar/locale/vi_VN';
import TaskModal from './TaskList/TaskModal';

type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Task {
  id: number;
  title: string;
  dueDate: string;
  priority: Priority;
  isCompleted: boolean;
  startTime?: string;
  endTime?: string;
  description?: string;
}

interface CalendarViewProps {
  tasks?: Task[];
  onAddTask?: (task: Omit<Task, 'id' | 'isCompleted'>) => Promise<void>;
  onUpdateTask?: (task: Task) => Promise<void>;
  onShowDetail?: (task: Task) => void;
}

const CalendarView: React.FC<CalendarViewProps> = ({
  tasks = [],
  onAddTask = async () => {},
  onUpdateTask = async () => {},
  onShowDetail = () => {},
}) => {
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs());
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [mode, setMode] = useState<'create' | 'edit'>('create');

  const getPriorityColor = (priority: Priority) => {
    switch (priority) {
      case 'LOW': return 'blue';
      case 'MEDIUM': return 'green';
      case 'HIGH': return 'orange';
      case 'URGENT': return 'red';
      default: return 'gray';
    }
  };

  const getTasksForDate = (date: Dayjs) => {
    const target = date.format('YYYY-MM-DD');
    return tasks.filter((task) => {
      const d = task.dueDate;
      if (d == null || d === '') return false;
      const parsed = dayjs(d);
      if (!parsed.isValid()) return false;
      return parsed.format('YYYY-MM-DD') === target;
    });
  };

  const openAddModalForDate = (date: Dayjs) => {
    setSelectedDate(date);
    setMode('create');
    setSelectedTask({
      id: 0,
      title: '',
      description: '',
      dueDate: date.format('YYYY-MM-DD'),
      priority: 'MEDIUM',
      isCompleted: false,
    });
    setIsModalOpen(true);
  };

  const handleModalOk = async (values: any) => {
    try {
      const dueDate = values.due_date
        ? dayjs(values.due_date).format('YYYY-MM-DD')
        : selectedTask?.dueDate
          ? dayjs(selectedTask.dueDate).format('YYYY-MM-DD')
          : undefined;
      if (mode === 'create' && onAddTask) {
        await onAddTask({
          title: values.title,
          description: values.description,
          priority: values.priority ?? 'MEDIUM',
          dueDate: dueDate ?? selectedDate.format('YYYY-MM-DD'),
        });
      } else if (mode === 'edit' && selectedTask && onUpdateTask) {
        await onUpdateTask({
          ...selectedTask,
          title: values.title,
          description: values.description,
          priority: values.priority ?? selectedTask.priority,
          dueDate: dueDate ?? selectedTask.dueDate,
        });
      }
      setIsModalOpen(false);
    } catch (error: any) {
      console.error('Error saving task:', error);
      const msg = error?.message || error?.response?.data?.message || 'Không lưu được công việc';
      message.error(typeof msg === 'string' ? msg : 'Không lưu được công việc');
    }
  };

  const getPriorityBg = (priority: Priority) => {
    switch (priority) {
      case 'LOW': return 'rgba(22, 119, 255, 0.12)';
      case 'MEDIUM': return 'rgba(82, 196, 26, 0.14)';
      case 'HIGH': return 'rgba(250, 140, 22, 0.16)';
      case 'URGENT': return 'rgba(255, 77, 79, 0.18)';
      default: return 'rgba(0, 0, 0, 0.06)';
    }
  };

  const getPriorityBorder = (priority: Priority) => {
    switch (priority) {
      case 'LOW': return '#1677ff';
      case 'MEDIUM': return '#52c41a';
      case 'HIGH': return '#fa8c16';
      case 'URGENT': return '#ff4d4f';
      default: return '#d9d9d9';
    }
  };

  const dateCellRender = (date: Dayjs) => {
    const tasksForDate = getTasksForDate(date);
    return (
      <div style={{ minHeight: 72 }}>
        {tasksForDate.map((task) => (
          <div
            key={task.id}
            role="button"
            tabIndex={0}
            className="ui-cal-task-chip"
            style={{
              margin: '2px 0',
              padding: '4px 8px',
              borderRadius: 6,
              fontSize: 12,
              cursor: 'pointer',
              background: getPriorityBg(task.priority),
              borderLeft: `3px solid ${getPriorityBorder(task.priority)}`,
              textDecoration: task.isCompleted ? 'line-through' : 'none',
              opacity: task.isCompleted ? 0.75 : 1,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
            onClick={(e) => {
              e.stopPropagation();
              setSelectedTask(task);
              setMode('edit');
              setIsModalOpen(true);
            }}
          >
            {task.title || '(Không có tiêu đề)'}
          </div>
        ))}
        <div
          role="button"
          tabIndex={0}
          style={{
            marginTop: 2,
            padding: '2px 6px',
            fontSize: 11,
            color: '#1677ff',
            cursor: 'pointer',
            opacity: 0.8,
          }}
          onClick={(e) => {
            e.stopPropagation();
            openAddModalForDate(date);
          }}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') {
              e.preventDefault();
              openAddModalForDate(date);
            }
          }}
        >
          + Thêm
        </div>
      </div>
    );
  };


  return (
    <div
      style={{
        padding: '20px',
        height: '100%',
        boxSizing: 'border-box',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <div style={{ 
        marginBottom: 16, 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center' 
      }}>
        <Space>
          <Button
            className="ui-icon-btn"
            icon={<LeftOutlined />}
            onClick={() => {
              const newDate = selectedDate.subtract(1, 'month');
              setSelectedDate(newDate);
            }}
          />
          <Button className="ui-icon-btn" onClick={() => setSelectedDate(dayjs())}>
            Hôm nay
          </Button>
          <Button
            className="ui-icon-btn"
            icon={<RightOutlined />}
            onClick={() => {
              const newDate = selectedDate.add(1, 'month');
              setSelectedDate(newDate);
            }}
          />
        </Space>
        <Button
          type="primary"
          className="ui-icon-btn"
          icon={<PlusOutlined />}
          onClick={() => openAddModalForDate(selectedDate)}
        >
          Thêm công việc
        </Button>
      </div>

      <div style={{ display: 'flex', gap: '20px', flex: 1, minHeight: 0 }}>
        <div style={{ flex: 2, minHeight: 0, overflow: 'auto' }}>
          <Calendar
            className="ui-elevated-panel"
            value={selectedDate}
            onPanelChange={(date) => setSelectedDate(date)}
            onSelect={(date) => setSelectedDate(date)}
            cellRender={dateCellRender}
            locale={viVN}
            style={{
              background: '#fff',
              padding: 16,
              height: '100%',
            }}
          />
        </div>

        <div style={{ flex: 1, minHeight: 0, overflow: 'auto' }}>
          <div className="ui-elevated-panel" style={{ background: '#fff', padding: 16, height: '100%' }}>
            <h3>{selectedDate.format('DD/MM/YYYY')}</h3>
            <List
              itemLayout="horizontal"
              dataSource={getTasksForDate(selectedDate)}
              renderItem={(task, index) => (
                <List.Item
                  className="ui-stagger-item"
                  style={{ cursor: 'pointer', padding: '8px 0', animationDelay: `${Math.min(index, 12) * 40}ms` }}
                  onClick={() => onShowDetail(task)}
                >
                  <List.Item.Meta
                    avatar={
                      task.isCompleted ? (
                        <CheckCircleOutlined style={{ color: 'green' }} />
                      ) : (
                        <ClockCircleOutlined style={{ color: 'orange' }} />
                      )
                    }
                    title={
                      <Space>
                        <span>{task.title}</span>
                        <Tag color={getPriorityColor(task.priority)}>
                          {task.priority}
                        </Tag>
                      </Space>
                    }
                    description={
                      task.startTime && task.endTime 
                        ? `${task.startTime} - ${task.endTime}` 
                        : 'Cả ngày'
                    }
                  />
                </List.Item>
              )}
            />
          </div>
        </div>
      </div>

      <TaskModal
        open={isModalOpen}
        onOk={handleModalOk}
        onCancel={() => setIsModalOpen(false)}
        initialValues={
          selectedTask && mode === 'edit'
            ? {
                ...selectedTask,
                due_date: selectedTask.dueDate ? dayjs(selectedTask.dueDate) : undefined,
              }
            : { due_date: selectedDate, recurrence: 'NONE' }
        }
        mode={mode}
      />
    </div>
  );
};

export default CalendarView;
