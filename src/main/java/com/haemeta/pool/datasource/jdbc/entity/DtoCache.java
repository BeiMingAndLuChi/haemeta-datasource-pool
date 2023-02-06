package com.haemeta.pool.datasource.jdbc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haemeta.common.entity.pojo.AB;
import com.haemeta.common.utils.lang.JavaBeanUtils;
import com.haemeta.common.utils.lang.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @date 2022年1月6日
 * @author Sjf
 */
public class DtoCache {

    private static final Map<String, DtoInfo> cache = new HashMap<>();

    public static <T> DtoInfo getDtoInfo(T bean) {
        Class<T> clazz = (Class<T>) bean.getClass();
        String className = clazz.getClass().getName();
        DtoInfo dtoInfo = cache.get(className);
        if (dtoInfo != null) return dtoInfo;
        synchronized (bean) {
            dtoInfo = cache.get(className);
            if (dtoInfo != null) return dtoInfo;
            dtoInfo = new DtoInfo();

            //设置 类名
            dtoInfo.setName(clazz.getSimpleName());
            //设置 精准类名
            dtoInfo.setClassName(clazz.getName());
            //设置字段列表
            dtoInfo.setFields(getVariableName(clazz));

            //获取 get set 方法
            AB<Map<String, Method>, Map<String, Method>> getAndSet = JavaBeanUtils.getGetSetMethod(clazz, dtoInfo.getFields());

            //设置 个字段的 get 方法
            dtoInfo.setGetMethods(getAndSet.getA());
            //设置 个字段的 set 方法
            dtoInfo.setSetMethods(getAndSet.getB());

            //设置 数据库 name
            TableName tn = clazz.getAnnotation(TableName.class);
            String tableName;
            if (tn != null) {
                tableName = tn.value();
            } else {
                tableName = StringUtil.humpToUnderline(
                        StringUtil.lowerFirstCase(dtoInfo.getName())
                ).toLowerCase();
            }
            dtoInfo.setTableName(tableName);

            Map<String, String> columns = new HashMap<>();
            Map<String, String> getFieldByColumn = new HashMap<>();
            dtoInfo.getFields().forEach(field -> {
                String columnName = StringUtil.humpToUnderline(field);
                columns.put(field, StringUtil.humpToUnderline(columnName));
                getFieldByColumn.put(columnName, field);
            });
            dtoInfo.setColumns(columns);
            dtoInfo.setGetFieldByColumn(getFieldByColumn);


            Map<String, Method> columnsGet = new HashMap<>();
            Map<String, Method> columnsSet = new HashMap<>();
            dtoInfo.getGetMethods().forEach((key, method) -> {
                columnsGet.put(
                        StringUtil.humpToUnderline(key),
                        method
                );
            });
            dtoInfo.getSetMethods().forEach((key, method) -> {
                columnsSet.put(
                        StringUtil.humpToUnderline(key),
                        method
                );
            });

            dtoInfo.setColumnsGet(columnsGet);
            dtoInfo.setColumnsSet(columnsSet);

            cache.put(dtoInfo.getClassName(), dtoInfo);
            return dtoInfo;
        }
    }

    public static void main(String[] args) {
        DtoInfo inf = getDtoInfo(new DtoInfo());
    }


    /**
     * 获取类的变量名 (包括所有父类)
     * @param clazz 类加载器
     * @return 获取类的变量名 (包括所有父类)
     */
    public static List<String> getVariableName(Class<?> clazz) {
        List<String> varNameList = new ArrayList<>();
        // 遍历所有父类字节码对象
        while (clazz != null) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                if (field.isAnnotationPresent(TableField.class)) {
                    TableField tf = field.getAnnotation(TableField.class);
                    if (tf != null && !tf.exist()) continue;
                }
                varNameList.add(field.getName());
            }
            // 获得父类的字节码对象
            clazz = clazz.getSuperclass();
        }
        return varNameList;
    }
}
