export const SESSION_NO_JWT = '__session_no_jwt__';

export function parseStoredUserId(): number | null {
  const raw = localStorage.getItem('userId');
  if (raw == null || raw === '' || raw === 'undefined' || raw === 'null') return null;
  const n = Number(String(raw).trim());
  if (!Number.isFinite(n) || n <= 0) return null;
  return n;
}

export function tryDecodeUserIdFromJwt(token: string): number | null {
  try {
    const body = token.split('.')[1];
    if (!body) return null;
    const padded = body.replace(/-/g, '+').replace(/_/g, '/');
    const json = JSON.parse(atob(padded));
    const candidates = [json.userId, json.user_id, json.id, json.sub];
    for (const v of candidates) {
      if (v == null || v === '') continue;
      const n = Number(v);
      if (Number.isFinite(n) && n > 0) return n;
    }
    return null;
  } catch {
    return null;
  }
}

export function extractLoginResult(res: unknown): { token: string; userId: number } | null {
  if (res == null || typeof res !== 'object') return null;
  const r = res as Record<string, unknown>;
  const token =
    (typeof r.accessToken === 'string' && r.accessToken) ||
    (typeof r.access_token === 'string' && r.access_token) ||
    (typeof r.token === 'string' && r.token);
  if (!token) return null;

  let userId: number | null = null;
  const rawId = r.userId ?? r.user_id ?? r.id;
  if (rawId != null && rawId !== '') {
    const n = Number(rawId);
    if (Number.isFinite(n) && n > 0) userId = n;
  }
  if (userId == null && r.user && typeof r.user === 'object') {
    const u = r.user as Record<string, unknown>;
    const uid = u.id ?? u.userId;
    if (uid != null && uid !== '') {
      const n = Number(uid);
      if (Number.isFinite(n) && n > 0) userId = n;
    }
  }
  if (userId == null) userId = tryDecodeUserIdFromJwt(token);
  if (userId == null) return null;
  return { token, userId };
}

export function getLegacyLoginSuccessPayload(res: unknown): string | null {
  const asString =
    typeof res === 'string'
      ? res
      : res != null && typeof res === 'object' && typeof (res as Record<string, unknown>).message === 'string'
        ? String((res as Record<string, unknown>).message)
        : null;
  if (asString == null) return null;
  const s = asString.trim().toLowerCase();
  if (s.includes('success') || s.includes('thành công')) return asString.trim();
  return null;
}
