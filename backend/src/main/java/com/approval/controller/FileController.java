package com.approval.controller;

import com.approval.common.result.Result;
import com.approval.service.FileService;
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
