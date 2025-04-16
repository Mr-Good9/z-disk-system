// z-disk-system/z-disk-system/src/main/java/com/good/zdisksystem/aspect/OperationLogAspect.java
package com.good.zdisksystem.aspect;

import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Pointcut("@annotation(com.good.zdisksystem.annotation.OperationLogger)")
    public void operationLogPointcut() {
    }

    @AfterReturning(pointcut = "operationLogPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        // 从注解中获取操作类型和模块
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLogger operationLogger = method.getAnnotation(OperationLogger.class);
        log.info("OperationLogger: {}", operationLogger);
        // 记录日志
        String module = operationLogger.module();
        String action = operationLogger.action();
        String detail = operationLogger.detail();

        // 如果detail为空，则使用方法名
        if (detail.isEmpty()) {
            detail = method.getName();
        }

        // 添加日志
        operationLogService.addLog(module, action, detail);
        log.info("日志记录成功");
    }
}
