// z-disk-system/z-disk-system/src/main/java/com/good/zdisksystem/annotation/OperationLogger.java
package com.good.zdisksystem.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLogger {
    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String action() default "";

    /**
     * 操作详情
     */
    String detail() default "";
}
