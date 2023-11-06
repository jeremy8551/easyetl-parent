package icu.etl.script.command;

import java.io.IOException;
import javax.script.ScriptEngineManager;

import icu.etl.printer.Printer;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptEngine;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.IO;
import org.junit.Before;
import org.junit.Test;

public class AnalysisTest {

    @Before
    public void setUp() throws Exception {
    }

//	@Test
//	public void testReadSetScript() throws IOException {
//		BufferedLineReader reader = new BufferedLineReader();
//		StandardScriptLexicalReader a = new StandardScriptLexicalReader(reader);
//		
//		reader.open("set name='etl';  set value='good';\n");
//		String s0 = a.readSetScript();
//		assertTrue("set name='etl';".equals(s0));
//		String s1 = a.readSetScript();
//		assertTrue("set value='good';".equals(s1));
//		
//		reader.open("set name='etl'");
//		String s2 = a.readSetScript();
//		assertTrue("set name='etl'".equals(s2));
//	}

    @Test
    public void testReadExportScript() {
//		List<String> list = new ArrayList<String>(1000);
//		for (int i = 1; i <= 1000; i++) {
//			list.add(String.valueOf(i));
//		}
//		
//		list.forEach(str -> {
//			int index = 0;
//			System.out.println(index++);
//		});
    }

    @Test
    public void testReadCommit() throws IOException {
//		SQLScriptEngine engine = new SQLScriptEngineFactory().getScriptEngine();
//		SQLScriptContext context = new SQLScriptContext(engine);
//		DefaultAnalysisReader reader = new DefaultAnalysisReader();
//		reader.open(context, null);
//		ExportCommand cmd = (ExportCommand) reader.read();
//		assertTrue(cmd.name.equals("name") && cmd.value.equals("'path'") && cmd.isJdbc == false);
//		
//		reader.open(context, null);
//		cmd = (ExportCommand) reader.read();
//		assertTrue(cmd.name.equals("name") && cmd.value.equals("'path'") && cmd.isJdbc == true);
    }

//	@Test
//	public void testReadRollback() {
//		assertTrue(!new Analysis().startsWith("abc", "A"));
//		assertTrue(new Analysis().startsWith("a bc", "A"));
//		assertTrue(new Analysis().startsWith("abc bc", "abc"));
//		assertTrue(!new Analysis().startsWith("abc bc", "abcd"));
//		assertTrue(new Analysis().startsWith("abc", "abc"));
//	}

    @Test
    public void testReadBreak() {
        ScriptEngineManager sm = new ScriptEngineManager();
        UniversalScriptEngine se = (UniversalScriptEngine) sm.getEngineByExtension("etl");
        UniversalScriptContext context = se.getContext();
        UniversalScriptStdout p = context.getStdout();
        try {
            new CallProcudureCommandCompiler().usage(context, p);
            new EchoCommandCompiler().usage(context, p);
            new SetCommandCompiler().usage(context, p);
            new ExportCommandCompiler().usage(context, p);
            new ExecuteFileCommandCompiler().usage(context, p);
            new CommitCommandCompiler().usage(context, p);
            new RollbackCommandCompiler().usage(context, p);
            new SSH2CommandCompiler().usage(context, p);
            new JavaCommandCompiler().usage(context, p);
            new StepCommandCompiler().usage(context, p);
            new JumpCommandCompiler().usage(context, p);
            new SQLCommandCompiler().usage(context, p);
            new QuietCommandCompiler().usage(context, p);
            new WaitCommandCompiler().usage(context, p);
            new FunctionCommandCompiler().usage(context, p);
            new DeclareHandlerCommandCompiler().usage(context, p);
            new UndeclareHandlerCommandCompiler().usage(context, p);
            new DeclareCursorCommandCompiler().usage(context, p);
            new DeclareStatementCommandCompiler().usage(context, p);
            new ExecuteFunctionCommandCompiler().usage(context, p);
            new TerminateCommandCompiler().usage(context, p);
            new IfCommandCompiler().usage(context, p);
            new WhileCommandCompiler().usage(context, p);
        } finally {
            IO.close(se);
        }
    }

    @Test
    public void testReadReturn() {

    }

    @Test
    public void testReadExit() {

    }

    @Test
    public void testReadContinue() {

    }

    @Test
    public void testReadSQL() {

    }

    @Test
    public void testIndexSqlMatchMultiMemoEnd() {

    }

    @Test
    public void testIndexSqlMatchQuatoEnd() {

    }

    @Test
    public void testReadFunctionScript() {

    }

    @Test
    public void testReadDeclareScript() {

    }

    @Test
    public void testReadScriptWords() {

    }

    @Test
    public void testReadDeclareHandlerScript() {

    }

    @Test
    public void testReadDeclareCursorScript() {

    }

    @Test
    public void testReadDeclareStatementScript() {

    }

    @Test
    public void testReadCursorLoopScript() {

    }

//	@Test
//	public void testReadWhileScript() throws Exception {
//		char c = ';';
//		StandardScriptSyntacticParser r = new StandardScriptSyntacticParser();
//		BufferedLineReader reader = new BufferedLineReader();
//		StandardScriptLexicalReader a = new StandardScriptLexicalReader(reader, c);
//		
//		reader.open("while 1 == 1 loop set name='lzj'; set value='good'; end loop commit; rollback; ");
//		String s0 = a.readWhileLoopScript();
////		System.out.println(s0);
//		assertTrue(s0.endsWith("end loop") && s0.startsWith("while"));
//		
////		
//		reader.open("while 1 == 1 loop set name='lzj'; set value='good'; end loop\n commit; rollback; ");
//		String s1 = a.readWhileLoopScript();
////		System.out.println(s1);
//		assertTrue("while 1 == 1 loop set name='lzj'; set value='good'; end loop".equals(s1));
//		
//		reader.open("while 1 == 1 loop while 2==2 loop set name='lzj'; set value='good'; end loop end loop\n commit; rollback; ");
////		String s2 = a.readWhileScript(r, reader, reader.readLine());
//		WhileCommand cmd = (WhileCommand) r.read(new StandardScriptLexicalReader(reader, c));
//		List<UniversalScriptCommand> cmds = cmd.block.values();
//		assertTrue(cmds.size() == 1 && cmds.get(0).getClass().getName().equals(WhileCommand.class.getName()));
//		
//		WhileCommand cw = (WhileCommand) cmds.get(0);
//		List<UniversalScriptCommand> cmds1 = cw.block.values();
//		assertTrue(cmds1.size() == 2);
//		for (UniversalScriptCommand cc : cmds1) {
//			assertTrue(cc.getClass().getName().equals(SetCommand.class.getName()));
//		}
//		
//		reader.open("while 1 == 1 loop while 2==2 loop set name='lzj'; set value='good'; end loop; end loop;; commit; rollback; ");
////		String s2 = a.readWhileScript(r, reader, reader.readLine());
//		cmd = (WhileCommand) r.read(new StandardScriptLexicalReader(reader, c));
//		cmds = cmd.block.values();
//		assertTrue(cmds.size() == 1 && cmds.get(0).getClass().getName().equals(WhileCommand.class.getName()));
//		
//		cw = (WhileCommand) cmds.get(0);
//		cmds1 = cw.block.values();
//		assertTrue(cmds1.size() == 2);
//		for (UniversalScriptCommand cc : cmds1) {
//			assertTrue(cc.getClass().getName().equals(SetCommand.class.getName()));
//		}
//		
////		System.out.println(cmd.getCommand());
////		assertTrue("while 1 == 1 loop while 2==2 loop set name='lzj'; set value='good'; end loop end loop".equals(s2));
//	}

    @Test
    public void testReadIfScript() {

    }

    public void echoUsage(Printer out) {

    }
}
