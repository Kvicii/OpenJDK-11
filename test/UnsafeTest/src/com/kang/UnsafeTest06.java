package com.kang;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

// 获取静态字段所属的类对象
public class UnsafeTest06 {
    public static void main(String[] args) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Unsafe unsafe = UnsafeUtil.getUnsafeInstance();
        
        // 静态字段
        Field booConstField  = Entry.class.getDeclaredField("booConst");
        
        // 静态字段所属的类字段
        Object object = unsafe.staticFieldBase(booConstField);
        
        // 强转为Class对象
        Class clszz = (Class)object;
        
        // 反射创建对象
        Entry entry = (Entry) clszz.getConstructor().newInstance();
        
        System.out.println(entry);
        
    }
}
