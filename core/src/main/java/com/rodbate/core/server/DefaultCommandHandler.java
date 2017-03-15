package com.rodbate.core.server;

import com.rodbate.core.advisor.AdviceListener;
import com.rodbate.core.advisor.AdviceWeaver;
import com.rodbate.core.advisor.Enhancer;
import com.rodbate.core.advisor.InvokeTraceable;
import com.rodbate.core.command.Command;
import com.rodbate.core.command.Commands;
import com.rodbate.core.command.QuitCommand;
import com.rodbate.core.command.ShutdownCommand;
import com.rodbate.core.exception.CommandException;
import com.rodbate.core.exception.CommandInitializationException;
import com.rodbate.core.exception.CommandNotFoundException;
import com.rodbate.core.exception.GaExecuteException;
import com.rodbate.core.util.GaStringUtils;
import com.rodbate.core.util.LogUtil;
import com.rodbate.core.util.affect.Affect;
import com.rodbate.core.util.affect.EnhancerAffect;
import com.rodbate.core.util.affect.RowAffect;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.rodbate.core.util.GaCheckUtils.$;
import static com.rodbate.core.util.GaCheckUtils.$$;
import static java.lang.String.format;
import static java.nio.ByteBuffer.wrap;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * 命令处理器
 * Created by oldmanpushcart@gmail.com on 15/5/2.
 */
public class DefaultCommandHandler implements CommandHandler {

    private final Logger logger = LogUtil.getLogger();

    private final GaServer gaServer;
    private final Instrumentation inst;

    public DefaultCommandHandler(GaServer gaServer, Instrumentation inst) {
        this.gaServer = gaServer;
        this.inst = inst;
    }

    @Override
    public void executeCommand(final String line, final Session session) throws IOException {

        final SocketChannel socketChannel = session.getSocketChannel();

        // 只有输入了有效字符才进行命令解析
        // 否则仅仅重绘提示符
        if (isBlank(line)) {

            // 这里因为控制不好，造成了输出两次提示符的问题
            // 第一次是因为这里，第二次则是下边（命令结束重绘提示符）
            // 这里做了一次取巧，虽然依旧是重绘了两次提示符，但在提示符之间增加了\r
            // 这样两次重绘都是在同一个位置，这样就没有人能发现，其实他们是被绘制了两次
            logger.debug("reDrawPrompt for blank line.");
            reDrawPrompt(session, socketChannel, session.getCharset(), session.prompt());
            return;
        }

        // don't ask why
        if ($(line)) {
            write(socketChannel, wrap($$()));
            reDrawPrompt(session, socketChannel, session.getCharset(), session.prompt());
            return;
        }

        try {
            final Command command = Commands.getInstance().newCommand(line);
            execute(session, command);

            // 退出命令，需要关闭会话
            if (command instanceof QuitCommand) {
                session.destroy();
            }

            // 关闭命令，需要关闭整个服务端
            else if (command instanceof ShutdownCommand) {
                DefaultCommandHandler.this.gaServer.destroy();
            }

            // 其他命令需要重新绘制提示符
            else {
                logger.debug("reDrawPrompt for command execute finished.");
                reDrawPrompt(session, socketChannel, session.getCharset(), session.prompt());
            }

        }

        // 命令准备错误(参数校验等)
        catch (CommandException t) {

            final String message;
            if (t instanceof CommandNotFoundException) {
                message = format("Command \"%s\" not found.", t.getCommand());
            } else if (t instanceof CommandInitializationException) {
                message = format("Command \"%s\" failed to initiate.", t.getCommand());
            } else {
                message = String.format("Command \"%s\" preprocessor failed : %s.", t.getCommand(), GaStringUtils.getCauseMessage(t));
            }

            write(socketChannel, message + "\n", session.getCharset());
            reDrawPrompt(session, socketChannel, session.getCharset(), session.prompt());

            logger.info(message, t);

        }

        // 命令执行错误
        catch (GaExecuteException e) {
            logger.warn("command execute failed.", e);

            final String cause = GaStringUtils.getCauseMessage(e);
            if (StringUtils.isNotBlank(cause)) {
                write(socketChannel, format("Command execution failed. cause : %s\n", cause), session.getCharset());
            } else {
                write(socketChannel, "Command execution failed.\n", session.getCharset());
            }

            reDrawPrompt(session, socketChannel, session.getCharset(), session.prompt());
        }

    }


    /*
     * 执行命令
     */
    private void execute(final Session session, final Command command) throws GaExecuteException, IOException {

        // 是否结束输入引用
        final AtomicBoolean isFinishRef = new AtomicBoolean(false);

        // 消息发送者
        final Command.Printer printer = new Command.Printer() {

            @Override
            public Command.Printer print(boolean isF, String message) {

                final BlockingQueue<String> writeQueue = session.getWriteQueue();
                if (null != message) {
                    if (!writeQueue.offer(message)) {
                        logger.warn("offer message failed. write-queue.size() was {}", writeQueue.size());
                    }
                }

                if (isF) {
                    finish();
                }

                return this;

            }

            @Override
            public Command.Printer println(boolean isF, String message) {
                return print(isF, message + "\n");
            }

            @Override
            public Command.Printer print(String message) {
                return print(false, message);
            }

            @Override
            public Command.Printer println(String message) {
                return println(false, message);
            }

            @Override
            public void finish() {
                isFinishRef.set(true);
            }

        };


        try {

            // 影响反馈
            final Affect affect;
            final Command.Action action = command.getAction();

            // 无任何后续动作的动作
            if (action instanceof Command.SilentAction) {
                affect = new Affect();
                ((Command.SilentAction) action).action(session, inst, printer);
            }

            // 需要反馈行影响范围的动作
            else if (action instanceof Command.RowAction) {
                affect = new RowAffect();
                final RowAffect rowAffect = ((Command.RowAction) action).action(session, inst, printer);
                ((RowAffect) affect).rCnt(rowAffect.rCnt());
            }

            // 需要做类增强的动作
            else if (action instanceof Command.GetEnhancerAction) {

                affect = new EnhancerAffect();

                // 执行命令动作 & 获取增强器
                final Command.GetEnhancer getEnhancer = ((Command.GetEnhancerAction) action).action(session, inst, printer);

                final int lock = session.getLock();
                final AdviceListener listener = getEnhancer.getAdviceListener();
                final EnhancerAffect enhancerAffect = Enhancer.enhance(
                        inst,
                        lock,
                        listener instanceof InvokeTraceable,
                        getEnhancer.getPointCut()
                );

                // 这里做个补偿,如果在enhance期间,unLock被调用了,则补偿性放弃
                if (session.getLock() == lock) {
                    // 注册通知监听器
                    AdviceWeaver.reg(lock, listener);

                    if (!session.isSilent()) {
                        printer.println(GaStringUtils.ABORT_MSG);
                    }

                    ((EnhancerAffect) affect).cCnt(enhancerAffect.cCnt());
                    ((EnhancerAffect) affect).mCnt(enhancerAffect.mCnt());
                    ((EnhancerAffect) affect).getClassDumpFiles().addAll(enhancerAffect.getClassDumpFiles());
                }
            }

            // 其他自定义动作
            else {
                // do nothing...
                affect = new Affect();
            }

            if (!session.isSilent()) {
                // 记录下命令执行的执行信息
                printer.print(false, affect.toString() + "\n");
            }

        }

        // 命令执行错误必须纪录
        catch (Throwable t) {
            throw new GaExecuteException(format("execute failed. sessionId=%s", session.getSessionId()), t);
        }

        // 跑任务
        jobRunning(session, isFinishRef);

    }

    private void jobRunning(Session session, AtomicBoolean isFinishRef) throws IOException, GaExecuteException {

        final Thread currentThread = Thread.currentThread();
        final BlockingQueue<String> writeQueue = session.getWriteQueue();
        try {

            while (!session.isDestroy()
                    && !currentThread.isInterrupted()
                    && session.isLocked()) {

                // touch the session
                session.touch();

                try {
                    final String segment = writeQueue.poll(200, TimeUnit.MILLISECONDS);

                    // 如果返回的片段为null,说明当前没有东西可写
                    if (null == segment) {

                        // 当读到EOF的时候，同时Sender标记为isFinished
                        // 说明整个命令结束了，标记整个会话为不可写，结束命令循环
                        if (isFinishRef.get()) {
                            session.unLock();
                            break;
                        }

                    }

                    // 读出了点东西
                    else {
                        write(session.getSocketChannel(), segment, session.getCharset());
                    }

                } catch (InterruptedException e) {
                    currentThread.interrupt();
                }

            }//while command running

        }

        // 遇到关闭的链接可以忽略
        catch (ClosedChannelException e) {
            logger.debug("session[{}] write failed, because socket broken.",
                    session.getSessionId(), e);
        }

    }

    /*
     * 绘制提示符
     */
    private void reDrawPrompt(Session session, SocketChannel socketChannel, Charset charset, String prompt) throws IOException {
        if (!session.isSilent()) {
            write(socketChannel, prompt, charset);
        }
    }

    /*
     * 输出到网络
     */
    private void write(SocketChannel socketChannel, String message, Charset charset) throws IOException {
        write(socketChannel, charset.encode(message));
    }

    private void write(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()
                && socketChannel.isConnected()) {
            if (-1 == socketChannel.write(buffer)) {
                // socket broken
                throw new IOException("write EOF");
            }
        }
    }

}
