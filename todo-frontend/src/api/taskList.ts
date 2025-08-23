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

export const updateTaskList = async (taskListId: number, data: any) => {
  const res = await axios.put(`${URL}/task-list/update/${taskListId}`, data);
  return res.data;
};

export const deleteTaskList = async (taskListId: number) => {
  const res = await axios.delete(`${URL}/task-list/delete/${taskListId}`);
  return res.data;
};

export const getTaskListById = async (taskListId: number) => {
  const res = await axios.get(`${URL}/task-list/${taskListId}`);
  return res.data;
};

// Chia sẻ Task List cho user khác
export const shareTaskList = async (taskListId: number, email: string) => {
  const res = await axios.post(`${URL}/task-list/share`, { taskListId, email });
  return res.data;
};

// Tham gia Task List bằng code
export const joinTaskList = async (code: string) => {
  const res = await axios.post(`${URL}/task-list/join`, { code });
  return res.data;
};

// Rời khỏi Task List
export const leaveTaskList = async (taskListId: number) => {
  const res = await axios.post(`${URL}/task-list/leave`, { taskListId });
  return res.data;
};

// Lấy danh sách thành viên của Task List
export const getTaskListMembers = async (taskListId: number) => {
  const res = await axios.get(`${URL}/task-list/${taskListId}/members`);
  return res.data;
};

// Xóa thành viên khỏi Task List
export const removeTaskListMember = async (taskListId: number, userId: number) => {
  const res = await axios.delete(`${URL}/task-list/${taskListId}/member/${userId}`);
  return res.data;
};

// Chuyển quyền host cho thành viên khác
export const transferTaskListHost = async (taskListId: number, newHostId: number) => {
  const res = await axios.patch(`${URL}/task-list/${taskListId}/transfer-host`, { newHostId });
  return res.data;
};
