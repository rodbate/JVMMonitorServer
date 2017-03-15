package com.rodbate.core.command;



import com.rodbate.core.command.annotation.Cmd;
import com.rodbate.core.server.Session;
import com.rodbate.core.util.affect.RowAffect;
import com.rodbate.core.util.GaStringUtils;

import java.lang.instrument.Instrumentation;


/**
 * 输出版本
 *
 * @author oldmanpushcart@gmail.com
 */
@Cmd(isHacking = true, name = "version", sort = 9, summary = "Display Greys version",
        eg = {
                "version"
        })
public class VersionCommand implements Command {

    @Override
    public Action getAction() {
        return new RowAction() {

            @Override
            public RowAffect action(Session session, Instrumentation inst, Printer printer) throws Throwable {
                printer.print(GaStringUtils.getLogo()).finish();
                return new RowAffect(1);
            }

        };
    }

}
