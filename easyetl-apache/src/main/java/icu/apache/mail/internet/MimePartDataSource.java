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
 * @(#)MimePartDataSource.java	1.12 05/08/29
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownServiceException;

import icu.apache.mail.MessageAware;
import icu.apache.mail.MessageContext;
import icu.apache.mail.MessagingException;
import icu.apache.mail.activation.DataSource;

/**
 * A utility class that implements a DataSource out of a MimePart. This class is primarily meant for service providers.
 *
 * @see MimePart
 * @see DataSource
 * @author John Mani
 */
public class MimePartDataSource implements DataSource, MessageAware {
	/**
	 * The MimePart that provides the data for this DataSource.
	 *
	 * @since JavaMail 1.4
	 */
	protected MimePart part;
	
	private MessageContext context;
	
	private static boolean ignoreMultipartEncoding = true;
	
	static {
		try {
			String s = System.getProperty("mail.mime.ignoremultipartencoding");
			// default to true
			ignoreMultipartEncoding = s == null || !s.equalsIgnoreCase("false");
		} catch (SecurityException sex) {
			// ignore it
		}
	}
	
	/**
	 * Constructor, that constructs a DataSource from a MimePart.
	 */
	public MimePartDataSource(MimePart part) {
		this.part = part;
	}
	
	/**
	 * Returns an input stream from this MimePart.
	 * <p>
	 *
	 * This method applies the appropriate transfer-decoding, based on the Content-Transfer-Encoding attribute of this MimePart. Thus the returned input stream is a decoded stream of bytes.
	 * <p>
	 *
	 * This implementation obtains the raw content from the Part using the <code>getContentStream()</code> method and decodes it using the <code>MimeUtility.decode()</code> method.
	 *
	 * @see MimeMessage#getContentStream
	 * @see MimeBodyPart#getContentStream
	 * @see MimeUtility#decode
	 * @return decoded input stream
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is;
		
		try {
			if (part instanceof MimeBodyPart)
				is = ((MimeBodyPart) part).getContentStream();
			else if (part instanceof MimeMessage)
				is = ((MimeMessage) part).getContentStream();
			else
				throw new MessagingException("Unknown part");
			
			String encoding = restrictEncoding(part.getEncoding(), part);
			if (encoding != null)
				return MimeUtility.decode(is, encoding);
			else
				return is;
		} catch (MessagingException mex) {
			throw new IOException(mex.getMessage());
		}
	}
	
	/**
	 * Restrict the encoding to values allowed for the Content-Type of the specified MimePart. Returns either the original encoding or null.
	 */
	private static String restrictEncoding(String encoding, MimePart part) throws MessagingException {
		if (!ignoreMultipartEncoding || encoding == null)
			return encoding;
		
		if (encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit") || encoding.equalsIgnoreCase("binary"))
			return encoding; // these encodings are always valid
			
		String type = part.getContentType();
		if (type == null)
			return encoding;
		
		try {
			/*
			 * multipart and message types aren't allowed to have encodings except for the three mentioned above. If it's one of these types, ignore the encoding.
			 */
			ContentType cType = new ContentType(type);
			if (cType.match("multipart/*") || cType.match("message/*"))
				return null;
		} catch (ParseException pex) {
			// ignore it
		}
		return encoding;
	}
	
	/**
	 * DataSource method to return an output stream.
	 * <p>
	 *
	 * This implementation throws the UnknownServiceException.
	 */
	public OutputStream getOutputStream() throws IOException {
		throw new UnknownServiceException();
	}
	
	/**
	 * Returns the content-type of this DataSource.
	 * <p>
	 *
	 * This implementation just invokes the <code>getContentType</code> method on the MimePart.
	 */
	public String getContentType() {
		try {
			return part.getContentType();
		} catch (MessagingException mex) {
			return null;
		}
	}
	
	/**
	 * DataSource method to return a name.
	 * <p>
	 *
	 * This implementation just returns an empty string.
	 */
	public String getName() {
		try {
			if (part instanceof MimeBodyPart)
				return ((MimeBodyPart) part).getFileName();
		} catch (MessagingException mex) {
			// ignore it
		}
		return "";
	}
	
	/**
	 * Return the <code>MessageContext</code> for the current part.
	 * 
	 * @since JavaMail 1.1
	 */
	public synchronized MessageContext getMessageContext() {
		if (context == null)
			context = new MessageContext(part);
		return context;
	}
}
