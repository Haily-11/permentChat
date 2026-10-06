package com.shanyangcode.redpacketservice.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RedPacketSendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 红包ID
     *
     */
    private Long redPacketId;

    /**
     * 消息
     */
    private Long messageId;
}
