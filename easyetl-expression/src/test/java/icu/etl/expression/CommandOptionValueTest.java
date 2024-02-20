package icu.etl.expression;

import icu.etl.expression.command.CommandOptionValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandOptionValueTest {

    @Test
    public void test() {
        assertEquals("--name test", new CommandOptionValue("name", "test", true).toString());
        assertEquals("--name \"1 2\"", new CommandOptionValue("name", "1 2", true).toString());
    }
}
