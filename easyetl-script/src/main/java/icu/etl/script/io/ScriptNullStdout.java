package icu.etl.script.io;

import java.io.Writer;
import java.text.Format;

import icu.etl.script.UniversalScriptStdout;

public class ScriptNullStdout implements UniversalScriptStdout {

    private UniversalScriptStdout proxy;

    public ScriptNullStdout(UniversalScriptStdout proxy) {
        this.proxy = proxy;
    }

    public Writer getWriter() {
        return this.proxy == null ? null : this.proxy.getWriter();
    }

    public void setWriter(Writer writer) {
        if (this.proxy != null) {
            this.proxy.setWriter(writer);
        }
    }

    public void setFormatter(Format f) {
        if (this.proxy != null) {
            this.proxy.setFormatter(f);
        }
    }

    public Format getFormatter() {
        return this.proxy == null ? null : this.proxy.getFormatter();
    }

    public void println(String id, CharSequence message) {

    }

    public void print(CharSequence msg) {

    }

    public void print(Object obj) {

    }

    public void println() {

    }

    public void println(CharSequence msg) {

    }

    public void println(CharSequence msg, Throwable e) {

    }

    public void println(Object obj) {

    }

    public void close() {
        if (this.proxy != null) {
            this.proxy.close();
        }
    }

}
