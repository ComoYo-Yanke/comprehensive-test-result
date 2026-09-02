package com.zongce.comprehensive.controller;

import com.zongce.comprehensive.common.BusinessException;
import com.zongce.comprehensive.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传接口（佐证图片）
 */
@Tag(name = "文件上传接口")
@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    @Value("${zongce.upload-dir:uploads}")
    private String uploadDir;

    @Operation(summary = "上传佐证图片，返回访问 URL")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        // 仅允许图片
        String lower = ext.toLowerCase();
        if (!List.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(lower)) {
            throw new BusinessException("仅支持图片文件");
        }
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            file.transferTo(new File(dir, filename).getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }
        return Result.success("/uploads/" + filename);
    }
}
