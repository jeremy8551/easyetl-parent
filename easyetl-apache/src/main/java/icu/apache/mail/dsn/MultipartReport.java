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
 * @(#)MultipartReport.java	1.5 06/03/09
 *
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.dsn;

import java.io.IOException;
import java.util.Vector;

import icu.apache.mail.BodyPart;
import icu.apache.mail.MessagingException;
import icu.apache.mail.Multipart;
import icu.apache.mail.activation.DataSource;
import icu.apache.mail.internet.ContentType;
import icu.apache.mail.internet.InternetHeaders;
import icu.apache.mail.internet.MimeBodyPart;
import icu.apache.mail.internet.MimeMessage;
import icu.apache.mail.internet.MimeMultipart;

/**
 * A multipart/report message content, as defined in <A HREF="http://www.ietf.org/rfc/rfc3462.txt">RFC 3462</A>. A multipart/report content is a container for mail reports of any kind, and is most often used to return a delivery status report. This class only supports that most common usage.
 * <p>
 * <p>
 * A MultipartReport object is a special type of MimeMultipart object with a restricted set of body parts. A MultipartReport object contains:
 * <ul>
 * <li>[Required] A human readable text message describing the reason the report was generated.</li>
 * <li>[Required] A {@link DeliveryStatus} object containing the details for why the report was generated.</li>
 * <li>[Optional] A returned copy of the entire message, or just its headers, which caused the generation of this report.
 * </ul>
 * Many of the normal MimeMultipart operations are restricted to ensure that the MultipartReport object always follows this structure.
 */
@SuppressWarnings({"rawtypes"})
public class MultipartReport extends MimeMultipart {
    protected boolean constructed; // true when done with constructor

    /**
     * Construct a multipart/report object with no content.
     */
    public MultipartReport() throws MessagingException {
        super("report");
        // always at least two body parts
        MimeBodyPart mbp = new MimeBodyPart();
        setBodyPart(mbp, 0);
        mbp = new MimeBodyPart();
        setBodyPart(mbp, 1);
        constructed = true;
    }

    /**
     * Construct a multipart/report object with the specified plain text and delivery status to be returned to the user.
     */
    public MultipartReport(String text, DeliveryStatus status) throws MessagingException {
        super("report");
        ContentType ct = new ContentType(contentType);
        ct.setParameter("report-type", "delivery-status");
        contentType = ct.toString();
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText(text);
        setBodyPart(mbp, 0);
        mbp = new MimeBodyPart();
        mbp.setContent(status, "message/delivery-status");
        setBodyPart(mbp, 1);
        constructed = true;
    }

    /**
     * Construct a multipart/report object with the specified plain text, delivery status, and original message to be returned to the user.
     */
    public MultipartReport(String text, DeliveryStatus status, MimeMessage msg) throws MessagingException {
        this(text, status);
        if (msg != null) {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(msg, "message/rfc822");
            setBodyPart(mbp, 2);
        }
    }

    /**
     * Construct a multipart/report object with the specified plain text, delivery status, and headers from the original message to be returned to the user.
     */
    public MultipartReport(String text, DeliveryStatus status, InternetHeaders hdr) throws MessagingException {
        this(text, status);
        if (hdr != null) {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(new MessageHeaders(hdr), "text/rfc822-headers");
            setBodyPart(mbp, 2);
        }
    }

    /**
     * Constructs a MultipartReport object and its bodyparts from the given DataSource.
     * <p>
     *
     * @param ds DataSource, can be a MultipartDataSource
     */
    public MultipartReport(DataSource ds) throws MessagingException {
        super(ds);
        parse();
        constructed = true;
        /*
         * Can't fail to construct object because some programs just want to treat this as a Multipart and examine the parts.
         *
         * if (getCount() < 2 || getCount() > 3) // XXX allow extra parts throw new MessagingException( "Wrong number of parts in multipart/report: " + getCount());
         */
    }

    /**
     * Get the plain text to be presented to the user, if there is any. Rarely, the message may contain only HTML text, or no text at all. If the text body part of this multipart/report object is of type text/plain, or if it is of type multipart/alternative and contains a text/plain part, the text from that part is returned. Otherwise, null is return and the {@link #getTextBodyPart getTextBodyPart} method may be used to extract the data.
     */
    public synchronized String getText() throws MessagingException {
        try {
            BodyPart bp = getBodyPart(0);
            if (bp.isMimeType("text/plain"))
                return (String) bp.getContent();
            if (bp.isMimeType("multipart/alternative")) {
                Multipart mp = (Multipart) bp.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/plain"))
                        return (String) bp.getContent();
                }
            }
        } catch (IOException ex) {
            throw new MessagingException("Exception getting text content", ex);
        }
        return null;
    }

    /**
     * Set the message to be presented to the user as just a text/plain part containing the specified text.
     */
    public synchronized void setText(String text) throws MessagingException {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText(text);
        setBodyPart(mbp, 0);
    }

    /**
     * Return the body part containing the message to be presented to the user, usually just a text/plain part.
     */
    public synchronized MimeBodyPart getTextBodyPart() throws MessagingException {
        return (MimeBodyPart) getBodyPart(0);
    }

    /**
     * Set the body part containing the text to be presented to the user. Usually this a text/plain part, but it might also be a text/html part or a multipart/alternative part containing text/plain and text/html parts. Any type is allowed here but these types are most common.
     */
    public synchronized void setTextBodyPart(MimeBodyPart mbp) throws MessagingException {
        setBodyPart(mbp, 0);
    }

    /**
     * Get the delivery status associated with this multipart/report.
     */
    public synchronized DeliveryStatus getDeliveryStatus() throws MessagingException {
        if (getCount() < 2)
            return null;
        BodyPart bp = getBodyPart(1);
        if (!bp.isMimeType("message/delivery-status"))
            return null;
        try {
            return (DeliveryStatus) bp.getContent();
        } catch (IOException ex) {
            throw new MessagingException("IOException getting DeliveryStatus", ex);
        }
    }

    /**
     * Set the delivery status associated with this multipart/report.
     */
    public synchronized void setDeliveryStatus(DeliveryStatus status) throws MessagingException {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setContent(status, "message/delivery-status");
        setBodyPart(mbp, 2);
        ContentType ct = new ContentType(contentType);
        ct.setParameter("report-type", "delivery-status");
        contentType = ct.toString();
    }

    /**
     * Get the original message that is being returned along with this multipart/report. If no original message is included, null is returned. In some cases only the headers of the original message will be returned as an object of type MessageHeaders.
     */
    public synchronized MimeMessage getReturnedMessage() throws MessagingException {
        if (getCount() < 3)
            return null;
        BodyPart bp = getBodyPart(2);
        if (!bp.isMimeType("message/rfc822") && !bp.isMimeType("text/rfc822-headers"))
            return null;
        try {
            return (MimeMessage) bp.getContent();
        } catch (IOException ex) {
            throw new MessagingException("IOException getting ReturnedMessage", ex);
        }
    }

    /**
     * Set the original message to be returned as part of the multipart/report. If msg is null, any previously set returned message or headers is removed.
     */
    public synchronized void setReturnedMessage(MimeMessage msg) throws MessagingException {
        if (msg == null) {
//            BodyPart part = (BodyPart)
            parts.elementAt(2);
            super.removeBodyPart(2);
            return;
        }
        MimeBodyPart mbp = new MimeBodyPart();
        if (msg instanceof MessageHeaders)
            mbp.setContent(msg, "text/rfc822-headers");
        else
            mbp.setContent(msg, "message/rfc822");
        setBodyPart(mbp, 2);
    }

    private synchronized void setBodyPart(BodyPart part, int index) throws MessagingException {
        if (parts == null) // XXX - can never happen?
            parts = new Vector();

        super.removeBodyPart(index);
        super.addBodyPart(part, index);
    }

    // Override Multipart methods to preserve integrity of multipart/report.

    /**
     * Set the subtype. Throws MessagingException.
     *
     * @param subtype Subtype
     * @throws MessagingException always; can't change subtype
     */
    public synchronized void setSubType(String subtype) throws MessagingException {
        throw new MessagingException("Can't change subtype of MultipartReport");
    }

    /**
     * Remove the specified part from the multipart message. Not allowed on a multipart/report object.
     *
     * @param part The part to remove
     * @throws MessagingException always
     */
    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        throw new MessagingException("Can't remove body parts from multipart/report");
    }

    /**
     * Remove the part at specified location (starting from 0). Not allowed on a multipart/report object.
     *
     * @param index Index of the part to remove
     * @throws MessagingException always
     */
    public void removeBodyPart(int index) throws MessagingException {
        throw new MessagingException("Can't remove body parts from multipart/report");
    }

    /**
     * Adds a Part to the multipart. Not allowed on a multipart/report object.
     *
     * @param part The Part to be appended
     * @throws MessagingException always
     */
    public synchronized void addBodyPart(BodyPart part) throws MessagingException {
        // Once constructor is done, don't allow this anymore.
        if (!constructed)
            super.addBodyPart(part);
        else
            throw new MessagingException("Can't add body parts to multipart/report 1");
    }

    /**
     * Adds a BodyPart at position <code>index</code>. Not allowed on a multipart/report object.
     *
     * @param part  The BodyPart to be inserted
     * @param index Location where to insert the part
     * @throws MessagingException always
     */
    public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
        throw new MessagingException("Can't add body parts to multipart/report 2");
    }
}
