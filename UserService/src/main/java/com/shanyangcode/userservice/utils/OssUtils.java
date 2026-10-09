package com.shanyangcode.userservice.utils;

import cn.hutool.core.util.StrUtil;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OssUtils {

    @Resource
    private MinioClient minioClient;

    @Value("${minio.url}")
    private String url;

    /**
     * 根据预签名url实现图片上传
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @param expires 过期时间
     * @return String
     */

    @SneakyThrows
    public String uploadUrl(String bucketName,String objectName,Integer expires){
        //生成一个带签名、带有效期的临时 URL，客户端可以直接用这个 URL 操作 MinIO，不需要再传 accessKey / secretKey。
        //带签名+会过期
        return minioClient.getPresignedObjectUrl(
                //presigned预签名，生成一个预签名上传 URL（PUT 方法，有效期 expires 秒）
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expires, TimeUnit.SECONDS)
                        .build());
    }

    //拼接出下载/访问地址
    //不带签名+永不过期
    public String downUrl(String bucketName,String fileName){
        return url + StrUtil.SLASH +bucketName+StrUtil.SLASH+fileName;
    }
}
