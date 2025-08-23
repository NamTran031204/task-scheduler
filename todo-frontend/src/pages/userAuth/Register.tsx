import { registerUser } from "../../api/register";
import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { LOGIN_URL, DASHBOARD_URL } from "../../constants/routeURL";
import { Form, Input, Alert, Row, Col, Typography, message } from "antd";
import Button from "antd/es/button";
import styled from "styled-components";
import LeftPanelLayout from "../../components/layout/LeftPanelLayout";
import { useAuth } from "../../contexts/AuthContext";

const RegisterContainer = styled.div`
  width: 570px;
  padding-left: 0;
  justify-content: center;
  align-items: center;
  margin-right: 0;
  
  @media (max-width: 768px) {
    width: 90%;
    height: 90%;
  }
  @media (max-width: 1080px) {
    width: 90%;
    height: 90%;
  }
  @media (max-width: 560px) {
    padding-left: 0px;
    width: 100%;
    height: 100%;
    margin-left: -200px;
  }

  .register-form {
    display: flex;
    flex-direction: column;
    @media (max-width: 768px) {
      width: 90%;
      height: 90%;
    }
    @media (max-width: 560px) {
      width: 100%;
      height: 100%;
      margin-left: 0;
      margin-right: 0;
    }
  }
`;

export interface RegisterInput {
  username: string;
  password: string;
  email: string;
  fullName: string;
}

export const useRegister = () => {
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const register = async (userData: RegisterInput) => {
    try {
      setLoading(true);
      setError(null);
      await registerUser(userData);
      message.success('Registration successful! Please log in.');
      navigate(LOGIN_URL);
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || "Registration failed. Please try again.";
      setError(errorMsg);
      message.error(errorMsg);
      console.error("Registration error:", err);
    } finally {
      setLoading(false);
    }
  };

  return { register, error, loading };
};

const RegisterPage: React.FC = () => {
  const [form] = Form.useForm<RegisterInput>();
  const { register, error, loading } = useRegister();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate(DASHBOARD_URL);
    }
  }, [isAuthenticated, navigate]);

  const onFinish = (values: RegisterInput) => {
    register(values);
  };

  return (
    <>
      <Row>
        <LeftPanelLayout />
        <Col span={12}>
          <RegisterContainer>
            <h1>Welcome to <br />Task Scheduler🤗</h1>
            <h5 style={{marginTop: '-20px', color:"gray"}}>Register an account to use our Website!</h5>
            {error && <Alert message={error} type="error" style={{ marginBottom: 10 }} />}
            <Form<RegisterInput> form={form} onFinish={onFinish} className="register-form">
              <Typography>
                User name:
              </Typography>
              <Form.Item
                name="username"
                rules={[{ required: true, message: "Please input your username!" }]}
              >
                <Input placeholder="Username" disabled={loading} />
              </Form.Item>
              <Typography>
                Password:
              </Typography>
              <Form.Item
                name="password"
                rules={[{ required: true, message: "Please input your password!" }]}
              >
                <Input.Password placeholder="Password" disabled={loading} />
              </Form.Item>
              <Typography>
                Email:
              </Typography>
              <Form.Item
                name="email"
                rules={[
                  { required: true, message: "Please input your email!" },
                  { type: "email", message: "Please enter a valid email!" },
                ]}
              >
                <Input placeholder="Email" disabled={loading} />
              </Form.Item>
              <Typography>
                Full name:
              </Typography>
              <Form.Item
                name="fullName"
                rules={[{ required: false, message: "Please input your full name!" }]}
              >
                <Input placeholder="Full Name" disabled={loading} />
              </Form.Item>
              <Button 
                type="primary" 
                htmlType="submit" 
                loading={loading}
                style={{ width: '100%' }}
              >
                Register
              </Button>
              <div style={{ marginTop: '16px', textAlign: 'center' }}>
                Already have an account? <Link to="/login">Login</Link>
              </div>
            </Form>
          </RegisterContainer>
        </Col>
      </Row>
    </>
  );
};

export default RegisterPage;