package icu.etl.expression;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 解析登录服务器的表达式, 格式: name user@host:port?password=
 *
 * @author jeremy8551@qq.com
 */
public class LoginExpression extends CommandExpression {

    /** 登录属性 */
    private String host;
    private String port;
    private String username;
    private String password;

    /** 属性集合 */
    private Map<String, String> attributes;

    /**
     * 解析登陆命令表达式
     *
     * @param analysis   语句分析器
     * @param expression 表达式, 格式: {@literal commandName username@host:port?password=password } <br>
     *                   {@literal ssh username@host:port?password=&key=value} <br>
     *                   {@literal ftp username@host:port?password=&key=value} <br>
     *                   {@literal sftp username@host:port?password=&key=value} <br>
     */
    public LoginExpression(Analysis analysis, String expression) {
        super(analysis, "", expression);
    }

    protected void prepared(Analysis analysis) {
        super.prepared(analysis);
        this.attributes = new LinkedHashMap<String, String>();
    }

    protected void parse(String pattern) {
        super.parse(pattern);
    }

    public void setValue(String command) {
        super.setValue(command);

        int index = this.indexOfLogin();
        if (index != -1) {
            String expr = this.parameter.get(index);
            this.parseLogin(expr);
        }
    }

    /**
     * 查询登录参数位置
     *
     * @return
     */
    protected int indexOfLogin() {
        for (int i = 0; i < this.parameter.size(); i++) {
            String str = this.parameter.get(i);
            if (this.analysis.indexOf(str, "@", 0, 2, 2) != -1 && this.analysis.indexOf(str, "?password=", 0, 2, 2) != -1) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 解析登陆命令参数 <br>
     *
     * @param loginExpr 表达式 <br>
     *                  {@literal ssh username@host:port?password=&key=value} <br>
     *                  {@literal ftp username@host:port?password=&key=value} <br>
     *                  {@literal sftp username@host:port?password=&key=value} <br>
     */
    protected void parseLogin(String loginExpr) {
        int mp = loginExpr.indexOf(':');
        int wp = loginExpr.indexOf('?');
        int pp = StringUtils.indexOf(loginExpr, "?password", 0, true);
        int rp = Numbers.max(mp, wp, pp); // 最右侧的位置

        int index = loginExpr.lastIndexOf('@', rp < 0 ? loginExpr.length() - 1 : rp);
        if (index != -1) {
            String hostPort = loginExpr.substring(index + 1); // host:port
            int passwordIndex = hostPort.lastIndexOf('?');
            if (passwordIndex != -1) { // host:port?password=&key=value
                String attributes = hostPort.substring(passwordIndex + 1);
                List<String> propertys = this.analysis.split(attributes, '&');
                for (String string : propertys) {
                    String[] array = StringUtils.splitProperty(string);
                    if (array.length != 2) {
                        throw new ExpressionException(this.command);
                    }

                    String key = array[0];
                    if ("password".equalsIgnoreCase(key)) {
                        this.password = array[1];
                    } else {
                        this.attributes.put(key, array[1]); // 保存所有属性信息
                    }
                }
                hostPort = hostPort.substring(0, passwordIndex);
            }

            int end = hostPort.indexOf(':');
            if (end != -1) {
                this.host = hostPort.substring(0, end);
                this.port = hostPort.substring(end + 1); // port
            } else {
                this.host = hostPort; // host
                String key = this.getName();
                if ("ftp".equalsIgnoreCase(key)) {
                    this.port = "21";
                } else if ("sftp".equalsIgnoreCase(key)) {
                    this.port = "22";
                } else if ("telnet".equalsIgnoreCase(key)) {
                    this.port = "23";
                } else if ("ssh".equalsIgnoreCase(key)) {
                    this.port = "22";
                } else {
                    this.port = "";
                }
            }

            this.username = loginExpr.substring(0, index);
        }

        if (!StringUtils.isInt(this.port)) {
            throw new ExpressionException(ResourcesUtils.getExpressionMessage(54, this.command, this.port));
        }

        if (StringUtils.isBlank(this.host)) {
            throw new ExpressionException(ResourcesUtils.getExpressionMessage(55, this.command));
        }

        if (StringUtils.isBlank(this.username)) {
            throw new ExpressionException(ResourcesUtils.getExpressionMessage(56, this.command));
        }
    }

    /**
     * 返回服务器host
     *
     * @return
     */
    public String getLoginHost() {
        return this.host;
    }

    /**
     * 返回登录服务器的端口
     *
     * @return
     */
    public String getLoginPort() {
        return this.port;
    }

    /**
     * 返回登录用户名
     *
     * @return
     */
    public String getLoginUsername() {
        return this.username;
    }

    /**
     * 返回登录密码
     *
     * @return
     */
    public String getLoginPassword() {
        return this.password;
    }

    /**
     * 返回属性值
     *
     * @param name 属性名
     * @return
     */
    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

}
