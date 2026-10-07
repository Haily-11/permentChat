package com.shanyangcode.common.model.dto;

import lombok.Data;

@Data
public class MessageBody {

    /**
     * 消息内容
     */
    private String content;

    /**
     * 回复id
     */
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
