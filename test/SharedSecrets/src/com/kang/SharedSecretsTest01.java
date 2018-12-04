package com.kang;

import com.bean.User;
import com.internal.misc.ComBeanAccess;
import com.internal.misc.SharedSecrets;

// 模拟SharedSecrets的实现机制
public class SharedSecretsTest01 {
    public static void main(String[] args) {
        User user = new User();
        
        // 访问User中的private方法和default方法
        ComBeanAccess access = SharedSecrets.getComBeanAccess();
        access.fun1(user);
        access.fun2(user);
    }
}
