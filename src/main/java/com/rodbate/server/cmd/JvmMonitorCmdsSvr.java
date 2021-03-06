package com.rodbate.server.cmd;

import com.rodbate.httpserver.annotations.RequestMapping;
import com.rodbate.httpserver.http.RBHttpRequest;
import com.rodbate.httpserver.http.RBHttpResponse;
import com.rodbate.server.common.Client;
import com.rodbate.server.common.JpsInfo;
import com.rodbate.server.common.JvmException;
import com.rodbate.server.common.Server;
import com.rodbate.server.help.ProcessUtil;
import com.rodbate.server.utils.StringUtils;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;





public class JvmMonitorCmdsSvr {

    private static final Gson gson = new Gson();

    @RequestMapping("/greys/cmds")
    public Object greysCmds(RBHttpRequest req, RBHttpResponse rsp) {

        String pid = String.valueOf(req.getParameter("pid"));
        String cmd = String.valueOf(req.getParameter("cmd"));
        if (StringUtils.isEmpty(pid)) {
            return "pid is empty";
        }

        if (StringUtils.isEmpty(cmd)) {
            return "cmd is empty";
        }

        String result;

        try {
            Server server = Server.launchServer(Integer.valueOf(pid));
            System.out.println(server.getPort());
            Client client = new Client(server);
            result = client.sendCmd(cmd);
        } catch (JvmException e) {
            result = e.getMessage();
        }
        return result;
    }

    @RequestMapping("/jvm/cmds")
    public Object jvmCmds(RBHttpRequest req, RBHttpRequest rsp) {

        String cmd = "jps -mvl";
        try {
            Process process = ProcessUtil.process(cmd);
            InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            List<JpsInfo> processList = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (!line.contains("jps")&&!line.contains("JVMMonitorServer")) {
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
        Server server = Server.launchServer(8776);
        System.out.println(server.getPort());

        Client client = new Client(server);
        String result = client.sendCmd("help");
        System.out.println(result);
    }

}
