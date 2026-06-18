import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Card, Row, Col, Tag, Spin, Empty } from 'antd';
import { MapPin, Briefcase, ArrowRight } from 'lucide-react';
import axios from 'axios';
import { API_BASE_URL } from '../../utils/constants';
import { ENDPOINTS } from '../../services/apiEndpoints';
import { JOB_STATUS } from '../../utils/constants';

const CareersPortalPage = () => {
  const { companyCode } = useParams();
  const [jobs, setJobs] = useState([]);
  const [company, setCompany] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [companyRes, jobsRes] = await Promise.all([
          axios.get(`${API_BASE_URL}${ENDPOINTS.PUBLIC_CAREERS.company(companyCode)}`),
          axios.get(`${API_BASE_URL}${ENDPOINTS.PUBLIC_CAREERS.jobs(companyCode)}`),
        ]);
        setCompany(companyRes.data?.data ?? companyRes.data);
        const jobData = jobsRes.data?.data ?? jobsRes.data;
        setJobs(Array.isArray(jobData) ? jobData : jobData?.jobs || []);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [companyCode]);

  if (loading) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;

  const primaryColor = company?.primaryColor || '#1E3A8A';

  return (
    <div className="careers-portal">
      <div className="careers-hero" style={{ borderBottom: `4px solid ${primaryColor}` }}>
        {company?.logo && <img src={company.logo} alt={company.name} className="careers-logo" />}
        <h1 style={{ color: primaryColor }}>{company?.name || companyCode}</h1>
        <p>{company?.careersTagline || 'Cơ hội nghề nghiệp — Gia nhập đội ngũ của chúng tôi'}</p>
        {company?.website && (
          <a href={company.website} target="_blank" rel="noreferrer" style={{ color: primaryColor, fontSize: 14 }}>
            {company.website}
          </a>
        )}
      </div>

      {jobs.length === 0 ? (
        <Empty description="Hiện chưa có vị trí tuyển dụng nào" />
      ) : (
        <Row gutter={[16, 16]}>
          {jobs.map((job) => (
            <Col xs={24} md={12} key={job.id}>
              <Link to={`/careers/${companyCode}/jobs/${job.id}`} className="careers-job-link">
                <Card className="careers-job-card" hoverable>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                      <h3>{job.title}</h3>
                      <div className="careers-job-meta">
                        {job.departmentName && <span><Briefcase size={14} /> {job.departmentName}</span>}
                        {job.location && <span><MapPin size={14} /> {job.location}</span>}
                      </div>
                      {job.salaryRange && <div className="careers-salary">{job.salaryRange}</div>}
                    </div>
                    <Tag color={JOB_STATUS[job.status]?.color || 'green'}>
                      {JOB_STATUS[job.status]?.label || 'Đang tuyển'}
                    </Tag>
                  </div>
                  <div className="careers-apply-hint" style={{ color: primaryColor }}>
                    Xem chi tiết <ArrowRight size={14} />
                  </div>
                </Card>
              </Link>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
};

export default CareersPortalPage;
