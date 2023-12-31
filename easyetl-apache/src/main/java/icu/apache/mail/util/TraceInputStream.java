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
 * @(#)TraceInputStream.java	1.7 05/08/29
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.util;

import java.io.*;

/**
 * This class is a FilterInputStream that writes the bytes being read from the given input stream into the given output stream. This class is typically used to provide a trace of the data that is being retrieved from an input stream.
 *
 * @author John Mani
 */
public class TraceInputStream extends FilterInputStream {
	private boolean trace = false;
	private boolean quote = false;
	private OutputStream traceOut;
	
	/**
	 * Creates an input stream filter built on top of the specified input stream.
	 * 
	 * @param in
	 *            the underlying input stream.
	 * @param out
	 *            the trace stream
	 */
	public TraceInputStream(InputStream in, OutputStream traceOut) {
		super(in);
		this.traceOut = traceOut;
	}
	
	/**
	 * Set trace mode.
	 * 
	 * @param trace
	 *            the trace mode
	 */
	public void setTrace(boolean trace) {
		this.trace = trace;
	}
	
	/**
	 * Set quote mode.
	 * 
	 * @param quote
	 *            the quote mode
	 */
	public void setQuote(boolean quote) {
		this.quote = quote;
	}
	
	/**
	 * Reads the next byte of data from this input stream. Returns <code>-1</code> if no data is available. Writes out the read byte into the trace stream, if trace mode is <code>true</code>
	 */
	public int read() throws IOException {
		int b = in.read();
		if (trace && b != -1) {
			if (quote)
				writeByte(b);
			else
				traceOut.write(b);
		}
		return b;
	}
	
	/**
	 * Reads up to <code>len</code> bytes of data from this input stream into an array of bytes. Returns <code>-1</code> if no more data is available. Writes out the read bytes into the trace stream, if trace mode is <code>true</code>
	 */
	public int read(byte b[], int off, int len) throws IOException {
		int count = in.read(b, off, len);
		if (trace && count != -1) {
			if (quote) {
				for (int i = 0; i < count; i++)
					writeByte(b[off + i]);
			} else
				traceOut.write(b, off, count);
		}
		return count;
	}
	
	/**
	 * Write a byte in a way that every byte value is printable ASCII.
	 */
	private final void writeByte(int b) throws IOException {
		b &= 0xff;
		if (b > 0x7f) {
			traceOut.write('M');
			traceOut.write('-');
			b &= 0x7f;
		}
		if (b == '\r') {
			traceOut.write('\\');
			traceOut.write('r');
		} else if (b == '\n') {
			traceOut.write('\\');
			traceOut.write('n');
			traceOut.write('\n');
		} else if (b == '\t') {
			traceOut.write('\\');
			traceOut.write('t');
		} else if (b < ' ') {
			traceOut.write('^');
			traceOut.write('@' + b);
		} else {
			traceOut.write(b);
		}
	}
}
