// import React, { useEffect, useState, useCallback } from 'react';
// import { message } from 'antd';
// import { createTask, updateTask, deleteTask, getTasksByTaskList } from '../api/task';
// import { createTaskList, getTaskListsByUser } from '../api/taskList';
// import DashboardLayout from '../components/dashboard';
// import type { Task, TaskList as TaskListType } from '../types/task';

// const Dashboard: React.FC = () => {
//   const userId = Number(localStorage.getItem('userId'));

//   const [collapsed, setCollapsed] = useState(false);
//   const [selectedListId, setSelectedListId] = useState<number | null>(null);
//   const [taskLists, setTaskLists] = useState<TaskListType[]>([]);
//   const [tasks, setTasks] = useState<Task[]>([]);
//   const [, setLoading] = useState(false);
//   const [showCalendar, setShowCalendar] = useState(true);

//   const [taskModalOpen, setTaskModalOpen] = useState(false);
//   const [taskModalMode, setTaskModalMode] = useState<'create' | 'edit'>('create');
//   const [taskListModalMode, setTaskListModalMode] = useState<'create' | 'edit'>('create');
//   const [editingTask, setEditingTask] = useState<Task | null>(null);
//   const [editingTaskList] = useState<TaskListType | null>(null);
//   const [taskListModalOpen, setTaskListModalOpen] = useState(false);
//   const [memberModalOpen, setMemberModalOpen] = useState(false);
//   const [memberModalTaskListId, setMemberModalTaskListId] = useState<number | null>(null);
//   const [, setDetailModalOpen] = useState(false);
//   const [, setDetailTaskId] = useState<number | null>(null);

//   // Toggle between list and calendar view
//   const toggleView = useCallback(() => {
//     setShowCalendar(!showCalendar);
//   }, [showCalendar]);

//   // Handle task completion toggle
//   const handleToggleComplete = async (task: Task) => {
//     try {
//       const updatedTask = { ...task, isCompleted: !task.isCompleted };
//       await updateTask(userId, task.id, updatedTask);
//       message.success('Task updated successfully');
//       refreshTasks(selectedListId);
//     } catch (error) {
//       console.error('Error updating task:', error);
//       message.error('Failed to update task');
//     }
//   };

//   // Task list handlers
//   const handleAddTaskList = useCallback(() => {
//     setTaskListModalMode('create');
//     setTaskListModalOpen(true);
//   }, []);

//   const handleTaskListModalOk = useCallback(async (values: any) => {
//     setLoading(true);
//     try {
//       if (taskListModalMode === 'create') {
//         await createTaskList(userId, values);
//         message.success('Task list created successfully');
//       } else if (editingTaskList) {
//         const { updateTaskList } = await import('../api/taskList');
//         await updateTaskList(editingTaskList.id, values);
//         message.success('Task list updated successfully');
//       }
//       refreshTaskLists();
//       setTaskListModalOpen(false);
//     } catch (error) {
//       console.error('Error saving task list:', error);
//       message.error('Failed to save task list');
//     } finally {
//       setLoading(false);
//     }
//   }, [userId, taskListModalMode, editingTaskList]);

//   const handleTaskListModalCancel = useCallback(() => {
//     setTaskListModalOpen(false);
//   }, []);

//   // Task handlers
//   const handleAddTask = useCallback(() => {
//     setEditingTask(null);
//     setTaskModalMode('create');
//     setTaskModalOpen(true);
//   }, []);

//   const handleEditTask = useCallback((task: Task) => {
//     setEditingTask(task);
//     setTaskModalMode('edit');
//     setTaskModalOpen(true);
//   }, []);

//   const handleDeleteTask = useCallback(async (taskId: number) => {
//     if (!window.confirm('Are you sure you want to delete this task?')) return;
//     setLoading(true);
//     try {
//       await deleteTask(userId, taskId);
//       message.success('Task deleted successfully');
//       refreshTasks(selectedListId);
//     } catch (error) {
//       console.error('Error deleting task:', error);
//       message.error('Failed to delete task');
//     } finally {
//       setLoading(false);
//     }
//   }, [userId, selectedListId]);

//   const handleShowDetail = useCallback((taskId: number) => {
//     setDetailTaskId(taskId);
//     setDetailModalOpen(true);
//   }, []);

//   const handleTaskModalOk = useCallback(async (values: any) => {
//     setLoading(true);
//     try {
//       if (taskModalMode === 'create') {
//         const res = await createTask(userId, { ...values, task_list_id: selectedListId });
//         message.success('Task created successfully');
//         if (values.recurrence && values.recurrence !== 'NONE') {
//           await import('../api/task').then(api => api.createTaskRecurrence(res.content.id, { type: values.recurrence }));
//         }
//       } else if (editingTask) {
//         await updateTask(userId, editingTask.id, values);
//         message.success('Task updated successfully');
//         if (values.recurrence && values.recurrence !== 'NONE' && values.recurrence !== editingTask.recurrence) {
//           await import('../api/task').then(api => api.createTaskRecurrence(editingTask.id, { type: values.recurrence }));
//         }
//       }
//       setTaskModalOpen(false);
//       refreshTasks(selectedListId);
//     } catch (error) {
//       console.error('Error saving task:', error);
//       message.error('Failed to save task');
//     } finally {
//       setLoading(false);
//     }
//   }, [userId, selectedListId, taskModalMode, editingTask]);

//   const handleTaskModalCancel = useCallback(() => {
//     setTaskModalOpen(false);
//   }, []);

//   // Fetch data
//   const refreshTaskLists = useCallback(async () => {
//     if (!userId) return;
//     setLoading(true);
//     try {
//       const data = await getTaskListsByUser(userId);
//       setTaskLists(data.content || []);
//     } catch (error) {
//       console.error('Error loading task lists:', error);
//       message.error('Failed to load task lists');
//     } finally {
//       setLoading(false);
//     }
//   }, [userId]);

//   const refreshTasks = useCallback(async (listId: number | null) => {
//     if (!userId) return;
//     try {
//       if (listId) {
//         const data = await getTasksByTaskList(userId, listId);
//         setTasks(data.content || []);
//       } else {
//         // If no list is selected, show all tasks from all lists
//         const allTasks: Task[] = [];
//         for (const list of taskLists) {
//           const data = await getTasksByTaskList(userId, list.id);
//           allTasks.push(...(data.content || []));
//         }
//         setTasks(allTasks);
//       }
//     } catch (error) {
//       console.error('Error loading tasks:', error);
//       message.error('Failed to load tasks');
//     }
//   }, [userId, taskLists]);

//   // Initial data loading
//   useEffect(() => {
//     refreshTaskLists();
//     refreshTasks(selectedListId);
//   }, [refreshTaskLists, refreshTasks, selectedListId]);

//   // Register member modal function
//   useEffect(() => {
//     (window as any).memberModalOpen = (taskListId: number) => {
//       setMemberModalTaskListId(taskListId);
//       setMemberModalOpen(true);
//     };
//     return () => { (window as any).memberModalOpen = undefined; };
//   }, []);

//   return (
//     <DashboardLayout
//       // Layout
//       collapsed={collapsed}
//       onCollapse={setCollapsed}
      
//       // Task list
//       taskLists={taskLists}
//       selectedListId={selectedListId}
//       onSelectList={setSelectedListId}
//       onAddTaskList={handleAddTaskList}
      
//       // Tasks
//       tasks={tasks}
//       onAddTask={handleAddTask}
//       onEditTask={handleEditTask}
//       onDeleteTask={handleDeleteTask}
//       onToggleComplete={handleToggleComplete}
//       onShowDetail={handleShowDetail}
      
//       // View
//       showCalendar={showCalendar}
//       onToggleView={toggleView}
      
//       // Modals
//       taskModalOpen={taskModalOpen}
//       onTaskModalOk={handleTaskModalOk}
//       onTaskModalCancel={handleTaskModalCancel}
//       taskModalMode={taskModalMode}
//       editingTask={editingTask}
      
//       taskListModalOpen={taskListModalOpen}
//       onTaskListModalOk={handleTaskListModalOk}
//       onTaskListModalCancel={handleTaskListModalCancel}
      
//       memberModalOpen={memberModalOpen}
//       onMemberModalClose={() => setMemberModalOpen(false)}
//       memberModalTaskListId={memberModalTaskListId}
//       currentUserId={userId}
//     />
//   );
// };

// export default Dashboard;
