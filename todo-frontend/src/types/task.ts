export interface Task {
  id: number;
  title: string;
  name?: string;
  description?: string;
  dueDate: string;
  isCompleted: boolean;
  taskListId?: number;
  task_list_id?: number;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  recurrence?: string;
}

export interface TaskList {
  id: number;
  name: string;
  color: string;
}
