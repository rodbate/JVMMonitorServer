package com.rodbate.server.common;


import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonUtil {

    private static final Logger _LOGGER_FILE = LogUtil.getLogger("jvmserver");

    //获取上级目录(绝对路径)
    public static String getLastDirectory(String path){
        Path current = Paths.get(path);
        return current.getParent().toAbsolutePath().toString();
    }

    //根据pid获取用户
    public static String getUserByPid(String pid){

        String cmd = "ps -ef|grep -w "+ pid +"|grep -v grep|awk '{print $1}'";
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
            process.waitFor();
            InputStream in = process.getInputStream();
            byte[] b = new byte[in.available()];
            in.read(b);
            in.close();
            process.destroy();
            String user = new String(b);
            return user.replace("\n", "").replace("\r\n", "");
        } catch (IOException e) {
            //e.printStackTrace();
            _LOGGER_FILE.info(ExceptionStackUtil.print(e));
        } catch (InterruptedException e) {
            //e.printStackTrace();
            _LOGGER_FILE.info(ExceptionStackUtil.print(e));
        }
        return "";
    }

    //将port写入 data/port中
    public synchronized static void writePortToFile(String port){
        String dir = Constant.USER_DIR + File.separator + "data";
        File portFile = new File(dir + File.separator + "port");
        File file = new File(dir);

        if (!file.exists()){
            boolean flag = file.mkdirs();
            if (flag) {
                if (!portFile.exists()){
                    try {
                        portFile.createNewFile();
                    } catch (IOException e) {
                        //e.printStackTrace();
                        _LOGGER_FILE.info(ExceptionStackUtil.print(e));
                    }
                }
            }
        } else {
            if (!portFile.exists()){
                try {
                    portFile.createNewFile();
                } catch (IOException e) {
                    //e.printStackTrace();
                    _LOGGER_FILE.info(ExceptionStackUtil.print(e));
                }
            }
        }

        try {
            if (!checkPortExist(portFile, port)) {
                FileOutputStream out = new FileOutputStream(portFile, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
                bw.write(port);
                bw.newLine();
                bw.close();
            }
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            _LOGGER_FILE.info(ExceptionStackUtil.print(e));
        } catch (IOException e) {
            _LOGGER_FILE.info(ExceptionStackUtil.print(e));
        }
    }


    //将指定的port从 data/port 文件中移除
    public static synchronized void removePortFromFile(String port){
        String dir = Constant.USER_DIR + File.separator + "data";
        File portFile = new File(dir + File.separator + "port");

        if (portFile.exists()) {
            List<String> ports = new ArrayList<>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(portFile)));
                String line;
                while ((line = br.readLine()) != null) {
                    if (!port.equals(line.trim())) {
                        ports.add(line.trim());
                    }
                }
                br.close();

                FileOutputStream out = new FileOutputStream(portFile);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
                for (int i = 0; i < ports.size(); i++) {
                    bw.write(ports.get(i));
                    bw.newLine();
                    bw.flush();
                }
                bw.close();
            } catch (FileNotFoundException e) {
                _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            } catch (IOException e) {
                _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            }
        }
    }

    private static boolean checkPortExist(File portFile, String port){
        boolean flag = false;
        Set<String> set = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(portFile)));
            String line;
            while ((line = br.readLine()) != null) {
                if (!port.equals(line.trim())) {
                    set.add(line.trim());
                }
            }
            br.close();

            return set.contains(port);
        } catch (FileNotFoundException e) {
            _LOGGER_FILE.info(ExceptionStackUtil.print(e));
        } catch (IOException e) {
            _LOGGER_FILE.info(ExceptionStackUtil.print(e));
        }

        return flag;
    }


    private static String[] watCmdArr = new String[]{"monitor", "trace", "js", "ptrace", "stack"};
    public static boolean isWaitToStopCommand(String command) {
        for (String s : watCmdArr) {
            if (command.startsWith(s)) {
                return true;
            }
        }
        return false;
    }


    private static String[] returnCmd = new String[]{"sc","sm","jvm","version","quit","session","reset","asm","help","tt","topthread"};
    public static boolean isReturnImmediatelyCmd(String command) {
        for (String s : returnCmd) {
            if (command.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isReturnImmediatelyCmd(("version" + "\n")));

        System.out.println(("version" + "\n").startsWith("version"));
    }
}
