import { create } from 'zustand';

interface ReminderItem {
  id: number;
  message?: string;
  remindAt?: string;
  remind_at?: string;
  dueDate?: string;
  createdAt?: string;
  status?: string;
  taskId?: number;
}

interface RemindersState {
  allReminders: ReminderItem[];
  pendingReminders: ReminderItem[];
  loadingAll: boolean;
  loadingPending: boolean;
  sending: boolean;
  editingId: number | null;
  editingMessage: string;
  taskIdFilter: string;
  byTaskList: ReminderItem[];
  loadingByTask: boolean;
  reminderLookupId: string;
  lookupResult: ReminderItem | null;
  lookupLoading: boolean;

  // Actions
  setAllReminders: (reminders: ReminderItem[]) => void;
  setPendingReminders: (reminders: ReminderItem[]) => void;
  setLoadingAll: (loading: boolean) => void;
  setLoadingPending: (loading: boolean) => void;
  setSending: (sending: boolean) => void;
  setEditingId: (id: number | null) => void;
  setEditingMessage: (message: string) => void;
  setTaskIdFilter: (filter: string) => void;
  setByTaskList: (reminders: ReminderItem[]) => void;
  setLoadingByTask: (loading: boolean) => void;
  setReminderLookupId: (id: string) => void;
  setLookupResult: (reminder: ReminderItem | null) => void;
  setLookupLoading: (loading: boolean) => void;

  // Reset
  resetEditingState: () => void;
}

export const useRemindersStore = create<RemindersState>((set) => ({
  allReminders: [],
  pendingReminders: [],
  loadingAll: false,
  loadingPending: false,
  sending: false,
  editingId: null,
  editingMessage: '',
  taskIdFilter: '',
  byTaskList: [],
  loadingByTask: false,
  reminderLookupId: '',
  lookupResult: null,
  lookupLoading: false,

  setAllReminders: (reminders: ReminderItem[]) => set({ allReminders: reminders }),
  setPendingReminders: (reminders: ReminderItem[]) => set({ pendingReminders: reminders }),
  setLoadingAll: (loading: boolean) => set({ loadingAll: loading }),
  setLoadingPending: (loading: boolean) => set({ loadingPending: loading }),
  setSending: (sending: boolean) => set({ sending }),
  setEditingId: (id: number | null) => set({ editingId: id }),
  setEditingMessage: (message: string) => set({ editingMessage: message }),
  setTaskIdFilter: (filter: string) => set({ taskIdFilter: filter }),
  setByTaskList: (reminders: ReminderItem[]) => set({ byTaskList: reminders }),
  setLoadingByTask: (loading: boolean) => set({ loadingByTask: loading }),
  setReminderLookupId: (id: string) => set({ reminderLookupId: id }),
  setLookupResult: (reminder: ReminderItem | null) => set({ lookupResult: reminder }),
  setLookupLoading: (loading: boolean) => set({ lookupLoading: loading }),

  resetEditingState: () => set({ editingId: null, editingMessage: '' }),
}));
