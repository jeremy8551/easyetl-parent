package icu.etl.script.io;

import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;

public class ScriptNullStderr extends ScriptNullStdout implements UniversalScriptStderr {

    public ScriptNullStderr(UniversalScriptStdout proxy) {
        super(proxy);
    }

}
