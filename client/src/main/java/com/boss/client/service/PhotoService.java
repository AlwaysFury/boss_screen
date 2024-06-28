package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.PhotoInfoDTO;
import com.boss.client.enities.Photo;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.PhotoInfoVO;
import com.boss.client.vo.PhotoVO;

import java.util.List;

/**
 * 摄影服务
 */
public interface PhotoService extends IService<Photo> {

    /**
     * 照片列表
     * @param condition
     * @return
     */
    PageResult<PhotoVO> photoList(ConditionDTO condition);

    /**
     * 根据id获取照片信息
     * @param id
     * @return
     */
    PhotoInfoVO getPhotoById(Long id);

    /**
     * 更新照片信息
     *
     * @param photoInfoDTO 照片信息
     */
    void saveOrUpdatePhotoInfo(PhotoInfoDTO photoInfoDTO);

    /**
     * 删除照片
     *
     * @param photoIdList 照片id列表
     */
    void deletePhotos(List<Long> photoIdList);

}
