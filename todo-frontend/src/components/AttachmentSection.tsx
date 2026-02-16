import React, { useEffect, useState } from 'react';
import { Upload, List, Button, message, Popconfirm, Space, Spin } from 'antd';
import { UploadOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons';
import { getTaskAttachments, uploadTaskAttachment, deleteTaskAttachment } from '../api/task';

interface AttachmentSectionProps {
  taskId: number;
}

const AttachmentSection: React.FC<AttachmentSectionProps> = ({ taskId }) => {
  const [attachments, setAttachments] = useState<any[]>([]);
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(false);

  const fetchAttachments = async () => {
    setLoading(true);
    try {
      const data = await getTaskAttachments(taskId);
      setAttachments(data.content || []);
    } catch {
      message.error('Không lấy được file đính kèm');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (taskId) fetchAttachments();
  }, [taskId]);

  const handleUpload = async ({ file }: any) => {
    setUploading(true);
    try {
      await uploadTaskAttachment(taskId, file);
      message.success('Upload thành công!');
      fetchAttachments();
    } catch {
      message.error('Upload thất bại!');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (attachmentId: number) => {
    setLoading(true);
    try {
      await deleteTaskAttachment(taskId, attachmentId);
      message.success('Đã xóa file!');
      fetchAttachments();
    } catch {
      message.error('Xóa file thất bại!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ marginTop: 24 }}>
      <Space direction="vertical" style={{ width: '100%' }}>
        <Upload customRequest={handleUpload} showUploadList={false} disabled={uploading}>
          <Button icon={<UploadOutlined />} loading={uploading}>
            Upload file đính kèm
          </Button>
        </Upload>
        <Spin spinning={loading}>
          <List
            header={<b>File đính kèm</b>}
            dataSource={attachments}
            locale={{ emptyText: 'Không có file đính kèm' }}
            renderItem={item => (
              <List.Item
                actions={[
                  <a href={item.url} target="_blank" rel="noopener noreferrer">
                    <Button icon={<DownloadOutlined />} size="small">Tải về</Button>
                  </a>,
                  <Popconfirm title="Xóa file này?" onConfirm={() => handleDelete(item.id)} okText="Xóa" cancelText="Hủy">
                    <Button icon={<DeleteOutlined />} size="small" danger>Xóa</Button>
                  </Popconfirm>
                ]}
              >
                <List.Item.Meta title={item.name} description={item.size ? `${(item.size / 1024).toFixed(1)} KB` : ''} />
              </List.Item>
            )}
          />
        </Spin>
      </Space>
    </div>
  );
};

export default AttachmentSection;
