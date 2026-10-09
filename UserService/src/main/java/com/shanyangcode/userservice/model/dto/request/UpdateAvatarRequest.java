package com.shanyangcode.userservice.model.dto.request;

import lombok.Data;

@Data
public class UpdateAvatarRequest {

    /**
     * 头像URL,就是uploadUrlResponse返回的url
     */
    private String url;

    private Long userId;
}
