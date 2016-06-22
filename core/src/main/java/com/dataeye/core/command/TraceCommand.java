package com.dataeye.core.command;


import com.dataeye.core.Advice;
import com.dataeye.core.GlobalOptions;
import com.dataeye.core.advisor.*;
import com.dataeye.core.command.annotation.Cmd;
import com.dataeye.core.command.annotation.IndexArg;
import com.dataeye.core.command.annotation.NamedArg;
import com.dataeye.core.exception.ExpressException;
import com.dataeye.core.server.Session;
import com.dataeye.core.textui.TTree;
import com.dataeye.core.util.PointCut;
import com.dataeye.core.util.matcher.ClassMatcher;
import com.dataeye.core.util.matcher.GaMethodMatcher;
import com.dataeye.core.util.matcher.PatternMatcher;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dataeye.core.util.Express.ExpressFactory.newExpress;
import static com.dataeye.core.util.GaStringUtils.getThreadInfo;
import static com.dataeye.core.util.GaStringUtils.tranClassName;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * 调用跟踪命令<br/>
 * 负责输出一个类中的所有方法调用路径 Created by oldmanpushcart@gmail.com on 15/5/27.
 */
@Cmd(name = "trace", sort = 6, summary = "Display the detailed thread stack of specified class and method",
        eg = {
                "trace -E org\\.apache\\.commons\\.lang\\.StringUtils isBlank",
                "trace org.apache.commons.lang.StringUtils isBlank",
                "trace *StringUtils isBlank",
                "trace *StringUtils isBlank params[0].length==1",
                "trace -n 2 *StringUtils isBlank",
        })
public class TraceCommand implements Command {

    @IndexArg(index = 0, name = "class-pattern", summary = "Path and classname of Pattern Matching")
    private String classPattern;

    @IndexArg(index = 1, name = "method-pattern", summary = "Method of Pattern Matching")
    private String methodPattern;

    @IndexArg(index = 2, name = "condition-express", isRequired = false,
            summary = "Conditional expression by OGNL",
            description = "" +
                    "FOR EXAMPLE" +
                    "\n" +
                    "     TRUE : 1==1\n" +
                    "     TRUE : true\n" +
                    "    FALSE : false\n" +
                    "     TRUE : params.length>=0\n" +
                    "    FALSE : 1==2\n" +
                    "\n" +
                    "THE STRUCTURE" +
                    "\n" +
                    "          target : the object \n" +
                    "           clazz : the object's class\n" +
                    "          method : the constructor or method\n" +
                    "    params[0..n] : the parameters of method\n" +
                    "       returnObj : the returned object of method\n" +
                    "        throwExp : the throw exception of method\n" +
                    "        isReturn : the method ended by return\n" +
                    "         isThrow : the method ended by throwing exception"
    )
    private String conditionExpress;

    @NamedArg(name = "E", summary = "Enable regular expression to match (wildcard matching by default)")
    private boolean isRegEx = false;

    @NamedArg(name = "n", hasValue = true, summary = "Threshold of execution times")
    private Integer threshold;

    @Override
    public Action getAction() {

        return new GetEnhancerAction() {

            @Override
            public GetEnhancer action(Session session, Instrumentation inst, final Printer printer) throws Throwable {

                return new GetEnhancer() {

                    @Override
                    public PointCut getPointCut() {
                        return new PointCut(
                                new ClassMatcher(new PatternMatcher(isRegEx, classPattern)),
                                new GaMethodMatcher(new PatternMatcher(isRegEx, methodPattern)),

                                // don't include the sub class when tracing...
                                // fixed for #94
                                GlobalOptions.isTracingSubClass
                        );
                    }

                    // 访问计数器
                    private final AtomicInteger timesRef = new AtomicInteger();

                    @Override
                    public AdviceListener getAdviceListener() {
                        return new ReflectAdviceTracingListenerAdapter<ProcessContext, TraceInnerContext>() {

                            @Override
                            public void invokeBeforeTracing(
                                    Integer tracingLineNumber,
                                    String tracingClassName,
                                    String tracingMethodName,
                                    String tracingMethodDesc,
                                    ProcessContext processContext,
                                    TraceInnerContext innerContext) throws Throwable {
                                final Entity entity = innerContext.entity;
                                if (null == tracingLineNumber) {
                                    entity.tTree.begin(tranClassName(tracingClassName) + ":" + tracingMethodName + "()");
                                } else {
                                    entity.tTree.begin(tranClassName(tracingClassName) + ":" + tracingMethodName + "(@" + tracingLineNumber + ")");
                                }

                            }

                            @Override
                            public void invokeAfterTracing(
                                    Integer tracingLineNumber,
                                    String tracingClassName,
                                    String tracingMethodName,
                                    String tracingMethodDesc,
                                    ProcessContext processContext,
                                    TraceInnerContext innerContext) throws Throwable {
                                innerContext.entity.tTree.end();
                            }

                            @Override
                            public void invokeThrowTracing(
                                    Integer tracingLineNumber,
                                    String tracingClassName,
                                    String tracingMethodName,
                                    String tracingMethodDesc,
                                    String throwException,
                                    ProcessContext processContext,
                                    TraceInnerContext innerContext) throws Throwable {
                                final Entity entity = innerContext.entity;
                                entity.tTree.set(entity.tTree.get() + "[throw " + throwException + "]");
                                entity.tTree.end();
                            }

                            @Override
                            public void before(Advice advice, ProcessContext processContext, TraceInnerContext innerContext) throws Throwable {

                                final Entity entity = innerContext.getEntity(new InitCallback<Entity>() {
                                    @Override
                                    public Entity init() {
                                        return new Entity();
                                    }
                                });

                                entity.tTree = new TTree(true, "Tracing for : " + getThreadInfo())
                                        .begin(advice.clazz.getName() + ":" + advice.method.getName() + "()");

                            }

                            @Override
                            protected ProcessContext newProcessContext() {
                                return new ProcessContext();
                            }

                            @Override
                            protected TraceInnerContext newInnerContext() {
                                return new TraceInnerContext();
                            }

                            @Override
                            public void afterReturning(Advice advice, ProcessContext processContext, TraceInnerContext innerContext) throws Throwable {
                                final Entity entity = innerContext.entity;
                                entity.tTree.end();
                            }

                            @Override
                            public void afterThrowing(Advice advice, ProcessContext processContext, TraceInnerContext innerContext) throws Throwable {
                                final Entity entity = innerContext.entity;
                                entity.tTree.begin("throw:" + advice.throwExp.getClass().getName() + "()").end().end();

                                // 这里将堆栈的end全部补上
                                //while (entity.tracingDeep-- >= 0) {
                                //    entity.tTree.end();
                                //}

                            }

                            private boolean isInCondition(Advice advice, long cost) {
                                try {
                                    return isBlank(conditionExpress)
                                            || newExpress(advice).bind("cost", cost).is(conditionExpress);
                                } catch (ExpressException e) {
                                    return false;
                                }
                            }

                            private boolean isOverThreshold(int currentTimes) {
                                return null != threshold
                                        && currentTimes >= threshold;
                            }

                            @Override
                            public void afterFinishing(Advice advice, ProcessContext processContext, TraceInnerContext innerContext) throws Throwable {
                                final long cost = innerContext.getCost();
                                if (isInCondition(advice, cost)) {
                                    final Entity entity = innerContext.entity;
                                    System.out.println(entity.tTree.rendering());

                                    printer.println(entity.tTree.rendering());
                                    if (isOverThreshold(timesRef.incrementAndGet())) {
                                        printer.finish();
                                    }
                                }
                            }

                        };
                    }
                };
            }

        };

    }

    private class Entity {
        TTree tTree;
    }

    private class TraceInnerContext extends InnerContext {

        Entity entity;

        Entity getEntity(InitCallback<Entity> initCallback) {
            if (null == entity) {
                return entity = initCallback.init();
            } else {
                return entity;
            }
        }

    }

}
