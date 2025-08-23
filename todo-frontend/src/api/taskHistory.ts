import api from '../api/api';
import type { TaskHistory } from '../types/taskHistory';

export const getTaskHistory = async (taskId: number): Promise<TaskHistory[]> => {
  const response = await api.get(`/tasks/${taskId}/history`);
  return response.data;
};
