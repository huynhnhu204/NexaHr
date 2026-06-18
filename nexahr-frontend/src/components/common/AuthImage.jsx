import { useEffect, useState } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../../utils/constants';

const UPLOAD_ORIGIN = API_BASE_URL.startsWith('http')
  ? API_BASE_URL.replace(/\/api\/?$/, '')
  : window.location.origin;

function resolveSrc(src) {
  if (!src) return null;
  if (src.startsWith('http')) return src;
  return `${UPLOAD_ORIGIN}${src.startsWith('/') ? src : `/${src}`}`;
}

export default function AuthImage({ src, alt, className, style }) {
  const [blobUrl, setBlobUrl] = useState(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!src) {
      setBlobUrl(null);
      return undefined;
    }
    let active = true;
    let objectUrl;
    const token = localStorage.getItem('token');
    const url = resolveSrc(src);

    axios.get(url, {
      responseType: 'blob',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then((res) => {
        if (!active) return;
        objectUrl = URL.createObjectURL(res.data);
        setBlobUrl(objectUrl);
        setFailed(false);
      })
      .catch(() => {
        if (active) setFailed(true);
      });

    return () => {
      active = false;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [src]);

  if (!src || failed) {
    return <div className={className} style={{ ...style, background: '#1e293b', color: '#94a3b8', fontSize: 11, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>Ảnh</div>;
  }

  if (!blobUrl) {
    return <div className={className} style={{ ...style, background: '#1e293b' }} />;
  }

  return <img src={blobUrl} alt={alt} className={className} style={style} />;
}
