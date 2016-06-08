package com.dataeye.help;


import com.dataeye.ResourceLoad;
import com.dataeye.common.Client;
import com.dataeye.common.Constant;
import com.dataeye.common.Server;
import com.xunlei.netty.httpserver.cmd.BaseCmd;
import com.xunlei.netty.httpserver.cmd.CmdMapper;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.net.Socket;

@Controller
public class Help extends BaseCmd{

    @CmdMapper("/jvm/help")
    public Object help(XLHttpRequest request, XLHttpResponse response) throws IOException {
        String pid = request.getParameter("pid");
        String cmd = request.getParameter("cmd");
        System.out.println("======== cmd =======" + cmd);
        Server server = new Server(Integer.parseInt(pid));
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();

        }
        Client c = new Client(server);

        String result = null;
        try {
            result = c.sendCmd(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @CmdMapper("/jvm/test")
    public Object test(XLHttpRequest request, XLHttpResponse response) throws Exception{
        return Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator + "jvmserver.properties", "corePath");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String command = "cmd.exe /c D:\\Java\\jdk1.7.0_79\\bin\\java -Xbootclasspath/a:D:\\Java\\jdk1.7.0_79\\lib\\tools.jar -jar  " +
                "E:\\MyProjects\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar -target 127.0.0.1:3658 " +
                "-core E:\\MyProjects\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar -agent " +
                "E:\\MyProjects\\JVMMonitorServer\\agent\\build\\libs\\agent-1.0.jar -pid 6782";

        Process process = ProcessUtil.process("jps -m -l");

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }


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
}
