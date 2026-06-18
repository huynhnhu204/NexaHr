import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const PayrollChart = ({ data = [] }) => {
  const chartData = data.map((d) => ({
    month: d.month,
    amount: Number(d.amount) / 1000000,
  }));

  if (!chartData.length) {
    return <div style={{ textAlign: 'center', color: '#94a3b8', padding: 40 }}>Chưa có dữ liệu</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={260}>
      <BarChart data={chartData}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
        <XAxis dataKey="month" tick={{ fontSize: 12 }} />
        <YAxis tick={{ fontSize: 12 }} unit="M" />
        <Tooltip formatter={(v) => [`${v.toFixed(1)}M VND`, 'Số tiền']} />
        <Bar dataKey="amount" fill="#2563eb" radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
};

export default PayrollChart;
