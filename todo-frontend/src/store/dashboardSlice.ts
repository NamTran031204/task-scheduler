import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit';
import { message } from 'antd';
import dayjs from 'dayjs';
import {
  createTask,
  updateTask,
  deleteTask,
  getTasksByTaskList,
  createTaskRecurrence,
  completeTask,
  undoCompleteTask,
} from '../api/task';
import { createTaskList, getTaskListsByUser, deleteTaskList, updateTaskList } from '../api/taskList';
import { buildTaskApiBody, pickTaskListId } from '../utils/taskPayload';
import type { Task, TaskList } from '../types/task';

export interface DashboardState {
  collapsed: boolean;
  selectedListId: number | null;
  taskLists: TaskList[];
  tasks: Task[];
  taskListsLoading: boolean;
  tasksLoading: boolean;
  showCalendar: boolean;
  taskModalOpen: boolean;
  taskModalMode: 'create' | 'edit';
  taskListModalMode: 'create' | 'edit';
  editingTask: Task | null;
  editingTaskList: TaskList | null;
  taskListModalOpen: boolean;
  memberModalOpen: boolean;
  memberModalTaskListId: number | null;
  detailModalOpen: boolean;
  detailTaskId: number | null;
  taskListDetailOpen: boolean;
  taskListDetailId: number | null;
}

const initialState: DashboardState = {
  collapsed: false,
  selectedListId: null,
  taskLists: [],
  tasks: [],
  taskListsLoading: false,
  tasksLoading: false,
  showCalendar: false,
  taskModalOpen: false,
  taskModalMode: 'create',
  taskListModalMode: 'create',
  editingTask: null,
  editingTaskList: null,
  taskListModalOpen: false,
  memberModalOpen: false,
  memberModalTaskListId: null,
  detailModalOpen: false,
  detailTaskId: null,
  taskListDetailOpen: false,
  taskListDetailId: null,
};

const normalizeTask = (task: any): Task => {
  const dueDate = task?.dueDate ?? task?.due_date ?? '';
  const taskListId = task?.taskListId ?? task?.task_list_id ?? task?.taskList?.id ?? task?.task_list?.id;
  return {
    ...task,
    dueDate,
    taskListId,
  } as Task;
};

function buildTaskListPayload(values: any) {
  const name = String(values?.name ?? '').trim();
  const rawDesc = values?.description;
  const description =
    rawDesc == null || String(rawDesc).trim() === '' ? undefined : String(rawDesc).trim();
  const rawColor = values?.color;
  const color =
    rawColor != null && String(rawColor).trim() !== '' ? String(rawColor).trim() : '#3b82f6';
  return { name, description, color };
}

export const fetchTaskLists = createAsyncThunk(
  'dashboard/fetchTaskLists',
  async (userId: number, { rejectWithValue }: { rejectWithValue: (value: any) => any }) => {
    try {
      const data = await getTaskListsByUser(userId);
      return data.content || [];
    } catch (error) {
      message.error('Không tải được danh sách task list');
      return rejectWithValue({
        message: error instanceof Error ? error.message : 'Unknown error',
        status: (error as any)?.response?.status,
      });
    }
  }
);

export const fetchTasks = createAsyncThunk(
  'dashboard/fetchTasks',
  async (
    { userId, listId, taskLists }: { userId: number; listId: number | null; taskLists: TaskList[] },
    { rejectWithValue }: { rejectWithValue: (value: any) => any }
  ) => {
    try {
        if (listId) {
          const data = await getTasksByTaskList(userId, listId);
          return (data.content || []).map(normalizeTask);
        }
        const allTasks: Task[] = [];
        for (const list of taskLists) {
          const data = await getTasksByTaskList(userId, list.id);
          allTasks.push(...(data.content || []).map(normalizeTask));
        }
        return allTasks;
    } catch (error) {
      message.error('Không tải được công việc');
      return rejectWithValue({
        message: error instanceof Error ? error.message : 'Unknown error',
        status: (error as any)?.response?.status,
      });
    }
  }
);

export const deleteTaskListThunk = createAsyncThunk(
  'dashboard/deleteTaskList',
  async (
    { userId, taskListId, selectedListId }: { userId: number; taskListId: number; selectedListId: number | null },
    { rejectWithValue }: { rejectWithValue: (value: any) => any }
  ) => {
    try {
      await deleteTaskList(taskListId, userId);
      message.success('Đã xóa task list');
      return { taskListId, selectedListId };
    } catch (error) {
      message.error('Xóa task list thất bại');
      return rejectWithValue({
        message: error instanceof Error ? error.message : 'Unknown error',
        status: (error as any)?.response?.status,
      });
    }
  }
);

export const saveTaskListThunk = createAsyncThunk(
  'dashboard/saveTaskList',
  async (
    {
      userId,
      values,
      mode,
      editingTaskList,
    }: {
      userId: number;
      values: any;
      mode: 'create' | 'edit';
      editingTaskList: TaskList | null;
    },
    { rejectWithValue }: { rejectWithValue: (value: any) => any }
  ) => {
    try {
      const payload = buildTaskListPayload(values);
      if (!payload.name) {
        message.warning('Tên danh sách không được để trống');
        return rejectWithValue(new Error('Tên trống'));
      }
      if (mode === 'create') {
        await createTaskList(userId, payload);
        message.success('Task list created successfully');
      } else if (editingTaskList) {
        await updateTaskList(editingTaskList.id, userId, payload);
        message.success('Task list updated successfully');
      }
      return null;
    } catch (error) {
      const err = error as any;
      const msg =
        err?.response?.data?.message || err?.response?.data?.error || 'Không lưu được task list';
      message.error(msg);
      return rejectWithValue({
        message: msg,
        status: err?.response?.status,
      });
    }
  }
);

export const deleteTaskThunk = createAsyncThunk(
  'dashboard/deleteTask',
  async ({ userId, taskId }: { userId: number; taskId: number }, { rejectWithValue }: { rejectWithValue: (value: any) => any }) => {
    try {
      await deleteTask(userId, taskId);
      message.success('Task deleted successfully');
      return taskId;
    } catch (error) {
      message.error('Failed to delete task');
      return rejectWithValue({
        message: error instanceof Error ? error.message : 'Unknown error',
        status: (error as any)?.response?.status,
      });
    }
  }
);

export const toggleCompleteThunk = createAsyncThunk(
  'dashboard/toggleComplete',
  async (
    { userId, task }: { userId: number; task: Task },
    { rejectWithValue }: { rejectWithValue: (value: any) => any }
  ) => {
    try {
      if (task.isCompleted) {
        await undoCompleteTask(userId, task.id);
      } else {
        await completeTask(userId, task.id);
      }
      message.success('Đã cập nhật trạng thái');
      return task;
    } catch (error: any) {
      const msg = error?.response?.data?.message || 'Không cập nhật được trạng thái';
      message.error(msg);
      return rejectWithValue({
        message: msg,
        status: error?.response?.status,
      });
    }
  }
);

export const saveTaskThunk = createAsyncThunk(
  'dashboard/saveTask',
  async (
    {
      userId,
      values,
      selectedListId,
      mode,
      editingTask,
    }: {
      userId: number;
      values: any;
      selectedListId: number;
      mode: 'create' | 'edit';
      editingTask: Task | null;
    },
    { rejectWithValue }: { rejectWithValue: (value: any) => any }
  ) => {
    try {
      const { recurrence, ...formFields } = values;
      const listIdForSave =
        mode === 'edit' && editingTask
          ? pickTaskListId(editingTask as unknown as Record<string, unknown>, selectedListId)
          : selectedListId;
      if (!listIdForSave) {
        message.warning('Thiếu task list id');
        return rejectWithValue(new Error('LIST_ID_INVALID'));
      }
      const payload = buildTaskApiBody(formFields, listIdForSave, (m) => message.warning(m));
      if (mode === 'create') {
        await createTask(userId, payload);
        message.success('Đã tạo công việc');
        if (recurrence && recurrence !== 'NONE') {
          const data = await getTasksByTaskList(userId, selectedListId);
          const newest = (data.content || [])[0] as Task | undefined;
          if (newest?.id) {
            await createTaskRecurrence(newest.id, { type: recurrence });
          }
        }
      } else if (editingTask) {
        await updateTask(userId, editingTask.id, payload);
        message.success('Đã cập nhật công việc');
        if (recurrence && recurrence !== 'NONE' && recurrence !== editingTask.recurrence) {
          await createTaskRecurrence(editingTask.id, { type: recurrence });
        }
      }
      return { listId: selectedListId };
    } catch (error: any) {
      const msg =
        error?.response?.data?.message || error?.response?.data?.error || 'Lưu công việc thất bại';
      message.error(msg);
      return rejectWithValue({
        message: msg,
        status: error?.response?.status,
      });
    }
  }
);

export const calendarAddTaskThunk = createAsyncThunk(
  'dashboard/calendarAddTask',
  async (
    {
      userId,
      partial,
      selectedListId,
      taskLists,
    }: {
      userId: number;
      partial: Omit<Task, 'id' | 'isCompleted'> & { dueDate?: string };
      selectedListId: number | null;
      taskLists: TaskList[];
    },
    { rejectWithValue }: { rejectWithValue: (value: any) => any }
  ) => {
    try {
      const listId = selectedListId ?? taskLists[0]?.id;
      if (!listId) {
        const msg = 'Tạo ít nhất một task list trước khi thêm công việc';
        message.warning(msg);
        return rejectWithValue(new Error(msg));
      }
      const formFields = {
        title: partial.title || '',
        description: partial.description || '',
        priority: partial.priority || 'MEDIUM',
        due_date: partial.dueDate ? dayjs(partial.dueDate).endOf('day') : dayjs().endOf('day'),
      };
      const payload = buildTaskApiBody(formFields, listId, (m) => message.warning(m));
      console.log('Calendar task creation payload:', payload);
      await createTask(userId, payload);
      message.success('Đã thêm công việc');
      return null;
    } catch (error: any) {
      const msg = error?.response?.data?.message || error?.response?.data?.error || error?.message || 'Không thêm được công việc';
      message.error(msg);
      return rejectWithValue({
        message: msg,
        status: error?.response?.status,
      });
    }
  }
);

export const calendarUpdateTaskThunk = createAsyncThunk(
  'dashboard/calendarUpdateTask',
  async (
    {
      userId,
      task,
      selectedListId,
    }: {
      userId: number;
      task: Task;
      selectedListId: number | null;
    },
    { rejectWithValue }: { rejectWithValue: (value: any) => any }
  ) => {
    try {
      const listId = pickTaskListId(task as unknown as Record<string, unknown>, selectedListId);
      if (!listId) {
        message.error('Thiếu task list');
        return rejectWithValue(new Error('LIST_ID_INVALID'));
      }
      const payload = buildTaskApiBody(
        {
          title: task.title,
          description: task.description,
          priority: task.priority,
          due_date: task.dueDate ? dayjs(task.dueDate) : undefined,
        },
        listId,
        (m) => message.warning(m)
      );
      await updateTask(userId, task.id, payload);
      message.success('Đã cập nhật công việc');
      return null;
    } catch (error) {
      return rejectWithValue({
        message: error instanceof Error ? error.message : 'Unknown error',
        status: (error as any)?.response?.status,
      });
    }
  }
);

const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {
    setCollapsed: (state: DashboardState, action: PayloadAction<boolean>) => {
      state.collapsed = action.payload;
    },
    setSelectedListId: (state: DashboardState, action: PayloadAction<number | null>) => {
      state.selectedListId = action.payload;
    },
    toggleShowCalendar: (state: DashboardState) => {
      state.showCalendar = !state.showCalendar;
    },
    openTaskModal: (state: DashboardState, action: PayloadAction<'create' | { mode: 'edit'; task: Task }>) => {
      state.taskModalOpen = true;
      if (action.payload === 'create') {
        state.taskModalMode = 'create';
        state.editingTask = null;
      } else {
        state.taskModalMode = 'edit';
        state.editingTask = action.payload.task;
      }
    },
    closeTaskModal: (state: DashboardState) => {
      state.taskModalOpen = false;
      state.editingTask = null;
    },
    openTaskListModal: (state: DashboardState, action: PayloadAction<'create' | { mode: 'edit'; list: TaskList }>) => {
      state.taskListModalOpen = true;
      if (action.payload === 'create') {
        state.taskListModalMode = 'create';
        state.editingTaskList = null;
      } else {
        state.taskListModalMode = 'edit';
        state.editingTaskList = action.payload.list;
      }
    },
    closeTaskListModal: (state: DashboardState) => {
      state.taskListModalOpen = false;
      state.editingTaskList = null;
    },
    openMemberModal: (state: DashboardState, action: PayloadAction<number>) => {
      state.memberModalOpen = true;
      state.memberModalTaskListId = action.payload;
    },
    closeMemberModal: (state: DashboardState) => {
      state.memberModalOpen = false;
      state.memberModalTaskListId = null;
    },
    openDetailModal: (state: DashboardState, action: PayloadAction<number>) => {
      state.detailModalOpen = true;
      state.detailTaskId = action.payload;
    },
    closeDetailModal: (state: DashboardState) => {
      state.detailModalOpen = false;
      state.detailTaskId = null;
    },
    openTaskListDetail: (state: DashboardState, action: PayloadAction<number>) => {
      state.taskListDetailOpen = true;
      state.taskListDetailId = action.payload;
    },
    closeTaskListDetail: (state: DashboardState) => {
      state.taskListDetailOpen = false;
      state.taskListDetailId = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchTaskLists.pending, (state: DashboardState) => {
        state.taskListsLoading = true;
      })
      .addCase(fetchTaskLists.fulfilled, (state: DashboardState, action: PayloadAction<TaskList[]>) => {
        state.taskListsLoading = false;
        state.taskLists = action.payload;
      })
      .addCase(fetchTaskLists.rejected, (state: DashboardState) => {
        state.taskListsLoading = false;
      })
      .addCase(fetchTasks.pending, (state: DashboardState) => {
        state.tasksLoading = true;
      })
      .addCase(fetchTasks.fulfilled, (state: DashboardState, action: PayloadAction<Task[]>) => {
        state.tasksLoading = false;
        state.tasks = action.payload;
      })
      .addCase(fetchTasks.rejected, (state: DashboardState) => {
        state.tasksLoading = false;
      })
      .addCase(deleteTaskListThunk.fulfilled, (state: DashboardState, action: PayloadAction<{ taskListId: number; selectedListId: number | null }>) => {
        const { taskListId } = action.payload;
        if (state.selectedListId === taskListId) {
          state.selectedListId = null;
        }
      })
      .addCase(saveTaskListThunk.fulfilled, (state: DashboardState) => {
        state.taskListModalOpen = false;
        state.editingTaskList = null;
      })
      .addCase(deleteTaskThunk.fulfilled, () => {})
      .addCase(toggleCompleteThunk.fulfilled, () => {})
      .addCase(saveTaskThunk.fulfilled, (state: DashboardState) => {
        state.taskModalOpen = false;
        state.editingTask = null;
      })
      .addCase(calendarAddTaskThunk.fulfilled, () => {})
      .addCase(calendarUpdateTaskThunk.fulfilled, () => {});
  },
});

export const {
  setCollapsed,
  setSelectedListId,
  toggleShowCalendar,
  openTaskModal,
  closeTaskModal,
  openTaskListModal,
  closeTaskListModal,
  openMemberModal,
  closeMemberModal,
  openDetailModal,
  closeDetailModal,
  openTaskListDetail,
  closeTaskListDetail,
} = dashboardSlice.actions;

export default dashboardSlice.reducer;
