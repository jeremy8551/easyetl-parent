/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * @(#)IMAPInputStream.java	1.10 06/03/24
 *
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.imap;

import java.io.IOException;
import java.io.InputStream;

import icu.apache.mail.Flags;
import icu.apache.mail.Folder;
import icu.apache.mail.FolderClosedException;
import icu.apache.mail.MessagingException;
import icu.apache.mail.iap.ByteArray;
import icu.apache.mail.iap.ProtocolException;
import icu.apache.mail.imap.protocol.BODY;
import icu.apache.mail.imap.protocol.IMAPProtocol;

/**
 * This class implements an IMAP data stream.
 *
 * @author John Mani
 */
public class IMAPInputStream extends InputStream {
	private IMAPMessage msg; // this message
	private String section; // section-id
	private int pos; // track the position within the IMAP datastream
	private int blksize; // number of bytes to read in each FETCH request
	private int max; // the total number of bytes in this section.
	// -1 indicates unknown
	private byte[] buf; // the buffer obtained from fetchBODY()
	private int bufcount; // The index one greater than the index of the
	// last valid byte in 'buf'
	private int bufpos; // The current position within 'buf'
	private boolean peek; // peek instead of fetch?
	
	/**
	 * Create an IMAPInputStream.
	 */
	public IMAPInputStream(IMAPMessage msg, String section, int max, boolean peek) {
		this.msg = msg;
		this.section = section;
		this.max = max;
		this.peek = peek;
		pos = 0;
		blksize = msg.getFetchBlockSize();
	}
	
	/**
	 * Fetch more data from the server. This method assumes that all data has already been read in, hence bufpos > bufcount.
	 */
	private void fill() throws IOException {
		/*
		 * If we know the total number of bytes available from this section, let's check if we have consumed that many bytes.
		 */
		if (max != -1 && pos >= max) {
			if (pos == 0)
				checkSeen();
			return; // the caller of fill() will return -1.
		}
		
		BODY b = null;
		
		// Acquire MessageCacheLock, to freeze seqnum.
		synchronized (msg.getMessageCacheLock()) {
			
			// Check whether this message is expunged
			if (msg.isExpunged())
				throw new IOException("No content for expunged message");
			
			int seqnum = msg.getSequenceNumber();
			int cnt = blksize;
			if (max != -1 && pos + blksize > max)
				cnt = max - pos;
			try {
				IMAPProtocol p = msg.getProtocol();
				if (peek)
					b = p.peekBody(seqnum, section, pos, cnt);
				else
					b = p.fetchBody(seqnum, section, pos, cnt);
			} catch (ProtocolException pex) {
				throw new IOException(pex.getMessage());
			} catch (FolderClosedException fex) {
				throw new IOException(fex.getMessage());
			}
		}
		
		ByteArray ba;
		if (b == null || ((ba = b.getByteArray()) == null))
			throw new IOException("No content");
		
		// make sure the SEEN flag is set after reading the first chunk
		if (pos == 0)
			checkSeen();
		
		// setup new values ..
		buf = ba.getBytes();
		bufpos = ba.getStart();
		int n = ba.getCount(); // will be zero, if all data has been
		// consumed from the server.
		bufcount = bufpos + n;
		pos += n;
	}
	
	/**
	 * Reads the next byte of data from this buffered input stream. If no byte is available, the value <code>-1</code> is returned.
	 */
	public synchronized int read() throws IOException {
		if (bufpos >= bufcount) {
			fill();
			if (bufpos >= bufcount)
				return -1; // EOF
		}
		return buf[bufpos++] & 0xff;
	}
	
	/**
	 * Reads up to <code>len</code> bytes of data from this input stream into the given buffer.
	 * <p>
	 *
	 * Returns the total number of bytes read into the buffer, or <code>-1</code> if there is no more data.
	 * <p>
	 *
	 * Note that this method mimics the "weird !" semantics of BufferedInputStream in that the number of bytes actually returned may be less that the requested value. So callers of this routine should be aware of this and must check the return value to insure that they have obtained the requisite number of bytes.
	 */
	public synchronized int read(byte b[], int off, int len) throws IOException {
		
		int avail = bufcount - bufpos;
		if (avail <= 0) {
			fill();
			avail = bufcount - bufpos;
			if (avail <= 0)
				return -1; // EOF
		}
		int cnt = (avail < len) ? avail : len;
		System.arraycopy(buf, bufpos, b, off, cnt);
		bufpos += cnt;
		return cnt;
	}
	
	/**
	 * Reads up to <code>b.length</code> bytes of data from this input stream into an array of bytes.
	 * <p>
	 *
	 * Returns the total number of bytes read into the buffer, or <code>-1</code> is there is no more data.
	 * <p>
	 *
	 * Note that this method mimics the "weird !" semantics of BufferedInputStream in that the number of bytes actually returned may be less that the requested value. So callers of this routine should be aware of this and must check the return value to insure that they have obtained the requisite number of bytes.
	 */
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}
	
	/**
	 * Returns the number of bytes that can be read from this input stream without blocking.
	 */
	public synchronized int available() throws IOException {
		return (bufcount - bufpos);
	}
	
	/**
	 * Normally the SEEN flag will have been set by now, but if not, force it to be set (as long as the folder isn't open read-only and we're not peeking). And of course, if there's no folder (e.g., a nested message) don't do anything.
	 */
	private void checkSeen() {
		if (peek) // if we're peeking, don't set the SEEN flag
			return;
		try {
			Folder f = msg.getFolder();
			if (f != null && f.getMode() != Folder.READ_ONLY && !msg.isSet(Flags.Flag.SEEN))
				msg.setFlag(Flags.Flag.SEEN, true);
		} catch (MessagingException ex) {
			// ignore it
		}
	}
}
