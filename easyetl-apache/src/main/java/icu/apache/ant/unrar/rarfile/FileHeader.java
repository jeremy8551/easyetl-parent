/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 22.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 *
 *
 * the unrar licence applies to all junrar source and binary distributions
 * you are not allowed to use this source to re-create the RAR compression algorithm
 *
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;"
 */
package icu.apache.ant.unrar.rarfile;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import icu.apache.ant.unrar.io.Raw;

/**
 * DOCUMENT ME
 *
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class FileHeader extends icu.apache.ant.unrar.rarfile.BlockHeader {

    /** JDK日志输出接口 */
    private final static Logger log = Logger.getLogger(FileHeader.class.getName());

    private static final byte SALT_SIZE = 8;

    private static final byte NEWLHD_SIZE = 32;

    private int unpSize;

    private icu.apache.ant.unrar.rarfile.HostSystem hostOS;

    private int fileCRC;

    private int fileTime;

    private byte unpVersion;

    private byte unpMethod;

    private short nameSize;

    private int highPackSize;

    private int highUnpackSize;

    private byte[] fileNameBytes;

    private String fileName;
    private String fileNameW;

    private byte[] subData;

    private byte[] salt = new byte[SALT_SIZE];

    private Date mTime;

    private Date cTime;

    private Date aTime;

    private Date arcTime;

    private long fullPackSize;

    private long fullUnpackSize;

    private int fileAttr;

    private int subFlags; // same as fileAttr (in header)

    private int recoverySectors = -1;

    public FileHeader(icu.apache.ant.unrar.rarfile.BlockHeader bh, byte[] fileHeader) {
        super(bh);

        int position = 0;
        unpSize = Raw.readIntLittleEndian(fileHeader, position);
        position += 4;
        hostOS = icu.apache.ant.unrar.rarfile.HostSystem.findHostSystem(fileHeader[4]);
        position++;

        fileCRC = Raw.readIntLittleEndian(fileHeader, position);
        position += 4;

        fileTime = Raw.readIntLittleEndian(fileHeader, position);
        position += 4;

        unpVersion |= fileHeader[13] & 0xff;
        position++;
        unpMethod |= fileHeader[14] & 0xff;
        position++;
        nameSize = Raw.readShortLittleEndian(fileHeader, position);
        position += 2;

        fileAttr = Raw.readIntLittleEndian(fileHeader, position);
        position += 4;
        if (isLargeBlock()) {
            highPackSize = Raw.readIntLittleEndian(fileHeader, position);
            position += 4;

            highUnpackSize = Raw.readIntLittleEndian(fileHeader, position);
            position += 4;
        } else {
            highPackSize = 0;
            highUnpackSize = 0;
            if (unpSize == 0xffffffff) {
                unpSize = 0xffffffff;
                highUnpackSize = Integer.MAX_VALUE;
            }
        }
        fullPackSize |= highPackSize;
        fullPackSize <<= 32;
        fullPackSize |= getPackSize();

        fullUnpackSize |= highUnpackSize;
        fullUnpackSize <<= 32;
        fullUnpackSize |= unpSize;

        nameSize = nameSize > 4 * 1024 ? 4 * 1024 : nameSize;

        fileNameBytes = new byte[nameSize];
        for (int i = 0; i < nameSize; i++) {
            fileNameBytes[i] = fileHeader[position];
            position++;
        }

        if (isFileHeader()) {
            if (isUnicode()) {
                int length = 0;
                fileName = "";
                fileNameW = "";
                while (length < fileNameBytes.length && fileNameBytes[length] != 0) {
                    length++;
                }
                byte[] name = new byte[length];
                System.arraycopy(fileNameBytes, 0, name, 0, name.length);
                fileName = new String(name);
                if (length != nameSize) {
                    length++;
                    fileNameW = icu.apache.ant.unrar.rarfile.FileNameDecoder.decode(fileNameBytes, length);
                }
            } else {
                fileName = new String(fileNameBytes);
                fileNameW = "";
            }
        }

        if (icu.apache.ant.unrar.rarfile.UnrarHeadertype.NewSubHeader.equals(headerType)) {
            int datasize = headerSize - NEWLHD_SIZE - nameSize;
            if (hasSalt()) {
                datasize -= SALT_SIZE;
            }
            if (datasize > 0) {
                subData = new byte[datasize];
                for (int i = 0; i < datasize; i++) {
                    subData[i] = (fileHeader[position]);
                    position++;
                }
            }

            if (icu.apache.ant.unrar.rarfile.NewSubHeaderType.SUBHEAD_TYPE_RR.byteEquals(fileNameBytes)) {
                recoverySectors = subData[8] + (subData[9] << 8) + (subData[10] << 16) + (subData[11] << 24);
            }
        }

        if (hasSalt()) {
            for (int i = 0; i < SALT_SIZE; i++) {
                salt[i] = fileHeader[position];
                position++;
            }
        }
        mTime = getDateDos(fileTime);
        // TODO rartime -> extended

    }

    public void print() {
        super.print();
        StringBuilder str = new StringBuilder();
        str.append("unpSize: " + getUnpSize());
        str.append("\nHostOS: " + hostOS.name());
        str.append("\nMDate: " + mTime);
        str.append("\nFileName: " + getFileNameString());
        str.append("\nunpMethod: " + Integer.toHexString(getUnpMethod()));
        str.append("\nunpVersion: " + Integer.toHexString(getUnpVersion()));
        str.append("\nfullpackedsize: " + getFullPackSize());
        str.append("\nfullunpackedsize: " + getFullUnpackSize());
        str.append("\nisEncrypted: " + isEncrypted());
        str.append("\nisfileHeader: " + isFileHeader());
        str.append("\nisSolid: " + isSolid());
        str.append("\nisSplitafter: " + isSplitAfter());
        str.append("\nisSplitBefore:" + isSplitBefore());
        str.append("\nunpSize: " + getUnpSize());
        str.append("\ndataSize: " + getDataSize());
        str.append("\nisUnicode: " + isUnicode());
        str.append("\nhasVolumeNumber: " + hasVolumeNumber());
        str.append("\nhasArchiveDataCRC: " + hasArchiveDataCRC());
        str.append("\nhasSalt: " + hasSalt());
        str.append("\nhasEncryptVersions: " + hasEncryptVersion());
        str.append("\nisSubBlock: " + isSubBlock());
        log.log(Level.INFO, str.toString());
    }

    private Date getDateDos(int time) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, (time >>> 25) + 1980);
        cal.set(Calendar.MONTH, ((time >>> 21) & 0x0f) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (time >>> 16) & 0x1f);
        cal.set(Calendar.HOUR_OF_DAY, (time >>> 11) & 0x1f);
        cal.set(Calendar.MINUTE, (time >>> 5) & 0x3f);
        cal.set(Calendar.SECOND, (time & 0x1f) * 2);
        return cal.getTime();
    }

    public Date getArcTime() {
        return arcTime;
    }

    public void setArcTime(Date arcTime) {
        this.arcTime = arcTime;
    }

    public Date getATime() {
        return aTime;
    }

    public void setATime(Date time) {
        aTime = time;
    }

    public Date getCTime() {
        return cTime;
    }

    public void setCTime(Date time) {
        cTime = time;
    }

    public int getFileAttr() {
        return fileAttr;
    }

    public void setFileAttr(int fileAttr) {
        this.fileAttr = fileAttr;
    }

    public int getFileCRC() {
        return fileCRC;
    }

    public byte[] getFileNameByteArray() {
        return fileNameBytes;
    }

    public String getFileNameString() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameW() {
        return fileNameW;
    }

    public void setFileNameW(String fileNameW) {
        this.fileNameW = fileNameW;
    }

    public int getHighPackSize() {
        return highPackSize;
    }

    public int getHighUnpackSize() {
        return highUnpackSize;
    }

    public icu.apache.ant.unrar.rarfile.HostSystem getHostOS() {
        return hostOS;
    }

    public Date getMTime() {
        return mTime;
    }

    public void setMTime(Date time) {
        mTime = time;
    }

    public short getNameSize() {
        return nameSize;
    }

    public int getRecoverySectors() {
        return recoverySectors;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getSubData() {
        return subData;
    }

    public int getSubFlags() {
        return subFlags;
    }

    public byte getUnpMethod() {
        return unpMethod;
    }

    public int getUnpSize() {
        return unpSize;
    }

    public byte getUnpVersion() {
        return unpVersion;
    }

    public long getFullPackSize() {
        return fullPackSize;
    }

    public long getFullUnpackSize() {
        return fullUnpackSize;
    }

    public String toString() {
        return super.toString();
    }

    /**
     * the file will be continued in the next archive part
     *
     * @return
     */
    public boolean isSplitAfter() {
        return (this.flags & icu.apache.ant.unrar.rarfile.BlockHeader.LHD_SPLIT_AFTER) != 0;
    }

    /**
     * the file is continued in this archive
     *
     * @return
     */
    public boolean isSplitBefore() {
        return (this.flags & LHD_SPLIT_BEFORE) != 0;
    }

    /**
     * this file is compressed as solid (all files handeled as one)
     *
     * @return
     */
    public boolean isSolid() {
        return (this.flags & LHD_SOLID) != 0;
    }

    /**
     * the file is encrypted
     *
     * @return
     */
    public boolean isEncrypted() {
        return (this.flags & icu.apache.ant.unrar.rarfile.BlockHeader.LHD_PASSWORD) != 0;
    }

    /**
     * the filename is also present in unicode
     *
     * @return
     */
    public boolean isUnicode() {
        return (flags & LHD_UNICODE) != 0;
    }

    public boolean isFileHeader() {
        return icu.apache.ant.unrar.rarfile.UnrarHeadertype.FileHeader.equals(headerType);
    }

    public boolean hasSalt() {
        return (flags & LHD_SALT) != 0;
    }

    public boolean isLargeBlock() {
        return (flags & LHD_LARGE) != 0;
    }

    /**
     * whether this fileheader represents a directory
     *
     * @return
     */
    public boolean isDirectory() {
        return (flags & LHD_WINDOWMASK) == LHD_DIRECTORY;
    }
}
