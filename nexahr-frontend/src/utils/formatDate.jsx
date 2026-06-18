import dayjs from 'dayjs';

export const formatDate = (date, format = 'DD/MM/YYYY') => {
  if (!date) return '-';
  return dayjs(date).format(format);
};

export const formatDateTime = (date, format = 'DD/MM/YYYY HH:mm') => {
  if (!date) return '-';
  return dayjs(date).format(format);
};

export const formatRelative = (date) => {
  if (!date) return '-';
  return dayjs(date).fromNow?.() || dayjs(date).format('DD/MM/YYYY');
};
