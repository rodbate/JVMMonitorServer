package com.rodbate.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.rodbate.core.util.GaReflectUtils.computeModifier;

/**
 * Greys封装的方法<br/>
 * 主要用来封装构造函数cinit/init/method
 * Created by oldmanpushcart@gmail.com on 15/5/24.
 */
public interface GaMethod {

    /**
     * {@link Method#invoke(Object, Object...)}
     */
    Object invoke(Object obj, Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException;

    /**
     * {@link Method#isAccessible()}
     */
    boolean isAccessible();

    /**
     * {@link Method#setAccessible(boolean)}
     */
    void setAccessible(boolean accessFlag);

    /**
     * {@link Method#getName()}
     */
    String getName();

    /**
     * {@link Method#getParameterTypes()}
     */
    Class<?>[] getParameterTypes();

    /**
     * {@link Method#getAnnotations()}
     */
    Annotation[] getAnnotations();

    /**
     * {@link Method#getModifiers()}
     */
    int getModifiers();

    /**
     * {@link Method#getDeclaringClass()}
     */
    Class<?> getDeclaringClass();

    /**
     * {@link Method#getReturnType()}
     */
    Class<?> getReturnType();

    /**
     * {@link Method#getExceptionTypes()}
     */
    Class<?>[] getExceptionTypes();

    /**
     * {@link Method#getDeclaredAnnotations()}
     */
    Annotation[] getDeclaredAnnotations();

    /**
     * 获取方法描述
     *
     * @return 方法描述
     */
    String getDesc();

    /**
     * 类实现
     */
    class MethodImpl implements GaMethod {

        private final Method target;

        public MethodImpl(Method target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object obj, Object... args)
                throws IllegalAccessException, InvocationTargetException, InstantiationException {
            return target.invoke(obj, args);
        }

        @Override
        public boolean isAccessible() {
            return target.isAccessible();
        }

        @Override
        public void setAccessible(boolean accessFlag) {
            target.setAccessible(accessFlag);
        }

        @Override
        public String getName() {
            return target.getName();
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return target.getParameterTypes();
        }

        @Override
        public Annotation[] getAnnotations() {
            return target.getAnnotations();
        }

        @Override
        public int getModifiers() {
            return target.getModifiers();
        }

        @Override
        public Class<?> getDeclaringClass() {
            return target.getDeclaringClass();
        }

        @Override
        public Class<?> getReturnType() {
            return target.getReturnType();
        }

        @Override
        public Class<?>[] getExceptionTypes() {
            return target.getExceptionTypes();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return target.getDeclaredAnnotations();
        }

        @Override
        public String getDesc() {
            return org.objectweb.asm.Type.getType(target).toString();
        }

        @Override
        public int hashCode() {
            return target.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return target.equals(obj);
        }
    }

    /**
     * 构造函数实现
     */
    class ConstructorImpl implements GaMethod {

        private final Constructor<?> target;

        public ConstructorImpl(Constructor<?> target) {
            this.target = target;
        }


        @Override
        public Object invoke(Object obj, Object... args)
                throws IllegalAccessException, InvocationTargetException, InstantiationException {
            return target.newInstance(args);
        }

        @Override
        public boolean isAccessible() {
            return target.isAccessible();
        }

        @Override
        public void setAccessible(boolean accessFlag) {
            target.setAccessible(accessFlag);
        }

        @Override
        public String getName() {
            return "<init>";
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return target.getParameterTypes();
        }

        @Override
        public Annotation[] getAnnotations() {
            return target.getAnnotations();
        }

        @Override
        public int getModifiers() {
            return GaReflectUtils.computeModifier(target);
        }

        @Override
        public Class<?> getDeclaringClass() {
            return target.getDeclaringClass();
        }

        @Override
        public Class<?> getReturnType() {
            return target.getDeclaringClass();
        }

        @Override
        public Class<?>[] getExceptionTypes() {
            return target.getExceptionTypes();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return target.getDeclaredAnnotations();
        }

        @Override
        public int hashCode() {
            return target.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return target.equals(obj);
        }

        @Override
        public String getDesc() {
            return org.objectweb.asm.Type.getType(target).toString();
        }

    }


}
