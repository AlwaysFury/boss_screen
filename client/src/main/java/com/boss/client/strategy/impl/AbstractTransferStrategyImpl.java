package com.boss.client.strategy.impl;


import cn.hutool.core.io.FileUtil;
import com.boss.client.dto.UploadChunkFileDTO;
import com.boss.client.exception.BizException;
import com.boss.client.strategy.FileTransferStrategy;
import com.boss.common.enums.FilePathEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 抽象上传模板
 */
@Service
@Slf4j
public abstract class AbstractTransferStrategyImpl implements FileTransferStrategy {

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    @Override
    public Map<String, String> uploadFile(MultipartFile file, String path) {
        try {
            //获取文件名
//            final long LIMITSIZE = 50 * 1024 * 1024L;
            String fileName = FileUtil.getName(file.getOriginalFilename());
//            if (file.getSize() >= LIMITSIZE) {
                log.info("文件开始上传");
                multipartUpload(path, fileName, file);

//            } else {
//                log.info("文件开始上传");
//                upload(path, fileName, file.getInputStream());
//            }

            Map<String, String> map = new HashMap<>();
            map.put("name", FileUtil.getName(file.getOriginalFilename()));
            map.put("src", getFileAccessUrl(path + fileName));

            log.info("文件结束上传");

            // 返回文件访问路径
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("文件上传失败");
        }
    }

    public Map<String, Object> uploadChunkFile(UploadChunkFileDTO uploadChunkFileDTO) {
        log.info("文件开始分片上传");
        Map<String, Object> map = new HashMap<>();
        map.put("name", uploadChunkFileDTO.getFilename());
        map.put("src", getFileAccessUrl(FilePathEnum.PHOTO.getPath() + uploadChunkFileDTO.getFilename()));
        uploadChunk(uploadChunkFileDTO);
//        log.info("文件结束上传");
        return map;
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    public abstract Boolean exists(String filePath);

    public Map<String, String> getUploadId(String key) {
        String id = uploadChunkInit(key);
        Map<String, String> map = new HashMap<>();
        map.put("uploadId", id);
        map.put("name", key);
        map.put("src", getFileAccessUrl(FilePathEnum.PHOTO.getPath() + key));

        return map;
    }

    /**
     * 上传
     *
     * @param path        路径
     * @param fileName    文件名
     * @param inputStream 输入流
     * @throws IOException io异常
     */
    public abstract void upload(String path, String fileName, InputStream inputStream) throws IOException;

    /**
     * 分片上传
     */
    public abstract void multipartUpload(String path, String fileName, MultipartFile file) throws Exception;

    public abstract Map<String, Object> uploadChunk(UploadChunkFileDTO uploadChunkFileDTO);

    public abstract String uploadChunkInit(String key);

    /**
     * 获取文件访问url
     *
     * @param filePath 文件路径
     * @return {@link String}
     */
    public abstract String getFileAccessUrl(String filePath);


    public abstract InputStream download(String path, String fileName);

    public abstract void deleteBatch(List<String> fileNames);

    /**
     * 下载
     *
     * @param path     路径
     * @param fileName 文件名
     */
    @Override
    public InputStream downloadFile(String path, String fileName) {
        try {
            if (!exists(path + fileName)) {
                throw new BizException("文件不存在");
            }
            return download(path, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("文件下载失败");
        }
    }

    @Override
    public void deleteFileBatch(List<String> fileNames) {
        try {
            List<String> newFileNames = new ArrayList<>();
            for (String fileName : fileNames) {
                newFileNames.add(FilePathEnum.PHOTO.getPath() + fileName);
            }
            deleteBatch(newFileNames);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("文件删除失败");
        }
    }

}
