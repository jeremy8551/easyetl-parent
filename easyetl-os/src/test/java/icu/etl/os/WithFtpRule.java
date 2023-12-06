package icu.etl.os;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class WithFtpRule extends WithSSHRule {

    public WithFtpRule() {
    }

    public Statement apply(Statement statement, Description description) {
        init();
        return new WithFtpStatement(this, statement);
    }

    @Override
    protected void init() {
        super.init();

        try {
            String sshhost = (String) environment.get("ftp.host");
            this.notFindServer = !Ping.ping(sshhost);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class WithFtpStatement extends Statement {
        private Statement statment;
        private WithFtpRule rule;

        public WithFtpStatement(WithFtpRule rule, Statement statment) {
            this.statment = statment;
        }

        @Override
        public void evaluate() throws Throwable {
            if (rule.notFindServer) {
                System.out.println("**************** 未找到可用的数据库 ****************");
                return;
            }

            try {
                System.out.println("================ 方法开始运行 ====================");
                System.out.println();
                this.statment.evaluate();
            } finally {
                System.out.println();
                System.out.println("================ 方法运行结束 ====================");
            }
        }
    }

}
