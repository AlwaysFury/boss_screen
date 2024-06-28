package com.boss.client.strategy.context;

import com.boss.client.strategy.FileTransferStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.boss.common.enums.UploadModeEnum.getStrategy;


/**
 * 上传策略上下文
 */
@Service
public class FileTransferStrategyContext {
    /**
     * 上传模式
     */
    @Value("${upload.mode}")
    private String uploadMode;

    @Autowired
    private Map<String, FileTransferStrategy> uploadStrategyMap;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 路径
     * @return {@link String} 文件地址
     */
    public Map<String, String> executeUploadStrategy(MultipartFile file, String path) {
        return uploadStrategyMap.get(getStrategy(uploadMode)).uploadFile(file, path);
    }

    /**
     * 下载文件
     *
     * @param path      路径
     * @param fileName  文件名
     */
    public InputStream executeDownloadStrategy(String path, String fileName) {
        return uploadStrategyMap.get(getStrategy(uploadMode)).downloadFile(path, fileName);
    }

    /**
     * 删除文件
     * @param fileName
     */
    public void executeDeleteStrategy(List<String> fileName) {
        uploadStrategyMap.get(getStrategy(uploadMode)).deleteFileBatch(fileName);
    }


}
