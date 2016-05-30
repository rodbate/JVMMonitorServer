package com.dataeye.core.advisor;

import com.dataeye.core.Advice;
import com.dataeye.core.util.GaCheckUtils;
import com.dataeye.core.util.GaMethod;
import com.dataeye.core.util.collection.GaStack;
import com.dataeye.core.util.collection.ThreadUnsafeGaStack;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.dataeye.core.Advice.newForAfterRetuning;
import static com.dataeye.core.Advice.newForAfterThrowing;
import static com.dataeye.core.Advice.newForBefore;
import static com.dataeye.core.util.GaStringUtils.tranClassName;


/**
 * 反射通知适配器<br/>
 * 通过反射拿到对应的Class/Method类，而不是原始的ClassName/MethodNam
 * 当然性能开销要比普通监听器高许多
 */
public abstract class ReflectAdviceListenerAdapter<PC extends ProcessContext, IC extends InnerContext> implements AdviceListener {

    /**
     * 构造过程上下文
     *
     * @return 返回过程上下文
     */
    abstract protected PC newProcessContext();

    /**
     * 构造方法内部上下文
     *
     * @return 返回方法内部上下文
     */
    abstract protected IC newInnerContext();

    @Override
    public void create() {

    }

    @Override
    public void destroy() {

    }

    private ClassLoader toClassLoader(ClassLoader loader) {
        return null != loader
                ? loader
                : AdviceListener.class.getClassLoader();
    }

    private Class<?> toClass(ClassLoader loader, String className) throws ClassNotFoundException {
        return Class.forName(tranClassName(className), true, toClassLoader(loader));
    }

    private GaMethod toMethod(ClassLoader loader, Class<?> clazz, String methodName, String methodDesc)
            throws ClassNotFoundException, NoSuchMethodException {
        final org.objectweb.asm.Type asmType = org.objectweb.asm.Type.getMethodType(methodDesc);

        // to arg types
        final Class<?>[] argsClasses = new Class<?>[asmType.getArgumentTypes().length];
        for (int index = 0; index < argsClasses.length; index++) {

            // asm class descriptor to jvm class
            final Class<?> argumentClass;
            final Type argumentAsmType = asmType.getArgumentTypes()[index];
            switch (argumentAsmType.getSort()) {
                case Type.BOOLEAN: {
                    argumentClass = boolean.class;
                    break;
                }
                case Type.CHAR: {
                    argumentClass = char.class;
                    break;
                }
                case Type.BYTE: {
                    argumentClass = byte.class;
                    break;
                }
                case Type.SHORT: {
                    argumentClass = short.class;
                    break;
                }
                case Type.INT: {
                    argumentClass = int.class;
                    break;
                }
                case Type.FLOAT: {
                    argumentClass = float.class;
                    break;
                }
                case Type.LONG: {
                    argumentClass = long.class;
                    break;
                }
                case Type.DOUBLE: {
                    argumentClass = double.class;
                    break;
                }
                case Type.ARRAY: {
                    argumentClass = toClass(loader, argumentAsmType.getInternalName());
                    break;
                }
                case Type.VOID: {
                    argumentClass = void.class;
                    break;
                }
                case Type.OBJECT:
                case Type.METHOD:
                default: {
                    argumentClass = toClass(loader, argumentAsmType.getClassName());
                    break;
                }
            }

            argsClasses[index] = argumentClass;
        }

        // to method or constructor
        if (GaCheckUtils.isEquals(methodName, "<init>")) {
            return new GaMethod.ConstructorImpl(toConstructor(clazz, argsClasses));
        } else {
            return new GaMethod.MethodImpl(toMethod(clazz, methodName, argsClasses));
        }
    }


    private Method toMethod(Class<?> clazz, String methodName, Class<?>[] argClasses) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(methodName, argClasses);
    }

    private Constructor<?> toConstructor(Class<?> clazz, Class<?>[] argClasses) throws NoSuchMethodException {
        return clazz.getDeclaredConstructor(argClasses);
    }


    /**
     * ProcessContext的内部封装
     */
    class ProcessContextBound {

        final PC processContext;
        final GaStack<IC> innerContextGaStack = new ThreadUnsafeGaStack<IC>();

        private ProcessContextBound(PC processContext) {
            this.processContext = processContext;
        }

        /**
         * 是否顶层
         *
         * @return 是否顶层上下文
         */
        private boolean isTop() {
            return innerContextGaStack.isEmpty();
        }

    }

    protected final ThreadLocal<ProcessContextBound> processContextBoundRef = new ThreadLocal<ProcessContextBound>() {
        @Override
        protected ProcessContextBound initialValue() {
            return new ProcessContextBound(newProcessContext());
        }
    };

    @Override
    final public void before(
            ClassLoader loader, String className, String methodName, String methodDesc,
            Object target, Object[] args) throws Throwable {
        final Class<?> clazz = toClass(loader, className);
        final ProcessContextBound bound = processContextBoundRef.get();
        final PC processContext = bound.processContext;
        final IC innerContext = newInnerContext();

        final GaStack<IC> innerContextGaStack = bound.innerContextGaStack;
        innerContextGaStack.push(innerContext);

        before(
                newForBefore(loader, clazz, toMethod(loader, clazz, methodName, methodDesc), target, args),
                processContext,
                innerContext
        );

    }

    @Override
    final public void afterReturning(
            ClassLoader loader, String className, String methodName, String methodDesc,
            Object target, Object[] args, Object returnObject) throws Throwable {

        final ProcessContextBound bound = processContextBoundRef.get();
        final PC processContext = bound.processContext;
        final GaStack<IC> innerContextGaStack = bound.innerContextGaStack;
        final IC innerContext = innerContextGaStack.pop();
        try {

            // 关闭上下文
            innerContext.close();

            final Class<?> clazz = toClass(loader, className);
            final GaMethod method = toMethod(loader, clazz, methodName, methodDesc);

            final Advice advice = newForAfterRetuning(
                    loader,
                    clazz,
                    method,
                    target,
                    args,

                    // #98 在return的时候,如果目标函数是<init>,会导致return的内容缺失
                    // 初步的想法是用target(this)去代替returnObj
                    method instanceof GaMethod.ConstructorImpl ? target : returnObject
            );

            afterReturning(advice, processContext, innerContext);
            afterFinishing(advice, processContext, innerContext);

        } finally {

            // 如果过程上下文已经到了顶层则需要清除掉上下文
            if (bound.isTop()) {
                processContext.close();
                processContextBoundRef.remove();
            }

        }

    }

    @Override
    final public void afterThrowing(
            ClassLoader loader, String className, String methodName, String methodDesc,
            Object target, Object[] args, Throwable throwable) throws Throwable {

        final ProcessContextBound bound = processContextBoundRef.get();
        final PC processContext = bound.processContext;
        final GaStack<IC> innerContextGaStack = bound.innerContextGaStack;
        final IC innerContext = innerContextGaStack.pop();

        try {

            // 关闭上下文
            innerContext.close();

            final Class<?> clazz = toClass(loader, className);
            final Advice advice = newForAfterThrowing(loader, clazz, toMethod(loader, clazz, methodName, methodDesc), target, args, throwable);
            afterThrowing(advice, processContext, innerContext);
            afterFinishing(advice, processContext, innerContext);

        } finally {

            // 如果过程上下文已经到了顶层则需要清除掉上下文
            if (bound.isTop()) {
                processContext.close();
                processContextBoundRef.remove();
            }

        }

    }


    /**
     * 前置通知
     *
     * @param advice         通知点
     * @param processContext 处理上下文
     * @param innerContext   当前方法调用上下文
     * @throws Throwable 通知过程出错
     */
    public void before(Advice advice, PC processContext, IC innerContext) throws Throwable {

    }

    /**
     * 返回通知
     *
     * @param advice         通知点
     * @param processContext 处理上下文
     * @param innerContext   当前方法调用上下文
     * @throws Throwable 通知过程出错
     */
    public void afterReturning(Advice advice, PC processContext, IC innerContext) throws Throwable {

    }

    /**
     * 异常通知
     *
     * @param advice         通知点
     * @param processContext 处理上下文
     * @param innerContext   当前方法调用上下文
     * @throws Throwable 通知过程出错
     */
    public void afterThrowing(Advice advice, PC processContext, IC innerContext) throws Throwable {

    }

    /**
     * 结束通知
     *
     * @param advice         通知点
     * @param processContext 处理上下文
     * @param innerContext   当前方法调用上下文
     * @throws Throwable 通知过程出错
     */
    public void afterFinishing(Advice advice, PC processContext, IC innerContext) throws Throwable {

    }


    /**
     * 默认实现
     */
    public static class DefaultReflectAdviceListenerAdapter extends ReflectAdviceListenerAdapter<ProcessContext, InnerContext> {

        @Override
        protected ProcessContext newProcessContext() {
            return new ProcessContext();
        }

        @Override
        protected InnerContext newInnerContext() {
            return new InnerContext();
        }
    }

}