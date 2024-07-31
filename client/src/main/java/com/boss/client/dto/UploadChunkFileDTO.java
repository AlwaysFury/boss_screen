package com.boss.client.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadChunkFileDTO {

    /**
     * 文件传输任务ID
     * 文件MD5编码
     */
    private String identifier;

    /**
     * 文件全名称 例如：123.png
     */
    private String filename;

    /**
     * 主体类型--这个字段是我项目中的其他业务逻辑可以忽略
     */
    private String objectType;
    /**
     * 分片总数
     */
    private int totalChunks;

    /**
     * 每个分块的大小
     */
    private long chunkSize;
    /**
     * 当前为第几分片
     */
    private int chunkNumber;
    /**
     * 当前分片大小
     */
    private long currentChunkSize;

    /**
     * 分块文件传输对象
     */
    private MultipartFile file;

    /**
     * oss上传时的上传id
     */
    private String uploadId;

    /**
     * oss上传时的文件key
     */
    private String key;

}