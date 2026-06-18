import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const RecruitmentChart = ({ data = [] }) => {
  const chartData = data.map((d) => ({ month: d.month, count: Number(d.count) }));

  if (!chartData.length) {
    return <div style={{ textAlign: 'center', color: '#94a3b8', padding: 40 }}>Chưa có dữ liệu</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={260}>
      <LineChart data={chartData}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
        <XAxis dataKey="month" tick={{ fontSize: 12 }} />
        <YAxis tick={{ fontSize: 12 }} />
        <Tooltip />
        <Line type="monotone" dataKey="count" stroke="#10b981" strokeWidth={2} dot={{ fill: '#10b981' }} />
      </LineChart>
    </ResponsiveContainer>
  );
};

export default RecruitmentChart;
