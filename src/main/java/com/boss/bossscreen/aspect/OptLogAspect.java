package com.boss.bossscreen.aspect;

import com.boss.bossscreen.annotation.OptLog;
import com.boss.bossscreen.enities.OperationLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;


/**
 * 操作日志切面处理
 */
//@Aspect
//@Component
public class OptLogAspect {

//    @Autowired
//    private OperationLogDao operationLogDao;

    /**
     * 设置操作日志切入点 记录操作日志 在注解的位置切入代码
     */
    @Pointcut("@annotation(com.boss.bossscreen.annotation.OptLog)")
    public void optLogPointCut() {}


    /**
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     *
     * @param joinPoint 切入点
     * @param keys      返回结果
     */
    @AfterReturning(value = "optLogPointCut()", returning = "keys")
    public void saveOptLog(JoinPoint joinPoint, Object keys) {
        OperationLog operationLog = new OperationLog();
        // 从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取切入点所在的方法
        Method method = signature.getMethod();
        // 获取操作
        OptLog optLog = method.getAnnotation(OptLog.class);
        // 操作类型
        operationLog.setOptType(optLog.optType());
        // 操作描述
        operationLog.setOptDesc(operationLog.getOptDesc());

//        operationLogDao.insert(operationLog);
    }

}
