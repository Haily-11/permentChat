package com.shanyangcode.offlinedataservice.model.dto;

import lombok.Data;

@Data
public class OfflineMessageRequest {

    //对接从UserService中拉来的数据
    private Long userId;

    private Long offlineTime;
}
