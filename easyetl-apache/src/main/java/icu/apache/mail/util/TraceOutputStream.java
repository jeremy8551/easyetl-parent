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
 * @(#)TraceOutputStream.java	1.5 05/08/29
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.util;

import java.io.*;

/**
 * This class is a subclass of DataOutputStream that copies the data being written into the DataOutputStream into another output stream. This class is used here to provide a debug trace of the stuff thats being written out into the DataOutputStream.
 *
 * @author John Mani
 */
public class TraceOutputStream extends FilterOutputStream {
	private boolean trace = false;
	private boolean quote = false;
	private OutputStream traceOut;
	
	/**
	 * Creates an output stream filter built on top of the specified underlying output stream.
	 *
	 * @param out
	 *            the underlying output stream.
	 * @param traceOut
	 *            the trace stream.
	 */
	public TraceOutputStream(OutputStream out, OutputStream traceOut) {
		super(out);
		this.traceOut = traceOut;
	}
	
	/**
	 * Set the trace mode.
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
	 * Writes the specified <code>byte</code> to this output stream. Writes out the byte into the trace stream if the trace mode is <code>true</code>
	 */
	public void write(int b) throws IOException {
		if (trace) {
			if (quote)
				writeByte(b);
			else
				traceOut.write(b);
		}
		out.write(b);
	}
	
	/**
	 * Writes <code>b.length</code> bytes to this output stream. Writes out the bytes into the trace stream if the trace mode is <code>true</code>
	 */
	public void write(byte b[], int off, int len) throws IOException {
		if (trace) {
			if (quote) {
				for (int i = 0; i < len; i++)
					writeByte(b[off + i]);
			} else
				traceOut.write(b, off, len);
		}
		out.write(b, off, len);
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
