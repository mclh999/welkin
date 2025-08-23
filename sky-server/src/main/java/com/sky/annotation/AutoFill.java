package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//表示该注解只能用于方法
@Retention(RetentionPolicy.RUNTIME)// 表示该注解在运行时保留
public @interface AutoFill {
    //用枚举方式指定数据库的操作类型
    OperationType value();
}
