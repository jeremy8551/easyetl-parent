package icu.etl.script.io;

import java.io.CharArrayReader;

public class ScriptReader extends CharArrayReader {

    public ScriptReader(String str) {
        super(str.toCharArray());
    }

}
