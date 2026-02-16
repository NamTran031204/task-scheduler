import axios from 'axios';
import { URL } from '../constants/routeURL';

export const getTaskListsByUser = async (userId: number) => {
  const res = await axios.get(`${URL}/task-list/user/${userId}?record=100&page=0`);
  return res.data;
};

export const createTaskList = async (userId: number, data: any) => {
  const res = await axios.post(`${URL}/task-list/create/${userId}`, data);
  return res.data;
};

export const updateTaskList = async (taskListId: number, userId: number, data: any) => {
  const res = await axios.put(`${URL}/task-list/update/${taskListId}/user/${userId}`, data);
  return res.data;
};

export const deleteTaskList = async (taskListId: number, userId: number) => {
  const res = await axios.delete(`${URL}/task-list/delete/${taskListId}/user/${userId}`);
  return res.data;
};

export const getTaskListById = async (taskListId: number) => {
  const res = await axios.get(`${URL}/task-list/${taskListId}`);
  return res.data;
};

// Chia sẻ task list cho user khác (theo userId)
export const shareTaskList = async (taskListId: number, userId: number) => {
  const res = await axios.put(`${URL}/task-list/share/${taskListId}/user/${userId}`);
  return res.data;
};

// Tham gia task list bằng code
export const joinTaskList = async (code: string, userId: number) => {
  const res = await axios.post(`${URL}/task-list/join/${code}/user/${userId}`);
  return res.data;
};

// Rời khỏi task list
export const leaveTaskList = async (taskListId: number, userId: number) => {
  const res = await axios.put(`${URL}/task-list/user/${userId}/leave/taskList/${taskListId}`);
  return res.data;
};

// Lấy danh sách thành viên của task list
export const getTaskListMembers = async (taskListId: number) => {
  const res = await axios.get(`${URL}/task-list/get-member/taskList/${taskListId}`);
  return res.data;
};

// Xóa thành viên khỏi task list
export const removeTaskListMember = async (
  taskListId: number,
  userId: number,
  byUserId: number
) => {
  const res = await axios.delete(
    `${URL}/task-list/delete/user/${userId}/inTaskList/${taskListId}/byUser/${byUserId}`
  );
  return res.data;
};

// Thay đổi vai trò thành viên
export const updateTaskListMemberRole = async (
  taskListId: number,
  userId: number,
  byUserId: number,
  role: 'HOST' | 'MEMBER'
) => {
  const formData = new FormData();
  formData.append('role', role);
  const res = await axios.put(
    `${URL}/task-list/authority/user/${userId}/inTaskList/${taskListId}/byUser/${byUserId}/role`,
    formData
  );
  return res.data;
};
