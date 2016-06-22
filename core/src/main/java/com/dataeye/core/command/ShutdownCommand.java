package com.dataeye.core.command;

import com.dataeye.core.advisor.Enhancer;
import com.dataeye.core.command.annotation.Cmd;
import com.dataeye.core.manager.ReflectManager;
import com.dataeye.core.server.Session;
import com.dataeye.core.util.LogUtil;
import com.dataeye.core.util.affect.EnhancerAffect;
import com.dataeye.core.util.affect.RowAffect;
import com.dataeye.core.util.matcher.ClassMatcher;
import com.dataeye.core.util.matcher.PatternMatcher;
import org.slf4j.Logger;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.apache.commons.lang3.reflect.FieldUtils.getField;

/**
 * 关闭命令
 * Created by oldmanpushcart@gmail.com on 14/10/23.
 */
@Cmd(isHacking = true, name = "shutdown", sort = 11, summary = "Shut down Greys server and exit the console",
        eg = {
                "shutdown"
        })
public class ShutdownCommand implements Command {

    private final Logger logger = LogUtil.getLogger();
    private final ReflectManager reflectManager = ReflectManager.Factory.getInstance();

    /*
     * 从GreysClassLoader中加载Spy
     */
    private Class<?> loadSpyClassFromGreysClassLoader(final ClassLoader greysClassLoader, final String spyClassName) {
        try {
            return greysClassLoader.loadClass(spyClassName);
        } catch (ClassNotFoundException e) {
            logger.warn("Spy load failed from GreysClassLoader, that is impossible!", e);
            return null;
        }
    }

    /*
     * 重置agent的greys
     * 让下载重新加载greys的时候能重新初始化ClassLoader
     */
    private void reset() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // 从GreysClassLoader中加载Spy
        final Class<?> spyClassFromGreysClassLoader = loadSpyClassFromGreysClassLoader(
                ShutdownCommand.class.getClassLoader(),
                "com.dataeye.agent.Spy"
        );
        if (null != spyClassFromGreysClassLoader) {

            // 重置整个greys
            final Method agentResetMethod = (Method) getField(spyClassFromGreysClassLoader, "AGENT_RESET_METHOD").get(null);
            agentResetMethod.invoke(null);

        }
    }

    /*
     * 重置所有已经加载到JVM的Spy
     */
    private void cleanSpy() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (final Class<?> spyClass : reflectManager.searchClass(new ClassMatcher(new PatternMatcher(PatternMatcher.Strategy.WILDCARD, "com.dataeye.agent.Spy")))) {
            final Method cleanMethod = spyClass.getMethod("clean");
            cleanMethod.invoke(null);
        }
    }

    @Override
    public Action getAction() {
        return new RowAction() {
            @Override
            public RowAffect action(Session session, Instrumentation inst, Printer printer) throws Throwable {

                // 退出之前需要重置所有的增强类
                // 重置之前增强的类
                final EnhancerAffect enhancerAffect = Enhancer.reset(inst);

                // reset for agent ClassLoader
                reset();

                // cleanSpy the spy
                cleanSpy();

                printer.println("Greys Server is shut down.").finish();
                return new RowAffect(enhancerAffect.cCnt());
            }

        };
    }

}
