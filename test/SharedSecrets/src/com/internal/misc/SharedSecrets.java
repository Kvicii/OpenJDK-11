package com.internal.misc;

// 模拟SharedSecrets的实现机制
public class SharedSecrets {
    private static ComBeanAccess comBeanAccess;
    
    public static ComBeanAccess getComBeanAccess() {
        return comBeanAccess;
    }
    
    public static void setComBeanAccess(ComBeanAccess access) {
        comBeanAccess = access;
    }
}
