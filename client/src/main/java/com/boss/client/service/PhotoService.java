package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.PhotoInfoDTO;
import com.boss.client.dto.UploadChunkFileDTO;
import com.boss.client.enities.Photo;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.PhotoInfoVO;
import com.boss.client.vo.PhotoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 摄影服务
 */
public interface PhotoService extends IService<Photo> {

    /**
     * 照片列表
     * @param condition
     * @return
     */
    PageResult<PhotoVO> photoListByCondition(ConditionDTO condition);

    /**
     * 根据id获取照片信息
     * @param id
     * @return
     */
    PhotoInfoVO getPhotoById(long id);

    /**
     * 更新照片信息
     *
     * @param photoInfoDTO 照片信息
     */
    void saveOrUpdatePhotoInfo(PhotoInfoDTO photoInfoDTO);

    /**
     * 保存上传信息
     * @param data
     */
    void saveUploadInfo(String data);

    /**
     * 删除照片
     *
     * @param photoIdList 照片id列表
     */
    void deleteBatchById(List<Long> photoIdList, boolean isDelete);

    /**
     * 上传
     */
    Map<String, String> upload(MultipartFile file);

    Map<String, String> getUploadId(String key);

    /**
     * 分片上传
     */
    Map<String, Object> uploadChunk(UploadChunkFileDTO uploadChunkFileDTO);

}
