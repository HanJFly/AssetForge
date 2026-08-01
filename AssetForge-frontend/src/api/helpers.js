export function normalizePageResult(payload, fallback = []) {
  const data = payload?.data ?? payload ?? {}
  const nestedRecords = data.records && typeof data.records === 'object' ? data.records : {}
  const records = Array.isArray(data.records)
    ? data.records
    : Array.isArray(nestedRecords.result)
      ? nestedRecords.result
      : Array.isArray(nestedRecords.list)
        ? nestedRecords.list
        : Array.isArray(nestedRecords.records)
          ? nestedRecords.records
          : Array.isArray(data.result)
            ? data.result
            : Array.isArray(data.list)
              ? data.list
              : fallback

  return {
    records,
    total: Number(data.total || nestedRecords.total || records.length || fallback.length || 0),
    page: Number(data.page || nestedRecords.pageNum || nestedRecords.page || 1),
    size: Number(data.size || data.pageSize || data.Size || nestedRecords.pageSize || nestedRecords.size || 20)
  }
}

export function normalizeDataResult(payload, fallback = null) {
  if (payload?.data !== undefined && payload?.data !== null) {
    return payload.data
  }

  return payload ?? fallback
}
