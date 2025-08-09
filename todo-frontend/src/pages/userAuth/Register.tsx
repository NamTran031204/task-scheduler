import { registerUser } from "../../api/register";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { LOGIN_URL } from "../../constants/routeURL";
import { Form, Input, Alert, Row, Col, Typography } from "antd";
import Button from "antd/es/button";
import styled from "styled-components";
import LeftPanelLayout from "../../components/LeftPanelLayout";

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

const RegisterButton = styled(Button)`
  width: 100%;
`;

export interface RegisterInput {
  username: string;
  password: string;
  email: string;
  fullName: string;
}

// export interface RegisterResponse{
//   id: number;
//   username: string;
//   email: string;
//   fullName: string;
//   createAt: string;
//   updatedAt: string;
// }

export const useRegister = () => {
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const register = async (userData: RegisterInput) => {
    try {
      await registerUser(userData);
      navigate(LOGIN_URL);
    } catch (err: any) {
      setError(err.response?.data?.message || "Registration failed. Please try again.");
      console.error("Registration error:", err);
    }
  };

  return { register, error };
};

const RegisterPage: React.FC = () => {
  const [form] = Form.useForm<RegisterInput>();
  const { register, error } = useRegister();

  const onFinish = (values: RegisterInput) => {
    register(values);
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
            <Form<RegisterInput> form={form} onFinish={onFinish} className="register-form"
            >
              <Typography>
                User name:
              </Typography>
              <Form.Item
                name="username"
                rules={[{ required: true, message: "Please input your username!" }]}
              >
                <Input placeholder="Username" />
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
                Full name:
              </Typography>
              <Form.Item
                name="fullName"
                rules={[{ required: false, message: "Please input your full name!" }]}
              >
                <Input placeholder="Full Name" />
              </Form.Item>
              <RegisterButton type="primary" htmlType="submit">
                Register
              </RegisterButton>
              <p style={{ marginTop: 0, textAlign: "right", justifyContent: "flex-end" }}>
                Already have an account? <a href="/login">Login</a>
              </p>
            </Form>
          </RegisterContainer>
        </Col>
      </Row>
    </>
  );
};

export default RegisterPage;