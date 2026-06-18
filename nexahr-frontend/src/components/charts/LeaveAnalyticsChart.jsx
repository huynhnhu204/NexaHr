import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

const COLORS = ['#1E3A8A', '#22C55E', '#F59E0B', '#EF4444', '#8B5CF6'];

const LeaveAnalyticsChart = ({ data = [] }) => {
  const chartData = data.map((d) => ({
    name: d.status,
    value: Number(d.count),
  }));

  if (!chartData.length) {
    return <div style={{ textAlign: 'center', color: '#94A3B8', padding: 40 }}>Chưa có dữ liệu</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={240}>
      <PieChart>
        <Pie data={chartData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} dataKey="value" paddingAngle={2}>
          {chartData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
        </Pie>
        <Tooltip />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  );
};

export default LeaveAnalyticsChart;
