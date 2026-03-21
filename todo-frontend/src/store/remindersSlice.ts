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

  setAllReminders: (reminders) => set({ allReminders: reminders }),
  setPendingReminders: (reminders) => set({ pendingReminders: reminders }),
  setLoadingAll: (loading) => set({ loadingAll: loading }),
  setLoadingPending: (loading) => set({ loadingPending: loading }),
  setSending: (sending) => set({ sending }),
  setEditingId: (id) => set({ editingId: id }),
  setEditingMessage: (message) => set({ editingMessage: message }),
  setTaskIdFilter: (filter) => set({ taskIdFilter: filter }),
  setByTaskList: (reminders) => set({ byTaskList: reminders }),
  setLoadingByTask: (loading) => set({ loadingByTask: loading }),
  setReminderLookupId: (id) => set({ reminderLookupId: id }),
  setLookupResult: (reminder) => set({ lookupResult: reminder }),
  setLookupLoading: (loading) => set({ lookupLoading: loading }),

  resetEditingState: () => set({ editingId: null, editingMessage: '' }),
}));
