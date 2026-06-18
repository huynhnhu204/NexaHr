import { Breadcrumb } from 'antd';
import { Home } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { BREADCRUMB_MAP } from '../../utils/constants';

const PageHeader = ({ title, subtitle, extra, breadcrumb }) => {
  const location = useLocation();
  const crumbs = breadcrumb || BREADCRUMB_MAP[location.pathname] || [];

  const items = [
    {
      title: (
        <Link to="/dashboard" className="breadcrumb-home">
          <Home size={14} />
        </Link>
      ),
    },
    ...crumbs.map((c) => ({ title: c.title })),
  ];

  return (
    <div className="page-header-enterprise">
      {crumbs.length > 0 && <Breadcrumb items={items} className="page-breadcrumb" />}
      <div className="page-header-row">
        <div>
          {title && <h2 className="page-title">{title}</h2>}
          {subtitle && <p className="page-subtitle">{subtitle}</p>}
        </div>
        {extra && <div className="page-header-extra">{extra}</div>}
      </div>
    </div>
  );
};

export default PageHeader;
