package icu.etl.expression;

import icu.etl.expression.command.CommandOptionValue;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CommandOptionValueTest {

    @Test
    public void test() {
        assertTrue(new CommandOptionValue("name", "test", true).toString().equals("--name test"));
        assertTrue(new CommandOptionValue("name", "1 2", true).toString().equals("--name \"1 2\""));
    }
}
