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

import java.util.logging.Level;
import java.util.logging.Logger;

import icu.apache.ant.unrar.io.Raw;

/**
 * Base class of headers that contain data
 *
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class BlockHeader extends icu.apache.ant.unrar.rarfile.BaseBlock {
    public static final short blockHeaderSize = 4;

    /** JDK日志输出接口 */
    private final static Logger log = Logger.getLogger(BlockHeader.class.getName());

    private int dataSize;
    private int packSize;

    public BlockHeader() {
    }

    public BlockHeader(BlockHeader bh) {
        super(bh);
        this.packSize = bh.getDataSize();
        this.dataSize = packSize;
        this.positionInFile = bh.getPositionInFile();
    }

    public BlockHeader(icu.apache.ant.unrar.rarfile.BaseBlock bb, byte[] blockHeader) {
        super(bb);

        this.packSize = Raw.readIntLittleEndian(blockHeader, 0);
        this.dataSize = this.packSize;
    }

    public int getDataSize() {
        return dataSize;
    }

    public int getPackSize() {
        return packSize;
    }

    public void print() {
        super.print();
        String s = "DataSize: " + getDataSize() + " packSize: " + getPackSize();
        log.log(Level.INFO, s);
    }
}
