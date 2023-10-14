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
 * @(#)SaslAuthenticator.java	1.3 05/08/29
 *
 * Copyright 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.imap.protocol;

import icu.apache.mail.iap.ProtocolException;

/**
 * Interface to make it easier to call IMAPSaslAuthenticator.
 */

public interface SaslAuthenticator {
	public boolean authenticate(String[] mechs, String realm, String authzid, String u, String p) throws ProtocolException;
	
}
