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
 * @(#)FetchResponse.java	1.10 05/08/29
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.imap.protocol;

import java.io.IOException;
import java.util.Vector;

import icu.apache.mail.iap.ParsingException;
import icu.apache.mail.iap.Protocol;
import icu.apache.mail.iap.ProtocolException;
import icu.apache.mail.iap.Response;

/**
 * This class represents a response obtained from the input stream of an IMAP server.
 *
 * @version 1.10, 05/08/29
 * @author John Mani
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FetchResponse extends IMAPResponse {
	private Item[] items;
	
	public FetchResponse(Protocol p) throws IOException, ProtocolException {
		super(p);
		parse();
	}
	
	public FetchResponse(IMAPResponse r) throws IOException, ProtocolException {
		super(r);
		parse();
	}
	
	public int getItemCount() {
		return items.length;
	}
	
	public Item getItem(int index) {
		return items[index];
	}
	
	public Item getItem(Class c) {
		for (int i = 0; i < items.length; i++) {
			if (c.isInstance(items[i]))
				return items[i];
		}
		
		return null;
	}
	
	public static Item getItem(Response[] r, int msgno, Class c) {
		if (r == null)
			return null;
		
		for (int i = 0; i < r.length; i++) {
			
			if (r[i] == null || !(r[i] instanceof FetchResponse) || ((FetchResponse) r[i]).getNumber() != msgno)
				continue;
			
			FetchResponse f = (FetchResponse) r[i];
			for (int j = 0; j < f.items.length; j++) {
				if (c.isInstance(f.items[j]))
					return f.items[j];
			}
		}
		
		return null;
	}
	
	private final static char[] HEADER = { '.', 'H', 'E', 'A', 'D', 'E', 'R' };
	private final static char[] TEXT = { '.', 'T', 'E', 'X', 'T' };
	
	private void parse() throws ParsingException {
		skipSpaces();
		if (buffer[index] != '(')
			throw new ParsingException("error in FETCH parsing, missing '(' at index " + index);
		
		Vector v = new Vector();
		Item i = null;
		do {
			index++; // skip '(', or SPACE
			
			if (index >= size)
				throw new ParsingException("error in FETCH parsing, ran off end of buffer, size " + size);
			
			switch (buffer[index]) {
				case 'E':
					if (match(ENVELOPE.name)) {
						index += ENVELOPE.name.length; // skip "ENVELOPE"
						i = new ENVELOPE(this);
					}
					break;
				case 'F':
					if (match(FLAGS.name)) {
						index += FLAGS.name.length; // skip "FLAGS"
						i = new FLAGS((IMAPResponse) this);
					}
					break;
				case 'I':
					if (match(INTERNALDATE.name)) {
						index += INTERNALDATE.name.length; // skip "INTERNALDATE"
						i = new INTERNALDATE(this);
					}
					break;
				case 'B':
					if (match(BODY.name)) {
						if (buffer[index + 4] == '[') {
							index += BODY.name.length; // skip "BODY"
							i = new BODY(this);
						} else {
							if (match(BODYSTRUCTURE.name))
								index += BODYSTRUCTURE.name.length;
							// skip "BODYSTRUCTURE"
							else
								index += BODY.name.length; // skip "BODY"
							i = new BODYSTRUCTURE(this);
						}
					}
					break;
				case 'R':
					if (match(RFC822SIZE.name)) {
						index += RFC822SIZE.name.length; // skip "RFC822.SIZE"
						i = new RFC822SIZE(this);
					} else {
						if (match(RFC822DATA.name)) {
							index += RFC822DATA.name.length;
							if (match(HEADER))
								index += HEADER.length; // skip ".HEADER"
							else if (match(TEXT))
								index += TEXT.length; // skip ".TEXT"
							i = new RFC822DATA(this);
						}
					}
					break;
				case 'U':
					if (match(UID.name)) {
						index += UID.name.length;
						i = new UID(this);
					}
					break;
				default:
			}
			if (i != null)
				v.addElement(i);
		} while (buffer[index] != ')');
		
		index++; // skip ')'
		items = new Item[v.size()];
		v.copyInto(items);
	}
	
	/*
	 * itemName is the name of the IMAP item to compare against. NOTE that itemName *must* be all uppercase.
	 */
	private boolean match(char[] itemName) {
		int len = itemName.length;
		for (int i = 0, j = index; i < len;)
			// IMAP tokens are case-insensitive. We store itemNames in
			// uppercase, so convert operand to uppercase before comparing.
			if (Character.toUpperCase((char) buffer[j++]) != itemName[i++])
				return false;
		return true;
	}
}
