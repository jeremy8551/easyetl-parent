package icu.etl.os.internal;

import icu.etl.os.OSNetworkCard;

/**
 * 操作系统上网络功能的接口实现类
 */
public class OSNetworkCardImpl implements OSNetworkCard {

    private int type;
    private boolean enabled;
    private String name;
    private String ipAddress;
    private String ip6Address;
    private String ip6Gateway;
    private String mask;
    private String gateway;
    private String macAddress;
    private String dns1;
    private String dns2;
    private boolean isDhcp;
    private boolean isStatic;

    public OSNetworkCardImpl() {
        super();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getName() {
        return this.name;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public String getIP6Address() {
        return this.ip6Address;
    }

    public String getMask() {
        return this.mask;
    }

    public String getGateway() {
        return this.gateway;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public String getDNS1() {
        return this.dns1;
    }

    public String getDNS2() {
        return this.dns2;
    }

    public boolean isDHCP() {
        return this.isDhcp;
    }

    public boolean isStatic() {
        return this.isStatic;
    }

    public String getDns1() {
        return dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public boolean isDhcp() {
        return isDhcp;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setIp6Address(String ip6Address) {
        this.ip6Address = ip6Address;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setDns1(String dns1) {
        this.dns1 = dns1;
    }

    public void setDns2(String dns2) {
        this.dns2 = dns2;
    }

    public void setDhcp(boolean isDhcp) {
        this.isDhcp = isDhcp;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public String getIp6Gateway() {
        return ip6Gateway;
    }

    public void setIp6Gateway(String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    public String toString() {
        return "OSNetworkCardImpl [enabled=" + enabled + ", name=" + name + ", ipAddress=" + ipAddress + ", ip6Address=" + ip6Address + ", ip6Gateway=" + ip6Gateway + ", mask=" + mask + ", gateway=" + gateway + ", macAddress=" + macAddress + ", dns1=" + dns1 + ", dns2=" + dns2 + ", isDhcp=" + isDhcp + ", isStatic=" + isStatic + "]";
    }

}
