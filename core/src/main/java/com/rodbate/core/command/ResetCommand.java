package com.rodbate.core.command;


import com.rodbate.core.advisor.Enhancer;
import com.rodbate.core.command.annotation.Cmd;
import com.rodbate.core.server.Session;
import com.rodbate.core.util.affect.EnhancerAffect;
import com.rodbate.core.util.affect.RowAffect;

import java.lang.instrument.Instrumentation;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * 恢复所有增强类<br/>
 * Created by oldmanpushcart@gmail.com on 15/5/29.
 */
@Cmd(isHacking = true, name = "reset", sort = 11, summary = "Reset all the enhanced classes",
        eg = {
                "reset",
                "reset *List",
                "reset -E .*List"
        })
public class ResetCommand implements Command {

    @Override
    public Action getAction() {

        return new RowAction() {

            @Override
            public RowAffect action(
                    Session session,
                    Instrumentation inst,
                    Printer printer) throws Throwable {

                final EnhancerAffect enhancerAffect = Enhancer.reset(inst);
                printer.print(EMPTY).finish();
                return new RowAffect(enhancerAffect.cCnt());
            }


        };
    }

}
