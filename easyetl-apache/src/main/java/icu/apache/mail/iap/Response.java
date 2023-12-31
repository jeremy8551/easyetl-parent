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
 * @(#)Response.java	1.18 05/08/29
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.iap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

import icu.apache.mail.util.ASCIIUtility;

/**
 * This class represents a response obtained from the input stream of an IMAP server.
 *
 * @version 1.1, 97/11/10
 * @author John Mani
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Response {
	protected int index; // internal index (updated during the parse)
	protected int pindex; // index after parse, for reset
	protected int size; // number of valid bytes in our buffer
	protected byte[] buffer = null;
	protected int type = 0;
	protected String tag = null;
	
	protected static final int increment = 100;
	
	// The first and second bits indicate whether this response
	// is a Continuation, Tagged or Untagged
	public final static int TAG_MASK = 0x03;
	public final static int CONTINUATION = 0x01;
	public final static int TAGGED = 0x02;
	public final static int UNTAGGED = 0x03;
	
	// The third, fourth and fifth bits indicate whether this response
	// is an OK, NO, BAD or BYE response
	public final static int TYPE_MASK = 0x1C;
	public final static int OK = 0x04;
	public final static int NO = 0x08;
	public final static int BAD = 0x0C;
	public final static int BYE = 0x10;
	
	// The sixth bit indicates whether a BYE response is synthetic or real
	public final static int SYNTHETIC = 0x20;
	
	public Response(String s) {
		buffer = ASCIIUtility.getBytes(s);
		size = buffer.length;
		parse();
	}
	
	/**
	 * Read a new Response from the given Protocol
	 * 
	 * @param p
	 *            the Protocol object
	 */
	public Response(Protocol p) throws IOException, ProtocolException {
		
		// read one response into 'buffer'
		ByteArray response = p.getInputStream().readResponse();
		buffer = response.getBytes();
		size = response.getCount() - 2; // Skip the terminating CRLF
		
		parse();
	}
	
	/**
	 * Copy constructor.
	 */
	public Response(Response r) {
		index = r.index;
		size = r.size;
		buffer = r.buffer;
		type = r.type;
		tag = r.tag;
	}
	
	/**
	 * Return a Response object that looks like a BYE protocol response. Include the details of the exception in the response string.
	 */
	public static Response byeResponse(Exception ex) {
		String err = "* BYE JavaMail Exception: " + ex.toString();
		err = err.replace('\r', ' ').replace('\n', ' ');
		Response r = new Response(err);
		r.type |= SYNTHETIC;
		return r;
	}
	
	private void parse() {
		index = 0; // position internal index at start
		
		if (buffer[index] == '+') { // Continuation statement
			type |= CONTINUATION;
			index += 1; // Position beyond the '+'
			return; // return
		} else if (buffer[index] == '*') { // Untagged statement
			type |= UNTAGGED;
			index += 1; // Position beyond the '*'
		} else { // Tagged statement
			type |= TAGGED;
			tag = readAtom(); // read the TAG, index positioned beyond tag
		}
		
		int mark = index; // mark
		String s = readAtom(); // updates index
		if (s.equalsIgnoreCase("OK"))
			type |= OK;
		else if (s.equalsIgnoreCase("NO"))
			type |= NO;
		else if (s.equalsIgnoreCase("BAD"))
			type |= BAD;
		else if (s.equalsIgnoreCase("BYE"))
			type |= BYE;
		else
			index = mark; // reset
			
		pindex = index;
		return;
	}
	
	public void skipSpaces() {
		while (index < size && buffer[index] == ' ')
			index++;
	}
	
	/**
	 * Skip to the next space, for use in error recovery while parsing.
	 */
	public void skipToken() {
		while (index < size && buffer[index] != ' ')
			index++;
	}
	
	public void skip(int count) {
		index += count;
	}
	
	public byte peekByte() {
		if (index < size)
			return buffer[index];
		else
			return 0; // XXX - how else to signal error?
	}
	
	/**
	 * Return the next byte from this Statement.
	 * 
	 * @return the next byte.
	 */
	public byte readByte() {
		if (index < size)
			return buffer[index++];
		else
			return 0; // XXX - how else to signal error?
	}
	
	/**
	 * Extract an ATOM, starting at the current position. Updates the internal index to beyond the Atom.
	 * 
	 * @return an Atom
	 */
	public String readAtom() {
		return readAtom('\0');
	}
	
	/**
	 * Extract an ATOM, but stop at the additional delimiter (if not NUL). Used to parse a response code inside [].
	 */
	public String readAtom(char delim) {
		skipSpaces();
		
		if (index >= size) // already at end of response
			return null;
		
		/*
		 * An ATOM is any CHAR delimited by : SPACE | CTL | '(' | ')' | '{' | '%' | '*' | '"' | '\'
		 */
		byte b;
		int start = index;
		while (index < size && ((b = buffer[index]) > ' ') && b != '(' && b != ')' && b != '%' && b != '*' && b != '"' && b != '\\' && b != 0x7f && (delim == '\0' || b != delim))
			index++;
		
		return ASCIIUtility.toString(buffer, start, index);
	}
	
	/**
	 * Read a string as an arbitrary sequence of characters, stopping at the delimiter Used to read part of a response code inside [].
	 */
	public String readString(char delim) {
		skipSpaces();
		
		if (index >= size) // already at end of response
			return null;
		
		int start = index;
		while (index < size && buffer[index] != delim)
			index++;
		
		return ASCIIUtility.toString(buffer, start, index);
	}
	
	public String[] readStringList() {
		skipSpaces();
		
		if (buffer[index] != '(') // not what we expected
			return null;
		index++; // skip '('
		
		Vector v = new Vector();
		do {
			v.addElement(readString());
		} while (buffer[index++] != ')');
		
		int size = v.size();
		if (size > 0) {
			String[] s = new String[size];
			v.copyInto(s);
			return s;
		} else // empty list
			return null;
	}
	
	/**
	 * Extract an integer, starting at the current position. Updates the internal index to beyond the number. Returns -1 if a number was not found.
	 *
	 * @return a number
	 */
	public int readNumber() {
		// Skip leading spaces
		skipSpaces();
		
		int start = index;
		while (index < size && Character.isDigit((char) buffer[index]))
			index++;
		
		if (index > start) {
			try {
				return ASCIIUtility.parseInt(buffer, start, index);
			} catch (NumberFormatException nex) {
			}
		}
		
		return -1;
	}
	
	/**
	 * Extract a long number, starting at the current position. Updates the internal index to beyond the number. Returns -1 if a long number was not found.
	 *
	 * @return a long
	 */
	public long readLong() {
		// Skip leading spaces
		skipSpaces();
		
		int start = index;
		while (index < size && Character.isDigit((char) buffer[index]))
			index++;
		
		if (index > start) {
			try {
				return ASCIIUtility.parseLong(buffer, start, index);
			} catch (NumberFormatException nex) {
			}
		}
		
		return -1;
	}
	
	/**
	 * Extract a NSTRING, starting at the current position. Return it as a String. The sequence 'NIL' is returned as null
	 *
	 * NSTRING := QuotedString | Literal | "NIL"
	 *
	 * @return a String
	 */
	public String readString() {
		return (String) parseString(false, true);
	}
	
	/**
	 * Extract a NSTRING, starting at the current position. Return it as a ByteArrayInputStream. The sequence 'NIL' is returned as null
	 *
	 * NSTRING := QuotedString | Literal | "NIL"
	 *
	 * @return a ByteArrayInputStream
	 */
	public ByteArrayInputStream readBytes() {
		ByteArray ba = readByteArray();
		if (ba != null)
			return ba.toByteArrayInputStream();
		else
			return null;
	}
	
	/**
	 * Extract a NSTRING, starting at the current position. Return it as a ByteArray. The sequence 'NIL' is returned as null
	 *
	 * NSTRING := QuotedString | Literal | "NIL"
	 *
	 * @return a ByteArray
	 */
	public ByteArray readByteArray() {
		/*
		 * Special case, return the data after the continuation uninterpreted. It's usually a challenge for an AUTHENTICATE command.
		 */
		if (isContinuation()) {
			skipSpaces();
			return new ByteArray(buffer, index, size - index);
		}
		return (ByteArray) parseString(false, false);
	}
	
	/**
	 * Extract an ASTRING, starting at the current position and return as a String. An ASTRING can be a QuotedString, a Literal or an Atom
	 *
	 * Any errors in parsing returns null
	 *
	 * ASTRING := QuotedString | Literal | Atom
	 *
	 * @return a String
	 */
	public String readAtomString() {
		return (String) parseString(true, true);
	}
	
	/**
	 * Generic parsing routine that can parse out a Quoted-String, Literal or Atom and return the parsed token as a String or a ByteArray. Errors or NIL data will return null.
	 */
	private Object parseString(boolean parseAtoms, boolean returnString) {
		byte b;
		
		// Skip leading spaces
		skipSpaces();
		
		b = buffer[index];
		if (b == '"') { // QuotedString
			index++; // skip the quote
			int start = index;
			int copyto = index;
			
			while ((b = buffer[index]) != '"') {
				if (b == '\\') // skip escaped byte
					index++;
				if (index != copyto) { // only copy if we need to
					// Beware: this is a destructive copy. I'm
					// pretty sure this is OK, but ... ;>
					buffer[copyto] = buffer[index];
				}
				copyto++;
				index++;
			}
			index++; // skip past the terminating quote
			
			if (returnString)
				return ASCIIUtility.toString(buffer, start, copyto);
			else
				return new ByteArray(buffer, start, copyto - start);
		} else if (b == '{') { // Literal
			int start = ++index; // note the start position
			
			while (buffer[index] != '}')
				index++;
			
			int count = 0;
			try {
				count = ASCIIUtility.parseInt(buffer, start, index);
			} catch (NumberFormatException nex) {
				// throw new ParsingException();
				return null;
			}
			
			start = index + 3; // skip "}\r\n"
			index = start + count; // position index to beyond the literal
			
			if (returnString) // return as String
				return ASCIIUtility.toString(buffer, start, start + count);
			else
				return new ByteArray(buffer, start, count);
		} else if (parseAtoms) { // parse as an ATOM
			int start = index; // track this, so that we can use to
			// creating ByteArrayInputStream below.
			String s = readAtom();
			if (returnString)
				return s;
			else // *very* unlikely
				return new ByteArray(buffer, start, index);
		} else if (b == 'N' || b == 'n') { // the only valid value is 'NIL'
			index += 3; // skip past NIL
			return null;
		}
		return null; // Error
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isContinuation() {
		return ((type & TAG_MASK) == CONTINUATION);
	}
	
	public boolean isTagged() {
		return ((type & TAG_MASK) == TAGGED);
	}
	
	public boolean isUnTagged() {
		return ((type & TAG_MASK) == UNTAGGED);
	}
	
	public boolean isOK() {
		return ((type & TYPE_MASK) == OK);
	}
	
	public boolean isNO() {
		return ((type & TYPE_MASK) == NO);
	}
	
	public boolean isBAD() {
		return ((type & TYPE_MASK) == BAD);
	}
	
	public boolean isBYE() {
		return ((type & TYPE_MASK) == BYE);
	}
	
	public boolean isSynthetic() {
		return ((type & SYNTHETIC) == SYNTHETIC);
	}
	
	/**
	 * Return the tag, if this is a tagged statement.
	 * 
	 * @return tag of this tagged statement
	 */
	public String getTag() {
		return tag;
	}
	
	/**
	 * Return the rest of the response as a string, usually used to return the arbitrary message text after a NO response.
	 */
	public String getRest() {
		skipSpaces();
		return ASCIIUtility.toString(buffer, index, size);
	}
	
	/**
	 * Reset pointer to beginning of response.
	 */
	public void reset() {
		index = pindex;
	}
	
	public String toString() {
		return ASCIIUtility.toString(buffer, 0, size);
	}
	
}
