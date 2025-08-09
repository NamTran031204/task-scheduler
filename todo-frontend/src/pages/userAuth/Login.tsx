import React from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { LOGIN_URL } from "../../constants/routeURL";
import { Form, Input, Alert, Row, Col, Typography } from "antd";
import Button from "antd/es/button";
import styled from "styled-components";
import LeftPanelLayout from "../../components/LeftPanelLayout";
import { loginUser } from "../../api/login";

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

const RegisterButton = styled(Button)`
  width: 100%;
  after::click {
    background-color: #1890ff;
    color: white;
    border: none;
  }
`;

export interface LoginInput {
  password: string;
  email: string;
}

export const useLogin = () => {
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const login = async (userData: LoginInput) => {
    try {
      await loginUser(userData);
      navigate(LOGIN_URL);
    } catch (err: any) {
      setError(err.response?.data?.message || "Lgin failed. Please try again.");
      console.error("Login error:", err);
    }
  };

  return { login, error };
};

const LoginPage: React.FC = () => {
  const [form] = Form.useForm<LoginInput>();
  const { login, error } = useLogin();

  const onFinish = (values: LoginInput) => {
    login(values);
    form.resetFields();
  };

  return (
    <>
      <Row>
        <LeftPanelLayout />
        <Col span={12}>
          <RegisterContainer>
            <h1>Welcome to <br></br>Task Scheduler</h1>
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
                Passsword:
              </Typography>
              <Form.Item
                name="password"
                rules={[{ required: true, message: "Please input your password!" }]}
              >
                <Input.Password placeholder="Password" />
              </Form.Item>
              <RegisterButton type="primary" htmlType="submit">
                Login
              </RegisterButton>
              <p style={{ marginTop: 0, textAlign: "right", justifyContent: "flex-end" }}>Don't have an account? <a href="/register">Register</a></p>
            </Form>
          </RegisterContainer>
        </Col>
      </Row>
    </>
  );
};

export default LoginPage;