package com.approval.controller;

import com.approval.common.result.Result;
import com.approval.service.FileService;
import com.approval.util.OssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

/**
 * 文件管理控制器
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    private final OssUtil ossUtil;
    
    /**
     * 上传文件到OSS
     */
    @Operation(summary = "上传文件到OSS")
    @PostMapping("/upload-oss")
    public Result<?> uploadToOss(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "folder", defaultValue = "expense/") String folder) {
        try {
            // 验证文件大小（限制为10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }
            
            // 验证文件类型（只允许图片和PDF）
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                if (!".jpg".equals(extension) && !".jpeg".equals(extension) 
                        && !".png".equals(extension) && !".pdf".equals(extension)) {
                    return Result.error("只允许上传图片（jpg/jpeg/png）或PDF文件");
                }
            }
            
            String fileUrl = ossUtil.uploadFile(file, folder);
            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 从 OSS 删除文件
     */
    @Operation(summary = "从DOSS删除文件")
    @DeleteMapping("/delete-oss")
    public Result<?> deleteFromOss(@RequestParam("fileUrl") String fileUrl) {
        try {
            ossUtil.deleteFile(fileUrl);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传文件
     */
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file,
                                @RequestParam("businessType") String businessType,
                                @RequestParam("businessId") Long businessId) {
        String filePath = fileService.uploadFile(file, businessType, businessId);
        return Result.success(filePath);
    }
    
    /**
     * 下载文件
     */
    @Operation(summary = "下载文件")
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) {
        try {
            String filePath = fileService.getFilePath(id);
            File file = new File(filePath);
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }
    
    /**
     * 删除文件
     */
    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public Result<?> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return Result.success("删除成功");
    }
}
