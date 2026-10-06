package com.shanyangcode.userservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanyangcode.userservice.mapper.FriendMapper;
import com.shanyangcode.userservice.model.dto.FriendDTO;
import com.shanyangcode.userservice.model.dto.ModifyFriendApplicationResponse;
import com.shanyangcode.userservice.model.entity.Friend;
import com.shanyangcode.userservice.model.entity.User;
import com.shanyangcode.userservice.model.vo.FriendDetailVO;
import com.shanyangcode.common.model.dto.PageRequest;

/**
 * 好友服务接口
 * 管理好友关系的完整生命周期
 * 支持添加、删除、拉黑好友等操作
 */
public interface FriendService extends IService< Friend> {

    /**
     * 根据关键字搜索用户
     * @param userId
     * @param keyword
     * @return
     */
    FriendDetailVO searchUserByKeyword(String userId, String keyword);

    IPage<FriendDTO> getFriends(String userId, PageRequest pageRequest, String keyword);

    /**
     * 删除好友关系
     * <p>
     * 处理流程：
     * 1. 删除双向好友关系
     * 2. 删除相关的好友申请记录
     * 3. 删除会话和用户会话关系
     *
     * @param userIdL   当前用户ID
     * @param friendId 好友ID
     * @return 删除是否成功
     */
    boolean deleteFriend(Long userIdL, Long friendId);

    /**
     * 拉黑好友
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @return 更新是否成功
     */
    boolean blockFriend(Long userId, Long friendId);

    /**
     * 取消拉黑好友
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @return 更新是否成功
     */
    boolean unblockFriend(Long userId, Long friendId);

    /**
     * 添加好友关系
     *
     * 处理流程：
     * 1. 验证用户存在性
     * 2. 检查是否已是好友关系
     * 3. 创建双向好友关系
     * 4. 创建会话和用户会话关系
     * 5. 发送Kafka通知
     *
     * @param recipient 接收好友请求的用户
     * @param friendId  申请添加的好友ID
     * @return ModifyFriendApplicationResponse 响应对象
     */
    ModifyFriendApplicationResponse addFriend(User recipient, Long friendId);

    FriendDetailVO getFriendDetails(String userId, String friendId);
}

