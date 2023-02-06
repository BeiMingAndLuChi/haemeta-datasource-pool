package com.haemeta.pool.datasource.utils;

import com.haemeta.pool.datasource.annotation.Transactions;
import com.haemeta.pool.datasource.config.datasource.MasterDSConfig;
import com.haemeta.pool.datasource.config.datasource.SecondaryDSConfig;
import com.haemeta.pool.datasource.entity.DynamicDataSource;
import com.haemeta.pool.datasource.entity.TransactionInfo;
import org.springframework.core.NamedThreadLocal;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Optional;
import java.util.Stack;

/**
 * 事务工具类
 */
public class TransactionUtil {

    public static final TransStatus EMPTY_STATUS = new TransStatus();

    /**
     * 当前线程的事务
     */
    private static final ThreadLocal<Stack<TransactionInfo>> pool = new NamedThreadLocal<>("trans-pool");

    /**
     * 当前线程是否开启事务
     */
    private static final ThreadLocal<TransStatus> transStatus = new NamedThreadLocal<>("trans-pool");

    /**
     * 获取当前线程的事物
     * @return
     */
    public static Stack<TransactionInfo> get(){
        return Optional.ofNullable(pool.get()).orElse(new Stack<>());
    }

    /**
     * 当前线程是否开启了事务
     * @return 默认 false
     */
    public static TransStatus inTrans(){
        return Optional.ofNullable(transStatus.get()).orElse(new TransStatus());
    }

    /**
     * 开启事务处理方法
     *
     * @param managerAndStatusStack
     * @param multiTransactional
     * @return
     */
    public static boolean openTransaction(Stack<TransactionInfo> managerAndStatusStack, Transactions multiTransactional) {
        TransStatus local = Optional.ofNullable(transStatus.get()).orElse(EMPTY_STATUS);

        TransStatus status = new TransStatus();

        transStatus.set(status);
        pool.set(managerAndStatusStack);

        if((multiTransactional.dynamic() || multiTransactional.dynamicOnly()) && !local.dynamic){
            status.dynamic = true;
        }

        if(multiTransactional.dynamicOnly()) {
            return true;
        }

        if(multiTransactional.master() && !local.master){
            status.master = true;
            //根据事务名称获取具体的事务
            managerAndStatusStack.push(
                    openTransaction(MasterDSConfig.TransactionManager)
            );
        }

        if(multiTransactional.secondary() && !local.secondary){
            status.secondary = true;
            //根据事务名称获取具体的事务
            managerAndStatusStack.push(
                    openTransaction(SecondaryDSConfig.TransactionManager)
            );
        }
        return true;
    }

    public static TransactionInfo openTransaction(String dataSourceManager) {
        //根据事务名称获取具体的事务
        DataSourceTransactionManager dataSourceTransactionManager = (DataSourceTransactionManager) SpringContextUtil
                .getBean(dataSourceManager);
        TransactionStatus transactionStatus = dataSourceTransactionManager
                .getTransaction(new DefaultTransactionDefinition());
        return new TransactionInfo(dataSourceTransactionManager, transactionStatus);
    }

    /**
     * 开启事务
     * DynamicDataSource 切换数据源时，自动触发
     * @param manager
     * @return
     */
    public static TransactionInfo openTransaction(DataSourceTransactionManager manager) {
        if(inTrans().dynamic){
            //根据事务名称获取具体的事务
            TransactionStatus transactionStatus = manager
                    .getTransaction(new DefaultTransactionDefinition());
            Stack<TransactionInfo> stack = Optional.ofNullable(pool.get()).orElse(new Stack<>());
            pool.set(stack);
            return stack.push(new TransactionInfo(manager, transactionStatus));
        }
        return null;
    }

    /**
     * 提交处理方法
     */
    public static void commit(Stack<TransactionInfo> managerAndStatusStack) {
        transStatus.set(null);
        TransactionInfo info = null;
        while (!managerAndStatusStack.isEmpty()) {
            TransactionInfo tInfo = managerAndStatusStack.pop();
            if(tInfo.getDataSource() instanceof DynamicDataSource) info = tInfo;
            else commit(tInfo);
        }
        if(info != null) commit(info);
    }

    /**
     * 提交处理方法
     */
    public static void commit(TransactionInfo managerAndStatus) {
        DataSourceTransactionManager manager = managerAndStatus.getManager();
        manager.commit(managerAndStatus.getStatus());
    }

    /**
     * 回滚处理方法
     *
     * @param managerAndStatusStack
     */
    public static void rollback(Stack<TransactionInfo> managerAndStatusStack) {
        transStatus.set(null);
        TransactionInfo info = null;
        while (!managerAndStatusStack.isEmpty()) {
            TransactionInfo tInfo = managerAndStatusStack.pop();
            if(tInfo.getDataSource() instanceof DynamicDataSource) info = tInfo;
            else rollback(tInfo);
        }
        if(info != null) rollback(info);
    }

    public static void rollback() {
        Stack<TransactionInfo> managerAndStatusStack = pool.get();
        rollback(managerAndStatusStack);
    }

    /**
     * 回滚处理方法
     * @param info
     */
    public static void rollback(TransactionInfo info) {
        info.getManager().rollback(info.getStatus());
    }

    public static class TransStatus{
        private boolean master;
        private boolean dynamic;
        private boolean secondary;


        public TransStatus(){
            this.master = false;
            this.dynamic = false;
            this.secondary = false;
        }

        public boolean master() {
            return master;
        }

        public void master(boolean master) {
            this.master = master;
        }

        public boolean dynamic() {
            return dynamic;
        }

        public void dynamic(boolean dynamic) {
            this.dynamic = dynamic;
        }

        public boolean secondary() {
            return secondary;
        }

        public void secondary(boolean secondary) {
            this.secondary = secondary;
        }
    }

}
