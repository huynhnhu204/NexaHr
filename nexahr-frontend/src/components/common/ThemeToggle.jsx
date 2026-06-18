import { Button, Tooltip } from 'antd';
import { Moon, Sun } from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import { toggleTheme } from '../../store/themeSlice';

const ThemeToggle = () => {
  const dispatch = useDispatch();
  const mode = useSelector((state) => state.theme?.mode || 'light');
  const isDark = mode === 'dark';

  return (
    <Tooltip title={isDark ? 'Chế độ sáng' : 'Chế độ tối'}>
      <Button
        type="text"
        className="topbar-icon-btn"
        icon={isDark ? <Sun size={20} /> : <Moon size={20} />}
        onClick={() => dispatch(toggleTheme())}
      />
    </Tooltip>
  );
};

export default ThemeToggle;
