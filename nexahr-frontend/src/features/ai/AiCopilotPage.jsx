import { useEffect, useRef, useState } from 'react';
import { Card, Input, Button, List, Tag, Spin, Row, Col } from 'antd';
import { Send, Sparkles, AlertTriangle, CheckCircle, Info } from 'lucide-react';
import { Link } from 'react-router-dom';
import PageHeader from '../../components/common/PageHeader';
import axiosClient from '../../services/axiosClient';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { useI18n } from '../../hooks/useI18n';

const SEVERITY_META = {
  CRITICAL: { color: 'red', icon: AlertTriangle },
  WARNING: { color: 'orange', icon: AlertTriangle },
  INFO: { color: 'blue', icon: Info },
  SUCCESS: { color: 'green', icon: CheckCircle },
};

const AiCopilotPage = () => {
  const { lang } = useI18n();
  const [insights, setInsights] = useState([]);
  const [messages, setMessages] = useState([
    { role: 'assistant', text: lang === 'en'
      ? 'Hello! I am NexaHR Copilot. Ask me about headcount, turnover, leave, payroll or recruitment.'
      : 'Xin chào! Tôi là NexaHR Copilot. Hỏi tôi về nhân sự, turnover, nghỉ phép, lương hoặc tuyển dụng.' },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [insightsLoading, setInsightsLoading] = useState(true);
  const [suggestions, setSuggestions] = useState([]);
  const [llmEnabled, setLlmEnabled] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    axiosClient.get(ENDPOINTS.AI.CONFIG)
      .then((res) => setLlmEnabled(!!res.data?.llmEnabled))
      .catch(() => {});
    axiosClient.get(ENDPOINTS.AI.INSIGHTS)
      .then((res) => setInsights(res.data || []))
      .finally(() => setInsightsLoading(false));
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const send = async (text) => {
    const msg = text || input;
    if (!msg.trim()) return;
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', text: msg }]);
    setLoading(true);
    try {
      const res = await axiosClient.post(ENDPOINTS.AI.CHAT, { message: msg });
      setMessages((prev) => [...prev, { role: 'assistant', text: res.data?.reply || '' }]);
      setSuggestions(res.data?.suggestions || []);
    } catch (err) {
      setMessages((prev) => [...prev, { role: 'assistant', text: err.message }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <PageHeader
        title={lang === 'en' ? 'AI HR Copilot' : 'AI HR Copilot'}
        subtitle={lang === 'en' ? 'Smart insights and HR assistant' : 'Phân tích thông minh và trợ lý nhân sự'}
        extra={(
          <Tag icon={<Sparkles size={12} />} color={llmEnabled ? 'purple' : 'default'}>
            {llmEnabled ? 'OpenAI' : (lang === 'en' ? 'Rule-based' : 'Rule-based')}
          </Tag>
        )}
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card className="copilot-chat-card" title={lang === 'en' ? 'Chat' : 'Trò chuyện'}>
            <div className="copilot-messages">
              {messages.map((m, i) => (
                <div key={i} className={`copilot-msg copilot-msg-${m.role}`}>{m.text}</div>
              ))}
              {loading && <Spin size="small" />}
              <div ref={bottomRef} />
            </div>
            {suggestions.length > 0 && (
              <div className="copilot-suggestions">
                {suggestions.map((s) => (
                  <Button key={s} size="small" type="dashed" onClick={() => send(s)}>{s}</Button>
                ))}
              </div>
            )}
            <div className="copilot-input-row">
              <Input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onPressEnter={() => send()}
                placeholder={lang === 'en' ? 'Ask about HR metrics...' : 'Hỏi về chỉ số nhân sự...'}
                disabled={loading}
              />
              <Button type="primary" icon={<Send size={16} />} loading={loading} onClick={() => send()} />
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={10}>
          <Card title={lang === 'en' ? 'AI Insights' : 'AI Insights'} loading={insightsLoading}>
            <List
              dataSource={insights}
              locale={{ emptyText: lang === 'en' ? 'No insights' : 'Không có insight' }}
              renderItem={(item) => {
                const meta = SEVERITY_META[item.severity] || SEVERITY_META.INFO;
                const Icon = meta.icon;
                return (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<Icon size={18} color={meta.color === 'green' ? '#22C55E' : meta.color === 'orange' ? '#F59E0B' : '#2563EB'} />}
                      title={<><Tag color={meta.color}>{item.category}</Tag> {item.title}</>}
                      description={item.description}
                    />
                    {item.actionPath && (
                      <Link to={item.actionPath}><Button size="small">{item.actionLabel}</Button></Link>
                    )}
                  </List.Item>
                );
              }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default AiCopilotPage;
