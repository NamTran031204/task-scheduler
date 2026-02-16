import axios from 'axios';
import { URL } from '../constants/routeURL';

export const getRemindersByUser = async (userId: number, record = 30, page = 0) => {
  const res = await axios.get(`${URL}/reminder/user/${userId}?record=${record}&page=${page}`);
  return res.data;
};

export const getPendingRemindersByUser = async (userId: number, record = 30, page = 0) => {
  const res = await axios.get(`${URL}/reminder/user/${userId}/pending?record=${record}&page=${page}`);
  return res.data;
};

export const getRemindersByTask = async (userId: number, taskId: number) => {
  const res = await axios.get(`${URL}/reminder/user/${userId}/task/${taskId}`);
  return res.data;
};

export const getReminderById = async (userId: number, reminderId: number) => {
  const res = await axios.get(`${URL}/reminder/user/${userId}/${reminderId}`);
  return res.data;
};

export const updateReminder = async (userId: number, reminderId: number, data: any) => {
  const res = await axios.put(`${URL}/reminder/user/${userId}/update/${reminderId}`, data);
  return res.data;
};

export const deleteReminder = async (userId: number, reminderId: number) => {
  const res = await axios.delete(`${URL}/reminder/delete/${reminderId}/user/${userId}`);
  return res.data;
};

export const sendDueReminders = async () => {
  const res = await axios.post(`${URL}/reminder/system/send-due-reminders`);
  return res.data;
};
