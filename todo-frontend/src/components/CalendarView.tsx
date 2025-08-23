import React, { useState } from 'react';
import { Calendar, Badge, Button, List, Tag, Space } from 'antd';
import { PlusOutlined, CheckCircleOutlined, ClockCircleOutlined, LeftOutlined, RightOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import 'dayjs/locale/vi';
import viVN from 'antd/es/calendar/locale/vi_VN';
import TaskModal from './TaskList/TaskListModal';

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
    return tasks.filter(task => task.dueDate === date.format('YYYY-MM-DD'));
  };

  const handleModalOk = async (values: any) => {
    try {
      if (mode === 'create' && onAddTask) {
        await onAddTask(values);
      } else if (mode === 'edit' && selectedTask && onUpdateTask) {
        await onUpdateTask({ ...selectedTask, ...values });
      }
      setIsModalOpen(false);
    } catch (error) {
      console.error('Error saving task:', error);
    }
  };

  const dateCellRender = (date: Dayjs) => {
    const tasksForDate = getTasksForDate(date);
    
    return (
      <div>
        {tasksForDate.map(task => (
          <div 
            key={task.id}
            style={{ margin: '2px 0', cursor: 'pointer' }}
            onClick={(e) => {
              e.stopPropagation();
              setSelectedTask(task);
              setMode('edit');
              setIsModalOpen(true);
            }}
          >
            <Badge
              status={task.isCompleted ? 'success' : 'processing'}
              text={
                <span style={{
                  fontSize: '12px',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  display: 'block'
                }}>
                  {task.title}
                </span>
              }
            />
          </div>
        ))}
      </div>
    );
  };

  return (
    <div style={{ padding: '20px' }}>
      <div style={{ 
        marginBottom: 16, 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center' 
      }}>
        <Space>
          <Button 
            icon={<LeftOutlined />}
            onClick={() => {
              const newDate = selectedDate.subtract(1, 'month');
              setSelectedDate(newDate);
            }}
          />
          <Button onClick={() => setSelectedDate(dayjs())}>Hôm nay</Button>
          <Button 
            icon={<RightOutlined />}
            onClick={() => {
              const newDate = selectedDate.add(1, 'month');
              setSelectedDate(newDate);
            }}
          />
        </Space>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => {
            setMode('create');
            setSelectedTask({
              id: 0,
              title: '',
              description: '',
              dueDate: selectedDate.format('YYYY-MM-DD'),
              priority: 'MEDIUM',
              isCompleted: false,
            });
            setIsModalOpen(true);
          }}
        >
          Thêm công việc
        </Button>
      </div>

      <div style={{ display: 'flex', gap: '20px' }}>
        <div style={{ flex: 2 }}>
          <Calendar
            value={selectedDate}
            onPanelChange={(date) => setSelectedDate(date)}
            dateCellRender={dateCellRender}
            locale={viVN}
            style={{ 
              background: '#fff', 
              padding: '16px', 
              borderRadius: '8px', 
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)' 
            }}
          />
        </div>

        <div style={{ flex: 1 }}>
          <div style={{ 
            background: '#fff', 
            padding: '16px', 
            borderRadius: '8px', 
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            height: '100%'
          }}>
            <h3>{selectedDate.format('DD/MM/YYYY')}</h3>
            <List
              itemLayout="horizontal"
              dataSource={getTasksForDate(selectedDate)}
              renderItem={(task) => (
                <List.Item
                  onClick={() => onShowDetail(task)}
                  style={{ cursor: 'pointer', padding: '8px 0' }}
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
        initialValues={selectedTask || { dueDate: selectedDate.format('YYYY-MM-DD') }}
        mode={mode}
      />
    </div>
  );
};

export default CalendarView;
