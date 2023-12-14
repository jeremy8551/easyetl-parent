package icu.etl.os.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import icu.etl.os.OSFileCommandException;
import icu.etl.util.StringUtils;

/**
 * 信道
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-08-10
 */
public class JschChannel {
    protected Channel sftp;
    protected boolean isTmp;
    private String charset;

    public JschChannel(Channel sftp, boolean isTmp) {
        super();
        this.sftp = sftp;
        this.isTmp = isTmp;
    }

    public ChannelSftp getSftp(String charsetName) {
        ChannelSftp channel = (ChannelSftp) this.sftp;
        if (channel != null && StringUtils.isNotBlank(charsetName) && !charsetName.equals(this.charset)) {
            try {
                channel.setFilenameEncoding(charsetName);
                this.charset = charsetName;
            } catch (SftpException e) {
                throw new OSFileCommandException(charsetName, e);
            }
        }
        return channel;
    }

    public boolean isConnected() {
        return this.sftp != null && this.sftp.isConnected();
    }

    public ChannelExec getExec() {
        return (ChannelExec) sftp;
    }

    public void closeSftp() {
        if (this.sftp != null) {
            this.sftp.disconnect();
            this.sftp = null;
        }
    }

    /**
     * 如果是临时通道，就关闭该通道
     */
    public void closeTempChannel() {
        if (this.isTmp) {
            this.closeSftp();
        }
    }
}
