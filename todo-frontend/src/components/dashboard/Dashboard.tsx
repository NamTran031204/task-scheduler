import { useEffect, useCallback, useMemo } from 'react';
import { useAppDispatch, useAppSelector } from '../../store';
import { message, Alert } from 'antd';
import dayjs from 'dayjs';
import { useAuth } from '../../contexts/AuthContext';
import DashboardLayout from './DashboardLayout';
import type { Task, TaskList as TaskListType } from '../../types/task';
import {
  fetchTaskLists,
  fetchTasks,
  deleteTaskListThunk,
  saveTaskListThunk,
  deleteTaskThunk,
  toggleCompleteThunk,
  saveTaskThunk,
  calendarAddTaskThunk,
  calendarUpdateTaskThunk,
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
  type DashboardState,
} from '../../store/dashboardSlice';

const Dashboard = () => {
  const { userId } = useAuth();
  const dispatch = useAppDispatch();

  const {
    collapsed,
    selectedListId,
    taskLists,
    tasks,
    taskListsLoading,
    tasksLoading,
    showCalendar,
    taskModalOpen,
    taskModalMode,
    taskListModalMode,
    editingTask,
    editingTaskList,
    taskListModalOpen,
    memberModalOpen,
    memberModalTaskListId,
    detailModalOpen,
    detailTaskId,
    taskListDetailOpen,
    taskListDetailId,
  } = useAppSelector((state: { dashboard: DashboardState }) => state.dashboard);

  useEffect(() => {
    if (userId != null) {
      dispatch(fetchTaskLists(userId));
    }
  }, [userId, dispatch]);

  useEffect(() => {
    if (userId != null) {
      dispatch(fetchTasks({ userId, listId: selectedListId, taskLists }));
    }
  }, [userId, selectedListId, taskLists, dispatch]);

  useEffect(() => {
    (window as any).memberModalOpen = (taskListId: number) => {
      dispatch(openMemberModal(taskListId));
    };
    return () => {
      (window as any).memberModalOpen = undefined;
    };
  }, [dispatch]);

  const refreshTasksCurrent = useCallback(() => {
    if (userId != null) {
      dispatch(fetchTasks({ userId, listId: selectedListId, taskLists }));
    }
  }, [userId, selectedListId, taskLists, dispatch]);

  const refreshAllTasks = useCallback(() => {
    if (userId != null) {
      dispatch(fetchTasks({ userId, listId: null, taskLists }));
    }
  }, [userId, taskLists, dispatch]);

  const handleToggleComplete = async (task: Task) => {
    if (userId == null) return;
    const result = await dispatch(toggleCompleteThunk({ userId, task }));
    if (toggleCompleteThunk.fulfilled.match(result)) {
      refreshTasksCurrent();
    }
  };

  const handleAddTaskList = () => {
    dispatch(openTaskListModal('create'));
  };

  const handleEditTaskList = (list: TaskListType) => {
    dispatch(openTaskListModal({ mode: 'edit', list }));
  };

  const handleDeleteTaskList = async (taskListId: number) => {
    if (userId == null) return;
    if (!window.confirm('Xóa task list này?')) return;
    const result = await dispatch(
      deleteTaskListThunk({ userId, taskListId, selectedListId })
    );
    if (deleteTaskListThunk.fulfilled.match(result)) {
      dispatch(fetchTaskLists(userId));
    }
  };

  const handleTaskListModalOk = async (values: any) => {
    if (userId == null) {
      message.error('Thiếu user ID. Đăng nhập lại.');
      return;
    }
    const result = await dispatch(
      saveTaskListThunk({
        userId,
        values,
        mode: taskListModalMode,
        editingTaskList,
      })
    );
    if (saveTaskListThunk.fulfilled.match(result)) {
      dispatch(fetchTaskLists(userId));
    }
  };

  const handleAddTask = () => {
    dispatch(openTaskModal('create'));
  };

  const handleEditTask = (task: Task) => {
    dispatch(openTaskModal({ mode: 'edit', task }));
  };

  const handleDeleteTask = async (taskId: number) => {
    if (userId == null) return;
    if (!window.confirm('Are you sure you want to delete this task?')) return;
    const result = await dispatch(deleteTaskThunk({ userId, taskId }));
    if (deleteTaskThunk.fulfilled.match(result)) {
      refreshTasksCurrent();
    }
  };

  const handleShowDetail = (taskId: number) => {
    dispatch(openDetailModal(taskId));
  };

  const handleTaskModalOk = async (values: any) => {
    if (userId == null) {
      message.error('Thiếu user ID. Đăng nhập lại.');
      return;
    }
    if (!selectedListId) {
      message.warning('Chọn một task list trước');
      return;
    }
    const result = await dispatch(
      saveTaskThunk({
        userId,
        values,
        selectedListId,
        mode: taskModalMode,
        editingTask,
      })
    );
    if (saveTaskThunk.fulfilled.match(result)) {
      refreshTasksCurrent();
    }
  };

  const handleCalendarAddTask = async (
    partial: Omit<Task, 'id' | 'isCompleted'> & { dueDate?: string }
  ) => {
    if (userId == null) {
      throw new Error('Thiếu user ID. Đăng nhập lại.');
    }
    const result = await dispatch(
      calendarAddTaskThunk({
        userId,
        partial,
        selectedListId,
        taskLists,
      })
    );
    if (calendarAddTaskThunk.fulfilled.match(result)) {
      dispatch(fetchTasks({ userId, listId: null, taskLists }));
    } else if (calendarAddTaskThunk.rejected.match(result)) {
      throw result.error;
    }
  };

  const handleCalendarUpdateTask = async (task: Task) => {
    if (userId == null) return;
    const result = await dispatch(
      calendarUpdateTaskThunk({
        userId,
        task,
        selectedListId,
      })
    );
    if (calendarUpdateTaskThunk.fulfilled.match(result)) {
      dispatch(fetchTasks({ userId, listId: null, taskLists }));
    }
  };

  const taskModalInitialValues = useMemo(() =>
    editingTask && taskModalMode === 'edit'
      ? {
          ...editingTask,
          due_date: editingTask.dueDate ? dayjs(editingTask.dueDate) : undefined,
          recurrence: editingTask.recurrence ?? 'NONE',
        }
      : {},
    [editingTask, taskModalMode]
  );

  const taskListModalInitialValues = useMemo(() =>
    editingTaskList && taskListModalMode === 'edit'
      ? {
          name: editingTaskList.name,
          description: (editingTaskList as TaskListType & { description?: string })
            .description,
          color: editingTaskList.color || '#1890ff',
        }
      : { color: '#1890ff' },
    [editingTaskList, taskListModalMode]
  );

  if (userId == null) {
    return (
      <div style={{ padding: 24 }}>
        <Alert
          showIcon
          type="error"
          message="Không có user ID hợp lệ trong phiên đăng nhập."
          description="Hãy đăng xuất (avatar → Đăng xuất) rồi đăng nhập lại. Backend cần trả về accessToken và userId (hoặc id) trong JSON, hoặc JWT có claim userId/sub."
        />
      </div>
    );
  }

  return (
    <DashboardLayout
      collapsed={collapsed}
      onCollapse={(v) => dispatch(setCollapsed(v))}
      taskLists={taskLists}
      taskListsLoading={taskListsLoading}
      tasksLoading={tasksLoading}
      selectedListId={selectedListId}
      onSelectList={(value) => {
        const next =
          typeof value === 'function' ? value(selectedListId) : value;
        dispatch(setSelectedListId(next));
      }}
      onAddTaskList={handleAddTaskList}
      onEditTaskList={handleEditTaskList}
      onDeleteTaskList={handleDeleteTaskList}
      tasks={tasks}
      refreshTasks={refreshTasksCurrent}
      refreshAllTasks={refreshAllTasks}
      onAddTask={handleAddTask}
      onEditTask={handleEditTask}
      onDeleteTask={handleDeleteTask}
      onToggleComplete={handleToggleComplete}
      onShowDetail={handleShowDetail}
      showCalendar={showCalendar}
      onToggleView={() => dispatch(toggleShowCalendar())}
      taskModalOpen={taskModalOpen}
      onTaskModalOk={handleTaskModalOk}
      onTaskModalCancel={() => dispatch(closeTaskModal())}
      taskModalMode={taskModalMode}
      taskModalInitialValues={taskModalInitialValues}
      taskListModalOpen={taskListModalOpen}
      onTaskListModalOk={handleTaskListModalOk}
      onTaskListModalCancel={() => dispatch(closeTaskListModal())}
      taskListModalMode={taskListModalMode}
      taskListModalInitialValues={taskListModalInitialValues}
      memberModalOpen={memberModalOpen}
      onMemberModalClose={() => dispatch(closeMemberModal())}
      memberModalTaskListId={memberModalTaskListId}
      currentUserId={userId}
      detailModalOpen={detailModalOpen}
      detailTaskId={detailTaskId}
      onDetailModalClose={() => dispatch(closeDetailModal())}
      onCalendarAddTask={handleCalendarAddTask}
      onCalendarUpdateTask={handleCalendarUpdateTask}
      taskListDetailOpen={taskListDetailOpen}
      taskListDetailId={taskListDetailId}
      onTaskListDetailClose={() => dispatch(closeTaskListDetail())}
      onShowTaskListDetail={(id) => dispatch(openTaskListDetail(id))}
    />
  );
};

export default Dashboard;
