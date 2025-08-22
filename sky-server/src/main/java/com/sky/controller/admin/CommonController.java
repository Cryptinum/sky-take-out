package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.22 00:21
 */

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Tag(name = "通用接口", description = "提供通用的功能接口，如上传图片等")
public class CommonController {

    // 从配置文件中注入文件保存的基础路径
    @Value("${sky.upload.path}")
    private String basePath;

    // 从配置文件中注入对外访问的URL前缀
    @Value("${sky.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 上传图片
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @Operation(summary = "上传图片", description = "提供图片上传功能")
    public Result<String> uploadImage(MultipartFile file) {
        log.info("上传图片：{}", file);

        // 1. 生成唯一文件名，防止重名覆盖
        String originalFilename = file.getOriginalFilename();

        // 提取文件后缀，例如 .jpg
        String extension = null;
        if (originalFilename != null) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 使用UUID生成新的文件名
        String newFileName = UUID.randomUUID() + extension;
        log.info("新文件名为：{}", newFileName);

        // 2. 创建文件保存的目录
        File dir = new File(basePath);
        // 如果目录不存在，则创建它
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 3. 将临时文件转存到指定位置
            File destFile = new File(basePath + newFileName);
            file.transferTo(destFile);
            log.info("文件上传成功，保存路径：{}", destFile.getAbsolutePath());

            // 4. 拼接可供外部访问的URL并返回
            String accessUrl = urlPrefix + newFileName;
            log.info("文件访问URL：{}", accessUrl);
            return Result.success(accessUrl);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            // throw new UploadException(MessageConstant.UPLOAD_FAILED); // 建议抛出自定义异常，由全局异常处理器捕获
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
