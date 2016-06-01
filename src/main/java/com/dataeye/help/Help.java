package com.dataeye.help;


import com.xunlei.netty.httpserver.cmd.BaseCmd;
import com.xunlei.netty.httpserver.cmd.CmdMapper;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Controller
public class Help extends BaseCmd{

    @CmdMapper("/JVM/help")
    public Object help(XLHttpRequest request, XLHttpResponse response) throws IOException {
        String pid = request.getParameter("pid");

        String command = "D:\\Java\\jdk1.7.0_79\\bin\\java -Xbootclasspath/a:D:\\Java\\jdk1.7.0_79\\lib\\tools.jar -jar  " +
                "E:\\MyProjects\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar -target 127.0.0.1:3658 " +
                "-core E:\\MyProjects\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar -agent " +
                "E:\\MyProjects\\JVMMonitorServer\\agent\\build\\libs\\agent-1.0.jar -pid " + pid;

        ProcessUtil.process(command);

        Socket socket = SimpleClient.connect(3658);
        InputStream in = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        os.write("help \r\n".getBytes());
        os.close();
        byte[] b = new byte[1024];
        int len = 0;
        StringBuilder sb = new StringBuilder();
        while ((len = in.read(b)) > 0) {
            sb.append(new String(b, 0, len));
        }
        in.close();
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String command = "cmd.exe /c D:\\Java\\jdk1.7.0_79\\bin\\java -Xbootclasspath/a:D:\\Java\\jdk1.7.0_79\\lib\\tools.jar -jar  " +
                "E:\\MyProjects\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar -target 127.0.0.1:3658 " +
                "-core E:\\MyProjects\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar -agent " +
                "E:\\MyProjects\\JVMMonitorServer\\agent\\build\\libs\\agent-1.0.jar -pid 3504";

        Process process = ProcessUtil.process(command);
        //process.waitFor();
        Socket socket = SimpleClient.connect(3658);
        InputStream in = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        os.write("help".getBytes());
        os.flush();

        byte[] b = new byte[1024];
        int len;
        StringBuilder sb = new StringBuilder();
        while ((len = in.read(b)) > 0) {
            sb.append(new String(b, 0, len));
        }

        os.close();
        in.close();

        socket.close();
        System.out.println(sb.toString());
    }
}
