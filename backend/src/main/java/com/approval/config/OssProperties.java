package com.approval.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    
    /**
     * OSS服务端点
     */
    private String endpoint;
    
    /**
     * 访问密钥ID
     */
    private String accessKeyId;
    
    /**
     * 访问密钥密码
     */
    private String accessKeySecret;
    
    /**
     * 存储桶名称
     */
    private String bucketName;
    
    /**
     * 文件访问基础URL
     */
    private String baseUrl;
}
