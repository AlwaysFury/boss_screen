package com.boss.client.controller;

import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.PhotoInfoDTO;
import com.boss.client.service.impl.GradeServiceImpl;
import com.boss.client.service.impl.PhotoServiceImpl;
import com.boss.client.strategy.context.FileTransferStrategyContext;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.PhotoInfoVO;
import com.boss.client.vo.PhotoVO;
import com.boss.client.vo.Result;
import com.boss.common.dto.UpdateStatusDTO;
import com.boss.common.enums.FilePathEnum;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.boss.common.enums.TagTypeEnum.PHOTO;


/**
 * 照片控制器
 */

@RestController
@RequestMapping("/photo")
@Slf4j
public class PhotoController {

    @Autowired
    private PhotoServiceImpl photoService;

    @Autowired
    private GradeServiceImpl gradeService;

    @Autowired
    private FileTransferStrategyContext fileTransferStrategyContext;

    /**
     * 上传
     * @param photoInfoDTO
     * @return
     */
    @PostMapping("/save")
    public Result<?> saveOrUpdatePhotoInfo(@Valid @RequestBody PhotoInfoDTO photoInfoDTO) {
        photoService.saveOrUpdatePhotoInfo(photoInfoDTO);
        return Result.ok();
    }


    /**
     * 获取照片列表
     * @param condition
     * @return
     */
    @GetMapping("/photoList")
    public Result<PageResult<PhotoVO>> skuList(ConditionDTO condition) {
        return Result.ok(photoService.photoListByCondition(condition));
    }

    /**
     * 根据id获取图案信息
     * @param id
     * @return
     */
    @GetMapping("/getPhotoInfo")
    public Result<PhotoInfoVO> getPhotoById(@RequestParam("photo_id") long id) {
        return Result.ok(photoService.getPhotoById(id));
    }

    /**
     * 刷新图片等级
     * @param id
     * @return
     */
    @GetMapping("/refreshGrade")
    public Result<?> refreshGrade() {
        gradeService.refreshGrade(PHOTO.getCode());
        return Result.ok();
    }


    /**
     * 上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadPhoto(MultipartFile file) {
        return Result.ok(fileTransferStrategyContext.executeUploadStrategy(file, FilePathEnum.PHOTO.getPath()));
    }

    /**
     * 批量上传
     */

    /**
     * 下载
     */
    @GetMapping("/download/{fileName}")
    public Result<String> downloadPhoto(@PathVariable("fileName") String fileName, HttpServletResponse response) throws UnsupportedEncodingException {
        // 设置响应头为下载
        // response.setContentType("application/x-download");
        // 设置下载的文件名
        // response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        // response.setCharacterEncoding("UTF-8");
        // 文件名以附件的形式下载
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

        try {
            // 读取文件内容。
            InputStream inputStream = fileTransferStrategyContext.executeDownloadStrategy(FilePathEnum.PHOTO.getPath(), fileName);
            BufferedInputStream in = new BufferedInputStream(inputStream);// 把输入流放入缓存流
            ServletOutputStream outputStream = response.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(outputStream);// 把输出流放入缓存流
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            if (out != null) {
                out.flush();
                out.close();
            }
            if (in != null) {
                in.close();
            }
            return Result.ok("文件下载成功");
        } catch (Exception e) {
            return Result.ok("文件下载失败");
        }
    }

    @PostMapping("/delete")
    public Result<?> deleteBatch(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        photoService.deleteBatchById(updateStatusDTO.getIdList(), updateStatusDTO.getIsDelete());
        return Result.ok();
    }


}
