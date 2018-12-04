package com.internal.misc;

import com.bean.User;

// 为访问com.bean包留下的后门，模拟jdk.internal.misc包下的后门接口
public interface ComBeanAccess {
    void fun1(User user);
    
    void fun2(User user);
}
