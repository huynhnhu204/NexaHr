import { useEffect, useRef, useState } from 'react';
import { Divider, message } from 'antd';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import authService from './authService';
import { loginWithGoogle } from './authSlice';

const GoogleLoginButton = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const btnRef = useRef(null);
  const [enabled, setEnabled] = useState(false);
  const [clientId, setClientId] = useState('');

  useEffect(() => {
    authService.getGoogleConfig()
      .then((config) => {
        if (config?.enabled && config?.clientId) {
          setEnabled(true);
          setClientId(config.clientId);
        }
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!enabled || !clientId || !btnRef.current) return;

    const initGoogle = () => {
      if (!window.google?.accounts?.id) return;
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: async (response) => {
          try {
            await dispatch(loginWithGoogle(response.credential)).unwrap();
            message.success('Đăng nhập Google thành công');
            navigate('/dashboard');
          } catch (err) {
            message.error(err || 'Đăng nhập Google thất bại');
          }
        },
      });
      window.google.accounts.id.renderButton(btnRef.current, {
        theme: 'outline',
        size: 'large',
        width: '100%',
        text: 'signin_with',
        locale: 'vi',
      });
    };

    if (window.google?.accounts?.id) {
      initGoogle();
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.onload = initGoogle;
    document.body.appendChild(script);
    return () => { script.remove(); };
  }, [enabled, clientId, dispatch, navigate]);

  if (!enabled) return null;

  return (
    <>
      <Divider plain style={{ margin: '16px 0', fontSize: 13, color: '#94a3b8' }}>hoặc</Divider>
      <div ref={btnRef} style={{ display: 'flex', justifyContent: 'center' }} />
    </>
  );
};

export default GoogleLoginButton;
