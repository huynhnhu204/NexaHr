import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

const COLORS = ['#2563eb', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#06b6d4'];

const EmployeeChart = ({ data = [] }) => {
  const chartData = data.map((d) => ({ name: d.name, value: Number(d.value) }));

  if (!chartData.length) {
    return <div style={{ textAlign: 'center', color: '#94a3b8', padding: 40 }}>Chưa có dữ liệu</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={260}>
      <PieChart>
        <Pie data={chartData} cx="50%" cy="50%" innerRadius={60} outerRadius={90} paddingAngle={3} dataKey="value">
          {chartData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
        </Pie>
        <Tooltip />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  );
};

export default EmployeeChart;
