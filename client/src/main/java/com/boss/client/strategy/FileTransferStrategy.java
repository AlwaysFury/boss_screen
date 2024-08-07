package com.boss.client.strategy;

import com.boss.client.dto.UploadChunkFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 上传策略
 */
public interface FileTransferStrategy {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 上传路径
     * @return {@link String} 文件地址
     */
    Map<String, String> uploadFile(MultipartFile file, String path);

    Map<String, Object> uploadChunkFile(UploadChunkFileDTO uploadChunkFileDTO);

    Map<String, String> getUploadId(String key);

    /**
     * 下载文件
     *
     * @param path
     * @param fileName
     */
    InputStream downloadFile(String path, String fileName);

    /**
     * 批量删除
     * @param fileNames
     */
    void deleteFileBatch(List<String> fileNames);

}
