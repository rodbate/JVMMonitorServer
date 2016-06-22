package com.dataeye.server.help;


import com.dataeye.core.GreysLauncher;
import com.dataeye.server.common.CommonUtil;
import com.dataeye.server.common.Constant;
import com.dataeye.server.common.Client;
import com.dataeye.server.common.Server;
import com.xunlei.netty.httpserver.cmd.BaseCmd;
import com.xunlei.netty.httpserver.cmd.CmdMapper;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import static jline.console.KeyMap.CTRL_D;

@Controller
public class Help extends BaseCmd{

    private static final Logger LOGGER = LoggerFactory.getLogger(Help.class);

    @CmdMapper("/jvm/help")
    public Object help(XLHttpRequest request, XLHttpResponse response) throws IOException {
        String pid = request.getParameter("pid");
        String cmd = request.getParameter("cmd");
        System.out.println("======== cmd =======" + cmd);
        System.out.println("========= user =====" + System.getProperty("user.name"));
        Process process = ProcessUtil.process("su hadoop");
        System.out.println("========= user =====" + System.getProperty("user.name"));


        return null;
    }

    @CmdMapper("/jvm/test")
    public Object test(XLHttpRequest request, XLHttpResponse response) throws Exception{
        return Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator + "jvmserver.properties", "corePath");
    }

    @CmdMapper("/jvm/user")
    public Object getUser(XLHttpRequest request, XLHttpResponse response) throws Exception{
        String pid = request.getParameter("pid");
        return CommonUtil.getUserByPid(pid);
    }

    public static void main(String[] args) {
       /* String command = "cmd.exe /c C:\\Program Files\\Java\\jdk1.7.0_80\\bin\\java -Xbootclasspath/a:C:\\Program Files\\Java\\jdk1.7.0_80\\lib\\tools.jar -jar  " +
                "E:\\wkspace\\greys\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar -target 127.0.0.1:3658 " +
                "-core E:\\wkspace\\greys\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar -agent " +
                "E:\\wkspace\\greys\\JVMMonitorServer\\agent\\build\\libs\\agent-1.0.jar -pid 8776";

        Process process = ProcessUtil.process(command);
        */



        String agent = "E:\\wkspace\\greys\\JVMMonitorServer\\agent\\build\\libs\\agent-1.0.jar";
        String ip = "127.0.0.1";
        String[] args1 = new String[4];
        args1[0] = "-p3936" ;
        args1[1] = "-t" + ip + ":8385";
        args1[2] = "-cE:\\wkspace\\greys\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar";
        args1[3] = "-a"+agent;
        try {
            new GreysLauncher(args1);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Socket socket = new Socket();
        OutputStream out = null;
        InputStream in = null;
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", 8385));
            out = socket.getOutputStream();
            out.write("trace com.dataeye.disconf.demo.TestGreys test1\n".getBytes());
            //
            out.flush();

            in = socket.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line = null;
            System.out.println("==================");
            System.out.println(in.read());

            Thread.sleep(1000);


            out.write(CTRL_D);
            out.flush();

            System.out.println(br.readLine());

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }






//        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line;
//        while ((line = br.readLine()) != null) {
//            System.out.println(line);
//        }


        /*Socket socket = SimpleClient.connect(3658);
        InputStream in = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        os.write("help\n".getBytes());
        os.flush();

        char b;
        StringBuilder sb = new StringBuilder();
        while ((b = (char) in.read()) != 0x04) {
            sb.append(b);
        }

        os.close();
        in.close();

        socket.close();
        System.out.println(sb.toString());*/
    }

    @CmdMapper("/jvm/process")
    public Object process(XLHttpRequest request, XLHttpResponse response) throws Exception{
        //String core = (String) Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator + "jvmserver.properties", "corePath");
        //String agent = (String) Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator + "jvmserver.properties", "agentPath");
        String pid = request.getParameter("pid");
        String cmd = request.getParameter("cmd");
        //Process p = ProcessUtil.process("su hadoop");

        //p.waitFor();

        /*String com = Constant.CONF_DIR + File.separator + "start.sh " + pid;

        LOGGER.info("start shell path {}", com);

        String[] cm = new String[]{"/bin/sh", "-c", "su hadoop -s " + com};

        Process process = Runtime.getRuntime().exec(cm);

        process.waitFor();

        Socket socket = SimpleClient.connect(3658);
        InputStream in = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        os.write((cmd + "\n").getBytes());
        os.flush();

        char b;
        StringBuilder sb = new StringBuilder();
        while ((b = (char) in.read()) != 0x04) {
            sb.append(b);
        }

        os.close();
        in.close();

        socket.close();*/

        Server server = Server.launchServer(Integer.valueOf(pid));

        Client c = new Client(server);
        return c.sendCmd(cmd);

    }
}
