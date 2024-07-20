package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.enities.Photo;
import com.boss.client.vo.PhotoInfoVO;
import com.boss.client.vo.PhotoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 照片映射器
 */
@Repository
public interface PhotoDao extends BaseMapper<Photo> {


    int photoCount(@Param("condition") ConditionDTO condition, @Param("ids") List<Long> ids);


    List<PhotoVO> photoList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition, @Param("ids") List<Long> ids);


    /**
     * 根据id查询图片信息
     *
     * @param photoId 图片id
     * @return 文章信息
     */
    PhotoInfoVO getPhotoInfoById(@Param("photoId") long photoId);

}




