import React from 'react';
import { Modal, Form, Input, Select, DatePicker, Button, Tabs, Spin } from 'antd';
import type { FC } from 'react';
import { lazy, Suspense } from 'react';

const TaskHistory = lazy(() => import('./TaskList/TaskHistory'));


interface TaskModalProps {
  open: boolean;
  onOk: (values: any) => void;
  onCancel: () => void;
  initialValues?: any;
  mode?: 'create' | 'edit';
}

const { Option } = Select;

const TaskModal: FC<TaskModalProps> = ({ open, onOk, onCancel, initialValues = {}, mode = 'create' }) => {
  const [form] = Form.useForm();

  React.useEffect(() => {
    if (open) {
      form.setFieldsValue(initialValues);
    } else {
      form.resetFields();
    }
  }, [open, initialValues, form]);

  const handleOk = () => {
    form.submit();
  };

  const formItems = (
    <Form
      form={form}
      layout="vertical"
      initialValues={initialValues}
      onFinish={onOk}
    >
      <Form.Item
        name="title"
        label="Tên công việc"
        rules={[{ required: true, message: 'Vui lòng nhập tên công việc' }]}
      >
        <Input />
      </Form.Item>
      <Form.Item
        name="description"
        label="Mô tả"
      >
        <Input.TextArea rows={4} />
      </Form.Item>
      <Form.Item
        name="priority"
        label="Mức độ ưu tiên"
        initialValue="MEDIUM"
      >
        <Select>
          <Option value="LOW">Thấp</Option>
          <Option value="MEDIUM">Trung bình</Option>
          <Option value="HIGH">Cao</Option>
          <Option value="URGENT">Khẩn cấp</Option>
        </Select>
      </Form.Item>
      <Form.Item
        name="due_date"
        label="Hạn chót"
      >
        <DatePicker showTime format="YYYY-MM-DD HH:mm" style={{ width: '100%' }} />
      </Form.Item>
      <Form.Item
        name="recurrence"
        label="Lặp lại"
        initialValue="NONE"
      >
        <Select>
          <Option value="NONE">Không lặp</Option>
          <Option value="DAILY">Hàng ngày</Option>
          <Option value="WEEKLY">Hàng tuần</Option>
          <Option value="MONTHLY">Hàng tháng</Option>
        </Select>
      </Form.Item>
    </Form>
  );

  const items = [
    {
      key: 'details',
      label: 'Chi tiết',
      children: formItems,
    },
  ];

  if (mode === 'edit' && initialValues?.id) {
    items.push({
      key: 'history',
      label: 'Lịch sử',
      children: (
        <Suspense fallback={<Spin />}>
          <TaskHistory taskId={initialValues.id} />
        </Suspense>
      ),
    });
  }

  return (
    <Modal
      title={mode === 'create' ? 'Thêm công việc' : 'Sửa công việc'}
      open={open}
      onOk={handleOk}
      onCancel={onCancel}
      width={800}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          Hủy
        </Button>,
        <Button key="submit" type="primary" onClick={handleOk}>
          {mode === 'create' ? 'Thêm' : 'Lưu'}
        </Button>,
      ]}
    >
      <Tabs defaultActiveKey="details" items={items} />
    </Modal>
  );
};

export default TaskModal;
