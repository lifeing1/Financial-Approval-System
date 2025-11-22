package com.approval.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.approval.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {
    
    private final OssProperties ossProperties;
    
    /**
     * 上传文件到OSS
     *
     * @param file 文件
     * @param folder 文件夹路径（如：expense/）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        OSS ossClient = null;
        try {
            // 创建OSS客户端
            ossClient = new OSSClientBuilder().build(
                    ossProperties.getEndpoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            
            // 生成文件名：folder/yyyyMMdd/uuid_原始文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = folder + datePath + "/" + UUID.randomUUID().toString() + extension;
            
            // 上传文件
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    fileName,
                    inputStream
            );
            
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            
            // 返回文件访问URL
            String fileUrl = ossProperties.getBaseUrl() + "/" + fileName;
            log.info("文件上传成功：{}", fileUrl);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 删除OSS文件
     *
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        OSS ossClient = null;
        try {
            // 创建OSS客户端
            ossClient = new OSSClientBuilder().build(
                    ossProperties.getEndpoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            
            // 从URL中提取文件路径
            String fileName = fileUrl.replace(ossProperties.getBaseUrl() + "/", "");
            
            // 删除文件
            ossClient.deleteObject(ossProperties.getBucketName(), fileName);
            log.info("文件删除成功：{}", fileUrl);
            
        } catch (Exception e) {
            log.error("文件删除失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
