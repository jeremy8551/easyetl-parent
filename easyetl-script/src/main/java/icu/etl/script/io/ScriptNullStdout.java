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

    public void print(char c) {

    }

    public void print(int i) {

    }

    public void print(float f) {

    }

    public void print(double d) {

    }

    public void print(boolean b) {

    }

    public void print(long d) {

    }

    public void print(char[] ca) {

    }

    public void print(Object obj) {

    }

    public void println() {

    }

    public void println(CharSequence msg) {

    }

    public void println(CharSequence msg, Throwable e) {

    }

    public void println(char c) {

    }

    public void println(int i) {

    }

    public void println(float f) {

    }

    public void println(double d) {

    }

    public void println(boolean b) {

    }

    public void println(long d) {

    }

    public void println(char[] ca) {

    }

    public void println(Object obj) {

    }

    public void close() {
        if (this.proxy != null) {
            this.proxy.close();
        }
    }

}
