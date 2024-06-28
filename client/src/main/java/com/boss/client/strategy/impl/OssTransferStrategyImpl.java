package com.boss.client.strategy.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.boss.client.cofig.OssConfigProperties;
import com.boss.common.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * oss上传策略
 */
@Service("ossUploadStrategyImpl")
public class OssTransferStrategyImpl extends AbstractTransferStrategyImpl {
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
    public void multipartUpload(String path, String fileName, MultipartFile file) throws Exception {
        OSS ossClient = getOssClient();
        String objectName = path + fileName;
        String bucketName = ossConfigProperties.getBucketName();
        // 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
        // 如果需要在初始化分片时设置文件存储类型，请参考以下示例代码
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // request.setObjectMetadata(metadata);
        // 初始化分片。
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upresult.getUploadId();
        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags = new ArrayList<>();
        // 计算文件有多少个分片。
        final long partSize = 20 * 1024 * 1024L;   // 1MB
        final File sampleFile = FileUtils.multipartFileToFile(file);
        long fileLength = sampleFile.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }
        // 遍历分片上传。
        for (int i = 0; i < partCount; i++) {
            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
            InputStream inStream = new FileInputStream(sampleFile);
            InputStream inStream1 = new FileInputStream(sampleFile);
            // 跳过已经上传的分片。
            inStream.skip(startPos);
            inStream1.skip(startPos);
            String md5;
            if(i == partCount-1){
                //注意最后一个分片读取的是到文件尾部的数据，非一个分片的大小
                md5 = md5(inStream1,fileLength - startPos);
            }else{
                md5 = md5(inStream1, partSize);
            }
            //instream1.skip(n)
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(objectName);
            uploadPartRequest.setUploadId(uploadId);
            uploadPartRequest.setInputStream(inStream);
            uploadPartRequest.setMd5Digest(md5);
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
            uploadPartRequest.setPartSize(curPartSize);
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
            uploadPartRequest.setPartNumber(i + 1);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            //System.out.println("server md5" +uploadPartResult.getETag());
            // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
            partETags.add(uploadPartResult.getPartETag());
        }
        // 创建CompleteMultipartUploadRequest对象。
        // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
        // 如果需要在完成文件上传的同时设置文件访问权限，请参考以下示例代码。
        // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.PublicRead);
        // 完成上传。
        ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        // 关闭OSSClient。
        ossClient.shutdown();
    }

    private static String md5(InputStream in , long length1) throws Exception{
        byte[] bytes = new byte[(int) length1];

        long length_tmp = length1;

        in.read(bytes, 0, (int) length_tmp);

        return BinaryUtil.toBase64String(BinaryUtil.calculateMd5(bytes));
    }

    @Override
    public String getFileAccessUrl(String filePath) {
        return ossConfigProperties.getUrl() + filePath;
    }

    @Override
    public InputStream download(String path, String fileName) {
        return getOssClient().getObject(new GetObjectRequest(ossConfigProperties.getBucketName(), path + fileName)).getObjectContent();
    }

    @Override
    public void deleteBatch(List<String> fileNames) {
        getOssClient().deleteObjects(new DeleteObjectsRequest(ossConfigProperties.getBucketName()).withKeys(fileNames).withEncodingType("url")).getDeletedObjects();
    }

    /**
     * 获取ossClient
     *
     * @return {@link OSS} ossClient
     */
    private OSS getOssClient() {
        return new OSSClientBuilder().build(ossConfigProperties.getEndpoint(), ossConfigProperties.getAccessKeyId(), ossConfigProperties.getAccessKeySecret());
    }

}
