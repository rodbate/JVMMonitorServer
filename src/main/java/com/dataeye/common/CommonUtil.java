package com.dataeye.common;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {


    //获取上级目录(绝对路径)
    public static String getLastDirectory(String path){
        Path current = Paths.get(path);
        return current.getParent().toAbsolutePath().toString();
    }

    //根据pid获取用户
    public static String getUserByPid(String pid){

        String cmd = "ps -ef|grep "+ pid +"|grep -v grep|awk '{print $1}'";
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
            process.waitFor();
            InputStream in = process.getInputStream();
            byte[] b = new byte[in.available()];
            in.read(b);
            in.close();
            process.destroy();
            return new String(b);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    //将port写入 data/port中
    public synchronized static void writePortToFile(String port){
        String dir = Constant.CONF_DIR + File.separator + "data";
        File portFile = new File(dir + File.separator + "port");
        File file = new File(dir);

        if (!file.exists()){
            boolean flag = file.mkdirs();
            if (flag) {
                if (!portFile.exists()){
                    try {
                        portFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(portFile, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write(port);
            bw.newLine();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //将指定的port从 data/port 文件中移除
    public static synchronized void removePortFromFile(String port){
        String dir = Constant.CONF_DIR + File.separator + "data";
        File portFile = new File(dir + File.separator + "port");

        if (portFile.exists()) {
            List<String> ports = new ArrayList<>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(portFile)));
                String line = "";
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
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
