package com.jzo2o.health.model;

import com.jzo2o.common.model.CurrentUserInfo;

public class UserThreadLocal {

    private static final ThreadLocal<CurrentUserInfo> THREAD_LOCAL_USER = new ThreadLocal<>();

    /**
     * 获取当前用户id
     *
     * @return 用户id
     */
    public static Long currentUserId() {
        return THREAD_LOCAL_USER.get().getId();
    }

    public static CurrentUserInfo currentUser() {
        return THREAD_LOCAL_USER.get();
    }

    /**
     * 设置当前用户id
     *
     * @param currentUserInfo 当前用户信息
     */
    public static void set(CurrentUserInfo currentUserInfo) {
        THREAD_LOCAL_USER.set(currentUserInfo);
    }

    /**
     * 清理当前线程中的用户信息
     */
    public static void clear() {
        THREAD_LOCAL_USER.remove();
    }
}
