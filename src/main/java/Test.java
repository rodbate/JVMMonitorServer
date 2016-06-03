import com.dataeye.ResourceLoad;
import com.dataeye.common.ServerMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test{





    public static void main(String[] args) {

        //System.out.println(System.getProperty("user.dir"));

        //System.out.println(ResourceLoad.getValue("jvmserver.properties", "he"));

        ServerMgr mgr = ServerMgr.getInstance();

        mgr.getPortAvailable();
        int port = mgr.getPort();
        System.out.println(port);
        mgr.getPortAvailable().remove(port);

        System.out.println(mgr.getPort());

    }
}
