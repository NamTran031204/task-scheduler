import api from './api';

export const getTasksByTaskList = async (userId: number, taskListId: number) => {
  const res = await api.get(`/task/user/${userId}/task-list/${taskListId}?record=100&page=0`);
  return res.data;
};

export const createTask = async (userId: number, data: any) => {
  const res = await api.post(`/task/user/${userId}/create`, data);
  return res.data;
};

export const updateTask = async (userId: number, taskId: number, data: any) => {
  const res = await api.put(`/task/user/${userId}/update/${taskId}`, data);
  return res.data;
};

export const deleteTask = async (userId: number, taskId: number) => {
  const res = await api.delete(`/task/user/${userId}/delete/${taskId}`);
  return res.data;
};

export const completeTask = async (userId: number, taskId: number) => {
  const res = await api.put(`/task/user/${userId}/complete/${taskId}`);
  return res.data;
};

export const undoCompleteTask = async (userId: number, taskId: number) => {
  const res = await api.put(`/task/user/${userId}/undo_complete/${taskId}`);
  return res.data;
};

export const getTaskById = async (userId: number, taskId: number) => {
  const res = await api.get(`/task/user/${userId}/${taskId}`);
  return res.data;
};

// Upload file đính kèm cho task
export const uploadTaskAttachment = async (taskId: number, file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  const res = await api.post(`/task/${taskId}/attachment/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
};

// Lấy danh sách file đính kèm của task
export const getTaskAttachments = async (taskId: number) => {
  const res = await api.get(`/task/${taskId}/attachments`);
  return res.data;
};

// Xóa file đính kèm khỏi task
export const deleteTaskAttachment = async (taskId: number, attachmentId: number) => {
  const res = await api.delete(`/task/${taskId}/attachment/${attachmentId}`);
  return res.data;
};

// Tạo nhắc nhở cho task
export const createTaskReminder = async (taskId: number, data: any) => {
  const res = await api.post(`/task/${taskId}/reminder/create`, data);
  return res.data;
};

// Lấy danh sách nhắc nhở của task
export const getTaskReminders = async (taskId: number) => {
  const res = await api.get(`/task/${taskId}/reminders`);
  return res.data;
};

// Sửa nhắc nhở
export const updateTaskReminder = async (reminderId: number, data: any) => {
  const res = await api.put(`/reminder/update/${reminderId}`, data);
  return res.data;
};

// Xóa nhắc nhở
export const deleteTaskReminder = async (reminderId: number) => {
  const res = await api.delete(`/reminder/delete/${reminderId}`);
  return res.data;
};

// Tạo recurrence cho task
export const createTaskRecurrence = async (taskId: number, data: any) => {
  const res = await api.post(`/task/${taskId}/recurrence/create`, data);
  return res.data;
};

// Lấy recurrence của task
export const getTaskRecurrence = async (taskId: number) => {
  const res = await api.get(`/task/${taskId}/recurrence`);
  return res.data;
};
