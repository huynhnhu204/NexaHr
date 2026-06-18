import { Component } from 'react';
import { Result, Button } from 'antd';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    console.error('NexaHR ErrorBoundary:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: 48, textAlign: 'center' }}>
          <Result
            status="error"
            title="Đã xảy ra lỗi"
            subTitle="Vui lòng tải lại trang hoặc thử lại sau."
            extra={<Button type="primary" onClick={() => window.location.reload()}>Tải lại trang</Button>}
          />
        </div>
      );
    }
    return this.props.children;
  }
}

export default ErrorBoundary;
