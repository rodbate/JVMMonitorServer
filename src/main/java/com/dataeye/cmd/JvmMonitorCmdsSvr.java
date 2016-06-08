package com.dataeye.cmd;

import com.dataeye.common.Client;
import com.dataeye.common.Constant;
import com.dataeye.common.JpsInfo;
import com.dataeye.common.Server;
import com.dataeye.core.GreysLauncher;
import com.dataeye.help.ProcessUtil;
import com.dataeye.utils.StringUtils;
import com.google.gson.Gson;
import com.xunlei.netty.httpserver.cmd.BaseCmd;
import com.xunlei.netty.httpserver.cmd.CmdMapper;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wendy on 2016/5/30.
 */
@Service
public class JvmMonitorCmdsSvr extends BaseCmd {

    private static final Gson gson = new Gson();

    @CmdMapper("/greys/cmds")
    public Object greysCmds(XLHttpRequest req, XLHttpResponse rsp) {
        rsp.setHeader("Access-Control-Allow-Origin", Constant.ACCROSS_DOMAIN);

        rsp.setHeader("Access-Control-Allow-Methods", "POST, GET");
        rsp.setHeader("Access-Control-Max-Age", "3600");
        rsp.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        String pid = req.getParameter("pid");
        String cmd = req.getParameter("cmd");
        if (StringUtils.isEmpty(pid)) {
            return "pid is empty";
        }

        if (StringUtils.isEmpty(cmd)) {
            return "cmd is empty";
        }

        Server server = new Server(Integer.parseInt(pid));
        System.out.println(server.getPort());
        try {
            server.start();
        } catch (Exception e) {
            return "server start error";
        }

        Client client = new Client(server);
        String result = null;
        try {
            result = client.sendCmd(cmd);
        } catch (Exception e) {
            return "server can not connect";
        }
        return result;
    }

    @CmdMapper("/jvm/cmds")
    public Object jvmCmds(XLHttpRequest req, XLHttpResponse rsp) {
        rsp.setHeader("Access-Control-Allow-Origin", Constant.ACCROSS_DOMAIN);

        rsp.setHeader("Access-Control-Allow-Methods", "POST, GET");
        rsp.setHeader("Access-Control-Max-Age", "3600");
        rsp.setHeader("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        String cmd = req.getParameter("cmd");
        if (StringUtils.isEmpty(cmd)) {
            cmd = "jps -mvl";
        }
        try {
            Process process = ProcessUtil.process(cmd);
            InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            List<JpsInfo> processList = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (!line.contains("jps")) {
                    JpsInfo pro = new JpsInfo();
                    int pid = Integer.parseInt(line.substring(0, line.indexOf(" ")));
                    pro.setPid(pid);
                    pro.setDetail(line.substring(line.indexOf(" ")));
                    processList.add(pro);
                }
            }
            return gson.toJson(processList);
        } catch (IOException e) {
            return "cmd not exits";
        }
    }

    public static void main(String[] args) throws Exception {
//        Server server = new Server(13216);
//        System.out.println(server.getPort());
//        server.start();
//
//
//        Client client = new Client(server);
//        String result = client.sendCmd("help");
//        System.out.println(result);

        String[] args1 = new String[4];

        args1[0] = "-p" + 2388;
        args1[1] = "-t127.0.0.1:" + 47788;
        args1[2] = "-cE:\\wkspace\\greys\\JVMMonitorServer\\core\\build\\libs\\core-1.0.jar";
        args1[3] = "-aE:\\wkspace\\greys\\JVMMonitorServer\\agent\\build\\libs\\agent-1.0.jar";
        
        new GreysLauncher(args1);

       // System.out.println(System.getProperty("user.dir") + File.separator + "jvmserver.properties");
    }

}
