package com.boss.client.controller;

import com.boss.client.dto.PhotoInfoDTO;
import com.boss.client.service.impl.PhotoServiceImpl;
import com.boss.client.strategy.context.FileTransferStrategyContext;
import com.boss.client.vo.Result;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

    /**
     * 批量下载为zip
     * @return
     */



    @PostMapping("/delete")
    public Result<?> deleteBatch() {
        List<String> fileNames = new ArrayList<>();
        fileNames.add("A2626.png");
        fileTransferStrategyContext.executeDeleteStrategy(fileNames);
        return Result.ok();
    }


}
