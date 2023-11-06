package icu.etl.database.internal;

import icu.etl.os.OSAccount;

public class StandardOSAccount implements OSAccount {

    private String username;
    private String password;
    private boolean admin;

    public StandardOSAccount() {
        super();
    }

    public StandardOSAccount(String username, String password, boolean admin) {
        this();
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public OSAccount clone() {
        StandardOSAccount obj = new StandardOSAccount();
        obj.username = this.username;
        obj.password = this.password;
        obj.admin = this.admin;
        return obj;
    }

    public String toString() {
        return this.username;
    }

}
