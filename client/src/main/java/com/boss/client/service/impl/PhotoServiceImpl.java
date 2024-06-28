package com.boss.client.service.impl;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.PhotoDao;
import com.boss.client.dao.SkuDao;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.PhotoInfoDTO;
import com.boss.client.enities.Photo;
import com.boss.client.enities.Sku;
import com.boss.client.service.PhotoService;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.PhotoInfoVO;
import com.boss.client.vo.PhotoVO;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private SkuServiceImpl skuService;

    @Override
    public PageResult<PhotoVO> photoList(ConditionDTO condition) {
        // 查询照片列表
        // 查询分类数量
        int count = photoDao.photoCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<PhotoVO> accountList = photoDao.photoList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(accountList, count);



//        Page<Photo> page = new Page<>(PageUtils.getCurrent(), PageUtils.getSize());
//        Page<Photo> photoPage = photoDao.selectPage(page, new LambdaQueryWrapper<Photo>()
//                .eq(Objects.nonNull(condition.getAlbumId()), Photo::getAlbumId, condition.getAlbumId())
//                .eq(Photo::getIsDelete, condition.getIsDelete())
//                .orderByDesc(Photo::getId)
//                .orderByDesc(Photo::getUpdateTime));
//        List<PhotoBackDTO> photoList = BeanCopyUtils.copyList(photoPage.getRecords(), PhotoBackDTO.class);
//        return new PageResult<>(photoList, (int) photoPage.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdatePhotoInfo(PhotoInfoDTO photoInfoDTO) {
        Photo photo = BeanCopyUtils.copyObject(photoInfoDTO, Photo.class);

        String name = photoInfoDTO.getPhotoName();
        if (name.contains("-")) {
            name = name.split("-")[0];
        }
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
    public PhotoInfoVO getPhotoById(Long id) {
        // todo 图片款号销量，等级搜索，所在链接，链接部分重写
        return photoDao.getPhotoInfoById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePhotos(List<Long> photoIdList) {
        photoDao.deleteBatchIds(photoIdList);
    }
}




