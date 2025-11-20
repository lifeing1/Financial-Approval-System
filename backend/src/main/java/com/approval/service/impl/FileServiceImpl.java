package com.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.approval.common.exception.BusinessException;
import com.approval.entity.Attachment;
import com.approval.mapper.AttachmentMapper;
import com.approval.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务实现
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final AttachmentMapper attachmentMapper;
    
    @Value("${file.upload-path}")
    private String uploadPath;
    
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".doc", ".docx", ".pdf", ".png", ".jpg", ".jpeg"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    @Override
    public String uploadFile(MultipartFile file, String businessType, Long businessId) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过10MB");
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件格式，仅支持：doc、docx、pdf、png、jpg");
        }
        
        try {
            // 创建日期目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String dirPath = uploadPath + File.separator + dateDir;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成新文件名
            String newFilename = UUID.randomUUID().toString() + extension;
            String filePath = dateDir + File.separator + newFilename;
            String fullPath = uploadPath + File.separator + filePath;
            
            // 保存文件
            Path path = Paths.get(fullPath);
            Files.copy(file.getInputStream(), path);
            
            // 保存文件信息到数据库
            Attachment attachment = new Attachment();
            attachment.setBusinessId(businessId);
            attachment.setBusinessType(businessType);
            attachment.setFileName(originalFilename);
            attachment.setFilePath(filePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(extension);
            attachment.setUploadUserId(StpUtil.getLoginIdAsLong());
            
            attachmentMapper.insert(attachment);
            
            return filePath;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(Long id) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 删除物理文件
        String fullPath = uploadPath + File.separator + attachment.getFilePath();
        File file = new File(fullPath);
        if (file.exists()) {
            file.delete();
        }
        
        // 删除数据库记录
        attachmentMapper.deleteById(id);
    }
    
    @Override
    public String getFilePath(Long id) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new BusinessException("文件不存在");
        }
        return uploadPath + File.separator + attachment.getFilePath();
    }
}
