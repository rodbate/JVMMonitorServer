package com.dataeye.core.command;



import com.dataeye.core.command.annotation.Cmd;
import com.dataeye.core.server.Session;
import com.dataeye.core.util.affect.RowAffect;

import java.lang.instrument.Instrumentation;

import static com.dataeye.core.util.GaStringUtils.getLogo;


/**
 * 输出版本
 *
 * @author oldmanpushcart@gmail.com
 */
@Cmd(name = "version", sort = 9, summary = "Display Greys version",
        eg = {
                "version"
        })
public class VersionCommand implements Command {

    @Override
    public Action getAction() {
        return new RowAction() {

            @Override
            public RowAffect action(Session session, Instrumentation inst, Printer printer) throws Throwable {
                printer.print(getLogo()).finish();
                return new RowAffect(1);
            }

        };
    }

}
