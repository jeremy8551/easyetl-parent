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
 * @(#)IMAPMultipartDataSource.java	1.6 05/08/29
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.imap;

import java.util.Vector;

import icu.apache.mail.BodyPart;
import icu.apache.mail.MessagingException;
import icu.apache.mail.MultipartDataSource;
import icu.apache.mail.imap.protocol.BODYSTRUCTURE;
import icu.apache.mail.internet.MimePart;
import icu.apache.mail.internet.MimePartDataSource;

/**
 * This class
 *
 * @version 1.6, 05/08/29
 * @author John Mani
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class IMAPMultipartDataSource extends MimePartDataSource implements MultipartDataSource {
	private Vector parts;
	
	protected IMAPMultipartDataSource(MimePart part, BODYSTRUCTURE[] bs, String sectionId, IMAPMessage msg) {
		super(part);
		
		parts = new Vector(bs.length);
		for (int i = 0; i < bs.length; i++)
			parts.addElement(new IMAPBodyPart(bs[i], sectionId == null ? Integer.toString(i + 1) : sectionId + "." + Integer.toString(i + 1), msg));
	}
	
	public int getCount() {
		return parts.size();
	}
	
	public BodyPart getBodyPart(int index) throws MessagingException {
		return (BodyPart) parts.elementAt(index);
	}
}
