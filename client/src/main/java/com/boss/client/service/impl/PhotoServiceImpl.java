package com.boss.client.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.PhotoDao;
import com.boss.client.dao.SkuDao;
import com.boss.client.dao.TagDao;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.PhotoInfoDTO;
import com.boss.client.dto.UploadChunkFileDTO;
import com.boss.client.enities.Photo;
import com.boss.client.enities.Sku;
import com.boss.client.exception.BizException;
import com.boss.client.service.PhotoService;
import com.boss.client.strategy.context.FileTransferStrategyContext;
import com.boss.client.vo.*;
import com.boss.common.enities.Tag;
import com.boss.common.enums.FilePathEnum;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.boss.common.enums.TagTypeEnum.PHOTO;

/**
 * 照片服务
 */
@Service
@Slf4j
public class PhotoServiceImpl extends ServiceImpl<PhotoDao, Photo> implements PhotoService {
    @Autowired
    private PhotoDao photoDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private TagDao tagDao;

    @Autowired
    private ProductOrImgTagServiceImpl productOrImgTagService;


    @Autowired
    private SkuServiceImpl skuService;

    @Autowired
    private FileTransferStrategyContext fileTransferStrategyContext;

    @Value("${upload.oss.url}")
    private String url;

    @Override
    public PageResult<PhotoVO> photoListByCondition(ConditionDTO condition) {
//        List<Long> ids = RedisUtil.getIdsByGrade(redisService, condition, GRADE_PHOTO);

        // 查询照片列表
        // 查询分类数量
        int count = photoDao.photoCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<PhotoVO> accountList = photoDao.photoList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(accountList, count);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdatePhotoInfo(PhotoInfoDTO photoInfoDTO) {
        Photo photo = BeanCopyUtils.copyObject(photoInfoDTO, Photo.class);

        String name = photoInfoDTO.getPhotoName();
        if (name.contains("-")) {
            name = name.split("-")[0];
        }
        name = FileUtil.getPrefix(name);
        Sku sku = skuDao.selectOne(new QueryWrapper<Sku>().select("id").eq("name", name));
        if (ObjectUtil.isNull(sku)) {
            long id = IdUtil.getSnowflakeNextId();
            sku = Sku.builder()
                    .id(id)
                    .name(name).build();
        }
        List<Long> relevanceIds = photoInfoDTO.getRelevanceIds();
        if (relevanceIds != null) {
            sku.setRelevanceIds(relevanceIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        skuService.saveOrUpdate(sku);
        photo.setSkuId(sku.getId());
        this.saveOrUpdate(photo);
        // 保存照片标签
        productOrImgTagService.saveProductOrImgTag(photoInfoDTO.getTagNameList(), sku.getId(), PHOTO.getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUploadInfo(String data) {
        log.info(data);
        Map<String, String> map = parseQueryString(data);
        String originFileName = map.get("filename");
        String fileName = originFileName.split("/")[1];
        log.info("上传文件信息：{}", originFileName);

        Photo existPhoto = photoDao.selectOne(new QueryWrapper<Photo>().eq("photo_name", fileName));
        if (ObjectUtil.isNotNull(existPhoto)) {
            photoDao.update(new UpdateWrapper<Photo>().set("photo_src", url + originFileName).eq("photo_name", fileName));
        } else {
            PhotoInfoDTO photoInfoDTO = PhotoInfoDTO.builder()
                    .photoName(fileName)
                    .photoSrc(url + originFileName)
                    .build();
            saveOrUpdatePhotoInfo(photoInfoDTO);
        }
    }

    @Override
    public PhotoInfoVO getPhotoById(long id) {
        PhotoInfoVO photoInfoVO = photoDao.getPhotoInfoById(id);
        if (photoInfoVO == null) {
            throw new BizException("该图案不存在");
        }

//        GradeObject gradeObject = orderItemDao.skuMinPriceAndCreateTime(photoInfoVO.getSkuName());

        List<Tag> tagList = tagDao.getTagListByItemOrImgId(photoInfoVO.getSkuId());
        if (tagList != null) {
            List<TagVO> tagVOList = tagList.stream().map(tag -> {
                return BeanCopyUtils.copyObject(tag, TagVO.class);
            }).toList();
            photoInfoVO.setTagList(tagVOList);
        }
        String relevanceIds = skuDao.selectOne(new QueryWrapper<Sku>().eq("id", photoInfoVO.getSkuId())).getRelevanceIds();
        if (relevanceIds != null && !relevanceIds.isEmpty()) {
            List<Long> relevanceIdList = Arrays.stream(relevanceIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
            List<Sku> relevanceSkus = skuDao.selectList(new QueryWrapper<Sku>().in("id", relevanceIdList));
            if (relevanceSkus != null) {
                List<RelevanceSkuVO> relevanceSkuVOList = relevanceSkus.stream()
                        .map(sku -> {
                            return BeanCopyUtils.copyObject(sku, RelevanceSkuVO.class);
                        }).collect(Collectors.toList());
                photoInfoVO.setRelevanceSku(relevanceSkuVOList);
            }
        }
        return photoInfoVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBatchById(List<Long> photoIdList, boolean isDelete) {

        List<String> photoNameList = new ArrayList<>();
        List<Long> skuIdList = new ArrayList<>();
        List<Photo> photos = photoDao.selectList(new QueryWrapper<Photo>().in("id", photoIdList));
        if (photos != null && photos.size() > 0) {
            for (Photo photo : photos) {
                skuIdList.add(photo.getSkuId());
                photoNameList.add(photo.getPhotoName());
            }
        }
        // 是否删除对应款号记录和关联款号
        if (isDelete) {
            skuService.deleteSku(skuIdList);
        }
        productOrImgTagService.deleteBatch(photoIdList);
        fileTransferStrategyContext.executeDeleteStrategy(photoNameList);
        photoDao.deleteBatchIds(photoIdList);
    }

    @Override
    public Map<String, String> upload(MultipartFile file) {
        return fileTransferStrategyContext.executeUploadStrategy(file, FilePathEnum.PHOTO.getPath());
    }

    @Override
    public Map<String, String> getUploadId(String key) {
        return fileTransferStrategyContext.getUploadId(key);
    }

    @Override
    public Map<String, Object> uploadChunk(UploadChunkFileDTO uploadChunkFileDTO) {
        return fileTransferStrategyContext.executeUploadChunkStrategy(uploadChunkFileDTO);
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> map = new HashMap<>();
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            }
        }
        return map;
    }
}




