package com.shanyangcode.common.model.dto;

import lombok.Data;

@Data
public class MessageBody {

    private String content;

    //信息id
    private Long replyId;

    /**
     * 红包ID（红包消息专用
     */
    private String redPacketId;

    /**
     * 红包封面文案
     */
    private String redPacketWrapperText;
}
