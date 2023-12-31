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
 * @(#)NoSuchProviderException.java	1.6 05/08/29
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail;

/**
 * This exception is thrown when Session attempts to instantiate a Provider that doesn't exist.
 * 
 * @author Max Spivak
 */
public class NoSuchProviderException extends MessagingException {
	
	private static final long serialVersionUID = 8058319293154708827L;
	
	/**
	 * Constructor.
	 */
	public NoSuchProviderException() {
		super();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param message
	 *            The detailed error message
	 */
	public NoSuchProviderException(String message) {
		super(message);
	}
}
