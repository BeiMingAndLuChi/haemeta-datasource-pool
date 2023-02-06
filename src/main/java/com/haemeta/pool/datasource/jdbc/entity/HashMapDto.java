package com.haemeta.pool.datasource.jdbc.entity;

import com.haemeta.common.utils.lang.StringUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HashMapDto<T,V> implements Map<T,V> {

    private Map<T,V> map;

    public HashMapDto(){
        map = new HashMap<>();
    }

    public HashMapDto(boolean linked){
        if (true) map = new LinkedHashMap<>();
        else map = new HashMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return containsValue( value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(T key, V value) {
        if(key instanceof String)
            return map.put((T) StringUtil.underlineToHump((String) key), value);
        else
            return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends T, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<T> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<T, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return map.getOrDefault( key,  defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super T, ? super V> action) {
       map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super T, ? super V, ? extends V> function) {
        map.replaceAll(function);
    }

    @Override
    public V putIfAbsent(T key, V value) {
        return putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(T key, V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(T key, V value) {
        return map.replace(key, value);
    }

    @Override
    public V computeIfAbsent(T key, Function<? super T, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(T key, BiFunction<? super T, ? super V, ? extends V> remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(T key, BiFunction<? super T, ? super V, ? extends V> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    @Override
    public V merge(T key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }

    public Integer getInteger(T key){
        Object valObj = map.get(key);
        if(valObj instanceof Integer)
            return (Integer) valObj;
        else
            return null;
    }

    public String getString(T key){
        Object valObj = map.get(key);
        if(valObj instanceof String)
            return (String) valObj;
        else
            return null;
    }
    public BigDecimal getBigDecimal(T key){
        Object valObj = map.get(key);
        if(valObj instanceof BigDecimal)
            return (BigDecimal) valObj;
        else
            return null;
    }
    public Long getLong(T key){
        Object valObj = map.get(key);
        if(valObj instanceof Long)
            return (Long) valObj;
        else
            return null;
    }
    public Double getDouble(T key){
        Object valObj = map.get(key);
        if(valObj instanceof Double)
            return (Double) valObj;
        else
            return null;
    }
    public Float getFlout(T key){
        Object valObj = map.get(key);
        if(valObj instanceof Float)
            return (Float) valObj;
        else
            return null;
    }
    public Boolean getBoolean(T key){
        Object valObj = map.get(key);
        if(valObj instanceof Boolean)
            return (Boolean) valObj;
        else
            return null;
    }
    public byte[] getBytes(T key){
        Object valObj = map.get(key);
        if(valObj instanceof byte[])
            return (byte[]) valObj;
        else
            return null;
    }
    public Date getDate(T key){
        Object valObj = map.get(key);
        if(valObj instanceof Date)
            return (Date) valObj;
        else
            return null;
    }
    public LocalDateTime getLocalDateTime(T key){
        Object valObj = map.get(key);
        if(valObj instanceof LocalDateTime)
            return (LocalDateTime) valObj;
        else
            return null;
    }
}
