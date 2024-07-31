package com.boss.client.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.OrderItemDao;
import com.boss.client.dao.PhotoDao;
import com.boss.client.dao.SkuDao;
import com.boss.client.dao.TagDao;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.PhotoInfoDTO;
import com.boss.client.enities.Photo;
import com.boss.client.enities.Sku;
import com.boss.client.exception.BizException;
import com.boss.client.service.PhotoService;
import com.boss.client.strategy.context.FileTransferStrategyContext;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.PhotoInfoVO;
import com.boss.client.vo.PhotoVO;
import com.boss.client.vo.Result;
import com.boss.common.enities.Tag;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.PageUtils;
import com.boss.common.vo.SelectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private TagDao tagDao;

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

    @Autowired
    private RuleServiceImpl ruleService;

    @Autowired
    private TagServiceImpl tagService;

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

    @Override
    public PhotoInfoVO getPhotoById(long id) {
        PhotoInfoVO photoInfoVO = photoDao.getPhotoInfoById(id);
        if (photoInfoVO == null) {
            throw new BizException("该图案不存在");
        }

//        GradeObject gradeObject = orderItemDao.skuMinPriceAndCreateTime(photoInfoVO.getSkuName());

        List<Tag> tagList = tagDao.getTagListByItemOrImgId(photoInfoVO.getId());
        if (tagList != null) {
//            List<Long> skuIds = tagList.stream().map(Tag::getId).toList();
            List<String> tagNameList = tagList.stream().map(Tag::getTagName).toList();
            photoInfoVO.setTagNameList(tagNameList);
//            gradeObject.setTagIds(skuIds);
        }
        String relevanceIds = skuDao.selectOne(new QueryWrapper<Sku>().eq("id", photoInfoVO.getSkuId())).getRelevanceIds();
        if (relevanceIds != null && !relevanceIds.isEmpty()) {
            List<Long> relevanceIdList = Arrays.stream(relevanceIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
            photoInfoVO.setRelevanceIds(relevanceIdList);
        }

//        Object grade = redisService.get(GRADE_PHOTO + photoInfoVO.getSkuId());
//        photoInfoVO.setGrade(grade == null ? "未设置" : String.valueOf(grade));

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

    @GetMapping("/gradeSelect")
    public Result<List<SelectVO>> gradeSelect() {
        return Result.ok(ruleService.gradeSelect(PHOTO.getCode()));
    }

    @GetMapping("/tagSelect")
    public Result<List<SelectVO>> tagSelect() {
        return Result.ok(tagService.tagSelect(PHOTO.getCode()));
    }
}




