package com.dataeye.core.command;



import com.dataeye.core.Advice;
import com.dataeye.core.advisor.AdviceListener;
import com.dataeye.core.advisor.InnerContext;
import com.dataeye.core.advisor.ProcessContext;
import com.dataeye.core.advisor.ReflectAdviceListenerAdapter;
import com.dataeye.core.command.annotation.Cmd;
import com.dataeye.core.command.annotation.IndexArg;
import com.dataeye.core.command.annotation.NamedArg;
import com.dataeye.core.server.Session;
import com.dataeye.core.textui.TTable;
import com.dataeye.core.util.GaMethod;
import com.dataeye.core.util.PointCut;
import com.dataeye.core.util.SimpleDateFormatHolder;
import com.dataeye.core.util.matcher.ClassMatcher;
import com.dataeye.core.util.matcher.GaMethodMatcher;
import com.dataeye.core.util.matcher.PatternMatcher;

import java.lang.instrument.Instrumentation;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.dataeye.core.util.GaCheckUtils.isEquals;


/**
 * 监控请求命令<br/>
 * 输出的内容格式为:<br/>
 * <style type="text/css">
 * table, th, td {
 * borders:1px solid #cccccc;
 * borders-collapse:collapse;
 * }
 * </style>
 * <table>
 * <tr>
 * <th>时间戳</th>
 * <th>统计周期(s)</th>
 * <th>类全路径</th>
 * <th>方法名</th>
 * <th>调用总次数</th>
 * <th>成功次数</th>
 * <th>失败次数</th>
 * <th>平均耗时(ms)</th>
 * <th>失败率</th>
 * </tr>
 * <tr>
 * <td>2012-11-07 05:00:01</td>
 * <td>120</td>
 * <td>com.taobao.item.ItemQueryServiceImpl</td>
 * <td>queryItemForDetail</td>
 * <td>1500</td>
 * <td>1000</td>
 * <td>500</td>
 * <td>15</td>
 * <td>30%</td>
 * </tr>
 * <tr>
 * <td>2012-11-07 05:00:01</td>
 * <td>120</td>
 * <td>com.taobao.item.ItemQueryServiceImpl</td>
 * <td>queryItemById</td>
 * <td>900</td>
 * <td>900</td>
 * <td>0</td>
 * <td>7</td>
 * <td>0%</td>
 * </tr>
 * </table>
 *
 * @author oldmanpushcart@gmail.com
 */
@Cmd(name = "monitor", sort = 2, summary = "Monitor the execution of specified Class and its method",
        eg = {
                "monitor -c 5 -E org\\.apache\\.commons\\.lang\\.StringUtils *",
                "monitor -c 5 org.apache.commons.lang.StringUtils is*",
                "monitor *StringUtils isBlank"
        })
public class MonitorCommand implements Command {

    @IndexArg(index = 0, name = "class-pattern", summary = "Path and classname of Pattern Matching")
    private String classPattern;

    @IndexArg(index = 1, name = "method-pattern", summary = "Method of Pattern Matching")
    private String methodPattern;

    @NamedArg(name = "c", hasValue = true, summary = "The cycle of monitor")
    private int cycle = 120;

    @NamedArg(name = "E", summary = "Enable regular expression to match (wildcard matching by default)")
    private boolean isRegEx = false;

    /**
     * 数据监控用的Key
     *
     * @author oldmanpushcart@gmail.com
     */
    private static class Key {
        private final String className;
        private final String methodName;

        private Key(Class<?> clazz, GaMethod method) {
            this.className = clazz.getName();
            this.methodName = method.getName();
        }

        @Override
        public int hashCode() {
            return className.hashCode() + methodName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj
                    || !(obj instanceof Key)) {
                return false;
            }
            Key oKey = (Key) obj;
            return isEquals(oKey.className, className)
                    && isEquals(oKey.methodName, methodName);
        }

    }

    /**
     * 数据监控用的value
     *
     * @author oldmanpushcart@gmail.com
     */
    private static class Data {
        private int total;
        private int success;
        private int failed;
        private long cost;
        private Long maxCost;
        private Long minCost;
    }

    @Override
    public Action getAction() {

        return new GetEnhancerAction() {

            @Override
            public GetEnhancer action(final Session session, Instrumentation inst, final Printer printer) throws Throwable {
                return new GetEnhancer() {

                    @Override
                    public PointCut getPointCut() {
                        return new PointCut(
                                new ClassMatcher(new PatternMatcher(isRegEx, classPattern)),
                                new GaMethodMatcher(new PatternMatcher(isRegEx, methodPattern))
                        );
                    }

                    @Override
                    public AdviceListener getAdviceListener() {

                        return new ReflectAdviceListenerAdapter.DefaultReflectAdviceListenerAdapter() {

                            /*
                             * 输出定时任务
                             */
                            private Timer timer;

                            /*
                             * 监控数据
                             */
                            private final ConcurrentHashMap<Key, AtomicReference<Data>> monitorData
                                    = new ConcurrentHashMap<Key, AtomicReference<Data>>();

                            private double div(double a, double b) {
                                if (b == 0) {
                                    return 0;
                                }
                                return a / b;
                            }

                            @Override
                            public void create() {
                                timer = new Timer("Timer-for-greys-monitor-" + session.getSessionId(), true);
                                timer.scheduleAtFixedRate(new TimerTask() {

                                    @Override
                                    public void run() {
//                                        if (monitorData.isEmpty()) {
//                                            return;
//                                        }

                                        final TTable tTable = new TTable(10)
                                                .addRow(
                                                        "TIMESTAMP",
                                                        "CLASS",
                                                        "METHOD",
                                                        "TOTAL",
                                                        "SUCCESS",
                                                        "FAIL",
                                                        "FAIL-RATE",
                                                        "AVG-RT(ms)",
                                                        "MIN-RT(ms)",
                                                        "MAX-RT(ms)"
                                                );

                                        for (Map.Entry<Key, AtomicReference<Data>> entry : monitorData.entrySet()) {
                                            final AtomicReference<Data> value = entry.getValue();

                                            Data data;
                                            while (true) {
                                                data = value.get();
                                                if (value.compareAndSet(data, new Data())) {
                                                    break;
                                                }
                                            }

                                            if (null != data) {

                                                final DecimalFormat df = new DecimalFormat("0.00");

                                                tTable.addRow(
                                                        SimpleDateFormatHolder.getInstance().format(new Date()),
                                                        entry.getKey().className,
                                                        entry.getKey().methodName,
                                                        data.total,
                                                        data.success,
                                                        data.failed,
                                                        df.format(100.0d * div(data.failed, data.total)) + "%",
                                                        df.format(div(data.cost, data.total)),
                                                        data.minCost,
                                                        data.maxCost
                                                );

                                            }
                                        }

                                        tTable.padding(1);

                                        printer.println(tTable.rendering());
                                    }

                                }, 0, cycle * 1000);
                            }

                            @Override
                            public void destroy() {
                                if (null != timer) {
                                    timer.cancel();
                                }
                            }

                            @Override
                            public void afterFinishing(Advice advice, ProcessContext processContext, InnerContext innerContext) throws Throwable {
                                final Key key = new Key(advice.clazz, advice.method);
                                final long cost = innerContext.getCost();

                                while (true) {
                                    final AtomicReference<Data> value = monitorData.get(key);
                                    if (null == value) {
                                        monitorData.putIfAbsent(key, new AtomicReference<Data>(new Data()));
                                        // 这里不去判断返回值，用continue去强制获取一次
                                        continue;
                                    }

                                    while (true) {
                                        Data oData = value.get();
                                        Data nData = new Data();
                                        nData.cost = oData.cost + cost;
                                        if (advice.isThrow) {
                                            nData.failed = oData.failed + 1;
                                            nData.success = oData.success;
                                        } else {
                                            nData.failed = oData.failed;
                                            nData.success = oData.success + 1;
                                        }
                                        nData.total = oData.total + 1;

                                        // setValue max-cost
                                        if (null == oData.maxCost) {
                                            nData.maxCost = cost;
                                        } else {
                                            nData.maxCost = Math.max(oData.maxCost, cost);
                                        }

                                        // setValue min-cost
                                        if (null == oData.minCost) {
                                            nData.minCost = cost;
                                        } else {
                                            nData.minCost = Math.min(oData.minCost, cost);
                                        }


                                        if (value.compareAndSet(oData, nData)) {
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }

                        };
                    }
                };
            }

        };
    }


}
