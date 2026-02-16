import React from 'react';
import { List } from 'antd';
import type { TaskList as TaskListType } from '../../types/task';

interface TaskListProps {
  taskLists: TaskListType[];
  selectedListId: number | null;
  onSelectList: (listId: number | null) => void;
}

const TaskList: React.FC<TaskListProps> = ({ taskLists, selectedListId, onSelectList }) => {
  return (
    <List
      itemLayout="horizontal"
      dataSource={[
        { id: null, name: 'All Tasks', color: '#1890ff' },
        ...taskLists.map(list => ({
          ...list,
          color: list.color || '#1890ff' 
        }))
      ]}
      renderItem={(item) => (
        <List.Item
          onClick={() => onSelectList(item.id)}
          style={{
            cursor: 'pointer',
            padding: '8px 16px',
            backgroundColor: selectedListId === item.id ? (item.color ? `${item.color}1a` : '#f0f7ff') : 'transparent',
            borderLeft: `3px solid ${item.color || '#1890ff'}`,
            borderRadius: '4px',
            marginBottom: '4px',
            transition: 'all 0.3s',
          }}
        >
          <List.Item.Meta
            title={
              <div style={{ 
                display: 'flex', 
                alignItems: 'center',
                color: selectedListId === item.id ? '#1890ff' : 'inherit'
              }}>
                <span 
                  style={{
                    display: 'inline-block',
                    width: '12px',
                    height: '12px',
                    borderRadius: '50%',
                    backgroundColor: item.color || '#ccc',
                    marginRight: '8px',
                  }}
                />
                {item.name}
              </div>
            }
          />
        </List.Item>
      )}
    />
  );
};

export default TaskList;
