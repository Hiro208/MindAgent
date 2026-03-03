import React, { useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Card,
  Typography,
  Button,
  Upload,
  Table,
  Popconfirm,
  Space,
  message,
  Empty,
} from "antd";
import {
  BookOutlined,
  UploadOutlined,
  DeleteOutlined,
  FileOutlined,
} from "@ant-design/icons";
import type { UploadProps } from "antd";
import { useKnowledgeBases } from "../../hooks/useKnowledgeBases.ts";
import { useDocuments } from "../../hooks/useDocuments.ts";
import { uploadDocument, type DocumentVO } from "../../api/api.ts";

const { Title, Text, Paragraph } = Typography;

const KnowledgeBaseView: React.FC = () => {
  const { knowledgeBaseId } = useParams<{ knowledgeBaseId?: string }>();
  const { knowledgeBases } = useKnowledgeBases();
  const { documents, loading, refreshDocuments, deleteDocument } =
    useDocuments(knowledgeBaseId);

  const [uploading, setUploading] = useState(false);

  // 查找当前知识库的详细信息
  const currentKnowledgeBase = useMemo(() => {
    if (!knowledgeBaseId) return null;
    return (
      knowledgeBases.find((kb) => kb.knowledgeBaseId === knowledgeBaseId) ||
      null
    );
  }, [knowledgeBaseId, knowledgeBases]);

  // 处理文件上传
  const handleUpload: UploadProps["customRequest"] = async (options) => {
    const { file, onSuccess, onError } = options;

    if (!knowledgeBaseId) {
      message.error("Please select a knowledge base first.");
      return;
    }

    setUploading(true);

    try {
      await uploadDocument(knowledgeBaseId, file as File);
      message.success("Document uploaded.");
      await refreshDocuments();
      onSuccess?.(file);
    } catch (error) {
      message.error(error instanceof Error ? error.message : "Upload failed.");
      onError?.(error as Error);
    } finally {
      setUploading(false);
    }
  };

  // 格式化文件大小
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
  };

  // Table columns
  const columns = [
    {
      title: "File name",
      dataIndex: "filename",
      key: "filename",
      render: (text: string) => (
        <Space>
          <FileOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: "Type",
      dataIndex: "filetype",
      key: "filetype",
      width: 120,
    },
    {
      title: "Size",
      dataIndex: "size",
      key: "size",
      width: 120,
      render: (size: number) => formatFileSize(size),
    },
    {
      title: "Actions",
      key: "action",
      width: 100,
      render: (_: unknown, record: DocumentVO) => (
        <Popconfirm
          title="Delete this document?"
          description="This action cannot be undone."
          onConfirm={() => deleteDocument(record.id)}
          okText="Delete"
          cancelText="Cancel"
        >
          <Button type="text" danger icon={<DeleteOutlined />} size="small">
            删除
          </Button>
        </Popconfirm>
      ),
    },
  ];

  // When no knowledge base is selected
  if (!knowledgeBaseId) {
    return (
      <div className="flex flex-col h-full items-center justify-center p-6">
        <Empty
          image={<BookOutlined className="text-6xl text-gray-300" />}
          description={
            <div className="mt-4">
              <Title level={4} type="secondary">
                No knowledge base selected
              </Title>
              <Text type="secondary" className="text-sm">
                Please select a knowledge base from the left list.
              </Text>
            </div>
          }
        />
      </div>
    );
  }

  // Knowledge base not found
  if (!currentKnowledgeBase) {
    return (
      <div className="flex flex-col h-full items-center justify-center p-6">
        <Empty
          description={
            <div className="mt-4">
              <Title level={4} type="secondary">
                Knowledge base not found
              </Title>
              <Text type="secondary" className="text-sm">
                Please check whether the ID is correct.
              </Text>
            </div>
          }
        />
      </div>
    );
  }

  // 显示知识库详情和文档列表
  return (
    <div className="flex flex-col h-full p-6 overflow-y-auto">
      <div className="max-w-6xl w-full mx-auto">
        <div className="mb-3">
          <Card>
            <div className="flex items-start gap-4">
              <div className="w-16 h-16 rounded-lg bg-gradient-to-br from-blue-200 to-purple-200 flex items-center justify-center text-3xl shrink-0">
                <BookOutlined />
              </div>
              <div className="flex-1">
                <Title level={3} className="mb-2">
                  {currentKnowledgeBase.name}
                </Title>
                {currentKnowledgeBase.description && (
                  <Paragraph className="text-gray-600 mb-0">
                    {currentKnowledgeBase.description}
                  </Paragraph>
                )}
                <Text type="secondary" className="text-sm">
                  Knowledge base ID: {currentKnowledgeBase.knowledgeBaseId}
                </Text>
              </div>
            </div>
          </Card>
        </div>
        {/* 知识库信息卡片 */}

        <div className="mb-3">
          {/* 上传文档区域 */}
          <Card title="Upload documents">
            <Upload
              customRequest={handleUpload}
              showUploadList={false}
              accept=".md"
              disabled={uploading}
            >
              <Button
                type="primary"
                icon={<UploadOutlined />}
                loading={uploading}
                size="large"
              >
                Select Markdown file
              </Button>
            </Upload>
            <Text type="secondary" className="block mt-2 text-xs">
              Supported format: Markdown
            </Text>
          </Card>
        </div>

        <div className="mb-3">
          {/* 文档列表 */}
          <Card title={`Documents (${documents.length})`}>
            {loading ? (
              <div className="text-center py-8">
                <Text type="secondary">Loading...</Text>
              </div>
            ) : documents.length === 0 ? (
              <Empty
                description={<Text type="secondary">No documents yet. Please upload.</Text>}
              />
            ) : (
              <Table
                columns={columns}
                dataSource={documents}
                rowKey="id"
                pagination={{
                  pageSize: 10,
                  showTotal: (total) => `Total ${total}`,
                }}
              />
            )}
          </Card>
        </div>
      </div>
    </div>
  );
};

export default KnowledgeBaseView;
