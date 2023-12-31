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
 * @(#)Utility.java	1.6 05/11/17
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.imap;

import java.util.Vector;

import icu.apache.mail.Message;
import icu.apache.mail.imap.protocol.MessageSet;
import icu.apache.mail.imap.protocol.UIDSet;

/**
 * Holder for some static utility methods.
 *
 * @version 1.6, 05/11/17
 * @author John Mani
 * @author Bill Shannon
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class Utility {
	
	// Cannot be initialized
	private Utility() {
	}
	
	/**
	 * Run thru the given array of messages, apply the given Condition on each message and generate sets of contiguous sequence-numbers for the successful messages. If a message in the given array is found to be expunged, it is ignored.
	 *
	 * ASSERT: Since this method uses and returns message sequence numbers, you should use this method only when holding the messageCacheLock.
	 */
	public static MessageSet[] toMessageSet(Message[] msgs, Condition cond) {
		Vector v = new Vector(1);
		int current, next;
		
		IMAPMessage msg;
		for (int i = 0; i < msgs.length; i++) {
			msg = (IMAPMessage) msgs[i];
			if (msg.isExpunged()) // expunged message, skip it
				continue;
			
			current = msg.getSequenceNumber();
			// Apply the condition. If it fails, skip it.
			if ((cond != null) && !cond.test(msg))
				continue;
			
			MessageSet set = new MessageSet();
			set.start = current;
			
			// Look for contiguous sequence numbers
			for (++i; i < msgs.length; i++) {
				// get next message
				msg = (IMAPMessage) msgs[i];
				
				if (msg.isExpunged()) // expunged message, skip it
					continue;
				next = msg.getSequenceNumber();
				
				// Does this message match our condition ?
				if ((cond != null) && !cond.test(msg))
					continue;
				
				if (next == current + 1)
					current = next;
				else { // break in sequence
					   // We need to reexamine this message at the top of
					   // the outer loop, so decrement 'i' to cancel the
					   // outer loop's autoincrement
					i--;
					break;
				}
			}
			set.end = current;
			v.addElement(set);
		}
		
		if (v.isEmpty()) // No valid messages
			return null;
		else {
			MessageSet[] sets = new MessageSet[v.size()];
			v.copyInto(sets);
			return sets;
		}
	}
	
	/**
	 * Return UIDSets for the messages. Note that the UIDs must have already been fetched for the messages.
	 */
	public static UIDSet[] toUIDSet(Message[] msgs) {
		Vector v = new Vector(1);
		long current, next;
		
		IMAPMessage msg;
		for (int i = 0; i < msgs.length; i++) {
			msg = (IMAPMessage) msgs[i];
			if (msg.isExpunged()) // expunged message, skip it
				continue;
			
			current = msg.getUID();
			
			UIDSet set = new UIDSet();
			set.start = current;
			
			// Look for contiguous UIDs
			for (++i; i < msgs.length; i++) {
				// get next message
				msg = (IMAPMessage) msgs[i];
				
				if (msg.isExpunged()) // expunged message, skip it
					continue;
				next = msg.getUID();
				
				if (next == current + 1)
					current = next;
				else { // break in sequence
					   // We need to reexamine this message at the top of
					   // the outer loop, so decrement 'i' to cancel the
					   // outer loop's autoincrement
					i--;
					break;
				}
			}
			set.end = current;
			v.addElement(set);
		}
		
		if (v.isEmpty()) // No valid messages
			return null;
		else {
			UIDSet[] sets = new UIDSet[v.size()];
			v.copyInto(sets);
			return sets;
		}
	}
	
	/**
	 * This interface defines the test to be executed in <code>toMessageSet()</code>.
	 */
	public static interface Condition {
		public boolean test(IMAPMessage message);
	}
}
