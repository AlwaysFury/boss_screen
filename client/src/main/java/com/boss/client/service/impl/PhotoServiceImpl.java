package com.boss.client.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.OrderItemDao;
import com.boss.client.dao.PhotoDao;
import com.boss.client.dao.SkuDao;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.GradeObject;
import com.boss.client.dto.PhotoInfoDTO;
import com.boss.client.enities.Photo;
import com.boss.client.enities.Sku;
import com.boss.client.exception.BizException;
import com.boss.client.service.PhotoService;
import com.boss.client.strategy.context.FileTransferStrategyContext;
import com.boss.client.util.RedisUtil;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.PhotoInfoVO;
import com.boss.client.vo.PhotoVO;
import com.boss.client.vo.TagVO;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.boss.common.constant.RedisPrefixConst.GRADE_PHOTO;
import static com.boss.common.enums.TagTypeEnum.PHOTO;

/**
 * 照片服务
 */
@Service
public class PhotoServiceImpl extends ServiceImpl<PhotoDao, Photo> implements PhotoService {
    @Autowired
    private PhotoDao photoDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private ProductOrImgTagServiceImpl productOrImgTagService;

    @Autowired
    private GradeServiceImpl gradeService;

    @Autowired
    private SkuServiceImpl skuService;

    @Autowired
    private FileTransferStrategyContext fileTransferStrategyContext;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private RedisServiceImpl redisService;

    @Override
    public PageResult<PhotoVO> photoListByCondition(ConditionDTO condition) {
        List<Long> ids = new ArrayList<>();
        RedisUtil.getIdsByGrade(redisService, condition, ids, GRADE_PHOTO);

        // 查询照片列表
        // 查询分类数量
        int count = photoDao.photoCount(condition, ids);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<PhotoVO> accountList = photoDao.photoList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition, ids)
                .stream().map(photoVO -> {
                    photoVO.setGrade(redisService.getStr(GRADE_PHOTO + photoVO.getId()));
                    return photoVO;
                }).collect(Collectors.toList());
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
            skuService.saveOrUpdate(sku);
        }
        photo.setSkuId(sku.getId());
        this.saveOrUpdate(photo);
        // 保存照片标签
        productOrImgTagService.saveProductOrImgTag(photoInfoDTO.getTagNameList(), photo.getId(), PHOTO.getCode());
    }

    @Override
    public PhotoInfoVO getPhotoById(long id) {
        PhotoInfoVO photoInfoVO = photoDao.getPhotoInfoById(id);
        if (photoInfoVO == null) {
            throw new BizException("该图案不存在");
        }

        GradeObject gradeObject = orderItemDao.skuMinPriceAndCreateTime(photoInfoVO.getSkuName());

        List<TagVO> tagVOList = photoInfoVO.getTagVOList();
        if (tagVOList != null) {
            List<Long> skuIds = tagVOList.stream().map(TagVO::getId).toList();
            gradeObject.setTagIds(skuIds);
        }
        photoInfoVO.setGrade(redisService.getStr(GRADE_PHOTO + photoInfoVO.getId()));

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
}




