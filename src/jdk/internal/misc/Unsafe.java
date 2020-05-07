/*
 * Copyright (c) 2000, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.internal.misc;

import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.vm.annotation.ForceInline;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

/**
 * A collection of methods for performing low-level, unsafe operations.
 * Although the class and all methods are public, use of this class is
 * limited because only trusted code can obtain instances of it.
 *
 * <em>Note:</em> It is the resposibility of the caller to make sure
 * arguments are checked before methods of this class are
 * called. While some rudimentary checks are performed on the input,
 * the checks are best effort and when performance is an overriding
 * priority, as when methods of this class are optimized by the
 * runtime compiler, some or all checks (if any) may be elided. Hence,
 * the caller must not rely on the checks and corresponding
 * exceptions!
 *
 * @author John R. Rose
 * @see #getUnsafe
 */
/*
 * 除了实现sun.misc.Unsafe中列出的方法 还提供了其他更精细的低级别操作 与底层交互紧密
 *
 * 针对多线程中单变量的可视性 以及多变量的顺序依赖和值依赖 JDK 9 引入五种资源同步的语义 由弱到强分别是:
 * Plain, Opaque, Release[write]/Acquire[read], Volatile, Locks。【参见VarHandle】
 */
public final class Unsafe {
    
    // 单例对象
    private static final Unsafe theUnsafe = new Unsafe();
    
    /**
     * This constant differs from all results that will ever be returned from
     * {@link #staticFieldOffset}, {@link #objectFieldOffset},
     * or {@link #arrayBaseOffset}.
     */
    // 无效的JVM内存偏移量标记
    public static final int INVALID_FIELD_OFFSET = -1;
    
    
    /* 用于 #arrayBaseOffset 的值*/
    
    /**
     * The value of {@code arrayBaseOffset(boolean[].class)}
     */
    public static final int ARRAY_BOOLEAN_BASE_OFFSET = theUnsafe.arrayBaseOffset(boolean[].class);
    /**
     * The value of {@code arrayBaseOffset(byte[].class)}
     */
    public static final int ARRAY_BYTE_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);
    /**
     * The value of {@code arrayBaseOffset(short[].class)}
     */
    public static final int ARRAY_SHORT_BASE_OFFSET = theUnsafe.arrayBaseOffset(short[].class);
    /**
     * The value of {@code arrayBaseOffset(char[].class)}
     */
    public static final int ARRAY_CHAR_BASE_OFFSET = theUnsafe.arrayBaseOffset(char[].class);
    /**
     * The value of {@code arrayBaseOffset(int[].class)}
     */
    public static final int ARRAY_INT_BASE_OFFSET = theUnsafe.arrayBaseOffset(int[].class);
    /**
     * The value of {@code arrayBaseOffset(long[].class)}
     */
    public static final int ARRAY_LONG_BASE_OFFSET = theUnsafe.arrayBaseOffset(long[].class);
    /**
     * The value of {@code arrayBaseOffset(float[].class)}
     */
    public static final int ARRAY_FLOAT_BASE_OFFSET = theUnsafe.arrayBaseOffset(float[].class);
    /**
     * The value of {@code arrayBaseOffset(double[].class)}
     */
    public static final int ARRAY_DOUBLE_BASE_OFFSET = theUnsafe.arrayBaseOffset(double[].class);
    /**
     * The value of {@code arrayBaseOffset(Object[].class)}
     */
    public static final int ARRAY_OBJECT_BASE_OFFSET = theUnsafe.arrayBaseOffset(Object[].class);
    
    
    /* 用于 #arrayIndexScale 的值*/
    
    /**
     * The value of {@code arrayIndexScale(boolean[].class)}
     */
    public static final int ARRAY_BOOLEAN_INDEX_SCALE = theUnsafe.arrayIndexScale(boolean[].class);
    /**
     * The value of {@code arrayIndexScale(byte[].class)}
     */
    public static final int ARRAY_BYTE_INDEX_SCALE = theUnsafe.arrayIndexScale(byte[].class);
    /**
     * The value of {@code arrayIndexScale(short[].class)}
     */
    public static final int ARRAY_SHORT_INDEX_SCALE = theUnsafe.arrayIndexScale(short[].class);
    /**
     * The value of {@code arrayIndexScale(char[].class)}
     */
    public static final int ARRAY_CHAR_INDEX_SCALE = theUnsafe.arrayIndexScale(char[].class);
    /**
     * The value of {@code arrayIndexScale(int[].class)}
     */
    public static final int ARRAY_INT_INDEX_SCALE = theUnsafe.arrayIndexScale(int[].class);
    /**
     * The value of {@code arrayIndexScale(long[].class)}
     */
    public static final int ARRAY_LONG_INDEX_SCALE = theUnsafe.arrayIndexScale(long[].class);
    /**
     * The value of {@code arrayIndexScale(float[].class)}
     */
    public static final int ARRAY_FLOAT_INDEX_SCALE = theUnsafe.arrayIndexScale(float[].class);
    /**
     * The value of {@code arrayIndexScale(double[].class)}
     */
    public static final int ARRAY_DOUBLE_INDEX_SCALE = theUnsafe.arrayIndexScale(double[].class);
    /**
     * The value of {@code arrayIndexScale(Object[].class)}
     */
    public static final int ARRAY_OBJECT_INDEX_SCALE = theUnsafe.arrayIndexScale(Object[].class);
    
    
    /** The value of {@code addressSize()} */
    // 本机指针的大小(以字节为单位)。参见#addressSize方法
    public static final int ADDRESS_SIZE = theUnsafe.addressSize0();
    
    
    static {
        registerNatives();
    }
    
    private static native void registerNatives();
    
    
    private Unsafe() {
    }
    
    
    /**
     * Provides the caller with the capability of performing unsafe
     * operations.
     *
     * <p>The returned {@code Unsafe} object should be carefully guarded
     * by the caller, since it can be used to read and write data at arbitrary
     * memory addresses.  It must never be passed to untrusted code.
     *
     * <p>Most methods in this class are very low-level, and correspond to a
     * small number of hardware instructions (on typical machines).  Compilers
     * are encouraged to optimize these methods accordingly.
     *
     * <p>Here is a suggested idiom for using unsafe operations:
     *
     * <pre> {@code
     * class MyTrustedClass {
     *   private static final Unsafe unsafe = Unsafe.getUnsafe();
     *   ...
     *   private long myCountAddress = ...;
     *   public int getCount() { return unsafe.getByte(myCountAddress); }
     * }}</pre>
     *
     * (It may assist compilers to make the local variable {@code final}.)
     */
    // 返回单例对象 由系统内部的方法调用
    public static Unsafe getUnsafe() {
        return theUnsafe;
    }
    
    
    
    /*▼ 构造Unsafe对象 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Allocates an instance but does not run any constructor.
     * Initializes the class if it has not yet been.
     */
    // 不调用构造方法就生成对象 但是该对象的字段会被赋为对应类型的"零值" 为该对象赋过的默认值也无效
    @HotSpotIntrinsicCandidate
    public native Object allocateInstance(Class<?> cls) throws InstantiationException;
    
    /**
     * Defines a class but does not make it known to the class loader or system dictionary.
     * <p>
     * For each CP entry, the corresponding CP patch must either be null or have
     * the a format that matches its tag:
     * <ul>
     * <li>Integer, Long, Float, Double: the corresponding wrapper object type from java.lang
     * <li>Utf8: a string (must have suitable syntax if used as signature or name)
     * <li>Class: any java.lang.Class object
     * <li>String: any object (not just a java.lang.String)
     * <li>InterfaceMethodRef: (NYI) a method handle to invoke on that call site's arguments
     * </ul>
     *
     * @param hostClass context for linkage, access control, protection domain, and class loader
     * @param data      bytes of a class file
     * @param cpPatches where non-null entries exist, they replace corresponding CP entries in data
     */
    public Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        if(hostClass == null || data == null) {
            throw new NullPointerException();
        }
        if(hostClass.isArray() || hostClass.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        
        return defineAnonymousClass0(hostClass, data, cpPatches);
    }
    
    private native Class<?> defineAnonymousClass0(Class<?> hostClass, byte[] data, Object[] cpPatches);
    
    /**
     * Tells the VM to define a class, without security checks.
     * By default, the class loader and protection domain come from the caller's class.
     */
    public Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) {
        if(b == null) {
            throw new NullPointerException();
        }
        if(len < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        return defineClass0(name, b, off, len, loader, protectionDomain);
    }
    
    public native Class<?> defineClass0(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain);
    
    /*▲ 构造Unsafe对象 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ 杂项 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Reports the location of a given field in the storage allocation of its
     * class.  Do not expect to perform any sort of arithmetic on this offset;
     * it is just a cookie which is passed to the unsafe heap memory accessors.
     *
     * <p>Any given field will always have the same offset and base, and no
     * two distinct fields of the same class will ever have the same offset
     * and base.
     *
     * <p>As of 1.4.1, offsets for fields are represented as long values,
     * although the Sun JVM does not use the most significant 32 bits.
     * However, JVM implementations which store static fields at absolute
     * addresses can use long offsets and null base pointers to express
     * the field locations in a form usable by {@link #getInt(Object, long)}.
     * Therefore, code which will be ported to such JVMs on 64-bit platforms
     * must preserve all bits of static field offsets.
     *
     * @see #getInt(Object, long)
     */
    // 获取非静态字段f的JVM偏移地址
    public long objectFieldOffset(Field f) {
        if(f == null) {
            throw new NullPointerException();
        }
        
        return objectFieldOffset0(f);
    }
    
    /**
     * Reports the location of the field with a given name in the storage allocation of its class.
     *
     * @throws NullPointerException if any parameter is {@code null}.
     * @throws InternalError        if there is no field named {@code name} declared
     *                              in class {@code c}, i.e., if {@code c.getDeclaredField(name)}
     *                              would throw {@code java.lang.NoSuchFieldException}.
     * @see #objectFieldOffset(Field)
     */
    // 获取非静态字段name在其类中的JVM偏移地址
    public long objectFieldOffset(Class<?> c, String name) {
        if(c == null || name == null) {
            throw new NullPointerException();
        }
        
        return objectFieldOffset1(c, name);
    }
    
    /**
     * Reports the location of a given static field, in conjunction with {@link
     * #staticFieldBase}.
     * <p>Do not expect to perform any sort of arithmetic on this offset;
     * it is just a cookie which is passed to the unsafe heap memory accessors.
     *
     * <p>Any given field will always have the same offset, and no two distinct
     * fields of the same class will ever have the same offset.
     *
     * <p>As of 1.4.1, offsets for fields are represented as long values,
     * although the Sun JVM does not use the most significant 32 bits.
     * It is hard to imagine a JVM technology which needs more than
     * a few bits to encode an offset within a non-array object,
     * However, for consistency with other methods in this class,
     * this method reports its result as a long value.
     *
     * @see #getInt(Object, long)
     */
    // 获取静态字段f的JVM偏移地址
    public long staticFieldOffset(Field f) {
        if(f == null) {
            throw new NullPointerException();
        }
        
        return staticFieldOffset0(f);
    }
    
    /**
     * Reports the location of a given static field, in conjunction with {@link
     * #staticFieldOffset}.
     * <p>Fetch the base "Object", if any, with which static fields of the
     * given class can be accessed via methods like {@link #getInt(Object,
     * long)}.  This value may be null.  This value may refer to an object
     * which is a "cookie", not guaranteed to be a real Object, and it should
     * not be used in any way except as argument to the get and put routines in
     * this class.
     */
    // 获取静态字段所属的类对象
    public Object staticFieldBase(Field f) {
        if (f == null) {
            throw new NullPointerException();
        }
        
        return staticFieldBase0(f);
    }
    
    /**
     * Detects if the given class may need to be initialized.
     * This is often needed in conjunction with obtaining the static field base of a class.
     *
     * @return false only if a call to {@code ensureClassInitialized} would have no effect
     */
    public boolean shouldBeInitialized(Class<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        
        return shouldBeInitialized0(c);
    }
    
    /**
     * Ensures the given class has been initialized. This is often
     * needed in conjunction with obtaining the static field base of a
     * class.
     */
    public void ensureClassInitialized(Class<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        
        ensureClassInitialized0(c);
    }
    
    
    
    private native long objectFieldOffset0(Field f);
    private native long objectFieldOffset1(Class<?> c, String name);
    private native long staticFieldOffset0(Field f);
    private native Object staticFieldBase0(Field f);
    private native boolean shouldBeInitialized0(Class<?> c);
    private native void ensureClassInitialized0(Class<?> c);
    
    /*▲ 杂项 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ 本地内存操作 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Allocates a new block of native memory, of the given size in bytes.  The
     * contents of the memory are uninitialized; they will generally be
     * garbage.  The resulting native pointer will never be zero, and will be
     * aligned for all value types.  Dispose of this memory by calling {@link
     * #freeMemory}, or resize it with {@link #reallocateMemory}.
     *
     * <em>Note:</em> It is the resposibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if the size is negative or too large
     *                          for the native size_t type
     * @throws OutOfMemoryError if the allocation is refused by the system
     * @see #getByte(long)
     * @see #putByte(long, byte)
     */
    // 申请bytes字节的本地内存 并返回分配的内存地址
    public long allocateMemory(long bytes) {
        allocateMemoryChecks(bytes);
        
        if(bytes == 0) {
            return 0;
        }
        
        long p = allocateMemory0(bytes);
        if(p == 0) {
            throw new OutOfMemoryError();
        }
        
        return p;
    }
    
    /**
     * Resizes a new block of native memory, to the given size in bytes.  The
     * contents of the new block past the size of the old block are
     * uninitialized; they will generally be garbage.  The resulting native
     * pointer will be zero if and only if the requested size is zero.  The
     * resulting native pointer will be aligned for all value types.  Dispose
     * of this memory by calling {@link #freeMemory}, or resize it with {@link
     * #reallocateMemory}.  The address passed to this method may be null, in
     * which case an allocation will be performed.
     *
     * <em>Note:</em> It is the resposibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if the size is negative or too large
     *                          for the native size_t type
     * @throws OutOfMemoryError if the allocation is refused by the system
     * @see #allocateMemory
     */
    // 在地址address的基础上扩容 如果address为0 则效果与#allocateMemory一致
    public long reallocateMemory(long address, long bytes) {
        reallocateMemoryChecks(address, bytes);
        
        if(bytes == 0) {
            freeMemory(address);
            return 0;
        }
        
        long p = (address == 0) ? allocateMemory0(bytes) : reallocateMemory0(address, bytes);
        if(p == 0) {
            throw new OutOfMemoryError();
        }
        
        return p;
    }
    
    /**
     * Sets all bytes in a given block of memory to a fixed value
     * (usually zero).  This provides a <em>single-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.
     *
     * <p>Equivalent to {@code setMemory(null, address, bytes, value)}.
     */
    // 为申请的内存批量填充初值 通常用0填充
    public void setMemory(long address, long bytes, byte value) {
        setMemory(null, address, bytes, value);
    }
    
    /**
     * Sets all bytes in a given block of memory to a fixed value
     * (usually zero).
     *
     * <p>This method determines a block's base address by means of two parameters,
     * and so it provides (in effect) a <em>double-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.  When the object reference is null,
     * the offset supplies an absolute base address.
     *
     * <p>The stores are in coherent (atomic) units of a size determined
     * by the address and length parameters.  If the effective address and
     * length are all even modulo 8, the stores take place in 'long' units.
     * If the effective address and length are (resp.) even modulo 4 or 2,
     * the stores take place in units of 'int' or 'short'.
     *
     * <em>Note:</em> It is the resposibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if any of the arguments is invalid
     * @since 1.7
     */
    // 为对象o的内存批量填充初值 通常用0填充
    public void setMemory(Object o, long offset, long bytes, byte value) {
        setMemoryChecks(o, offset, bytes, value);
        
        if(bytes == 0) {
            return;
        }
        
        setMemory0(o, offset, bytes, value);
    }
    
    /**
     * Sets all bytes in a given block of memory to a copy of another
     * block.  This provides a <em>single-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.
     *
     * Equivalent to {@code copyMemory(null, srcAddress, null, destAddress, bytes)}.
     */
    // 内存数据拷贝
    public void copyMemory(long srcAddress, long destAddress, long bytes) {
        copyMemory(null, srcAddress, null, destAddress, bytes);
    }
    
    /**
     * Sets all bytes in a given block of memory to a copy of another
     * block.
     *
     * <p>This method determines each block's base address by means of two parameters,
     * and so it provides (in effect) a <em>double-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.  When the object reference is null,
     * the offset supplies an absolute base address.
     *
     * <p>The transfers are in coherent (atomic) units of a size determined
     * by the address and length parameters.  If the effective addresses and
     * length are all even modulo 8, the transfer takes place in 'long' units.
     * If the effective addresses and length are (resp.) even modulo 4 or 2,
     * the transfer takes place in units of 'int' or 'short'.
     *
     * <em>Note:</em> It is the resposibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if any of the arguments is invalid
     * @since 1.7
     */
    // 内存数据拷贝
    public void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
        copyMemoryChecks(srcBase, srcOffset, destBase, destOffset, bytes);
        
        if(bytes == 0) {
            return;
        }
        
        copyMemory0(srcBase, srcOffset, destBase, destOffset, bytes);
    }
    
    /**
     * Copies all elements from one block of memory to another block, byte swapping the
     * elements on the fly.
     *
     * This provides a <em>single-register</em> addressing mode, as
     * discussed in {@link #getInt(Object, long)}.
     *
     * Equivalent to {@code copySwapMemory(null, srcAddress, null, destAddress, bytes, elemSize)}.
     */
    // 内存数据拷贝
    public void copySwapMemory(long srcAddress, long destAddress, long bytes, long elemSize) {
        copySwapMemory(null, srcAddress, null, destAddress, bytes, elemSize);
    }
    
    /**
     * Copies all elements from one block of memory to another block,
     * *unconditionally* byte swapping the elements on the fly.
     *
     * <p>This method determines each block's base address by means of two parameters,
     * and so it provides (in effect) a <em>double-register</em> addressing mode,
     * as discussed in {@link #getInt(Object, long)}.  When the object reference is null,
     * the offset supplies an absolute base address.
     *
     * <em>Note:</em> It is the resposibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if any of the arguments is invalid
     * @since 9
     */
    // 内存数据拷贝
    public void copySwapMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes, long elemSize) {
        copySwapMemoryChecks(srcBase, srcOffset, destBase, destOffset, bytes, elemSize);
        
        if(bytes == 0) {
            return;
        }
        
        copySwapMemory0(srcBase, srcOffset, destBase, destOffset, bytes, elemSize);
    }
    
    /**
     * Disposes of a block of native memory, as obtained from {@link
     * #allocateMemory} or {@link #reallocateMemory}.  The address passed to
     * this method may be null, in which case no action is taken.
     *
     * <em>Note:</em> It is the resposibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @throws RuntimeException if any of the arguments is invalid
     * @see #allocateMemory
     */
    // 用于释放allocateMemory和reallocateMemory申请的内存
    public void freeMemory(long address) {
        freeMemoryChecks(address);
        
        if(address == 0) {
            return;
        }
        
        freeMemory0(address);
    }
    
    
    
    /** @see #getAddress(Object, long) */
    // 从本地内存地址address处获取一个本地指针值
    @ForceInline
    public long getAddress(long address) {
        return getAddress(null, address);
    }
    
    /**
     * Fetches a native pointer from a given memory address.  If the address is
     * zero, or does not point into a block obtained from {@link
     * #allocateMemory}, the results are undefined.
     *
     * <p>If the native pointer is less than 64 bits wide, it is extended as
     * an unsigned number to a Java long.  The pointer may be indexed by any
     * given byte offset, simply by adding that offset (as a simple integer) to
     * the long representing the pointer.  The number of bytes actually read
     * from the target address may be determined by consulting {@link
     * #addressSize}.
     *
     * @see #allocateMemory
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的long型字段的值
    @ForceInline
    public long getAddress(Object o, long offset) {
        // 如果本机指针的宽度小于8字节 则将其作为无符号数扩展为Java的long类型
        if (ADDRESS_SIZE == 4) {
            // 获取对象o中offset地址处对应的int型字段的值
            int x = getInt(o, offset);
            // 转为无符号的long
            return Integer.toUnsignedLong(x);
        } else {
            // 获取对象o中offset地址处对应的long型字段的值
            return getLong(o, offset);
        }
    }
    
    /** @see #putAddress(Object, long, long) */
    // 向本地内存地址address处存入一个本地指针值x
    @ForceInline
    public void putAddress(long address, long x) {
        putAddress(null, address, x);
    }
    
    /**
     * Stores a native pointer into a given memory address.  If the address is
     * zero, or does not point into a block obtained from {@link
     * #allocateMemory}, the results are undefined.
     *
     * <p>The number of bytes actually written at the target address may be
     * determined by consulting {@link #addressSize}.
     *
     * @see #allocateMemory
     * @see #putInt(Object, long, int)
     */
    // 向对象o中offset地址处存入long型字段的值
    @ForceInline
    public void putAddress(Object o, long offset, long x) {
        if (ADDRESS_SIZE == 4) {
            putInt(o, offset, (int)x);
        } else {
            putLong(o, offset, x);
        }
    }
    
    /**
     * Reports the size in bytes of a native pointer, as stored via {@link #putAddress}.
     * This value will be either 4 or 8.
     * Note that the sizes of other primitive types (as stored in native memory blocks) is determined fully by their information content.
     */
    // 检查通过{@link #putAddress}存储的本机指针的大小(以字节为单位)。此值为4或8。请注意 其他基本类型的大小(存储在本机内存块中)完全由其信息内容决定。
    public int addressSize() {
        return ADDRESS_SIZE;
    }
    
    
    
    private native long allocateMemory0(long bytes);
    private native long reallocateMemory0(long address, long bytes);
    private native void setMemory0(Object o, long offset, long bytes, byte value);
    @HotSpotIntrinsicCandidate
    private native void copyMemory0(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes);
    private native void copySwapMemory0(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes, long elemSize);
    private native void freeMemory0(long address);
    private native int addressSize0();
    
    /*▲ 本地内存操作 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /* 获取/设置字段值 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ */
    
    /*
     * (1.1) 基于JVM内存地址 获取/设置字段值
     * - getXXX(o, offset)
     *    获取对象o中offset地址处对应的字段值
     *    对象o可以是数组
     *    offset的值由#objectFieldOffset或#staticFieldOffset获取
     *    也可以由#arrayBaseOffset[B]和#arrayIndexScale[S]共同构成:B + N * S
     *
     * - putXXX(o, offset, x)
     *     设置对象o中offset地址处对应的字段为新值x
     *
     * (1.2) 基于本地内存地址 获取/设置字段值
     * - getXXX(address)
     * - putXXX(address, x)
     *
     * (2) 基于JVM内存地址 获取/设置字段值 Opaque版本
     * - getXXXOpaque(o, offset)
     * - putXXXOpaque(o, offset, x)
     *
     * (3) 基于JVM内存地址 获取/设置字段值 Release/Acquire版本
     * - getXXXAcquire(o, offset)
     * - putXXXRelease(o, offset, x)
     *
     * (4) 基于JVM内存地址 获取/设置字段值 Volatile版本
     * - getXXXVolatile(o, offset)
     * - putXXXVolatile(o, offset, x)
     */
    
    /*▼ (1.1) getXXX/putXXX 获取/设置字段值(基于JVM内存地址) ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的int型字段的值
    @HotSpotIntrinsicCandidate
    public native byte getByte(Object o, long offset);
    
    /**
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的short型字段的值
    @HotSpotIntrinsicCandidate
    public native short getShort(Object o, long offset);
    
    /**
     * Fetches a value from a given Java variable.
     * More specifically, fetches a field or array element within the given
     * object {@code o} at the given offset, or (if {@code o} is null)
     * from the memory address whose numerical value is the given offset.
     * <p>
     * The results are undefined unless one of the following cases is true:
     * <ul>
     * <li>The offset was obtained from {@link #objectFieldOffset} on
     * the {@link java.lang.reflect.Field} of some Java field and the object
     * referred to by {@code o} is of a class compatible with that
     * field's class.
     *
     * <li>The offset and object reference {@code o} (either null or
     * non-null) were both obtained via {@link #staticFieldOffset}
     * and {@link #staticFieldBase} (respectively) from the
     * reflective {@link Field} representation of some Java field.
     *
     * <li>The object referred to by {@code o} is an array, and the offset
     * is an integer of the form {@code B+N*S}, where {@code N} is
     * a valid index into the array, and {@code B} and {@code S} are
     * the values obtained by {@link #arrayBaseOffset} and {@link
     * #arrayIndexScale} (respectively) from the array's class.  The value
     * referred to is the {@code N}<em>th</em> element of the array.
     *
     * </ul>
     * <p>
     * If one of the above cases is true, the call references a specific Java
     * variable (field or array element).  However, the results are undefined
     * if that variable is not in fact of the type returned by this method.
     * <p>
     * This method refers to a variable by means of two parameters, and so
     * it provides (in effect) a <em>double-register</em> addressing mode
     * for Java variables.  When the object reference is null, this method
     * uses its offset as an absolute address.  This is similar in operation
     * to methods such as {@link #getInt(long)}, which provide (in effect) a
     * <em>single-register</em> addressing mode for non-Java variables.
     * However, because Java variables may have a different layout in memory
     * from non-Java variables, programmers should not assume that these
     * two addressing modes are ever equivalent.  Also, programmers should
     * remember that offsets from the double-register addressing mode cannot
     * be portably confused with longs used in the single-register addressing
     * mode.
     *
     * @param o      Java heap object in which the variable resides, if any, else
     *               null
     * @param offset indication of where the variable resides in a Java heap
     *               object, if any, else a memory address locating the variable
     *               statically
     *
     * @return the value fetched from the indicated Java variable
     *
     * @throws RuntimeException No defined exceptions are thrown, not even
     *                          {@link NullPointerException}
     */
    // 获取对象o中offset地址处对应的int型字段的值
    @HotSpotIntrinsicCandidate
    public native int getInt(Object o, long offset);
    
    /**
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的long型字段的值
    @HotSpotIntrinsicCandidate
    public native long getLong(Object o, long offset);
    
    /**
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的float型字段的值
    @HotSpotIntrinsicCandidate
    public native float getFloat(Object o, long offset);
    
    /**
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的double型字段的值
    @HotSpotIntrinsicCandidate
    public native double getDouble(Object o, long offset);
    
    /**
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的char型字段的值
    @HotSpotIntrinsicCandidate
    public native char getChar(Object o, long offset);
    
    /**
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的boolean型字段的值
    @HotSpotIntrinsicCandidate
    public native boolean getBoolean(Object o, long offset);
    
    /**
     * Fetches a reference value from a given Java variable.
     *
     * @see #getInt(Object, long)
     */
    // 获取对象o中offset地址处对应的引用类型字段的值
    @HotSpotIntrinsicCandidate
    public native Object getObject(Object o, long offset);
    
    
    
    /**
     * @see #putInt(Object, long, int)
     */
    // 设置对象o中offset地址处对应的byte型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putByte(Object o, long offset, byte x);
    
    /**
     * @see #putInt(Object, long, int)
     */
    // 设置对象o中offset地址处对应的short型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putShort(Object o, long offset, short x);
    
    /**
     * Stores a value into a given Java variable.
     * <p>
     * The first two parameters are interpreted exactly as with
     * {@link #getInt(Object, long)} to refer to a specific
     * Java variable (field or array element).  The given value
     * is stored into that variable.
     * <p>
     * The variable must be of the same type as the method
     * parameter {@code x}.
     *
     * @param o      Java heap object in which the variable resides, if any, else
     *               null
     * @param offset indication of where the variable resides in a Java heap
     *               object, if any, else a memory address locating the variable
     *               statically
     * @param x      the value to store into the indicated Java variable
     *
     * @throws RuntimeException No defined exceptions are thrown, not even
     *                          {@link NullPointerException}
     */
    // 设置对象o中offset地址处对应的int型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putInt(Object o, long offset, int x);
    
    /**
     * @see #putInt(Object, long, int)
     */
    // 设置对象o中offset地址处对应的long型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putLong(Object o, long offset, long x);
    
    /**
     * @see #putInt(Object, long, int)
     */
    // 设置对象o中offset地址处对应的float型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putFloat(Object o, long offset, float x);
    
    /**
     * @see #putInt(Object, long, int)
     */
    // 设置对象o中offset地址处对应的double型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putDouble(Object o, long offset, double x);
    
    /**
     * @see #putInt(Object, long, int)
     */
    // 设置对象o中offset地址处对应的char型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putChar(Object o, long offset, char x);
    
    /**
     * @see #putInt(Object, long, int)
     */
    // 设置对象o中offset地址处对应的boolean型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putBoolean(Object o, long offset, boolean x);
    
    /**
     * Stores a reference value into a given Java variable.
     * <p>
     * Unless the reference {@code x} being stored is either null
     * or matches the field type, the results are undefined.
     * If the reference {@code o} is non-null, card marks or
     * other store barriers for that object (if the VM requires them)
     * are updated.
     *
     * @see #putInt(Object, long, int)
     */
    // 设置对象o中offset地址处对应的引用类型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putObject(Object o, long offset, Object x);
    
    /*▲ (1.1) getXXX/putXXX 获取/设置字段值(基于JVM内存地址) ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.2) getXXX/putXXX 获取/设置字段值(基于本地内存地址) ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Fetches a value from a given memory address.  If the address is zero, or
     * does not point into a block obtained from {@link #allocateMemory}, the
     * results are undefined.
     *
     * @see #allocateMemory
     */
    // 获取本地内存中address地址处对应的byte类型字段的值
    @ForceInline
    public byte getByte(long address) {
        return getByte(null, address);
    }
    
    /** @see #getByte(long) */
    // 获取本地内存中address地址处对应的short类型字段的值
    @ForceInline
    public short getShort(long address) {
        return getShort(null, address);
    }
    
    /** @see #getByte(long) */
    // 获取本地内存中address地址处对应的int类型字段的值
    @ForceInline
    public int getInt(long address) {
        return getInt(null, address);
    }
    
    /** @see #getByte(long) */
    // 获取本地内存中address地址处对应的long类型字段的值
    @ForceInline
    public long getLong(long address) {
        return getLong(null, address);
    }
    
    /** @see #getByte(long) */
    // 获取本地内存中address地址处对应的float类型字段的值
    @ForceInline
    public float getFloat(long address) {
        return getFloat(null, address);
    }
    
    /** @see #getByte(long) */
    // 获取本地内存中address地址处对应的double类型字段的值
    @ForceInline
    public double getDouble(long address) {
        return getDouble(null, address);
    }
    
    /** @see #getByte(long) */
    // 获取本地内存中address地址处对应的char类型字段的值
    @ForceInline
    public char getChar(long address) {
        return getChar(null, address);
    }
    
    
    
    /**
     * Stores a value into a given memory address.  If the address is zero, or
     * does not point into a block obtained from {@link #allocateMemory}, the
     * results are undefined.
     *
     * @see #getByte(long)
     */
    // 设置本地内存中address地址处对应的byte型字段为新值x
    @ForceInline
    public void putByte(long address, byte x) {
        putByte(null, address, x);
    }
    
    /** @see #putByte(long, byte) */
    // 设置本地内存中address地址处对应的short型字段为新值x
    @ForceInline
    public void putShort(long address, short x) {
        putShort(null, address, x);
    }
    
    /** @see #putByte(long, byte) */
    // 设置本地内存中address地址处对应的int型字段为新值x
    @ForceInline
    public void putInt(long address, int x) {
        putInt(null, address, x);
    }
    
    /** @see #putByte(long, byte) */
    // 设置本地内存中address地址处对应的long型字段为新值x
    @ForceInline
    public void putLong(long address, long x) {
        putLong(null, address, x);
    }
    
    /** @see #putByte(long, byte) */
    // 设置本地内存中address地址处对应的float型字段为新值x
    @ForceInline
    public void putFloat(long address, float x) {
        putFloat(null, address, x);
    }
    
    /** @see #putByte(long, byte) */
    // 设置本地内存中address地址处对应的double型字段为新值x
    @ForceInline
    public void putDouble(long address, double x) {
        putDouble(null, address, x);
    }
    
    /** @see #putByte(long, byte) */
    // 设置本地内存中address地址处对应的char型字段为新值x
    @ForceInline
    public void putChar(long address, char x) {
        putChar(null, address, x);
    }
    
    /*▲ (1.2) getXXX/putXXX 获取/设置字段值(基于本地内存地址) ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (2) getXXXOpaque/putXXXOpaque 获取/设置字段值 Opaque版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Opaque version of {@link #getByteVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的byte型字段的值
    @HotSpotIntrinsicCandidate
    public final byte getByteOpaque(Object o, long offset) {
        return getByteVolatile(o, offset);
    }
    
    /**
     * Opaque version of {@link #getShortVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的short型字段的值
    @HotSpotIntrinsicCandidate
    public final short getShortOpaque(Object o, long offset) {
        return getShortVolatile(o, offset);
    }
    
    /**
     * Opaque version of {@link #getIntVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的int型字段的值
    @HotSpotIntrinsicCandidate
    public final int getIntOpaque(Object o, long offset) {
        return getIntVolatile(o, offset);
    }
    
    /**
     * Opaque version of {@link #getLongVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的long型字段的值
    @HotSpotIntrinsicCandidate
    public final long getLongOpaque(Object o, long offset) {
        return getLongVolatile(o, offset);
    }
    
    /**
     * Opaque version of {@link #getFloatVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的float型字段的值
    @HotSpotIntrinsicCandidate
    public final float getFloatOpaque(Object o, long offset) {
        return getFloatVolatile(o, offset);
    }
    
    /**
     * Opaque version of {@link #getDoubleVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的double型字段的值
    @HotSpotIntrinsicCandidate
    public final double getDoubleOpaque(Object o, long offset) {
        return getDoubleVolatile(o, offset);
    }
    
    /**
     * Opaque version of {@link #getCharVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的char型字段的值
    @HotSpotIntrinsicCandidate
    public final char getCharOpaque(Object o, long offset) {
        return getCharVolatile(o, offset);
    }
    
    /**
     * Opaque version of {@link #getBooleanVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的boolean型字段的值
    @HotSpotIntrinsicCandidate
    public final boolean getBooleanOpaque(Object o, long offset) {
        return getBooleanVolatile(o, offset);
    }
    
    /**
     * Opaque version of {@link #getObjectVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的引用类型字段的值
    @HotSpotIntrinsicCandidate
    public final Object getObjectOpaque(Object o, long offset) {
        return getObjectVolatile(o, offset);
    }
    
    
    
    /**
     * Opaque version of {@link #putByteVolatile(Object, long, byte)}
     */
    // 设置对象o中offset地址处对应的byte型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putByteOpaque(Object o, long offset, byte x) {
        putByteVolatile(o, offset, x);
    }
    
    /**
     * Opaque version of {@link #putShortVolatile(Object, long, short)}
     */
    // 设置对象o中offset地址处对应的short型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putShortOpaque(Object o, long offset, short x) {
        putShortVolatile(o, offset, x);
    }
    
    /**
     * Opaque version of {@link #putIntVolatile(Object, long, int)}
     */
    // 设置对象o中offset地址处对应的int型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putIntOpaque(Object o, long offset, int x) {
        putIntVolatile(o, offset, x);
    }
    
    /**
     * Opaque version of {@link #putLongVolatile(Object, long, long)}
     */
    // 设置对象o中offset地址处对应的long型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putLongOpaque(Object o, long offset, long x) {
        putLongVolatile(o, offset, x);
    }
    
    /**
     * Opaque version of {@link #putFloatVolatile(Object, long, float)}
     */
    // 设置对象o中offset地址处对应的float型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putFloatOpaque(Object o, long offset, float x) {
        putFloatVolatile(o, offset, x);
    }
    
    /**
     * Opaque version of {@link #putDoubleVolatile(Object, long, double)}
     */
    // 设置对象o中offset地址处对应的double型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putDoubleOpaque(Object o, long offset, double x) {
        putDoubleVolatile(o, offset, x);
    }
    
    /**
     * Opaque version of {@link #putCharVolatile(Object, long, char)}
     */
    // 设置对象o中offset地址处对应的char型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putCharOpaque(Object o, long offset, char x) {
        putCharVolatile(o, offset, x);
    }
    
    /**
     * Opaque version of {@link #putBooleanVolatile(Object, long, boolean)}
     */
    // 设置对象o中offset地址处对应的boolean型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putBooleanOpaque(Object o, long offset, boolean x) {
        putBooleanVolatile(o, offset, x);
    }
    
    /**
     * Opaque version of {@link #putObjectVolatile(Object, long, Object)}
     */
    // 设置对象o中offset地址处对应的引用类型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putObjectOpaque(Object o, long offset, Object x) {
        putObjectVolatile(o, offset, x);
    }
    
    /*▲ (2) getXXXOpaque/putXXXOpaque 获取/设置字段值 Opaque版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (3) getXXXAcquire/putXXXRelease 获取/设置字段值 Release/Acquire版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Acquire version of {@link #getByteVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的byte型字段的值
    @HotSpotIntrinsicCandidate
    public final byte getByteAcquire(Object o, long offset) {
        return getByteVolatile(o, offset);
    }
    
    /**
     * Acquire version of {@link #getShortVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的short型字段的值
    @HotSpotIntrinsicCandidate
    public final short getShortAcquire(Object o, long offset) {
        return getShortVolatile(o, offset);
    }
    
    /**
     * Acquire version of {@link #getIntVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的int型字段的值
    @HotSpotIntrinsicCandidate
    public final int getIntAcquire(Object o, long offset) {
        return getIntVolatile(o, offset);
    }
    
    /**
     * Acquire version of {@link #getLongVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的long型字段的值
    @HotSpotIntrinsicCandidate
    public final long getLongAcquire(Object o, long offset) {
        return getLongVolatile(o, offset);
    }
    
    /**
     * Acquire version of {@link #getFloatVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的float型字段的值
    @HotSpotIntrinsicCandidate
    public final float getFloatAcquire(Object o, long offset) {
        return getFloatVolatile(o, offset);
    }
    
    /**
     * Acquire version of {@link #getDoubleVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的double型字段的值
    @HotSpotIntrinsicCandidate
    public final double getDoubleAcquire(Object o, long offset) {
        return getDoubleVolatile(o, offset);
    }
    
    /**
     * Acquire version of {@link #getCharVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的char型字段的值
    @HotSpotIntrinsicCandidate
    public final char getCharAcquire(Object o, long offset) {
        return getCharVolatile(o, offset);
    }
    
    /**
     * Acquire version of {@link #getBooleanVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的boolean型字段的值
    @HotSpotIntrinsicCandidate
    public final boolean getBooleanAcquire(Object o, long offset) {
        return getBooleanVolatile(o, offset);
    }
    
    /**
     * Acquire version of {@link #getObjectVolatile(Object, long)}
     */
    // 获取对象o中offset地址处对应的引用类型字段的值
    @HotSpotIntrinsicCandidate
    public final Object getObjectAcquire(Object o, long offset) {
        return getObjectVolatile(o, offset);
    }
    
    
    
    /*
     * Versions of {@link #putObjectVolatile(Object, long, Object)} that do not guarantee immediate visibility of the store to other threads.
     * This method is generally only useful if the underlying field is a Java volatile
     * (or if an array cell, one that is otherwise only accessed using volatile accesses).
     *
     * Corresponds to C11 atomic_store_explicit(..., memory_order_release).
     */
    
    /** Release version of {@link #putByteVolatile(Object, long, byte)} */
    // 设置对象o中offset地址处对应的byte型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putByteRelease(Object o, long offset, byte x) {
        putByteVolatile(o, offset, x);
    }
    
    /** Release version of {@link #putShortVolatile(Object, long, short)} */
    // 设置对象o中offset地址处对应的short型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putShortRelease(Object o, long offset, short x) {
        putShortVolatile(o, offset, x);
    }
    
    /** Release version of {@link #putIntVolatile(Object, long, int)} */
    // 设置对象o中offset地址处对应的int型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putIntRelease(Object o, long offset, int x) {
        putIntVolatile(o, offset, x);
    }
    
    /** Release version of {@link #putLongVolatile(Object, long, long)} */
    // 设置对象o中offset地址处对应的long型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putLongRelease(Object o, long offset, long x) {
        putLongVolatile(o, offset, x);
    }
    
    /** Release version of {@link #putFloatVolatile(Object, long, float)} */
    // 设置对象o中offset地址处对应的float型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putFloatRelease(Object o, long offset, float x) {
        putFloatVolatile(o, offset, x);
    }
    
    /** Release version of {@link #putDoubleVolatile(Object, long, double)} */
    // 设置对象o中offset地址处对应的double型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putDoubleRelease(Object o, long offset, double x) {
        putDoubleVolatile(o, offset, x);
    }
    
    /** Release version of {@link #putCharVolatile(Object, long, char)} */
    // 设置对象o中offset地址处对应的char型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putCharRelease(Object o, long offset, char x) {
        putCharVolatile(o, offset, x);
    }
    
    /** Release version of {@link #putBooleanVolatile(Object, long, boolean)} */
    // 设置对象o中offset地址处对应的boolean型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putBooleanRelease(Object o, long offset, boolean x) {
        putBooleanVolatile(o, offset, x);
    }
    
    /** Release version of {@link #putObjectVolatile(Object, long, Object)} */
    // 设置对象o中offset地址处对应的引用类型字段为新值x
    @HotSpotIntrinsicCandidate
    public final void putObjectRelease(Object o, long offset, Object x) {
        putObjectVolatile(o, offset, x);
    }
    
    /*▲ (3) getXXXAcquire/putXXXRelease 获取/设置字段值 Release/Acquire版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (4) getXXXVolatile/putXXXVolatile 获取/设置字段值 Volatile版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Volatile version of {@link #getByte(Object, long)}
     */
    // 获取对象o中offset地址处对应的byte型字段的值
    @HotSpotIntrinsicCandidate
    public native byte getByteVolatile(Object o, long offset);
    
    /**
     * Volatile version of {@link #getShort(Object, long)}
     */
    // 获取对象o中offset地址处对应的short型字段的值
    @HotSpotIntrinsicCandidate
    public native short getShortVolatile(Object o, long offset);
    
    /**
     * Volatile version of {@link #getInt(Object, long)}
     */
    // 获取对象o中offset地址处对应的int型字段的值
    @HotSpotIntrinsicCandidate
    public native int getIntVolatile(Object o, long offset);
    
    /**
     * Volatile version of {@link #getLong(Object, long)}
     */
    // 获取对象o中offset地址处对应的long型字段的值
    @HotSpotIntrinsicCandidate
    public native long getLongVolatile(Object o, long offset);
    
    /**
     * Volatile version of {@link #getFloat(Object, long)}
     */
    // 获取对象o中offset地址处对应的float型字段的值
    @HotSpotIntrinsicCandidate
    public native float getFloatVolatile(Object o, long offset);
    
    /**
     * Volatile version of {@link #getDouble(Object, long)}
     */
    // 获取对象o中offset地址处对应的double型字段的值
    @HotSpotIntrinsicCandidate
    public native double getDoubleVolatile(Object o, long offset);
    
    /**
     * Volatile version of {@link #getChar(Object, long)}
     */
    // 获取对象o中offset地址处对应的char型字段的值
    @HotSpotIntrinsicCandidate
    public native char getCharVolatile(Object o, long offset);
    
    /**
     * Volatile version of {@link #getBoolean(Object, long)}
     */
    // 获取对象o中offset地址处对应的boolean型字段的值
    @HotSpotIntrinsicCandidate
    public native boolean getBooleanVolatile(Object o, long offset);
    
    /**
     * Fetches a reference value from a given Java variable, with volatile
     * load semantics. Otherwise identical to {@link #getObject(Object, long)}
     */
    // 获取对象o中offset地址处对应的引用类型字段的值
    @HotSpotIntrinsicCandidate
    public native Object getObjectVolatile(Object o, long offset);
    
    
    
    /**
     * Volatile version of {@link #putByte(Object, long, byte)}
     */
    // 设置对象o中offset地址处对应的byte型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putByteVolatile(Object o, long offset, byte x);
    
    /**
     * Volatile version of {@link #putShort(Object, long, short)}
     */
    // 设置对象o中offset地址处对应的short型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putShortVolatile(Object o, long offset, short x);
    
    /**
     * Volatile version of {@link #putInt(Object, long, int)}
     */
    // 设置对象o中offset地址处对应的int型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putIntVolatile(Object o, long offset, int x);
    
    /**
     * Volatile version of {@link #putLong(Object, long, long)}
     */
    // 设置对象o中offset地址处对应的long型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putLongVolatile(Object o, long offset, long x);
    
    /**
     * Volatile version of {@link #putFloat(Object, long, float)}
     */
    // 设置对象o中offset地址处对应的float型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putFloatVolatile(Object o, long offset, float x);
    
    /**
     * Volatile version of {@link #putDouble(Object, long, double)}
     */
    // 设置对象o中offset地址处对应的double型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putDoubleVolatile(Object o, long offset, double x);
    
    /**
     * Volatile version of {@link #putChar(Object, long, char)}
     */
    // 设置对象o中offset地址处对应的char型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putCharVolatile(Object o, long offset, char x);
    
    /**
     * Volatile version of {@link #putBoolean(Object, long, boolean)}
     */
    // 设置对象o中offset地址处对应的boolean型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putBooleanVolatile(Object o, long offset, boolean x);
    
    /**
     * Stores a reference value into a given Java variable, with
     * volatile store semantics. Otherwise identical to {@link #putObject(Object, long, Object)}
     */
    // 设置对象o中offset地址处对应的引用类型字段为新值x
    @HotSpotIntrinsicCandidate
    public native void putObjectVolatile(Object o, long offset, Object x);
    
    /*▲ (4) getXXXVolatile/putXXXVolatile 获取/设置字段值 Volatile版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ 对数组字段/变量直接操作 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Reports the offset of the first element in the storage allocation of a
     * given array class.  If {@link #arrayIndexScale} returns a non-zero value
     * for the same class, you may use that scale factor, together with this
     * base offset, to form new offsets to access elements of arrays of the
     * given class.
     *
     * @see #getInt(Object, long)
     * @see #putInt(Object, long, int)
     */
    // 寻找某类型数组中的元素时约定的起始偏移地址(更像是一个标记) 与#arrayIndexScale配合使用
    public int arrayBaseOffset(Class<?> arrayClass) {
        if(arrayClass == null) {
            throw new NullPointerException();
        }
        
        return arrayBaseOffset0(arrayClass);
    }
    
    /**
     * Reports the scale factor for addressing elements in the storage
     * allocation of a given array class.  However, arrays of "narrow" types
     * will generally not work properly with accessors like {@link
     * #getByte(Object, long)}, so the scale factor for such classes is reported
     * as zero.
     *
     * @see #arrayBaseOffset
     * @see #getInt(Object, long)
     * @see #putInt(Object, long, int)
     */
    // 某类型数组每个元素所占字节数 与#arrayBaseOffset配合使用
    public int arrayIndexScale(Class<?> arrayClass) {
        if(arrayClass == null) {
            throw new NullPointerException();
        }
        
        return arrayIndexScale0(arrayClass);
    }
    
    private native int arrayBaseOffset0(Class<?> arrayClass);
    private native int arrayIndexScale0(Class<?> arrayClass);
    
    /*▲ 对数组字段/变量直接操作 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    /* 获取/设置字段值 ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ */
    
    
    
    
    
    
    /* 原子操作 设置值 基于JVM内存操作 属于乐观锁&自旋锁 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ */
    
    /*
     * (1.1) 设置值 Release版本
     * - getAndSetXXXRelease(o, offset, newValue)
     *   返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
     *   设置新值newValue的时候 要保证该字段修改过程中没有被其他线程修改 否则不断自旋 直到成功修改
     *
     * (1.2) 设置值 Acquire版本
     * - getAndSetXXXAcquire(o, offset, newValue)
     *
     * (1.3) 设置值 Volatile版本
     * - getAndSetXXX(o, offset, newValue)
     *
     *
     *
     * (2.1) 设置值
     * - compareAndExchangeXXX(o, offset, expected, x)
     *   拿对象o中offset地址的field值与期望值expected作比较(内存值可能被其他线程修改掉 所以需要比较)。
     *   如果field值与期望值相等 则设置field为x 否则不断重试 直到成功。
     *   返回的是期望值。
     *
     * (2.2) 设置值 Release版本
     * - compareAndExchangeXXX(o, offset, expected, x)
     *
     * (2.3) 设置值 Acquire版本
     * - compareAndExchangeXXX(o, offset, expected, x)
     */
    
    /*▼ (1.1) getAndSetXXXRelease 设置值 Release版本 ████████████████████████████████████████████████████████████████████████████████┓ */

    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final byte getAndSetByteRelease(Object o, long offset, byte newValue) {
        byte v;
        do {
            v = getByte(o, offset);
        } while (!weakCompareAndSetByteRelease(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final short getAndSetShortRelease(Object o, long offset, short newValue) {
        short v;
        do {
            v = getShort(o, offset);
        } while (!weakCompareAndSetShortRelease(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final int getAndSetIntRelease(Object o, long offset, int newValue) {
        int v;
        do {
            v = getInt(o, offset);
        } while (!weakCompareAndSetIntRelease(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final long getAndSetLongRelease(Object o, long offset, long newValue) {
        long v;
        do {
            v = getLong(o, offset);
        } while (!weakCompareAndSetLongRelease(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final float getAndSetFloatRelease(Object o, long offset, float newValue) {
        int v = getAndSetIntRelease(o, offset, Float.floatToRawIntBits(newValue));
        return Float.intBitsToFloat(v);
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final double getAndSetDoubleRelease(Object o, long offset, double newValue) {
        long v = getAndSetLongRelease(o, offset, Double.doubleToRawLongBits(newValue));
        return Double.longBitsToDouble(v);
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final char getAndSetCharRelease(Object o, long offset, char newValue) {
        return s2c(getAndSetShortRelease(o, offset, c2s(newValue)));
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final boolean getAndSetBooleanRelease(Object o, long offset, boolean newValue) {
        return byte2bool(getAndSetByteRelease(o, offset, bool2byte(newValue)));
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final Object getAndSetObjectRelease(Object o, long offset, Object newValue) {
        Object v;
        do {
            v = getObject(o, offset);
        } while (!weakCompareAndSetObjectRelease(o, offset, v, newValue));
        return v;
    }
    
    /*▲ (1.1) getAndSetXXXRelease 设置值 Release版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.2) getAndSetXXXAcquire 设置值 Acquire版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final byte getAndSetByteAcquire(Object o, long offset, byte newValue) {
        byte v;
        do {
            v = getByteAcquire(o, offset);
        } while (!weakCompareAndSetByteAcquire(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final short getAndSetShortAcquire(Object o, long offset, short newValue) {
        short v;
        do {
            v = getShortAcquire(o, offset);
        } while (!weakCompareAndSetShortAcquire(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final int getAndSetIntAcquire(Object o, long offset, int newValue) {
        int v;
        do {
            v = getIntAcquire(o, offset);
        } while (!weakCompareAndSetIntAcquire(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final long getAndSetLongAcquire(Object o, long offset, long newValue) {
        long v;
        do {
            v = getLongAcquire(o, offset);
        } while (!weakCompareAndSetLongAcquire(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final float getAndSetFloatAcquire(Object o, long offset, float newValue) {
        int v = getAndSetIntAcquire(o, offset, Float.floatToRawIntBits(newValue));
        return Float.intBitsToFloat(v);
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final double getAndSetDoubleAcquire(Object o, long offset, double newValue) {
        long v = getAndSetLongAcquire(o, offset, Double.doubleToRawLongBits(newValue));
        return Double.longBitsToDouble(v);
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final char getAndSetCharAcquire(Object o, long offset, char newValue) {
        return s2c(getAndSetShortAcquire(o, offset, c2s(newValue)));
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final boolean getAndSetBooleanAcquire(Object o, long offset, boolean newValue) {
        return byte2bool(getAndSetByteAcquire(o, offset, bool2byte(newValue)));
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final Object getAndSetObjectAcquire(Object o, long offset, Object newValue) {
        Object v;
        do {
            v = getObjectAcquire(o, offset);
        } while (!weakCompareAndSetObjectAcquire(o, offset, v, newValue));
        return v;
    }
    
    /*▲ (1.2) getAndSetXXXAcquire 设置值 Acquire版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.3) getAndSetXXX 设置值 Volatile版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @HotSpotIntrinsicCandidate
    public final byte getAndSetByte(Object o, long offset, byte newValue) {
        byte v;
        do {
            v = getByteVolatile(o, offset);
        } while (!weakCompareAndSetByte(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @HotSpotIntrinsicCandidate
    public final short getAndSetShort(Object o, long offset, short newValue) {
        short v;
        do {
            v = getShortVolatile(o, offset);
        } while (!weakCompareAndSetShort(o, offset, v, newValue));
        return v;
    }
    
    /**
     * Atomically exchanges the given value with the current value of
     * a field or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param newValue new value
     * @return the previous value
     * @since 1.8
     */
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @HotSpotIntrinsicCandidate
    public final int getAndSetInt(Object o, long offset, int newValue) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!weakCompareAndSetInt(o, offset, v, newValue));
        return v;
    }
    
    /**
     * Atomically exchanges the given value with the current value of
     * a field or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param newValue new value
     * @return the previous value
     * @since 1.8
     */
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @HotSpotIntrinsicCandidate
    public final long getAndSetLong(Object o, long offset, long newValue) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!weakCompareAndSetLong(o, offset, v, newValue));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final float getAndSetFloat(Object o, long offset, float newValue) {
        int v = getAndSetInt(o, offset, Float.floatToRawIntBits(newValue));
        return Float.intBitsToFloat(v);
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final double getAndSetDouble(Object o, long offset, double newValue) {
        long v = getAndSetLong(o, offset, Double.doubleToRawLongBits(newValue));
        return Double.longBitsToDouble(v);
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final char getAndSetChar(Object o, long offset, char newValue) {
        return s2c(getAndSetShort(o, offset, c2s(newValue)));
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @ForceInline
    public final boolean getAndSetBoolean(Object o, long offset, boolean newValue) {
        return byte2bool(getAndSetByte(o, offset, bool2byte(newValue)));
    }
    
    /**
     * Atomically exchanges the given reference value with the current
     * reference value of a field or array element within the given
     * object {@code o} at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param newValue new value
     * @return the previous value
     * @since 1.8
     */
    // 返回对象o的offset地址处的值 并将该值原子性地设置为新值newValue
    @HotSpotIntrinsicCandidate
    public final Object getAndSetObject(Object o, long offset, Object newValue) {
        Object v;
        do {
            v = getObjectVolatile(o, offset);
        } while (!weakCompareAndSetObject(o, offset, v, newValue));
        return v;
    }
    
    /*▲ (1.3) getAndSetXXX 设置值 Volatile版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    /*▼ (2.1) compareAndExchangeXXX 设置值 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final byte compareAndExchangeByte(Object o, long offset, byte expected, byte x) {
        long wordOffset = offset & ~3;
        int shift = (int) (offset & 3) << 3;
        if(BE) {
            shift = 24 - shift;
        }
        int mask = 0xFF << shift;
        int maskedExpected = (expected & 0xFF) << shift;
        int maskedX = (x & 0xFF) << shift;
        int fullWord;
        do {
            fullWord = getIntVolatile(o, wordOffset);
            if((fullWord & mask) != maskedExpected)
                return (byte) ((fullWord & mask) >> shift);
        } while(!weakCompareAndSetInt(o, wordOffset, fullWord, (fullWord & ~mask) | maskedX));
        return expected;
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final short compareAndExchangeShort(Object o, long offset, short expected, short x) {
        if((offset & 3) == 3) {
            throw new IllegalArgumentException("Update spans the word, not supported");
        }
        long wordOffset = offset & ~3;
        int shift = (int) (offset & 3) << 3;
        if(BE) {
            shift = 16 - shift;
        }
        int mask = 0xFFFF << shift;
        int maskedExpected = (expected & 0xFFFF) << shift;
        int maskedX = (x & 0xFFFF) << shift;
        int fullWord;
        do {
            fullWord = getIntVolatile(o, wordOffset);
            if((fullWord & mask) != maskedExpected) {
                return (short) ((fullWord & mask) >> shift);
            }
        } while(!weakCompareAndSetInt(o, wordOffset, fullWord, (fullWord & ~mask) | maskedX));
        return expected;
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final native int compareAndExchangeInt(Object o, long offset, int expected, int x);
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final native long compareAndExchangeLong(Object o, long offset, long expected, long x);
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @ForceInline
    public final float compareAndExchangeFloat(Object o, long offset, float expected, float x) {
        int w = compareAndExchangeInt(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
        return Float.intBitsToFloat(w);
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @ForceInline
    public final double compareAndExchangeDouble(Object o, long offset, double expected, double x) {
        long w = compareAndExchangeLong(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
        return Double.longBitsToDouble(w);
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @ForceInline
    public final char compareAndExchangeChar(Object o, long offset, char expected, char x) {
        return s2c(compareAndExchangeShort(o, offset, c2s(expected), c2s(x)));
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @ForceInline
    public final boolean compareAndExchangeBoolean(Object o, long offset, boolean expected, boolean x) {
        return byte2bool(compareAndExchangeByte(o, offset, bool2byte(expected), bool2byte(x)));
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final native Object compareAndExchangeObject(Object o, long offset, Object expected, Object x);
    
    /*▲ (2.1) compareAndExchangeXXX 设置值 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (2.2) compareAndExchangeXXXRelease 设置值 Release版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final byte compareAndExchangeByteRelease(Object o, long offset, byte expected, byte x) {
        return compareAndExchangeByte(o, offset, expected, x);
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final short compareAndExchangeShortRelease(Object o, long offset, short expected, short x) {
        return compareAndExchangeShort(o, offset, expected, x);
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final int compareAndExchangeIntRelease(Object o, long offset, int expected, int x) {
        return compareAndExchangeInt(o, offset, expected, x);
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final long compareAndExchangeLongRelease(Object o, long offset, long expected, long x) {
        return compareAndExchangeLong(o, offset, expected, x);
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @ForceInline
    public final float compareAndExchangeFloatRelease(Object o, long offset, float expected, float x) {
        int w = compareAndExchangeIntRelease(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
        return Float.intBitsToFloat(w);
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @ForceInline
    public final double compareAndExchangeDoubleRelease(Object o, long offset, double expected, double x) {
        long w = compareAndExchangeLongRelease(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
        return Double.longBitsToDouble(w);
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @ForceInline
    public final char compareAndExchangeCharRelease(Object o, long offset, char expected, char x) {
        return s2c(compareAndExchangeShortRelease(o, offset, c2s(expected), c2s(x)));
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @ForceInline
    public final boolean compareAndExchangeBooleanRelease(Object o, long offset, boolean expected, boolean x) {
        return byte2bool(compareAndExchangeByteRelease(o, offset, bool2byte(expected), bool2byte(x)));
    }
    
    // 原子地设置对象o的offset地址处的值为x 返回期望值expected
    @HotSpotIntrinsicCandidate
    public final Object compareAndExchangeObjectRelease(Object o, long offset, Object expected, Object x) {
        return compareAndExchangeObject(o, offset, expected, x);
    }
    
    /*▲ (2.2) compareAndExchangeXXXRelease 设置值 Release版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (2.3) compareAndExchangeXXXAcquire 设置值 Acquire版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @HotSpotIntrinsicCandidate
    public final byte compareAndExchangeByteAcquire(Object o, long offset, byte expected, byte x) {
        return compareAndExchangeByte(o, offset, expected, x);
    }
    
    @HotSpotIntrinsicCandidate
    public final short compareAndExchangeShortAcquire(Object o, long offset, short expected, short x) {
        return compareAndExchangeShort(o, offset, expected, x);
    }
    
    @HotSpotIntrinsicCandidate
    public final int compareAndExchangeIntAcquire(Object o, long offset, int expected, int x) {
        return compareAndExchangeInt(o, offset, expected, x);
    }
    
    @HotSpotIntrinsicCandidate
    public final long compareAndExchangeLongAcquire(Object o, long offset, long expected, long x) {
        return compareAndExchangeLong(o, offset, expected, x);
    }
    
    @ForceInline
    public final float compareAndExchangeFloatAcquire(Object o, long offset, float expected, float x) {
        int w = compareAndExchangeIntAcquire(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
        return Float.intBitsToFloat(w);
    }
    
    @ForceInline
    public final double compareAndExchangeDoubleAcquire(Object o, long offset, double expected, double x) {
        long w = compareAndExchangeLongAcquire(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
        return Double.longBitsToDouble(w);
    }
    
    @ForceInline
    public final char compareAndExchangeCharAcquire(Object o, long offset, char expected, char x) {
        return s2c(compareAndExchangeShortAcquire(o, offset, c2s(expected), c2s(x)));
    }
    
    @ForceInline
    public final boolean compareAndExchangeBooleanAcquire(Object o, long offset, boolean expected, boolean x) {
        return byte2bool(compareAndExchangeByteAcquire(o, offset, bool2byte(expected), bool2byte(x)));
    }
    
    @HotSpotIntrinsicCandidate
    public final Object compareAndExchangeObjectAcquire(Object o, long offset, Object expected, Object x) {
        return compareAndExchangeObject(o, offset, expected, x);
    }
    
    /*▲ (2.3) compareAndExchangeXXXAcquire 设置值 Acquire版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    /* 原子操作 设置值 基于JVM内存操作 属于乐观锁&自旋锁 ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ */
    
    
    
    
    
    
    /* 原子操作 更新值 基于JVM内存操作 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ */
    
    /*
     * (1.1) 更新值 Plain版本
     * - weakCompareAndSetBytePlain(o, offset, expected, x)
     *   拿对象o中offset地址的field值与期望值expected作比较(内存值可能被其他线程修改掉 所以需要比较)。
     *   如果发现该值被修改 则返回false 否则 原子地更新该值为x 且返回true。
     *   @param offset   JVM内存偏移地址 对象o中某字段field的地址
     *   @param o        包含field值的对象
     *   @param expected field当前的期望值
     *   @param x        如果field的当前值与期望值expected相同 那么更新filed的值为这个新值x
     *   @return         更新成功返回true 更新失败返回false
     *
     * (1.2) 更新值 Release版本
     * - weakCompareAndSetXXXRelease(o, offset, expected, x)
     *
     * (1.3) 更新值 Acquire版本
     * - weakCompareAndSetXXXAcquire(o, offset, expected, x)
     *
     * (1.4) 更新值 Volatile版本
     * - weakCompareAndSetXXX(o, offset, expected, x)
     *
     *
     *
     * (2.1) 更新值
     * - compareAndSetXXX(o, offset, expected, x)
     */
    
    /*▼ (1.1) weakCompareAndSetXXXPlain 更新值 Plain版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetBytePlain(Object o, long offset, byte expected, byte x) {
        return weakCompareAndSetByte(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetShortPlain(Object o, long offset, short expected, short x) {
        return weakCompareAndSetShort(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetIntPlain(Object o, long offset, int expected, int x) {
        return compareAndSetInt(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetLongPlain(Object o, long offset, long expected, long x) {
        return compareAndSetLong(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetFloatPlain(Object o, long offset, float expected, float x) {
        return weakCompareAndSetIntPlain(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetDoublePlain(Object o, long offset, double expected, double x) {
        return weakCompareAndSetLongPlain(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetCharPlain(Object o, long offset, char expected, char x) {
        return weakCompareAndSetShortPlain(o, offset, c2s(expected), c2s(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetBooleanPlain(Object o, long offset, boolean expected, boolean x) {
        return weakCompareAndSetBytePlain(o, offset, bool2byte(expected), bool2byte(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetObjectPlain(Object o, long offset, Object expected, Object x) {
        return compareAndSetObject(o, offset, expected, x);
    }
    
    /*▲ (1.1) weakCompareAndSetXXXPlain 更新值 Plain版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.2) weakCompareAndSetXXXRelease 更新值 Release版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetIntRelease(Object o, long offset, int expected, int x) {
        return compareAndSetInt(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetByteRelease(Object o, long offset, byte expected, byte x) {
        return weakCompareAndSetByte(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetShortRelease(Object o, long offset, short expected, short x) {
        return weakCompareAndSetShort(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetCharRelease(Object o, long offset, char expected, char x) {
        return weakCompareAndSetShortRelease(o, offset, c2s(expected), c2s(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetLongRelease(Object o, long offset, long expected, long x) {
        return compareAndSetLong(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetFloatRelease(Object o, long offset, float expected, float x) {
        return weakCompareAndSetIntRelease(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetDoubleRelease(Object o, long offset, double expected, double x) {
        return weakCompareAndSetLongRelease(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetBooleanRelease(Object o, long offset, boolean expected, boolean x) {
        return weakCompareAndSetByteRelease(o, offset, bool2byte(expected), bool2byte(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetObjectRelease(Object o, long offset, Object expected, Object x) {
        return compareAndSetObject(o, offset, expected, x);
    }
    
    /*▲ (1.2) weakCompareAndSetXXXRelease 更新值 Release版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.3) weakCompareAndSetXXXAcquire 更新值 Acquire版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetByteAcquire(Object o, long offset, byte expected, byte x) {
        return weakCompareAndSetByte(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetShortAcquire(Object o, long offset, short expected, short x) {
        return weakCompareAndSetShort(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetIntAcquire(Object o, long offset, int expected, int x) {
        return compareAndSetInt(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetLongAcquire(Object o, long offset, long expected, long x) {
        return compareAndSetLong(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetFloatAcquire(Object o, long offset, float expected, float x) {
        return weakCompareAndSetIntAcquire(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetDoubleAcquire(Object o, long offset, double expected, double x) {
        return weakCompareAndSetLongAcquire(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetCharAcquire(Object o, long offset, char expected, char x) {
        return weakCompareAndSetShortAcquire(o, offset, c2s(expected), c2s(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetBooleanAcquire(Object o, long offset, boolean expected, boolean x) {
        return weakCompareAndSetByteAcquire(o, offset, bool2byte(expected), bool2byte(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetObjectAcquire(Object o, long offset, Object expected, Object x) {
        return compareAndSetObject(o, offset, expected, x);
    }
    
    /*▲ (1.3) weakCompareAndSetXXXAcquire 更新值 Acquire版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.4) weakCompareAndSetXXX 更新值 Volatile版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetByte(Object o, long offset, byte expected, byte x) {
        return compareAndSetByte(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetShort(Object o, long offset, short expected, short x) {
        return compareAndSetShort(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetInt(Object o, long offset, int expected, int x) {
        return compareAndSetInt(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetLong(Object o, long offset, long expected, long x) {
        return compareAndSetLong(o, offset, expected, x);
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetFloat(Object o, long offset, float expected, float x) {
        return weakCompareAndSetInt(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetDouble(Object o, long offset, double expected, double x) {
        return weakCompareAndSetLong(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetChar(Object o, long offset, char expected, char x) {
        return weakCompareAndSetShort(o, offset, c2s(expected), c2s(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean weakCompareAndSetBoolean(Object o, long offset, boolean expected, boolean x) {
        return weakCompareAndSetByte(o, offset, bool2byte(expected), bool2byte(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean weakCompareAndSetObject(Object o, long offset, Object expected, Object x) {
        return compareAndSetObject(o, offset, expected, x);
    }
    
    /*▲ (1.4) weakCompareAndSetXXX 更新值 Volatile版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    
    /*▼ (2.1) compareAndSetXXX 更新值 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean compareAndSetByte(Object o, long offset, byte expected, byte x) {
        return compareAndExchangeByte(o, offset, expected, x) == expected;
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final boolean compareAndSetShort(Object o, long offset, short expected, short x) {
        return compareAndExchangeShort(o, offset, expected, x) == expected;
    }
    
    /**
     * Atomically updates Java variable to {@code x} if it is currently holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong.
     *
     * @return {@code true} if successful
     */
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final native boolean compareAndSetInt(Object o, long offset, int expected, int x);
    
    /**
     * Atomically updates Java variable to {@code x} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong.
     *
     * @return {@code true} if successful
     */
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final native boolean compareAndSetLong(Object o, long offset, long expected, long x);
    
    /**
     * Atomically updates Java variable to {@code x} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong.
     *
     * @return {@code true} if successful
     */
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean compareAndSetFloat(Object o, long offset, float expected, float x) {
        return compareAndSetInt(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
    }
    
    /**
     * Atomically updates Java variable to {@code x} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong.
     *
     * @return {@code true} if successful
     */
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean compareAndSetDouble(Object o, long offset, double expected, double x) {
        return compareAndSetLong(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean compareAndSetChar(Object o, long offset, char expected, char x) {
        return compareAndSetShort(o, offset, c2s(expected), c2s(x));
    }
    
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @ForceInline
    public final boolean compareAndSetBoolean(Object o, long offset, boolean expected, boolean x) {
        return compareAndSetByte(o, offset, bool2byte(expected), bool2byte(x));
    }
    
    /**
     * Atomically updates Java variable to {@code x} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong.
     *
     * @return {@code true} if successful
     */
    // 拿期望值expected与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为x
    @HotSpotIntrinsicCandidate
    public final native boolean compareAndSetObject(Object o, long offset, Object expected, Object x);
    
    /*▲ (2.1) compareAndSetXXX 更新值 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    /* 原子操作 更新值 基于JVM内存操作 ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ */
    
    
    
    
    
    
    /* 原子操作 增减值 基于JVM内存操作 属于乐观锁&自旋锁 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ */
    
    /*
     * (1.1) 增减值 Release版本
     * - getAndAddXXXRelease(o, offset, newValue)
     *   返回对象o的offset地址处的值 并将该值原子性地增加delta(delta可以为负数)
     *   增减值的时候 要保证该字段增减过程中没有被其他线程修改 否则不断自旋 直到增减成功
     *
     * (1.2) 增减值 Acquire版本
     * - getAndAddXXXAcquire(o, offset, delta)
     *
     * (1.3) 增减值 Volatile版本
     * - getAndAddXXX(o, offset, delta)
     */
    
    /*▼ (1.1) getAndAddXXXRelease 增减值 Release版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final byte getAndAddByteRelease(Object o, long offset, byte delta) {
        byte v;
        do {
            v = getByte(o, offset);
        } while (!weakCompareAndSetByteRelease(o, offset, v, (byte) (v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final short getAndAddShortRelease(Object o, long offset, short delta) {
        short v;
        do {
            v = getShort(o, offset);
        } while (!weakCompareAndSetShortRelease(o, offset, v, (short) (v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final int getAndAddIntRelease(Object o, long offset, int delta) {
        int v;
        do {
            v = getInt(o, offset);
        } while (!weakCompareAndSetIntRelease(o, offset, v, v + delta));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final long getAndAddLongRelease(Object o, long offset, long delta) {
        long v;
        do {
            v = getLong(o, offset);
        } while (!weakCompareAndSetLongRelease(o, offset, v, v + delta));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final float getAndAddFloatRelease(Object o, long offset, float delta) {
        int expectedBits;
        float v;
        do {
            // Load and CAS with the raw bits to avoid issues with NaNs and
            // possible bit conversion from signaling NaNs to quiet NaNs that
            // may result in the loop not terminating.
            expectedBits = getInt(o, offset);
            v = Float.intBitsToFloat(expectedBits);
        } while (!weakCompareAndSetIntRelease(o, offset,
            expectedBits, Float.floatToRawIntBits(v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final double getAndAddDoubleRelease(Object o, long offset, double delta) {
        long expectedBits;
        double v;
        do {
            // Load and CAS with the raw bits to avoid issues with NaNs and
            // possible bit conversion from signaling NaNs to quiet NaNs that
            // may result in the loop not terminating.
            expectedBits = getLong(o, offset);
            v = Double.longBitsToDouble(expectedBits);
        } while (!weakCompareAndSetLongRelease(o, offset,
            expectedBits, Double.doubleToRawLongBits(v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final char getAndAddCharRelease(Object o, long offset, char delta) {
        return (char) getAndAddShortRelease(o, offset, (short) delta);
    }
    
    /*▲ (1.1) getAndAddXXXRelease 增减值 Release版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.2) getAndAddXXXAcquire 增减值 Acquire版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final byte getAndAddByteAcquire(Object o, long offset, byte delta) {
        byte v;
        do {
            v = getByteAcquire(o, offset);
        } while (!weakCompareAndSetByteAcquire(o, offset, v, (byte) (v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final short getAndAddShortAcquire(Object o, long offset, short delta) {
        short v;
        do {
            v = getShortAcquire(o, offset);
        } while (!weakCompareAndSetShortAcquire(o, offset, v, (short) (v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final int getAndAddIntAcquire(Object o, long offset, int delta) {
        int v;
        do {
            v = getIntAcquire(o, offset);
        } while (!weakCompareAndSetIntAcquire(o, offset, v, v + delta));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final long getAndAddLongAcquire(Object o, long offset, long delta) {
        long v;
        do {
            v = getLongAcquire(o, offset);
        } while (!weakCompareAndSetLongAcquire(o, offset, v, v + delta));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final float getAndAddFloatAcquire(Object o, long offset, float delta) {
        int expectedBits;
        float v;
        do {
            // Load and CAS with the raw bits to avoid issues with NaNs and
            // possible bit conversion from signaling NaNs to quiet NaNs that
            // may result in the loop not terminating.
            expectedBits = getIntAcquire(o, offset);
            v = Float.intBitsToFloat(expectedBits);
        } while (!weakCompareAndSetIntAcquire(o, offset,
            expectedBits, Float.floatToRawIntBits(v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final double getAndAddDoubleAcquire(Object o, long offset, double delta) {
        long expectedBits;
        double v;
        do {
            // Load and CAS with the raw bits to avoid issues with NaNs and
            // possible bit conversion from signaling NaNs to quiet NaNs that
            // may result in the loop not terminating.
            expectedBits = getLongAcquire(o, offset);
            v = Double.longBitsToDouble(expectedBits);
        } while (!weakCompareAndSetLongAcquire(o, offset,
            expectedBits, Double.doubleToRawLongBits(v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final char getAndAddCharAcquire(Object o, long offset, char delta) {
        return (char) getAndAddShortAcquire(o, offset, (short) delta);
    }
    
    /*▲ (1.2) getAndAddXXXAcquire 增减值 Acquire版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.3) getAndAddXXX 增减值 Volatile版本 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @HotSpotIntrinsicCandidate
    public final byte getAndAddByte(Object o, long offset, byte delta) {
        byte v;
        do {
            v = getByteVolatile(o, offset);
        } while (!weakCompareAndSetByte(o, offset, v, (byte) (v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @HotSpotIntrinsicCandidate
    public final short getAndAddShort(Object o, long offset, short delta) {
        short v;
        
        do {
            v = getShortVolatile(o, offset);
        } while (!weakCompareAndSetShort(o, offset, v, (short) (v + delta)));
        
        return v;
    }
    
    /**
     * Atomically adds the given value to the current value of a field
     * or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param delta the value to add
     * @return the previous value
     * @since 1.8
     */
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @HotSpotIntrinsicCandidate
    public final int getAndAddInt(Object o, long offset, int delta) {
        int v;
        
        do {
            // 获取对象o中offset地址处对应的int型字段的值 支持volatile语义
            v = getIntVolatile(o, offset);
            
            // 拿期望值v与对象o的offset地址处的当前值比较 如果两个值相等 将当前值更新为v + delta 并返回true 否则返回false
        } while (!weakCompareAndSetInt(o, offset, v, v + delta));
        
        return v;
    }
    
    /**
     * Atomically adds the given value to the current value of a field
     * or array element within the given object {@code o}
     * at the given {@code offset}.
     *
     * @param o object/array to update the field/element in
     * @param offset field/element offset
     * @param delta the value to add
     * @return the previous value
     * @since 1.8
     */
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @HotSpotIntrinsicCandidate
    public final long getAndAddLong(Object o, long offset, long delta) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!weakCompareAndSetLong(o, offset, v, v + delta));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final float getAndAddFloat(Object o, long offset, float delta) {
        int expectedBits;
        float v;
        do {
            // Load and CAS with the raw bits to avoid issues with NaNs
            // and possible bit conversion from signaling NaNs to quiet NaNs that may result in the loop not terminating.
            expectedBits = getIntVolatile(o, offset);
            v = Float.intBitsToFloat(expectedBits);
        } while (!weakCompareAndSetInt(o, offset,
            expectedBits, Float.floatToRawIntBits(v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final double getAndAddDouble(Object o, long offset, double delta) {
        long expectedBits;
        double v;
        do {
            // Load and CAS with the raw bits to avoid issues with NaNs
            // and possible bit conversion from signaling NaNs to quiet NaNs that may result in the loop not terminating.
            expectedBits = getLongVolatile(o, offset);
            v = Double.longBitsToDouble(expectedBits);
        } while (!weakCompareAndSetLong(o, offset, expectedBits, Double.doubleToRawLongBits(v + delta)));
        return v;
    }
    
    // 返回对象o的offset地址处的值 并将该值原子性地增加delta
    @ForceInline
    public final char getAndAddChar(Object o, long offset, char delta) {
        // 转为short形式相加
        return (char) getAndAddShort(o, offset, (short) delta);
    }
    
    /*▲ (1.3) getAndAddXXX 增减值 Volatile版本 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    /* 原子操作 增减值 基于JVM内存操作 属于乐观锁&自旋锁 ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ */
    
    
    
    
    
    
    /* 原子位操作 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ */
    
    /*
     * (1) 按位与
     * (1.1) getAndBitwiseAndXXXRelease Release版本
     * (1.2) getAndBitwiseAndXXXAcquire Acquire版本
     * (1.3) getAndBitwiseAndXXX Volatile版本
     *
     * (2) 按位或
     * (2.1) getAndBitwiseOrXXXRelease Release版本
     * (2.2) getAndBitwiseOrXXXAcquire Acquire版本
     * (2.3) getAndBitwiseOrXXX Volatile版本
     *
     * (3) 按位异或
     * (3.1) getAndBitwiseXorXXXRelease Release版本
     * (3.2) getAndBitwiseXorXXXAcquire Acquire版本
     * (3.3) getAndBitwiseXorXXX Volatile版本
     */
    
    /*▼ (1.1) getAndBitwiseAndXXXRelease ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseAndByteRelease(Object o, long offset, byte mask) {
        byte current;
        do {
            current = getByte(o, offset);
        } while(!weakCompareAndSetByteRelease(o, offset, current, (byte) (current & mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseAndShortRelease(Object o, long offset, short mask) {
        short current;
        do {
            current = getShort(o, offset);
        } while(!weakCompareAndSetShortRelease(o, offset, current, (short) (current & mask)));
        return current;
    }
    
    @ForceInline
    public final int getAndBitwiseAndIntRelease(Object o, long offset, int mask) {
        int current;
        do {
            current = getInt(o, offset);
        } while(!weakCompareAndSetIntRelease(o, offset, current, current & mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseAndLongRelease(Object o, long offset, long mask) {
        long current;
        do {
            current = getLong(o, offset);
        } while(!weakCompareAndSetLongRelease(o, offset, current, current & mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseAndCharRelease(Object o, long offset, char mask) {
        return s2c(getAndBitwiseAndShortRelease(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseAndBooleanRelease(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseAndByteRelease(o, offset, bool2byte(mask)));
    }
    
    /*▲ (1.1) getAndBitwiseAndXXXRelease ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.2) getAndBitwiseAndXXXAcquire ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseAndByteAcquire(Object o, long offset, byte mask) {
        byte current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getByte(o, offset);
        } while(!weakCompareAndSetByteAcquire(o, offset, current, (byte) (current & mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseAndShortAcquire(Object o, long offset, short mask) {
        short current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getShort(o, offset);
        } while(!weakCompareAndSetShortAcquire(o, offset, current, (short) (current & mask)));
        return current;
    }
    
    @ForceInline
    public final int getAndBitwiseAndIntAcquire(Object o, long offset, int mask) {
        int current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getInt(o, offset);
        } while(!weakCompareAndSetIntAcquire(o, offset, current, current & mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseAndLongAcquire(Object o, long offset, long mask) {
        long current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getLong(o, offset);
        } while(!weakCompareAndSetLongAcquire(o, offset, current, current & mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseAndCharAcquire(Object o, long offset, char mask) {
        return s2c(getAndBitwiseAndShortAcquire(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseAndBooleanAcquire(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseAndByteAcquire(o, offset, bool2byte(mask)));
    }
    
    /*▲ (1.2) getAndBitwiseAndXXXAcquire ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (1.3) getAndBitwiseAndXXX ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseAndByte(Object o, long offset, byte mask) {
        byte current;
        do {
            current = getByteVolatile(o, offset);
        } while(!weakCompareAndSetByte(o, offset, current, (byte) (current & mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseAndShort(Object o, long offset, short mask) {
        short current;
        do {
            current = getShortVolatile(o, offset);
        } while(!weakCompareAndSetShort(o, offset, current, (short) (current & mask)));
        return current;
    }
    
    /**
     * Atomically replaces the current value of a field or array element within
     * the given object with the result of bitwise AND between the current value
     * and mask.
     *
     * @param o      object/array to update the field/element in
     * @param offset field/element offset
     * @param mask   the mask value
     *
     * @return the previous value
     *
     * @since 1.9
     */
    @ForceInline
    public final int getAndBitwiseAndInt(Object o, long offset, int mask) {
        int current;
        do {
            current = getIntVolatile(o, offset);
        } while(!weakCompareAndSetInt(o, offset, current, current & mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseAndLong(Object o, long offset, long mask) {
        long current;
        do {
            current = getLongVolatile(o, offset);
        } while(!weakCompareAndSetLong(o, offset, current, current & mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseAndChar(Object o, long offset, char mask) {
        return s2c(getAndBitwiseAndShort(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseAndBoolean(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseAndByte(o, offset, bool2byte(mask)));
    }
    
    /*▲ (1.3) getAndBitwiseAndXXX ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    
    /*▼ (2.1) getAndBitwiseOrXXXRelease ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseOrByteRelease(Object o, long offset, byte mask) {
        byte current;
        do {
            current = getByte(o, offset);
        } while(!weakCompareAndSetByteRelease(o, offset, current, (byte) (current | mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseOrShortRelease(Object o, long offset, short mask) {
        short current;
        do {
            current = getShort(o, offset);
        } while(!weakCompareAndSetShortRelease(o, offset, current, (short) (current | mask)));
        return current;
    }
    
    @ForceInline
    public final int getAndBitwiseOrIntRelease(Object o, long offset, int mask) {
        int current;
        do {
            current = getInt(o, offset);
        } while(!weakCompareAndSetIntRelease(o, offset, current, current | mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseOrLongRelease(Object o, long offset, long mask) {
        long current;
        do {
            current = getLong(o, offset);
        } while(!weakCompareAndSetLongRelease(o, offset, current, current | mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseOrCharRelease(Object o, long offset, char mask) {
        return s2c(getAndBitwiseOrShortRelease(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseOrBooleanRelease(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseOrByteRelease(o, offset, bool2byte(mask)));
    }
    
    /*▲ (2.1) getAndBitwiseOrXXXRelease ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (2.2) getAndBitwiseOrXXXAcquire ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseOrByteAcquire(Object o, long offset, byte mask) {
        byte current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getByte(o, offset);
        } while(!weakCompareAndSetByteAcquire(o, offset, current, (byte) (current | mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseOrShortAcquire(Object o, long offset, short mask) {
        short current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getShort(o, offset);
        } while(!weakCompareAndSetShortAcquire(o, offset, current, (short) (current | mask)));
        return current;
    }
    
    @ForceInline
    public final int getAndBitwiseOrIntAcquire(Object o, long offset, int mask) {
        int current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getInt(o, offset);
        } while(!weakCompareAndSetIntAcquire(o, offset, current, current | mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseOrLongAcquire(Object o, long offset, long mask) {
        long current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getLong(o, offset);
        } while(!weakCompareAndSetLongAcquire(o, offset, current, current | mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseOrCharAcquire(Object o, long offset, char mask) {
        return s2c(getAndBitwiseOrShortAcquire(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseOrBooleanAcquire(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseOrByteAcquire(o, offset, bool2byte(mask)));
    }
    
    /*▲ (2.2) getAndBitwiseOrXXXAcquire ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (2.3) getAndBitwiseOrXXX ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseOrByte(Object o, long offset, byte mask) {
        byte current;
        do {
            current = getByteVolatile(o, offset);
        } while(!weakCompareAndSetByte(o, offset, current, (byte) (current | mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseOrShort(Object o, long offset, short mask) {
        short current;
        do {
            current = getShortVolatile(o, offset);
        } while(!weakCompareAndSetShort(o, offset, current, (short) (current | mask)));
        return current;
    }
    
    @ForceInline
    public final int getAndBitwiseOrInt(Object o, long offset, int mask) {
        int current;
        do {
            current = getIntVolatile(o, offset);
        } while(!weakCompareAndSetInt(o, offset, current, current | mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseOrLong(Object o, long offset, long mask) {
        long current;
        do {
            current = getLongVolatile(o, offset);
        } while(!weakCompareAndSetLong(o, offset, current, current | mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseOrChar(Object o, long offset, char mask) {
        return s2c(getAndBitwiseOrShort(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseOrBoolean(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseOrByte(o, offset, bool2byte(mask)));
    }
    
    /*▲ (2.3) getAndBitwiseOrXXX ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    
    /*▼ (3.1) getAndBitwiseXorXXXRelease ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseXorByteRelease(Object o, long offset, byte mask) {
        byte current;
        do {
            current = getByte(o, offset);
        } while(!weakCompareAndSetByteRelease(o, offset, current, (byte) (current ^ mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseXorShortRelease(Object o, long offset, short mask) {
        short current;
        do {
            current = getShort(o, offset);
        } while(!weakCompareAndSetShortRelease(o, offset, current, (short) (current ^ mask)));
        return current;
    }
    
    @ForceInline
    public final int getAndBitwiseXorIntRelease(Object o, long offset, int mask) {
        int current;
        do {
            current = getInt(o, offset);
        } while(!weakCompareAndSetIntRelease(o, offset, current, current ^ mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseXorLongRelease(Object o, long offset, long mask) {
        long current;
        do {
            current = getLong(o, offset);
        } while(!weakCompareAndSetLongRelease(o, offset, current, current ^ mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseXorCharRelease(Object o, long offset, char mask) {
        return s2c(getAndBitwiseXorShortRelease(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseXorBooleanRelease(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseXorByteRelease(o, offset, bool2byte(mask)));
    }
    
    /*▲ (3.1) getAndBitwiseXorXXXRelease ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (3.2) getAndBitwiseXorXXXAcquire ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseXorByteAcquire(Object o, long offset, byte mask) {
        byte current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getByte(o, offset);
        } while(!weakCompareAndSetByteAcquire(o, offset, current, (byte) (current ^ mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseXorShortAcquire(Object o, long offset, short mask) {
        short current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getShort(o, offset);
        } while(!weakCompareAndSetShortAcquire(o, offset, current, (short) (current ^ mask)));
        return current;
    }
    
    @ForceInline
    public final int getAndBitwiseXorIntAcquire(Object o, long offset, int mask) {
        int current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getInt(o, offset);
        } while(!weakCompareAndSetIntAcquire(o, offset, current, current ^ mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseXorLongAcquire(Object o, long offset, long mask) {
        long current;
        do {
            // Plain read, the value is a hint, the acquire CAS does the work
            current = getLong(o, offset);
        } while(!weakCompareAndSetLongAcquire(o, offset, current, current ^ mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseXorCharAcquire(Object o, long offset, char mask) {
        return s2c(getAndBitwiseXorShortAcquire(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseXorBooleanAcquire(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseXorByteAcquire(o, offset, bool2byte(mask)));
    }
    
    /*▲ (3.2) getAndBitwiseXorXXXAcquire ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ (3.3) getAndBitwiseXorXXX ████████████████████████████████████████████████████████████████████████████████┓ */
    
    @ForceInline
    public final byte getAndBitwiseXorByte(Object o, long offset, byte mask) {
        byte current;
        do {
            current = getByteVolatile(o, offset);
        } while(!weakCompareAndSetByte(o, offset, current, (byte) (current ^ mask)));
        return current;
    }
    
    @ForceInline
    public final short getAndBitwiseXorShort(Object o, long offset, short mask) {
        short current;
        do {
            current = getShortVolatile(o, offset);
        } while(!weakCompareAndSetShort(o, offset, current, (short) (current ^ mask)));
        return current;
    }
    
    @ForceInline
    public final int getAndBitwiseXorInt(Object o, long offset, int mask) {
        int current;
        do {
            current = getIntVolatile(o, offset);
        } while(!weakCompareAndSetInt(o, offset, current, current ^ mask));
        return current;
    }
    
    @ForceInline
    public final long getAndBitwiseXorLong(Object o, long offset, long mask) {
        long current;
        do {
            current = getLongVolatile(o, offset);
        } while(!weakCompareAndSetLong(o, offset, current, current ^ mask));
        return current;
    }
    
    @ForceInline
    public final char getAndBitwiseXorChar(Object o, long offset, char mask) {
        return s2c(getAndBitwiseXorShort(o, offset, c2s(mask)));
    }
    
    @ForceInline
    public final boolean getAndBitwiseXorBoolean(Object o, long offset, boolean mask) {
        return byte2bool(getAndBitwiseXorByte(o, offset, bool2byte(mask)));
    }
    
    /*▲ (3.3) getAndBitwiseXorXXX ████████████████████████████████████████████████████████████████████████████████┛ */
    
    /* 原子位操作 ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ */
    
    
    
    
    
    
    /*▼ 线程操作 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Unblocks the given thread blocked on {@code park}, or, if it is
     * not blocked, causes the subsequent call to {@code park} not to
     * block.  Note: this operation is "unsafe" solely because the
     * caller must somehow ensure that the thread has not been
     * destroyed. Nothing special is usually required to ensure this
     * when called from Java (in which there will ordinarily be a live
     * reference to the thread) but this is not nearly-automatically
     * so when calling from native code.
     *
     * @param thread the thread to unpark.
     */
    // 发给目标线程一个许可证 该许可证被park消费 用于唤醒被park阻塞的线程
    @HotSpotIntrinsicCandidate
    public native void unpark(Object thread);
    
    /**
     * Blocks current thread, returning when a balancing
     * {@code unpark} occurs, or a balancing {@code unpark} has
     * already occurred, or the thread is interrupted, or, if not
     * absolute and time is not zero, the given time nanoseconds have
     * elapsed, or if absolute, the given deadline in milliseconds
     * since Epoch has passed, or spuriously (i.e., returning for no
     * "reason"). Note: This operation is in the Unsafe class only
     * because {@code unpark} is, so it would be strange to place it
     * elsewhere.
     */
    // 等待消费一个许可证 这会使线程陷入阻塞。
    @HotSpotIntrinsicCandidate
    public native void park(boolean isAbsolute, long time);
    
    /*▲ 线程操作 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ 内存屏障 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Ensures that loads before the fence will not be reordered with loads and
     * stores after the fence; a "LoadLoad plus LoadStore barrier".
     *
     * Corresponds to C11 atomic_thread_fence(memory_order_acquire)
     * (an "acquire fence").
     *
     * A pure LoadLoad fence is not provided, since the addition of LoadStore
     * is almost always desired, and most current hardware instructions that
     * provide a LoadLoad barrier also provide a LoadStore barrier for free.
     * @since 1.8
     */
    @HotSpotIntrinsicCandidate
    public native void loadFence();
    
    /**
     * Ensures that loads and stores before the fence will not be reordered with
     * stores after the fence; a "StoreStore plus LoadStore barrier".
     *
     * Corresponds to C11 atomic_thread_fence(memory_order_release)
     * (a "release fence").
     *
     * A pure StoreStore fence is not provided, since the addition of LoadStore
     * is almost always desired, and most current hardware instructions that
     * provide a StoreStore barrier also provide a LoadStore barrier for free.
     * @since 1.8
     */
    @HotSpotIntrinsicCandidate
    public native void storeFence();
    
    /**
     * Ensures that loads and stores before the fence will not be reordered
     * with loads and stores after the fence.  Implies the effects of both
     * loadFence() and storeFence(), and in addition, the effect of a StoreLoad
     * barrier.
     *
     * Corresponds to C11 atomic_thread_fence(memory_order_seq_cst).
     * @since 1.8
     */
    @HotSpotIntrinsicCandidate
    public native void fullFence();
    
    /**
     * Ensures that loads before the fence will not be reordered with
     * loads after the fence.
     */
    public final void loadLoadFence() {
        loadFence();
    }
    
    /**
     * Ensures that stores before the fence will not be reordered with
     * stores after the fence.
     */
    public final void storeStoreFence() {
        storeFence();
    }
    
    /*▲ 内存屏障 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ getXXXUnaligned/putXXXUnaligned ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * @see #getLongUnaligned(Object, long)
     */
    @HotSpotIntrinsicCandidate
    public final int getIntUnaligned(Object o, long offset) {
        if((offset & 3) == 0) {
            return getInt(o, offset);
        } else if((offset & 1) == 0) {
            return makeInt(getShort(o, offset), getShort(o, offset + 2));
        } else {
            return makeInt(getByte(o, offset), getByte(o, offset + 1), getByte(o, offset + 2), getByte(o, offset + 3));
        }
    }
    
    /**
     * @see #getLongUnaligned(Object, long, boolean)
     */
    public final int getIntUnaligned(Object o, long offset, boolean bigEndian) {
        return convEndian(bigEndian, getIntUnaligned(o, offset));
    }
    
    /**
     * @see #getLongUnaligned(Object, long)
     */
    @HotSpotIntrinsicCandidate
    public final short getShortUnaligned(Object o, long offset) {
        if((offset & 1) == 0) {
            return getShort(o, offset);
        } else {
            return makeShort(getByte(o, offset), getByte(o, offset + 1));
        }
    }
    
    /**
     * @see #getLongUnaligned(Object, long, boolean)
     */
    public final short getShortUnaligned(Object o, long offset, boolean bigEndian) {
        return convEndian(bigEndian, getShortUnaligned(o, offset));
    }
    
    /**
     * @see #getLongUnaligned(Object, long)
     */
    @HotSpotIntrinsicCandidate
    public final char getCharUnaligned(Object o, long offset) {
        if((offset & 1) == 0) {
            return getChar(o, offset);
        } else {
            return (char) makeShort(getByte(o, offset), getByte(o, offset + 1));
        }
    }
    
    /**
     * @see #getLongUnaligned(Object, long, boolean)
     */
    public final char getCharUnaligned(Object o, long offset, boolean bigEndian) {
        return convEndian(bigEndian, getCharUnaligned(o, offset));
    }
    
    /**
     * Fetches a value at some byte offset into a given Java object.
     * More specifically, fetches a value within the given object
     * <code>o</code> at the given offset, or (if <code>o</code> is
     * null) from the memory address whose numerical value is the
     * given offset.  <p>
     *
     * The specification of this method is the same as {@link
     * #getLong(Object, long)} except that the offset does not need to
     * have been obtained from {@link #objectFieldOffset} on the
     * {@link java.lang.reflect.Field} of some Java field.  The value
     * in memory is raw data, and need not correspond to any Java
     * variable.  Unless <code>o</code> is null, the value accessed
     * must be entirely within the allocated object.  The endianness
     * of the value in memory is the endianness of the native platform.
     *
     * <p> The read will be atomic with respect to the largest power
     * of two that divides the GCD of the offset and the storage size.
     * For example, getLongUnaligned will make atomic reads of 2-, 4-,
     * or 8-byte storage units if the offset is zero mod 2, 4, or 8,
     * respectively.  There are no other guarantees of atomicity.
     * <p>
     * 8-byte atomicity is only guaranteed on platforms on which
     * support atomic accesses to longs.
     *
     * @param o      Java heap object in which the value resides, if any, else
     *               null
     * @param offset The offset in bytes from the start of the object
     *
     * @return the value fetched from the indicated object
     *
     * @throws RuntimeException No defined exceptions are thrown, not even
     *                          {@link NullPointerException}
     * @since 9
     */
    @HotSpotIntrinsicCandidate
    public final long getLongUnaligned(Object o, long offset) {
        if((offset & 7) == 0) {
            return getLong(o, offset);
        } else if((offset & 3) == 0) {
            return makeLong(getInt(o, offset), getInt(o, offset + 4));
        } else if((offset & 1) == 0) {
            return makeLong(getShort(o, offset), getShort(o, offset + 2), getShort(o, offset + 4), getShort(o, offset + 6));
        } else {
            return makeLong(getByte(o, offset), getByte(o, offset + 1), getByte(o, offset + 2), getByte(o, offset + 3), getByte(o, offset + 4), getByte(o, offset + 5), getByte(o, offset + 6), getByte(o, offset + 7));
        }
    }
    
    /**
     * As {@link #getLongUnaligned(Object, long)} but with an
     * additional argument which specifies the endianness of the value
     * as stored in memory.
     *
     * @param o         Java heap object in which the variable resides
     * @param offset    The offset in bytes from the start of the object
     * @param bigEndian The endianness of the value
     *
     * @return the value fetched from the indicated object
     *
     * @since 9
     */
    public final long getLongUnaligned(Object o, long offset, boolean bigEndian) {
        return convEndian(bigEndian, getLongUnaligned(o, offset));
    }
    
    /**
     * @see #putLongUnaligned(Object, long, long)
     */
    @HotSpotIntrinsicCandidate
    public final void putIntUnaligned(Object o, long offset, int x) {
        if((offset & 3) == 0) {
            putInt(o, offset, x);
        } else if((offset & 1) == 0) {
            putIntParts(o, offset, (short) (x >> 0), (short) (x >>> 16));
        } else {
            putIntParts(o, offset, (byte) (x >>> 0), (byte) (x >>> 8), (byte) (x >>> 16), (byte) (x >>> 24));
        }
    }
    
    /**
     * @see #putLongUnaligned(Object, long, long, boolean)
     */
    public final void putIntUnaligned(Object o, long offset, int x, boolean bigEndian) {
        putIntUnaligned(o, offset, convEndian(bigEndian, x));
    }
    
    /**
     * @see #putLongUnaligned(Object, long, long)
     */
    @HotSpotIntrinsicCandidate
    public final void putShortUnaligned(Object o, long offset, short x) {
        if((offset & 1) == 0) {
            putShort(o, offset, x);
        } else {
            putShortParts(o, offset, (byte) (x >>> 0), (byte) (x >>> 8));
        }
    }
    
    /**
     * @see #putLongUnaligned(Object, long, long, boolean)
     */
    public final void putShortUnaligned(Object o, long offset, short x, boolean bigEndian) {
        putShortUnaligned(o, offset, convEndian(bigEndian, x));
    }
    
    /**
     * @see #putLongUnaligned(Object, long, long)
     */
    @HotSpotIntrinsicCandidate
    public final void putCharUnaligned(Object o, long offset, char x) {
        putShortUnaligned(o, offset, (short) x);
    }
    
    /**
     * @see #putLongUnaligned(Object, long, long, boolean)
     */
    public final void putCharUnaligned(Object o, long offset, char x, boolean bigEndian) {
        putCharUnaligned(o, offset, convEndian(bigEndian, x));
    }
    
    /**
     * Stores a value at some byte offset into a given Java object.
     * <p>
     * The specification of this method is the same as {@link
     * #getLong(Object, long)} except that the offset does not need to
     * have been obtained from {@link #objectFieldOffset} on the
     * {@link java.lang.reflect.Field} of some Java field.  The value
     * in memory is raw data, and need not correspond to any Java
     * variable.  The endianness of the value in memory is the
     * endianness of the native platform.
     * <p>
     * The write will be atomic with respect to the largest power of
     * two that divides the GCD of the offset and the storage size.
     * For example, putLongUnaligned will make atomic writes of 2-, 4-,
     * or 8-byte storage units if the offset is zero mod 2, 4, or 8,
     * respectively.  There are no other guarantees of atomicity.
     * <p>
     * 8-byte atomicity is only guaranteed on platforms on which
     * support atomic accesses to longs.
     *
     * @param o      Java heap object in which the value resides, if any, else
     *               null
     * @param offset The offset in bytes from the start of the object
     * @param x      the value to store
     *
     * @throws RuntimeException No defined exceptions are thrown, not even
     *                          {@link NullPointerException}
     * @since 9
     */
    @HotSpotIntrinsicCandidate
    public final void putLongUnaligned(Object o, long offset, long x) {
        if((offset & 7) == 0) {
            putLong(o, offset, x);
        } else if((offset & 3) == 0) {
            putLongParts(o, offset, (int) (x >> 0), (int) (x >>> 32));
        } else if((offset & 1) == 0) {
            putLongParts(o, offset, (short) (x >>> 0), (short) (x >>> 16), (short) (x >>> 32), (short) (x >>> 48));
        } else {
            putLongParts(o, offset, (byte) (x >>> 0), (byte) (x >>> 8), (byte) (x >>> 16), (byte) (x >>> 24), (byte) (x >>> 32), (byte) (x >>> 40), (byte) (x >>> 48), (byte) (x >>> 56));
        }
    }
    
    /**
     * As {@link #putLongUnaligned(Object, long, long)} but with an additional
     * argument which specifies the endianness of the value as stored in memory.
     *
     * @param o         Java heap object in which the value resides
     * @param offset    The offset in bytes from the start of the object
     * @param x         the value to store
     * @param bigEndian The endianness of the value
     *
     * @throws RuntimeException No defined exceptions are thrown, not even
     *                          {@link NullPointerException}
     * @since 9
     */
    public final void putLongUnaligned(Object o, long offset, long x, boolean bigEndian) {
        putLongUnaligned(o, offset, convEndian(bigEndian, x));
    }
    
    /*▲ getXXXUnaligned/putXXXUnaligned ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    // JVM interface methods。 BE is true iff the native endianness of this platform is big.
    private static final boolean BE = theUnsafe.isBigEndian0();
    private native boolean isBigEndian0();
    /**
     * @return Returns true if the native byte ordering of this
     * platform is big-endian, false if it is little-endian.
     */
    public final boolean isBigEndian() {
        return BE;
    }
    
    
    // unalignedAccess is true if this platform can perform unaligned accesses.
    private static final boolean unalignedAccess = theUnsafe.unalignedAccess0();
    private native boolean unalignedAccess0();
    /**
     * @return Returns true if this platform is capable of performing accesses at addresses which are not aligned for the type of the primitive type being accessed, false otherwise.
     */
    // true:该平台支持对字节未对齐的基本类型访问
    public final boolean unalignedAccess() {
        return unalignedAccess;
    }
    
    
    /**
     * Allocates an array of a given type, but does not do zeroing.
     * <p>
     * This method should only be used in the very rare cases where a high-performance code
     * overwrites the destination array completely, and compilers cannot assist in zeroing elimination.
     * In an overwhelming majority of cases, a normal Java allocation should be used instead.
     * <p>
     * Users of this method are <b>required</b> to overwrite the initial (garbage) array contents
     * before allowing untrusted code, or code in other threads, to observe the reference
     * to the newly allocated array. In addition, the publication of the array reference must be
     * safe according to the Java Memory Model requirements.
     * <p>
     * The safest approach to deal with an uninitialized array is to keep the reference to it in local
     * variable at least until the initialization is complete, and then publish it <b>once</b>, either
     * by writing it to a <em>volatile</em> field, or storing it into a <em>final</em> field in constructor,
     * or issuing a {@link #storeFence} before publishing the reference.
     * <p>
     *
     * @param componentType array component type to allocate
     * @param length        array size to allocate
     *
     * @throws IllegalArgumentException if component type is null, or not a primitive class;
     *                                  or the length is negative
     * @implnote This method can only allocate primitive arrays, to avoid garbage reference
     * elements that could break heap integrity.
     */
    public Object allocateUninitializedArray(Class<?> componentType, int length) {
        if(componentType == null) {
            throw new IllegalArgumentException("Component type is null");
        }
        if(!componentType.isPrimitive()) {
            throw new IllegalArgumentException("Component type is not primitive");
        }
        if(length < 0) {
            throw new IllegalArgumentException("Negative length");
        }
        return allocateUninitializedArray0(componentType, length);
    }
    
    @HotSpotIntrinsicCandidate
    private Object allocateUninitializedArray0(Class<?> componentType, int length) {
        // These fallbacks provide zeroed arrays, but intrinsic is not required to
        // return the zeroed arrays.
        if(componentType == byte.class)
            return new byte[length];
        if(componentType == boolean.class)
            return new boolean[length];
        if(componentType == short.class)
            return new short[length];
        if(componentType == char.class)
            return new char[length];
        if(componentType == int.class)
            return new int[length];
        if(componentType == float.class)
            return new float[length];
        if(componentType == long.class)
            return new long[length];
        if(componentType == double.class)
            return new double[length];
        return null;
    }
    
    
    /**
     * Gets the load average in the system run queue assigned
     * to the available processors averaged over various periods of time.
     * This method retrieves the given {@code nelem} samples and
     * assigns to the elements of the given {@code loadavg} array.
     * The system imposes a maximum of 3 samples, representing
     * averages over the last 1,  5,  and  15 minutes, respectively.
     *
     * @param loadavg an array of double of size nelems
     * @param nelems the number of samples to be retrieved and
     *        must be 1 to 3.
     *
     * @return the number of samples actually retrieved; or -1
     *         if the load average is unobtainable.
     */
    public int getLoadAverage(double[] loadavg, int nelems) {
        if (nelems < 0 || nelems > 3 || nelems > loadavg.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        return getLoadAverage0(loadavg, nelems);
    }
    
    private native int getLoadAverage0(double[] loadavg, int nelems);
    
    
    // short转char
    @ForceInline
    private char s2c(short s) {
        return (char) s;
    }
    
    // char转short
    @ForceInline
    private short c2s(char s) {
        return (short) s;
    }
    
    /**
     * The JVM converts integral values to boolean values using two
     * different conventions, byte testing against zero and truncation
     * to least-significant bit.
     *
     * <p>The JNI documents specify that, at least for returning
     * values from native methods, a Java boolean value is converted
     * to the value-set 0..1 by first truncating to a byte (0..255 or
     * maybe -128..127) and then testing against zero. Thus, Java
     * booleans in non-Java data structures are by convention
     * represented as 8-bit containers containing either zero (for
     * false) or any non-zero value (for true).
     *
     * <p>Java booleans in the heap are also stored in bytes, but are
     * strongly normalized to the value-set 0..1 (i.e., they are
     * truncated to the least-significant bit).
     *
     * <p>The main reason for having different conventions for
     * conversion is performance: Truncation to the least-significant
     * bit can be usually implemented with fewer (machine)
     * instructions than byte testing against zero.
     *
     * <p>A number of Unsafe methods load boolean values from the heap
     * as bytes. Unsafe converts those values according to the JNI
     * rules (i.e, using the "testing against zero" convention). The
     * method {@code byte2bool} implements that conversion.
     *
     * @param b the byte to be converted to boolean
     *
     * @return the result of the conversion
     */
    // byte转boolean
    @ForceInline
    private boolean byte2bool(byte b) {
        return b != 0;
    }
    
    /**
     * Convert a boolean value to a byte. The return value is strongly
     * normalized to the value-set 0..1 (i.e., the value is truncated
     * to the least-significant bit). See {@link #byte2bool(byte)} for
     * more details on conversion conventions.
     *
     * @param b the boolean to be converted to byte (and then normalized)
     *
     * @return the result of the conversion
     */
    // boolean转byte
    @ForceInline
    private byte bool2byte(boolean b) {
        return b ? (byte) 1 : (byte) 0;
    }
    
    
    /**
     * Fetches an uncompressed reference value from a given native variable ignoring the VM's compressed references mode.
     *
     * @param address a memory address locating the variable
     * @return the value fetched from the indicated native variable
     */
    public native Object getUncompressedObject(long address);
    
    
    /**
     * Reports the size in bytes of a native memory page (whatever that is).
     * This value will always be a power of two.
     */
    // 返回内存分页大小
    public native int pageSize();
    
    
    
    /*▼ 范围检查 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Validate the arguments to allocateMemory
     *
     * @throws RuntimeException if the arguments are invalid
     *                          (<em>Note:</em> after optimization, invalid inputs may
     *                          go undetected, which will lead to unpredictable
     *                          behavior)
     */
    private void allocateMemoryChecks(long bytes) {
        checkSize(bytes);
    }
    
    /**
     * Validate the arguments to reallocateMemory
     *
     * @throws RuntimeException if the arguments are invalid
     *                          (<em>Note:</em> after optimization, invalid inputs may
     *                          go undetected, which will lead to unpredictable
     *                          behavior)
     */
    private void reallocateMemoryChecks(long address, long bytes) {
        checkPointer(null, address);
        checkSize(bytes);
    }
    
    /**
     * Validate the arguments to setMemory
     *
     * @throws RuntimeException if the arguments are invalid
     *                          (<em>Note:</em> after optimization, invalid inputs may
     *                          go undetected, which will lead to unpredictable
     *                          behavior)
     */
    private void setMemoryChecks(Object o, long offset, long bytes, byte value) {
        checkPrimitivePointer(o, offset);
        checkSize(bytes);
    }
    
    /**
     * Validate the arguments to copyMemory
     *
     * @throws RuntimeException if any of the arguments is invalid
     *                          (<em>Note:</em> after optimization, invalid inputs may
     *                          go undetected, which will lead to unpredictable
     *                          behavior)
     */
    private void copyMemoryChecks(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
        checkSize(bytes);
        checkPrimitivePointer(srcBase, srcOffset);
        checkPrimitivePointer(destBase, destOffset);
    }
    
    private void copySwapMemoryChecks(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes, long elemSize) {
        checkSize(bytes);
        
        if(elemSize != 2 && elemSize != 4 && elemSize != 8) {
            throw invalidInput();
        }
        if(bytes % elemSize != 0) {
            throw invalidInput();
        }
        
        checkPrimitivePointer(srcBase, srcOffset);
        checkPrimitivePointer(destBase, destOffset);
    }
    
    /**
     * Validate the arguments to freeMemory
     *
     * @throws RuntimeException if the arguments are invalid
     *                          (<em>Note:</em> after optimization, invalid inputs may
     *                          go undetected, which will lead to unpredictable
     *                          behavior)
     */
    private void freeMemoryChecks(long address) {
        checkPointer(null, address);
    }
    
    /**
     * Check the validity of a size (the equivalent of a size_t)
     *
     * @throws RuntimeException if the size is invalid
     *         (<em>Note:</em> after optimization, invalid inputs may
     *         go undetected, which will lead to unpredictable
     *         behavior)
     */
    private void checkSize(long size) {
        if (ADDRESS_SIZE == 4) {
            // Note: this will also check for negative sizes
            if (!is32BitClean(size)) {
                throw invalidInput();
            }
        } else if (size < 0) {
            throw invalidInput();
        }
    }
    
    /**
     * Check the validity of a native address (the equivalent of void*)
     *
     * @throws RuntimeException if the address is invalid
     *         (<em>Note:</em> after optimization, invalid inputs may
     *         go undetected, which will lead to unpredictable
     *         behavior)
     */
    private void checkNativeAddress(long address) {
        if (ADDRESS_SIZE == 4) {
            // Accept both zero and sign extended pointers. A valid
            // pointer will, after the +1 below, either have produced
            // the value 0x0 or 0x1. Masking off the low bit allows
            // for testing against 0.
            if ((((address >> 32) + 1) & ~1) != 0) {
                throw invalidInput();
            }
        }
    }
    
    /**
     * Check the validity of an offset, relative to a base object
     *
     * @param o the base object
     * @param offset the offset to check
     *
     * @throws RuntimeException if the size is invalid
     *         (<em>Note:</em> after optimization, invalid inputs may
     *         go undetected, which will lead to unpredictable
     *         behavior)
     */
    private void checkOffset(Object o, long offset) {
        if (ADDRESS_SIZE == 4) {
            // Note: this will also check for negative offsets
            if (!is32BitClean(offset)) {
                throw invalidInput();
            }
        } else if (offset < 0) {
            throw invalidInput();
        }
    }
    
    /**
     * Check the validity of a double-register pointer
     *
     * Note: This code deliberately does *not* check for NPE for (at
     * least) three reasons:
     *
     * 1) NPE is not just NULL/0 - there is a range of values all
     * resulting in an NPE, which is not trivial to check for
     *
     * 2) It is the responsibility of the callers of Unsafe methods
     * to verify the input, so throwing an exception here is not really
     * useful - passing in a NULL pointer is a critical error and the
     * must not expect an exception to be thrown anyway.
     *
     * 3) the actual operations will detect NULL pointers anyway by
     * means of traps and signals (like SIGSEGV).
     *
     * @param o Java heap object, or null
     * @param offset indication of where the variable resides in a Java heap
     *        object, if any, else a memory address locating the variable
     *        statically
     *
     * @throws RuntimeException if the pointer is invalid
     *         (<em>Note:</em> after optimization, invalid inputs may
     *         go undetected, which will lead to unpredictable
     *         behavior)
     */
    private void checkPointer(Object o, long offset) {
        if (o == null) {
            checkNativeAddress(offset);
        } else {
            checkOffset(o, offset);
        }
    }
    
    /**
     * Check that a pointer is a valid primitive array type pointer
     *
     * Note: pointers off-heap are considered to be primitive arrays
     *
     * @throws RuntimeException if the pointer is invalid
     *         (<em>Note:</em> after optimization, invalid inputs may
     *         go undetected, which will lead to unpredictable
     *         behavior)
     */
    private void checkPrimitivePointer(Object o, long offset) {
        checkPointer(o, offset);
        
        if (o != null) {
            // If on heap, it must be a primitive array
            checkPrimitiveArray(o.getClass());
        }
    }
    
    /**
     * Check if a type is a primitive array type
     *
     * @param c the type to check
     *
     * @return true if the type is a primitive array type
     */
    private void checkPrimitiveArray(Class<?> c) {
        Class<?> componentType = c.getComponentType();
        if (componentType == null || !componentType.isPrimitive()) {
            throw invalidInput();
        }
    }
    
    /**
     * Create an exception reflecting that some of the input was invalid
     *
     * <em>Note:</em> It is the resposibility of the caller to make
     * sure arguments are checked before the methods are called. While
     * some rudimentary checks are performed on the input, the checks
     * are best effort and when performance is an overriding priority,
     * as when methods of this class are optimized by the runtime
     * compiler, some or all checks (if any) may be elided. Hence, the
     * caller must not rely on the checks and corresponding
     * exceptions!
     *
     * @return an exception object
     */
    private RuntimeException invalidInput() {
        return new IllegalArgumentException();
    }
    
    /**
     * Check if a value is 32-bit clean (32 MSB are all zero)
     *
     * @param value the 64-bit value to check
     *
     * @return true if the value is 32-bit clean
     */
    private boolean is32BitClean(long value) {
        return value >>> 32 == 0;
    }
    
    /*▲ 范围检查 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼ 抛异常 ████████████████████████████████████████████████████████████████████████████████┓ */
    
    /**
     * Throws the exception without telling the verifier.
     */
    public native void throwException(Throwable ee);
    
    /**
     * Throws IllegalAccessError; for use by the VM for access control
     * error support.
     * @since 1.8
     */
    private static void throwIllegalAccessError() {
        throw new IllegalAccessError();
    }
    
    /*▲ 抛异常 ████████████████████████████████████████████████████████████████████████████████┛ */
    
    
    
    /*▼  ████████████████████████████████████████████████████████████████████████████████┓ */
    
    // These methods construct integers from bytes.  The byte ordering is the native endianness of this platform.
    private static long makeLong(byte i0, byte i1, byte i2, byte i3, byte i4, byte i5, byte i6, byte i7) {
        return ((toUnsignedLong(i0) << pickPos(56, 0))
            | (toUnsignedLong(i1) << pickPos(56, 8))
            | (toUnsignedLong(i2) << pickPos(56, 16))
            | (toUnsignedLong(i3) << pickPos(56, 24))
            | (toUnsignedLong(i4) << pickPos(56, 32))
            | (toUnsignedLong(i5) << pickPos(56, 40))
            | (toUnsignedLong(i6) << pickPos(56, 48))
            | (toUnsignedLong(i7) << pickPos(56, 56)));
    }
    private static long makeLong(short i0, short i1, short i2, short i3) {
        return ((toUnsignedLong(i0) << pickPos(48, 0))
            | (toUnsignedLong(i1) << pickPos(48, 16))
            | (toUnsignedLong(i2) << pickPos(48, 32))
            | (toUnsignedLong(i3) << pickPos(48, 48)));
    }
    private static long makeLong(int i0, int i1) {
        return (toUnsignedLong(i0) << pickPos(32, 0))
            | (toUnsignedLong(i1) << pickPos(32, 32));
    }
    private static int makeInt(short i0, short i1) {
        return (toUnsignedInt(i0) << pickPos(16, 0))
            | (toUnsignedInt(i1) << pickPos(16, 16));
    }
    private static int makeInt(byte i0, byte i1, byte i2, byte i3) {
        return ((toUnsignedInt(i0) << pickPos(24, 0))
            | (toUnsignedInt(i1) << pickPos(24, 8))
            | (toUnsignedInt(i2) << pickPos(24, 16))
            | (toUnsignedInt(i3) << pickPos(24, 24)));
    }
    private static short makeShort(byte i0, byte i1) {
        return (short)((toUnsignedInt(i0) << pickPos(8, 0))
            | (toUnsignedInt(i1) << pickPos(8, 8)));
    }
    
    private static int pickPos(int top, int pos) { return BE ? top - pos : pos; }
    
    private static byte  pick(byte  le, byte  be) { return BE ? be : le; }
    private static short pick(short le, short be) { return BE ? be : le; }
    private static int   pick(int   le, int   be) { return BE ? be : le; }
    
    // These methods write integers to memory from smaller parts provided by their caller.
    // The ordering in which these parts are written is the native endianness of this platform.
    private void putLongParts(Object o, long offset, byte i0, byte i1, byte i2, byte i3, byte i4, byte i5, byte i6, byte i7) {
        putByte(o, offset + 0, pick(i0, i7));
        putByte(o, offset + 1, pick(i1, i6));
        putByte(o, offset + 2, pick(i2, i5));
        putByte(o, offset + 3, pick(i3, i4));
        putByte(o, offset + 4, pick(i4, i3));
        putByte(o, offset + 5, pick(i5, i2));
        putByte(o, offset + 6, pick(i6, i1));
        putByte(o, offset + 7, pick(i7, i0));
    }
    private void putLongParts(Object o, long offset, short i0, short i1, short i2, short i3) {
        putShort(o, offset + 0, pick(i0, i3));
        putShort(o, offset + 2, pick(i1, i2));
        putShort(o, offset + 4, pick(i2, i1));
        putShort(o, offset + 6, pick(i3, i0));
    }
    private void putLongParts(Object o, long offset, int i0, int i1) {
        putInt(o, offset + 0, pick(i0, i1));
        putInt(o, offset + 4, pick(i1, i0));
    }
    private void putIntParts(Object o, long offset, short i0, short i1) {
        putShort(o, offset + 0, pick(i0, i1));
        putShort(o, offset + 2, pick(i1, i0));
    }
    private void putIntParts(Object o, long offset, byte i0, byte i1, byte i2, byte i3) {
        putByte(o, offset + 0, pick(i0, i3));
        putByte(o, offset + 1, pick(i1, i2));
        putByte(o, offset + 2, pick(i2, i1));
        putByte(o, offset + 3, pick(i3, i0));
    }
    private void putShortParts(Object o, long offset, byte i0, byte i1) {
        putByte(o, offset + 0, pick(i0, i1));
        putByte(o, offset + 1, pick(i1, i0));
    }
    
    // Zero-extend an integer
    private static int toUnsignedInt(byte n)    { return n & 0xff; }
    private static int toUnsignedInt(short n)   { return n & 0xffff; }
    private static long toUnsignedLong(byte n)  { return n & 0xffl; }
    private static long toUnsignedLong(short n) { return n & 0xffffl; }
    private static long toUnsignedLong(int n)   { return n & 0xffffffffl; }
    
    // Maybe byte-reverse an integer
    private static char convEndian(boolean big, char n)   { return big == BE ? n : Character.reverseBytes(n); }
    private static short convEndian(boolean big, short n) { return big == BE ? n : Short.reverseBytes(n)    ; }
    private static int convEndian(boolean big, int n)     { return big == BE ? n : Integer.reverseBytes(n)  ; }
    private static long convEndian(boolean big, long n)   { return big == BE ? n : Long.reverseBytes(n)     ; }
    
    /*▲  ████████████████████████████████████████████████████████████████████████████████┛ */
    
}
