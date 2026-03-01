package com.spider.apigateway.doc;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.OffsetDateTime;
import java.util.UUID;

@TableName("product_doc_version")
public class ProductDocVersionEntity {
    @TableId(type = IdType.INPUT)
    private UUID id;
    @TableField("doc_id")
    private UUID docId;
    @TableField("version_no")
    private Integer versionNo;
    @TableField("parent_version_id")
    private UUID parentVersionId;
    private String content;
    @TableField("change_summary")
    private String changeSummary;
    @TableField("source_type")
    private String sourceType;
    @TableField("created_by")
    private String createdBy;
    @TableField("created_at")
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDocId() {
        return docId;
    }

    public void setDocId(UUID docId) {
        this.docId = docId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public UUID getParentVersionId() {
        return parentVersionId;
    }

    public void setParentVersionId(UUID parentVersionId) {
        this.parentVersionId = parentVersionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
