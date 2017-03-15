package com.rodbate.core.command;


import com.rodbate.core.Advice;
import com.rodbate.core.advisor.AdviceListener;
import com.rodbate.core.advisor.InnerContext;
import com.rodbate.core.advisor.ProcessContext;
import com.rodbate.core.advisor.ReflectAdviceListenerAdapter;
import com.rodbate.core.command.annotation.Cmd;
import com.rodbate.core.command.annotation.IndexArg;
import com.rodbate.core.command.annotation.NamedArg;
import com.rodbate.core.exception.ExpressException;
import com.rodbate.core.server.Session;
import com.rodbate.core.util.PointCut;
import com.rodbate.core.util.matcher.ClassMatcher;
import com.rodbate.core.util.matcher.GaMethodMatcher;
import com.rodbate.core.util.matcher.PatternMatcher;
import com.rodbate.core.util.GaStringUtils;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rodbate.core.util.Express.ExpressFactory.newExpress;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Jstack命令<br/>
 * 负责输出当前方法执行上下文
 *
 * @author oldmanpushcart@gmail.com
 */
@Cmd(name = "stack", sort = 6, summary = "Display the stack trace of specified class and method",
        eg = {
                "stack -E org\\.apache\\.commons\\.lang\\.StringUtils isBlank",
                "stack org.apache.commons.lang.StringUtils isBlank",
                "stack *StringUtils isBlank",
                "stack *StringUtils isBlank params[0].length==1"
        })
public class StackCommand implements Command {

    @IndexArg(index = 0, name = "class-pattern", summary = "Path and classname of Pattern Matching")
    private String classPattern;

    @IndexArg(index = 1, name = "method-pattern", isRequired = false, summary = "Method of Pattern Matching")
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

                    private final AtomicInteger times = new AtomicInteger();

                    @Override
                    public PointCut getPointCut() {
                        return new PointCut(
                                new ClassMatcher(new PatternMatcher(isRegEx, classPattern)),
                                new GaMethodMatcher(new PatternMatcher(isRegEx, methodPattern))
                        );
                    }

                    @Override
                    public AdviceListener getAdviceListener() {
                        return new ReflectAdviceListenerAdapter<ProcessContext, StackInnerContext>() {

                            @Override
                            protected ProcessContext newProcessContext() {
                                return new ProcessContext();
                            }

                            @Override
                            protected StackInnerContext newInnerContext() {
                                return new StackInnerContext();
                            }

                            @Override
                            public void before(Advice advice, ProcessContext processContext, StackInnerContext innerContext) throws Throwable {
                                innerContext.stack = GaStringUtils.getStack();
                            }

                            private boolean isInCondition(Advice advice) {
                                try {
                                    return isBlank(conditionExpress)
                                            || newExpress(advice).is(conditionExpress);
                                } catch (ExpressException e) {
                                    return false;
                                }
                            }

                            private boolean isOverThreshold(int currentTimes) {
                                return null != threshold
                                        && currentTimes >= threshold;
                            }

                            @Override
                            public void afterFinishing(Advice advice, ProcessContext processContext, StackInnerContext innerContext) throws Throwable {
                                if (isInCondition(advice)) {
                                    printer.println(innerContext.stack);
                                    if (isOverThreshold(times.incrementAndGet())) {
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

    private class StackInnerContext extends InnerContext {
        private String stack;
    }

}
