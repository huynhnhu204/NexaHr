import { Outlet, Link } from 'react-router-dom';

const PublicLayout = () => (
  <div className="public-layout">
    <header className="public-header">
      <Link to="/" className="public-logo">
        <span className="public-logo-mark">N</span>
        <div>
          <strong>NexaHR</strong>
          <span>Careers</span>
        </div>
      </Link>
      <Link to="/login" className="public-login-link">Đăng nhập hệ thống</Link>
    </header>
    <main className="public-main">
      <Outlet />
    </main>
    <footer className="public-footer">
      <p>© {new Date().getFullYear()} NexaHR — Work Smarter. Manage Better.</p>
    </footer>
  </div>
);

export default PublicLayout;
