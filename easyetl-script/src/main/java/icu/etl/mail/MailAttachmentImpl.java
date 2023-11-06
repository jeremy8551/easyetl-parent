package icu.etl.mail;

/**
 * 邮件附件的接口实现类
 */
public class MailAttachmentImpl implements MailAttachment {

    private Mail mail;
    private String description;
    private String name;
//	private String path;
//	private URL url;
//	private boolean isDir;

    public Mail getMail() {
        return this.mail;
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

//	public String getPath() {
//		return this.path;
//	}
//	
//	public URL getURL() {
//		return this.url;
//	}
//	
//	public boolean isDirectory() {
//		return this.isDir;
//	}

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

//	public void setPath(String path) {
//		this.path = path;
//	}
//	
//	public void setUrl(URL url) {
//		this.url = url;
//	}
//	
//	public void setDir(boolean isDir) {
//		this.isDir = isDir;
//	}

    public String toString() {
        return "MailAttachmentImpl [description=" + description + ", name=" + name + "]";
    }

}
