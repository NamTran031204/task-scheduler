import dayjs from 'dayjs';

const PRIORITIES = new Set(['LOW', 'MEDIUM', 'HIGH', 'URGENT']);

export type TaskFormValues = {
  title?: string;
  description?: string;
  priority?: string;
  due_date?: unknown;
  taskListId?: number;
  recurrence?: string;
};

export function buildTaskApiBody(
  values: TaskFormValues,
  taskListId?: number,
  onWarn?: (msg: string) => void
): Record<string, string | number> {
  const title = String(values?.title ?? '').trim();
  const lid = Number(taskListId ?? values.taskListId);
  if (!title) {
    throw new Error('TITLE_REQUIRED');
  }

  const body: Record<string, string | number> = {
    title,
  };

  if (Number.isFinite(lid) && lid > 0) {
    body.task_list_id = lid;
  }

  const desc = values?.description;
  if (desc != null && String(desc).trim() !== '') {
    body.description = String(desc).trim();
  }

  const p = values?.priority;
  body.priority = p && PRIORITIES.has(String(p)) ? String(p) : 'MEDIUM';

  const raw = values?.due_date;
  if (raw != null && raw !== '') {
    const d = dayjs.isDayjs(raw) ? raw : dayjs(raw as string | Date);
    if (d.isValid()) {
      if (d.isBefore(dayjs())) {
        onWarn?.('Hạn chót đã qua; lưu task không kèm hạn chót.');
      }
      body.due_date = d.format('YYYY-MM-DDTHH:mm');
    }
  }
  const rec = values?.recurrence;
  if (rec && ['NONE', 'DAILY', 'WEEKLY', 'MONTHLY'].includes(String(rec))) {
    body.recurrence = String(rec);
  }

  return body;
}

export function pickTaskListId(task: Record<string, unknown>, fallback: number | null): number {
  const a = task.taskListId ?? task.task_list_id;
  const n = Number(a);
  if (Number.isFinite(n) && n > 0) return n;
  return Number(fallback) > 0 ? Number(fallback) : 0;
}


