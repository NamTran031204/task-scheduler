export interface TaskHistory {
  id: number;
  taskId: number;
  action: 'CREATE' | 'UPDATE' | 'DELETE' | 'STATUS_CHANGE' | 'ASSIGNEE_CHANGE';
  field?: string;
  oldValue?: string | number | boolean | null;
  newValue?: string | number | boolean | null;
  performedBy: {
    id: number;
    username: string;
    avatarUrl?: string;
  };
  performedAt: string;
}
