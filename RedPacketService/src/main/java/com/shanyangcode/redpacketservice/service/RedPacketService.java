package com.shanyangcode.redpacketservice.service;

import com.shanyangcode.redpacketservice.model.dto.RedPacketReceiveRequest;
import com.shanyangcode.redpacketservice.model.dto.RedPacketSendRequest;
import com.shanyangcode.redpacketservice.model.vo.ReceiveResultVO;
import com.shanyangcode.redpacketservice.model.vo.RedPacketBasicVO;
import com.shanyangcode.redpacketservice.model.vo.RedPacketDetailVO;
import com.shanyangcode.redpacketservice.model.vo.RedPacketSendVO;
import jakarta.validation.Valid;

public interface RedPacketService {

    /**
     * 发送红包
     *
     * @param request
     * @return
     */
    RedPacketSendVO sendRedPacket(RedPacketSendRequest request);


    /**
     * 处理红包过期（Kafka 消费者调用）
     *
     * @param redPacketId 红包 ID
     */
    void handleRedPacketExpiration(Long redPacketId);

    ReceiveResultVO receiveRedPacket(@Valid RedPacketReceiveRequest request);

    void handleRedPacketReceive(Long userId, Long redPacketId, Long receivedAmount, Long receiveTime);

    void handleRedPacketCompleted(Long redPacketId);

    RedPacketDetailVO getRedPacketDetail(Long redPacketId, Integer pageNum, Integer pageSize);

    RedPacketBasicVO getRedPacketBasicInfo(Long redPacketId);
}