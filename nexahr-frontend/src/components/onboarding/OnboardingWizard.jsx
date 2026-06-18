import { useEffect, useState } from 'react';
import { Modal, Steps, Button, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { usePermission } from '../../hooks/usePermission';
import { useAuth } from '../../hooks/useAuth';

const OnboardingWizard = () => {
  const navigate = useNavigate();
  const { isAdmin, isHR } = usePermission();
  const company = useSelector((state) => state.auth.company);
  const [open, setOpen] = useState(false);
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  const canManage = isAdmin || isHR;

  useEffect(() => {
    if (!canManage) return;
    const fetch = async () => {
      try {
        const res = await axiosClient.get(ENDPOINTS.ONBOARDING.STATUS);
        const data = res.data;
        setStatus(data);
        if (data && !data.completed) setOpen(true);
      } catch { /* ignore */ }
    };
    fetch();
  }, [canManage, company?.id]);

  const handleComplete = async () => {
    setLoading(true);
    try {
      await axiosClient.post(ENDPOINTS.ONBOARDING.COMPLETE);
      message.success('Hoàn tất thiết lập công ty!');
      setOpen(false);
    } finally {
      setLoading(false);
    }
  };

  const goToStep = (key) => {
    const routes = {
      'company-info': '/settings',
      departments: '/departments',
      employees: '/employees',
      complete: '/dashboard',
    };
    if (routes[key]) navigate(routes[key]);
    setOpen(false);
  };

  if (!status || status.completed || !canManage) return null;

  const items = (status.steps || []).map((s) => ({
    title: s.title,
    description: s.done ? 'Đã hoàn thành' : 'Chưa hoàn thành',
    status: s.done ? 'finish' : 'wait',
  }));

  return (
    <Modal
      open={open}
      title={`Chào mừng đến ${company?.name || 'NexaHR'}!`}
      footer={[
        <Button key="later" onClick={() => setOpen(false)}>Để sau</Button>,
        <Button key="complete" type="primary" loading={loading} onClick={handleComplete}
          disabled={!status.steps?.every((s) => s.done)}>
          Hoàn tất thiết lập
        </Button>,
      ]}
      width={560}
      closable
      onCancel={() => setOpen(false)}
    >
      <p style={{ color: '#64748B', marginBottom: 24 }}>
        Hoàn thành các bước sau để thiết lập công ty của bạn.
      </p>
      <Steps direction="vertical" current={(status.currentStep || 1) - 1} items={items} />
      <div style={{ marginTop: 20, display: 'flex', flexDirection: 'column', gap: 8 }}>
        {(status.steps || []).filter((s) => !s.done).map((s) => (
          <Button key={s.key} type="link" style={{ textAlign: 'left', padding: 0 }}
            onClick={() => goToStep(s.key)}>
            → {s.title}
          </Button>
        ))}
      </div>
    </Modal>
  );
};

export default OnboardingWizard;
