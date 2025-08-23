import React, { useEffect, useState } from 'react';
import { List, Avatar, Tag, Typography, Spin, Empty, Card } from 'antd';
import { ClockCircleOutlined, UserOutlined } from '@ant-design/icons';
import { getTaskHistory } from '../../api/taskHistory';
import type { TaskHistory } from '../../types/taskHistory';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

interface TaskHistoryProps {
  taskId: number;
}

const TaskHistory: React.FC<TaskHistoryProps> = ({ taskId }) => {
  const [loading, setLoading] = useState(true);
  const [history, setHistory] = useState<TaskHistory[]>([]);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const data = await getTaskHistory(taskId);
        setHistory(data);
      } catch (error) {
        console.error('Error fetching task history:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [taskId]);

  const getActionText = (item: TaskHistory) => {
    switch (item.action) {
      case 'CREATE':
        return 'created this task';
      case 'UPDATE':
        return `updated ${item.field}`;
      case 'DELETE':
        return 'deleted this task';
      case 'STATUS_CHANGE':
        return 'changed status';
      case 'ASSIGNEE_CHANGE':
        return 'changed assignee';
      default:
        return 'performed an action';
    }
  };

  const renderValue = (value: any) => {
    if (value === null || value === undefined) return 'None';
    if (typeof value === 'boolean') return value ? 'Yes' : 'No';
    return String(value);
  };

  if (loading) return <Spin />;
  if (history.length === 0) return <Empty description="No history available" />;

  return (
    <Card 
      size="small" 
      style={{ maxHeight: 400, overflowY: 'auto' }}
      bodyStyle={{ padding: '16px 0' }}
    >
      <List
        itemLayout="horizontal"
        dataSource={history}
        split={false}
        renderItem={(item) => (
          <List.Item 
            style={{
              padding: '12px 16px',
              borderBottom: '1px solid #f0f0f0',
              transition: 'background-color 0.3s',
              cursor: 'pointer',
            }}
            className="task-history-item"
          >
            <List.Item.Meta
              avatar={
                <Avatar
                  src={item.performedBy.avatarUrl}
                  icon={<UserOutlined />}
                />
              }
              title={
                <div style={{ marginBottom: 4 }}>
                  <Typography.Text strong>{item.performedBy.username}</Typography.Text>
                  {' '}
                  {getActionText(item)}
                  {item.field && (
                    <Tag color="blue" style={{ marginLeft: 8 }}>
                      {item.field}
                    </Tag>
                  )}
                </div>
              }
              description={
                <div>
                  {(item.oldValue !== undefined || item.newValue !== undefined) && (
                    <div 
                      style={{
                        backgroundColor: '#f6ffed',
                        padding: '4px 8px',
                        borderRadius: 4,
                        margin: '4px 0',
                        display: 'inline-block',
                        fontSize: 13,
                      }}
                    >
                      {item.oldValue !== undefined && (
                        <span>
                          <Typography.Text type="secondary">From: </Typography.Text>
                          <Typography.Text delete>{renderValue(item.oldValue)}</Typography.Text>
                          {' '}
                        </span>
                      )}
                      {item.newValue !== undefined && (
                        <span>
                          <Typography.Text type="secondary">To: </Typography.Text>
                          <Typography.Text>{renderValue(item.newValue)}</Typography.Text>
                        </span>
                      )}
                    </div>
                  )}
                  <div style={{ 
                    color: '#8c8c8c',
                    fontSize: 12,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 4,
                    marginTop: 4,
                  }}>
                    <ClockCircleOutlined />
                    {dayjs(item.performedAt).fromNow()}
                  </div>
                </div>
              }
            />
          </List.Item>
        )}
      />
    </Card>
  );
};

export default TaskHistory;
