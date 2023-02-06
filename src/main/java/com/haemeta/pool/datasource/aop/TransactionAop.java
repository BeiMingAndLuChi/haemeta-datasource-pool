package com.haemeta.pool.datasource.aop;

import com.haemeta.pool.datasource.annotation.Transactions;
import com.haemeta.pool.datasource.entity.TransactionInfo;
import com.haemeta.pool.datasource.utils.TransactionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Stack;

/**
 * 数据源事务切面
 */
@Aspect
@Order(100)
@Component
public class TransactionAop {

    /**
     * 记录当前AOP是否参与了 事务事件
     * 因为AOP 是around事件 的执行顺序 是从 由外向内 然后 由内向外
     *
     * around start ->  方法体(运行) -> 方法体(结束)  -> around end
     *
     * 当多个AOP 监听到同一事件时 会发生一下情况
     *
     * around_A(order:1) start -> around_B(order:2) start
     *  -> 方法体(运行) -> 方法体(结束)
     *  ->  around_B(order:2) end -> around_A(order:1) end
     *
     * 所以会导致 baseDao 执行的释放行为 被提前执行，导致 事务关闭失败
     * 所以需要记录 当前线程释放存在事务
     */
    private static final ThreadLocal<Boolean> transRecord = new NamedThreadLocal("trans_record");

    public static Boolean getInTransaction(){
        return transRecord.get() == null? false:transRecord.get();
    }

    /**
     * @Transaction 注解的切面 ,所有 @Transaction 都会被此拦截
     */
    @Order(0)
    @Pointcut(value = "@annotation(com.haemeta.pool.datasource.annotation.Transactions)")
    public void annotationTransaction(){}

    /**
     * 拦截，并根据注解中的数据源，设置多个事务
     * @param point
     * @param annotation
     * @return
     * @throws Throwable
     */
    @Order(0)
    @Around(value = "annotationTransaction()&&@annotation(annotation)")
    public Object twiceAsOld(ProceedingJoinPoint point, Transactions annotation) throws Throwable {
        System.out.println("开启事务");
        //事务缓存列
        Stack<TransactionInfo> managerAndStatusStack = new Stack<TransactionInfo>();
        try {
            if (!TransactionUtil.openTransaction(managerAndStatusStack, annotation)) {
                return null;
            }
            transRecord.set(true);
            Object ret = point.proceed();
            TransactionUtil.commit(managerAndStatusStack);
            return ret;
        } catch (Throwable e) {
            TransactionUtil.rollback();
            throw e;
        } finally {
            transRecord.set(false);
        }
    }

}
