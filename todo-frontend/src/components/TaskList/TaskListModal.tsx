import React from 'react';
import { Modal, Form, Input, Button } from 'antd';

interface TaskListModalProps {
  open: boolean;
  onOk: (values: any) => void;
  onCancel: () => void;
  initialValues?: any;
  mode?: 'create' | 'edit';
}

const TaskListModal: React.FC<TaskListModalProps> = ({ open, onOk, onCancel, initialValues = {}, mode = 'create' }) => {
  const [form] = Form.useForm();

  React.useEffect(() => {
    if (open) {
      form.setFieldsValue(initialValues);
    } else {
      form.resetFields();
    }
  }, [open, initialValues, form]);

  return (
    <Modal
      open={open}
      title={mode === 'create' ? 'Thêm danh sách' : 'Sửa danh sách'}
      onOk={() => form.submit()}
      onCancel={onCancel}
      okText={mode === 'create' ? 'Thêm' : 'Lưu'}
      cancelText="Hủy"
    >
      <Form form={form} layout="vertical" onFinish={onOk} initialValues={initialValues}>
        <Form.Item name="name" label="Tên danh sách" rules={[{ required: true, message: 'Vui lòng nhập tên danh sách' }]}> 
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Mô tả">
          <Input.TextArea rows={2} />
        </Form.Item>
        <Form.Item name="color" label="Màu sắc">
          <Input type="color" style={{ width: 40, padding: 0, border: 'none', background: 'none' }} />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" block>
            {mode === 'create' ? 'Thêm danh sách' : 'Lưu thay đổi'}
          </Button>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default TaskListModal;
