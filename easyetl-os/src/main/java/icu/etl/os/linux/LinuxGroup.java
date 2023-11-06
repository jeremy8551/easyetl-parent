package icu.etl.os.linux;

import java.util.ArrayList;
import java.util.List;

import icu.etl.os.OSUserGroup;
import icu.etl.util.StringUtils;

/**
 * Linux 上用户组的接口实现类
 */
public class LinuxGroup implements OSUserGroup {

    private String name;
    private String password;
    private String gid;
    private List<String> users;

    public LinuxGroup() {
        super();
        this.users = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public List<String> getUsers() {
        return users;
    }

    public void clearUser() {
        this.users.clear();
    }

    public void addUser(String username) {
        this.users.add(username);
    }

    public String toString() {
        return "LinuxGroup [name=" + name + ", password=" + password + ", gid=" + gid + ", users=" + StringUtils.toString(users) + "]";
    }

}
