import { Segmented } from 'antd';
import { useDispatch, useSelector } from 'react-redux';
import { setLocale } from '../../store/localeSlice';
import { useI18n } from '../../hooks/useI18n';

const LanguageToggle = ({ size = 'small' }) => {
  const dispatch = useDispatch();
  const lang = useSelector((state) => state.locale?.lang || 'vi');
  const { t } = useI18n();

  return (
    <Segmented
      size={size}
      value={lang}
      onChange={(v) => dispatch(setLocale(v))}
      options={[
        { label: 'VI', value: 'vi' },
        { label: 'EN', value: 'en' },
      ]}
      aria-label={t('lang.vi')}
    />
  );
};

export default LanguageToggle;
