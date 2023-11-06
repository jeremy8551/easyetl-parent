package icu.etl.os.linux;

import java.util.ArrayList;
import java.util.List;

import icu.etl.os.OSUser;
import icu.etl.util.StringUtils;

/**
 * Linux 操作系统用户的接口实现类
 */
public class LinuxUser implements OSUser {
    private String name;
    private String password;
    private String id;
    private String group;
    private String memo;
    private String home;
    private String shell;
    private List<String> profiles = new ArrayList<String>();

    public LinuxUser() {
        super();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getShell() {
        return shell;
    }

    public void setShell(String shell) {
        this.shell = shell;
    }

    public boolean equals(Object obj) {
        if (obj instanceof OSUser) {
            OSUser u = (OSUser) obj;
            return u.getName().equals(this.getName());
        } else {
            return false;
        }
    }

    public List<String> getProfiles() {
        return java.util.Collections.unmodifiableList(this.profiles);
    }

    public void setProfiles(List<String> list) {
        this.profiles.clear();
        this.profiles.addAll(list);
    }

    public boolean isRoot() { // uid 等于 0 表示root权限
        return "0".equalsIgnoreCase(this.getId()) || "root".equalsIgnoreCase(this.getName());
    }

    public String toString() {
        return "LinuxUser [name=" + name + ", password=" + password + ", id=" + id + ", group=" + group + ", memo=" + memo + ", home=" + home + ", shell=" + shell + ", profiles=" + StringUtils.toString(profiles) + "]";
    }

}
