import { useSelector } from 'react-redux';
import { t as translate } from '../i18n/translations';

export const useI18n = () => {
  const lang = useSelector((state) => state.locale?.lang || 'vi');
  return {
    lang,
    t: (key) => translate(lang, key),
  };
};
