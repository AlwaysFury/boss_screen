package com.boss.client.strategy.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.boss.client.cofig.OssConfigProperties;
import com.boss.client.dto.UploadChunkFileDTO;
import com.boss.client.exception.BizException;
import com.boss.client.service.impl.RedisServiceImpl;
import com.boss.common.constant.RedisPrefixConst;
import com.boss.common.enums.FilePathEnum;
import com.boss.common.util.CommonUtil;
import com.boss.common.util.FileUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * oss上传策略
 */
@Service("ossUploadStrategyImpl")
public class OssTransferStrategyImpl extends AbstractTransferStrategyImpl {
    @Autowired
    private OssConfigProperties ossConfigProperties;

    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    private static OSS ossClient = null;

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
        long startTime = System.currentTimeMillis();

        OSS ossClient = getOssClient();
        String objectName = path + fileName;
        String bucketName = ossConfigProperties.getBucketName();
        File sampleFile = FileUtils.multipartFileToFile(file);
        try {
            // 创建InitiateMultipartUploadRequest对象。
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);

            // 如果需要在初始化分片时设置请求头，请参考以下示例代码。
//            ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // 指定该Object的网页缓存行为。
            // metadata.setCacheControl("no-cache");
            // 指定该Object被下载时的名称。
            // metadata.setContentDisposition("attachment;filename=oss_MultipartUpload.txt");
            // 指定该Object的内容编码格式。
            // metadata.setContentEncoding(OSSConstants.DEFAULT_CHARSET_NAME);
            // 指定初始化分片上传时是否覆盖同名Object。此处设置为true，表示禁止覆盖同名Object。
            // metadata.setHeader("x-oss-forbid-overwrite", "true");
            // 指定上传该Object的每个part时使用的服务器端加密方式。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
            // 指定Object的加密算法。如果未指定此选项，表明Object使用AES256加密算法。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_DATA_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
            // 指定KMS托管的用户主密钥。
            // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION_KEY_ID, "9468da86-3509-4f8d-a61e-6eab1eac****");
            // 指定Object的存储类型。
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard);
            // 指定Object的对象标签，可同时设置多个标签。
            // metadata.setHeader(OSSHeaders.OSS_TAGGING, "a:1");
            // request.setObjectMetadata(metadata);

            // 根据文件自动设置ContentType。如果不设置，ContentType默认值为application/oct-srream。
//            if (metadata.getContentType() == null) {
//                metadata.setContentType(Mimetypes.getInstance().getMimetype(new File(filePath), objectName));
//            }

            // 初始化分片。
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId。
            String uploadId = upresult.getUploadId();
            // 根据uploadId执行取消分片上传事件或者列举已上传分片的操作。
            // 如果您需要根据uploadId执行取消分片上传事件的操作，您需要在调用InitiateMultipartUpload完成初始化分片之后获取uploadId。
            // 如果您需要根据uploadId执行列举已上传分片的操作，您需要在调用InitiateMultipartUpload完成初始化分片之后，且在调用CompleteMultipartUpload完成分片上传之前获取uploadId。
            // System.out.println(uploadId);

            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<CompletableFuture<PartETag>> futures = new ArrayList<>();
            List<PartETag> partETags =  new ArrayList<PartETag>();
            // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
            final long partSize = 20 * 1024 * 1024L;   //1 MB。

            // 根据上传的数据大小计算分片数。以本地文件为例，说明如何通过File.length()获取上传数据的大小。
//            final File sampleFile = new File(filePath);
            long fileLength = sampleFile.length();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            ExecutorService executor = Executors.newFixedThreadPool(10);
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                // 设置上传的分片流。
                // 以本地文件为例说明如何创建FIleInputstream，并通过InputStream.skip()方法跳过指定数据。
                InputStream instream = new FileInputStream(sampleFile);
                instream.skip(startPos);
                uploadPartRequest.setInputStream(instream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
                uploadPartRequest.setPartNumber(i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                CompletableFuture<PartETag> future = CompletableFuture.supplyAsync(() -> {
                    UploadPartResult result = ossClient.uploadPart(uploadPartRequest);
                    return result.getPartETag();
                });
                futures.add(future);
//                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
//                partETags.add(uploadPartResult.getPartETag());
            }

            for (CompletableFuture<PartETag> future : futures) {
                try {
                    PartETag partETag = future.get();
                    partETags.add(partETag);
                } catch (Exception e) {
                    // Handle exceptions here, e.g., log them or take corrective actions
                    System.err.println("Failed to upload a part: " + e.getMessage());
                    // You might want to abort the multipart upload and clean up resources
                    // if any critical error occurs.
                }
            }


            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

            // 如果需要在完成分片上传的同时设置文件访问权限，请参考以下示例代码。
            // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.Private);
            // 指定是否列举当前UploadId已上传的所有Part。仅在Java SDK为3.14.0及以上版本时，支持通过服务端List分片数据来合并完整文件时，将CompleteMultipartUploadRequest中的partETags设置为null。
            // Map<String, String> headers = new HashMap<String, String>();
            // 如果指定了x-oss-complete-all:yes，则OSS会列举当前UploadId已上传的所有Part，然后按照PartNumber的序号排序并执行CompleteMultipartUpload操作。
            // 如果指定了x-oss-complete-all:yes，则不允许继续指定body，否则报错。
            // headers.put("x-oss-complete-all","yes");
            // completeMultipartUploadRequest.setHeaders(headers);

            // 完成分片上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            System.out.println(completeMultipartUploadResult.getETag());
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        System.out.println("耗时：" + (System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * 分片上传
     *
     * @param param 上传参数
     * @return
     */
    @Override
    public Map uploadChunk(UploadChunkFileDTO param) {
        if (ObjectUtil.isEmpty(param.getKey())) {
            String key = getKey(null, param.getIdentifier(), param.getFilename());
            param.setKey(key);
        }
        return uploadChunk(param.getUploadId(), param.getKey(), param.getFile(), param.getChunkNumber(),
                param.getCurrentChunkSize(), param.getTotalChunks());
    }

    /**
     * 分片上传
     * 1、检查文件是否上传
     * 2、检查文件是否第一次上传，第一次上传创建上传id uploadId
     * 3、检查是否是断点续传，如果是返回已上传的分片
     * 4、分片上传到阿里云OSS上，并记录上传信息到Redis
     * 5、判断是否已上传完成，已完成：合并所有分片为源文件
     *
     * @param uploadId   上传id
     * @param key        文件在OSS上的key
     * @param file       文件分片
     * @param chunkIndex 分片索引
     * @param chunkSize  分片大小
     * @param chunkCount 总分片数
     * @return
     */
    public Map uploadChunk(String uploadId, String key, MultipartFile file, Integer chunkIndex,
                           long chunkSize, Integer chunkCount) {
        if (ObjectUtil.isEmpty(key)) {
            key = getKey(FilePathEnum.PHOTO.getPath(), null, FileUtil.getSuffix(file.getOriginalFilename()));
        }
        ossClient = getOssClient();
        try {
            Map<String, Object> map = MapUtil.newHashMap();
            // 判断是否上传
            if (checkExist(key)) {
                map.put("skipUpload", true);
                map.put("url", getUrl(key));
                return map;
            }
            // 判断是否第一次上传
            if (StringUtils.isBlank(uploadId)) {
                uploadId = uploadChunkInit(file, key);
                map.put("skipUpload", false);
                map.put("uploadId", uploadId);
                map.put("uploaded", null);
                return map;
            }
//            RedisManager redisService = initRedisManager();
            // 检查分片是否已上传 实现断点续传
            if (file == null) {
                Map<String, String> uploadedCache = (Map<String, String>) redisService.hGet(RedisPrefixConst.ALI_OSS_KEY + uploadId, chunkIndex + ",");
                List<Integer> uploaded = Lists.newArrayList();
                for (Map.Entry<String, String> entry : uploadedCache.entrySet()) {
                    uploaded.add(JSONUtil.toBean(entry.getValue(), PartETag.class).getPartNumber());
                }
                map.put("skipUpload", false);
                map.put("uploadId", uploadId);
                map.put("uploaded", uploaded);
                return map;
            }
            // 上传分片
            PartETag partETag = uploadChunkPart(uploadId, key, file.getInputStream(), chunkIndex, chunkSize, chunkCount);
            // 分片上传完成缓存key
            redisService.hSet(RedisPrefixConst.ALI_OSS_KEY + uploadId, chunkIndex + ",", JSONUtil.toJsonStr(partETag));
            // 取出所有已上传的分片信息
            Map<String, String> dataMap = (Map<String, String>) redisService.hGet(RedisPrefixConst.ALI_OSS_KEY + uploadId, chunkIndex + ",");
            List<PartETag> partETagList = Lists.newArrayList();
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                partETagList.add(JSONUtil.toBean(entry.getValue(), PartETag.class));
            }
            // 判断是否上传完成
            if (dataMap.keySet().size() == chunkCount) {
                uploadChunkComplete(uploadId, key, partETagList);
                for (String mapKey : dataMap.keySet()) {
                    redisService.hDel(RedisPrefixConst.ALI_OSS_KEY + uploadId, mapKey);
                }
                map.put("skipUpload", true);
                map.put("uploadId", uploadId);
                map.put("url", getUrl(key));
            } else {
                List<Integer> list = partETagList.stream().map(PartETag::getPartNumber).collect(Collectors.toList());
                map.put("uploaded", list);
                map.put("skipUpload", false);
                map.put("uploadId", uploadId);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("上传失败：" + e.getMessage());
        }

    }

    /**
     * 上传分片文件
     *
     * @param uploadId   上传id
     * @param key        key
     * @param instream   文件分片流
     * @param chunkIndex 分片索引
     * @param chunkSize  分片大小
     * @return
     */
    public PartETag uploadChunkPart(String uploadId, String key, InputStream instream,
                                    Integer chunkIndex, long chunkSize, Integer chunkCount) {
        ossClient = getOssClient();
        try {
            UploadPartRequest partRequest = new UploadPartRequest();
            // 阿里云 oss 文件根目录
            partRequest.setBucketName(ossConfigProperties.getBucketName());
            // 文件key
            partRequest.setKey(key);
            // 分片上传uploadId
            partRequest.setUploadId(uploadId);
            // 分片文件
            partRequest.setInputStream(instream);
            // 分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
            partRequest.setPartSize(chunkSize);
            System.out.println(chunkSize + "    " + chunkIndex + "   " + uploadId);
            // 分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
            partRequest.setPartNumber(chunkIndex);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(partRequest);
            // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在redis中。
            return uploadPartResult.getPartETag();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("分片上传失败：" + e.getMessage());
        }
    }

    /**
     * 文件合并
     *
     * @param uploadId  上传id
     * @param key       key
     * @param chunkTags 分片上传信息
     * @return
     */
    public CompleteMultipartUploadResult uploadChunkComplete(String uploadId, String key, List<PartETag> chunkTags) {
        ossClient = getOssClient();
        try {
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(ossConfigProperties.getBucketName(), key, uploadId, chunkTags);
            CompleteMultipartUploadResult result = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("分片合并失败：" + e.getMessage());
        }
    }

    /**
     * 初始化上传id uploadId
     *
     * @param key
     * @return
     */
    public String uploadChunkInit(String key) {
        if (ObjectUtil.isEmpty(key)) {
            throw new BizException("key不能为空");
        }
        ossClient = getOssClient();
        try {
            // 创建分片上传对象
            InitiateMultipartUploadRequest uploadRequest = new InitiateMultipartUploadRequest(ossConfigProperties.getBucketName(), key);
            // 初始化分片
            InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(uploadRequest);
            // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
            return result.getUploadId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("初始化分片失败：" + e.getMessage());
        }
    }

    /**
     * 初始化分片上传
     *
     * @param key
     * @return 分片上传的uploadId
     */
    public String uploadChunkInit(MultipartFile file, String key) {
        if (ObjectUtil.isEmpty(key)) {
            key = getKey(FilePathEnum.PHOTO.getPath(), null, FileUtil.getSuffix(file.getOriginalFilename()));
        }
        return uploadChunkInit(key);
    }

    /**
     * 获取上传文件的key
     * 上传和删除时除了需要bucketName外还需要此值
     *
     * @param prefix   前缀（非必传），可以用于区分是哪个模块或子项目上传的文件,默认 file 文件夹
     * @param fileName 文件名称（非必传），如果为空默认生成文件名，格式：yyyyMMdd-UUID
     * @param suffix   后缀 , 可以是 png jpg
     * @return
     */
    private String getKey(final String prefix, final String fileName, final String suffix) {
        StringBuffer keySb = new StringBuffer();
        // 前缀处理
        if (StringUtils.isNotEmpty(prefix)) {
            keySb.append(prefix);
        } else {
            keySb.append(FilePathEnum.PHOTO.getPath());
        }
        // 文件名处理
        if (StringUtils.isBlank(fileName)) {
            // 上传时间 因为后期可能会用 - 将key进行split，然后进行分类统计
            keySb.append(CommonUtil.localDateTime2String(LocalDateTime.now(), "yyyyMMdd"));
            keySb.append("-");
            // 生成uuid
            keySb.append(UUID.randomUUID());
        } else {
            keySb.append(fileName);
        }
        // 后缀处理
        if (StringUtils.isBlank(suffix)) {
            throw new NullPointerException("文件后缀不能为空");
        }
        if (suffix.contains(".")) {
            keySb.append(suffix.substring(suffix.lastIndexOf(".")));
        } else {
            keySb.append("." + suffix);
        }
        return keySb.toString();
    }

    /**
     * 根据key生成文件的访问地址
     *
     * @param key
     * @return
     */
    public String getUrl(String key) {
        // 拼接文件访问路径。由于拼接的字符串大多为String对象，而不是""的形式，所以直接用+拼接的方式没有优势
        StringBuffer url = new StringBuffer();
        url.append("http://")
                .append(ossConfigProperties.getBucketName())
                .append(".")
                .append(ossConfigProperties.getEndpoint())
                .append("/").append(key);
        return url.toString();
    }

    public Boolean checkExist(String key) {
        ossClient = getOssClient();
        return ossClient.doesObjectExist(ossConfigProperties.getBucketName(), key);
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
