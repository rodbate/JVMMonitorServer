package com.dataeye.server.cmd;

import com.dataeye.server.common.Client;
import com.dataeye.server.common.Constant;
import com.dataeye.server.common.JpsInfo;
import com.dataeye.server.common.Server;
import com.dataeye.server.help.ProcessUtil;
import com.dataeye.server.utils.StringUtils;
import com.google.gson.Gson;
import com.xunlei.netty.httpserver.cmd.BaseCmd;
import com.xunlei.netty.httpserver.cmd.CmdMapper;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        rsp.setHeader("Access-Control-Allow-Origin", Constant.ACROSS_DOMAIN);
        rsp.setHeader("Access-Control-Allow-Methods", "POST, GET");
        rsp.setHeader("Access-Control-Max-Age", "3600");
        rsp.setHeader("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        String pid = req.getParameter("pid");
        String cmd = req.getParameter("cmd");
        if (StringUtils.isEmpty(pid)) {
            return "pid is empty";
        }

        if (StringUtils.isEmpty(cmd)) {
            return "cmd is empty";
        }

        Server server = Server.launchServer(Integer.valueOf(pid));
        System.out.println(server.getPort());
        Client client = new Client(server);
        String result = client.sendCmd(cmd);
        return result;
    }

    @CmdMapper("/jvm/cmds")
    public Object jvmCmds(XLHttpRequest req, XLHttpResponse rsp) {
        rsp.setHeader("Access-Control-Allow-Origin", Constant.ACROSS_DOMAIN);
        rsp.setHeader("Access-Control-Allow-Methods", "POST, GET");
        rsp.setHeader("Access-Control-Max-Age", "3600");
        rsp.setHeader("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        String cmd = req.getParameter("cmd");
        if (StringUtils.isEmpty(cmd)) {
            cmd = "jps -ml";
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
            process.destroy();
            return gson.toJson(processList);
        } catch (IOException e) {
            return "cmd not exits";
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = Server.launchServer(1231);
        System.out.println(server.getPort());


        Client client = new Client(server);
        String result = client.sendCmd("help");
        System.out.println(result);
    }

}
