import { useEffect, useState } from 'react';
import { Button, Modal, Input, message } from 'antd';
import { Shield } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';

const SamlLoginButton = () => {
  const [searchParams] = useSearchParams();
  const companyCode = searchParams.get('saml') || 'NEXA-DEMO';
  const [sso, setSso] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [email, setEmail] = useState('admin@nexahr.com');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    axiosClient.get(ENDPOINTS.AUTH.SAML_SSO(companyCode))
      .then((res) => setSso(res.data))
      .catch(() => {});
  }, [companyCode]);

  if (!sso?.enabled) return null;

  const handleDemoLogin = async () => {
    setLoading(true);
    try {
      const res = await axiosClient.post(ENDPOINTS.AUTH.SAML_DEMO, {
        companyCode,
        email,
      });
      const data = res.data;
      if (data?.accessToken) {
        localStorage.setItem('token', data.accessToken);
        if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('user', JSON.stringify(data));
        window.location.href = '/dashboard';
      }
    } catch (err) {
      message.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Button block icon={<Shield size={16} />} style={{ marginTop: 12 }} onClick={() => setModalOpen(true)}>
        Đăng nhập SSO — {sso.companyName}
      </Button>
      <Modal
        title={`SAML SSO — ${sso.companyName}`}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleDemoLogin}
        okText="Đăng nhập demo"
        confirmLoading={loading}
      >
        <p style={{ color: '#64748B', marginBottom: 12 }}>
          Chế độ demo: nhập email đã có trong hệ thống thuộc công ty {companyCode}.
        </p>
        <Input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="email@company.com" />
        {sso.demoMode && (
          <p style={{ marginTop: 8, fontSize: 12, color: '#94A3B8' }}>
            Production: redirect tới IdP tại {sso.ssoUrl}
          </p>
        )}
      </Modal>
    </>
  );
};

export default SamlLoginButton;
