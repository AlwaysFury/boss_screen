package com.boss.bossscreen.strategy.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import com.boss.bossscreen.cofig.OssConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.InputStream;

/**
 * oss上传策略
 */
//@Service("ossUploadStrategyImpl")
public class OssUploadStrategyImpl extends AbstractUploadStrategyImpl {
    @Autowired
    private OssConfigProperties ossConfigProperties;

    @Override
    public Boolean exists(String filePath) {
        return getOssClient().doesObjectExist(ossConfigProperties.getBucketName(), filePath);
    }

    @Override
    public void upload(String path, String fileName, InputStream inputStream) {
        getOssClient().putObject(ossConfigProperties.getBucketName(), path + fileName, inputStream);
    }

    @Override
    public String getFileAccessUrl(String filePath) {
        return ossConfigProperties.getUrl() + filePath;
    }

    /**
     * 获取ossClient
     *
     * @return {@link OSS} ossClient
     */
    private OSS getOssClient() {
        return new OSSClientBuilder().build(ossConfigProperties.getEndpoint(), ossConfigProperties.getAccessKeyId(), ossConfigProperties.getAccessKeySecret());
    }

    public static void main(String[] args) {
        String endpoint = "<您的OSS服务访问域名>";
        String accessKeyId = "<您的AccessKeyId>";
        String accessKeySecret = "<您的AccessKeySecret>";
        String bucketName = "<您的存储空间名称>";
        String objectKey = "<您要下载的文件对象键>";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 下载OSS中的文件到本地。
            ossClient.getObject(new GetObjectRequest(bucketName, objectKey), new File("<本地保存的文件路径>"));
            System.out.println("文件下载成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("文件下载失败");
        } finally {
            // 关闭OSSClient。
            ossClient.shutdown();
        }
    }

}
