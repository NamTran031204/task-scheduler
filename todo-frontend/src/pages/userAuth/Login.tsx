import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { DASHBOARD_URL } from "../../constants/routeURL";
import { Form, Input, Alert, Row, Col, Typography, message } from "antd";
import Button from "antd/es/button";
import styled from "styled-components";
import LeftPanelLayout from "../../components/layout/LeftPanelLayout";
import { loginUser } from "../../api/login";
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

  .login-form {
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
      .h1{
        font-size: 10px;}
    }
  }
`;

// const RegisterButton = styled(Button)`
//   width: 100%;
//   after::click {
//     background-color: #1890ff;
//     color: white;
//     border: none;
//   }
// `;

export interface LoginInput {
  password: string;
  email: string;
}

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const { login, isAuthenticated } = useAuth();

  // If already authenticated, redirect to dashboard
  useEffect(() => {
    if (isAuthenticated) {
      navigate(DASHBOARD_URL);
    }
  }, [isAuthenticated, navigate]);

  const onFinish = async (values: LoginInput) => {
    try {
      setLoading(true);
      setError("");
      const response = await loginUser(values);
console.log('login response', response);

      login(response.accessToken, String(response.userId));
      message.success('Login successful!');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Failed to login. Please check your credentials.';
      setError(errorMessage);
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Row>
        <LeftPanelLayout />
        <Col span={12}>
          <RegisterContainer>
            <h1>Welcome to <br></br>Task Scheduler😘</h1>
            <h5 style={{marginTop: '-20px', color:"gray"}}>Login to continue!</h5>
            {error && <Alert message={error} type="error" style={{ marginBottom: 10 }} />}
            <Form<LoginInput> form={form} onFinish={onFinish} className="login-form"
            >
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
                <Input placeholder="Email" />
              </Form.Item>
              <Typography>
                Password:
              </Typography>
              <Form.Item
                name="password"
                rules={[{ required: true, message: "Please input your password!" }]}
              >
                <Input.Password placeholder="Password" />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading} block>
                  Login
                </Button>
              </Form.Item>
              <div style={{ textAlign: 'center', marginTop: '16px' }}>
                Don't have an account? <Link to="/register">Register</Link>
              </div>
            </Form>
          </RegisterContainer>
        </Col>
      </Row>
    </>
  );
};

export default LoginPage;