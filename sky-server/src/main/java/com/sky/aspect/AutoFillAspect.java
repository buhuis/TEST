package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 拦截带有 @AutoFill 注解的方法，在执行目标方法前为公共字段赋值
     *
     * @param joinPoint
     * @param autoFill
     * @return
     * @throws Throwable
     */
    @Around("@annotation(autoFill)")
    public Object autoFill(ProceedingJoinPoint joinPoint, AutoFill autoFill) throws Throwable {
        log.info("开始进行公共字段自动填充...");

        //1、获取方法签名上的数据库操作类型（INSERT / UPDATE）
        OperationType operationType = autoFill.value();

        //2、获取到当前被拦截的方法的参数列表（实体对象）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 第一个参数为实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.proceed();
        }
        Object entity = args[0];

        //3、准备赋值的数据：当前时间、当前登录用户id
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //4、根据当前不同的操作类型，为对应的属性通过反射来赋值
        if (operationType == OperationType.INSERT) {
            // 为4个公共字段赋值
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //通过反射为对象属性赋值
            setCreateTime.invoke(entity, now);
            setCreateUser.invoke(entity, currentId);
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
        } else if (operationType == OperationType.UPDATE) {
            // 为2个公共字段赋值
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //通过反射为对象属性赋值
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
        }

        //5、执行目标方法
        return joinPoint.proceed();
    }

}