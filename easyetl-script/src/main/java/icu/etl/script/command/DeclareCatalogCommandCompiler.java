package icu.etl.script.command;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.database.Jdbc;
import icu.etl.expression.WordIterator;
import icu.etl.os.OSConnectCommand;
import icu.etl.os.OSShellCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "declare", keywords = {"declare", "global", "catalog"})
public class DeclareCatalogCommandCompiler extends AbstractGlobalCommandCompiler {

    public final static String file = "file";

    public final static String REGEX = "^(?i)\\s*(declare)\\s+([global\\s+]*)(\\S+)\\s+catalog\\s+configuration\\s+[use]*\\s+(.*)";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException {
        WordIterator it = analysis.parse(command);
        it.assertNext("declare");
        boolean global = it.isNext("global");
        if (global) {
            it.assertNext("global");
        }
        String name = it.next();
        it.assertNext("catalog");
        it.assertNext("configuration");
        it.assertNext("use");

        Properties config = new Properties();
        String[] keys = {DeclareCatalogCommandCompiler.file, Jdbc.driverClassName, Jdbc.url, OSConnectCommand.username, OSConnectCommand.password, Jdbc.admin, Jdbc.adminPw, OSShellCommand.sshPort};
        String part = it.readOther();
        List<String> list = analysis.split(part);
        for (int i = 0; i < list.size(); i++) {
            String key = list.get(i);

            if (key.equalsIgnoreCase(OSConnectCommand.host)) {
                key = OSConnectCommand.host;
            } else if (key.equalsIgnoreCase(Jdbc.driver)) {
                key = Jdbc.driverClassName;
            } else if (key.equalsIgnoreCase(Jdbc.url)) {
                key = Jdbc.url;
            } else if (key.equalsIgnoreCase(OSConnectCommand.username)) {
                key = OSConnectCommand.username;
            } else if (key.equalsIgnoreCase(OSConnectCommand.password)) {
                key = OSConnectCommand.password;
            } else if (key.equalsIgnoreCase(Jdbc.admin)) {
                key = Jdbc.admin;
            } else if (key.equalsIgnoreCase(Jdbc.adminPw)) {
                key = Jdbc.adminPw;
            } else if (key.equalsIgnoreCase(DeclareCatalogCommandCompiler.file)) {
                key = DeclareCatalogCommandCompiler.file;
            } else if (key.equalsIgnoreCase(OSShellCommand.sshUser)) {
                key = OSShellCommand.sshUser;
            } else if (key.equalsIgnoreCase(OSShellCommand.sshUserPw)) {
                key = OSShellCommand.sshUserPw;
            } else if (key.equalsIgnoreCase(OSShellCommand.sshPort)) {
                key = OSShellCommand.sshPort;
            } else {
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(131, command, key, StringUtils.join(keys, ", ")));
            }

            int next = i + 1;
            if (next < list.size()) {
                String value = list.get(next);
                if (StringUtils.inArrayIgnoreCase(value, keys)) {
                    config.setProperty(key, "");
                } else {
                    config.setProperty(key, analysis.unQuotation(value));
                    i++;
                }
            } else {
                config.setProperty(key, "");
            }
        }

        return new DeclareCatalogCommand(this, command, name, config, global);
    }

}
