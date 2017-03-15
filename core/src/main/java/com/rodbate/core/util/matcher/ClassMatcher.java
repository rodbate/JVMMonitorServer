package com.rodbate.core.util.matcher;


import com.rodbate.core.util.GaReflectUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * 类匹配
 * Created by vlinux on 15/10/31.
 */
public class ClassMatcher extends ReflectMatcher<Class<?>> {

    // 类型
    private final int type;

    /**
     * 类匹配通用构造函数
     *
     * @param modifier         访问修饰符枚举，参考 {@link }
     * @param type             类型枚举，参考 {@link }
     * @param classNameMatcher 类名匹配
     * @param annotations      直接修饰类的Annotation匹配器
     */
    public ClassMatcher(
            final int modifier,
            final int type,
            final Matcher<String> classNameMatcher,
            final Collection<Matcher<Class<? extends Annotation>>> annotations) {
        super(modifier, classNameMatcher, annotations);
        this.type = type;
    }


    /**
     * 类类匹配构造函数<br/>
     * 主要用于Command的场景
     *
     * @param classNameMatcher 类名匹配
     */
    public ClassMatcher(final Matcher<String> classNameMatcher) {
        this(GaReflectUtils.DEFAULT_MOD, GaReflectUtils.DEFAULT_TYPE, classNameMatcher, null);
    }

    @Override
    public boolean reflectMatching(Class<?> target) {

        // 匹配type
        return matchingType(target);

    }

    @Override
    int getTargetModifiers(Class<?> target) {
        return GaReflectUtils.computeModifier(target);
    }

    @Override
    String getTargetName(Class<?> target) {
        return target.getName();
    }

    @Override
    Annotation[] getTargetAnnotationArray(Class<?> target) {
        return target.getAnnotations();
    }

    private boolean matchingType(Class<?> targetClass) {
        // 如果默认就是全类型，就不用比了
        if (type == GaReflectUtils.DEFAULT_TYPE) {
            return true;
        }
        return (type & GaReflectUtils.computeClassType(targetClass)) != 0;
    }


}
