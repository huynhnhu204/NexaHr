export const formatCurrency = (amount, currency = 'VND') => {
  if (amount === null || amount === undefined) return '-';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(amount);
};

export const formatNumber = (num) => {
  if (num === null || num === undefined) return '-';
  return new Intl.NumberFormat('vi-VN').format(num);
};
