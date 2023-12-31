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
 * @(#)POP3Store.java	1.29 05/08/29
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package icu.apache.mail.pop3;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Constructor;

import icu.apache.mail.AuthenticationFailedException;
import icu.apache.mail.Folder;
import icu.apache.mail.MessagingException;
import icu.apache.mail.Session;
import icu.apache.mail.Store;
import icu.apache.mail.URLName;

/**
 * A POP3 Message Store. Contains only one folder, "INBOX".
 *
 * See the <a href="package-summary.html">com.sun.mail.pop3</a> package documentation for further information on the POP3 protocol provider.
 * <p>
 *
 * @author Bill Shannon
 * @author John Mani
 */
@SuppressWarnings({ "rawtypes" })
public class POP3Store extends Store {
	
	private String name = "pop3"; // my protocol name
	private int defaultPort = 110; // default POP3 port
	private boolean isSSL = false; // use SSL?
	
	private Protocol port = null; // POP3 port for self
	private POP3Folder portOwner = null; // folder owning port
	private String host = null; // host
	private int portNum = -1;
	private String user = null;
	private String passwd = null;
	boolean rsetBeforeQuit = false;
	boolean disableTop = false;
	boolean forgetTopHeaders = false;
	Constructor messageConstructor = null;
	
	public POP3Store(Session session, URLName url) {
		this(session, url, "pop3", 110, false);
	}
	
	@SuppressWarnings("unchecked")
	public POP3Store(Session session, URLName url, String name, int defaultPort, boolean isSSL) {
		super(session, url);
		if (url != null)
			name = url.getProtocol();
		this.name = name;
		this.defaultPort = defaultPort;
		this.isSSL = isSSL;
		
		String s = session.getProperty("mail." + name + ".rsetbeforequit");
		if (s != null && s.equalsIgnoreCase("true"))
			rsetBeforeQuit = true;
		
		s = session.getProperty("mail." + name + ".disabletop");
		if (s != null && s.equalsIgnoreCase("true"))
			disableTop = true;
		
		s = session.getProperty("mail." + name + ".forgettopheaders");
		if (s != null && s.equalsIgnoreCase("true"))
			forgetTopHeaders = true;
		
		s = session.getProperty("mail." + name + ".message.class");
		if (s != null) {
			if (session.getDebug())
				session.getDebugOut().println("DEBUG: POP3 message class: " + s);
			try {
				ClassLoader cl = this.getClass().getClassLoader();
				
				// now load the class
				Class messageClass = null;
				try {
					// First try the "application's" class loader.
					// This should eventually be replaced by
					// Thread.currentThread().getContextClassLoader().
					messageClass = cl.loadClass(s);
				} catch (ClassNotFoundException ex1) {
					// That didn't work, now try the "system" class loader.
					// (Need both of these because JDK 1.1 class loaders
					// may not delegate to their parent class loader.)
					messageClass = Class.forName(s);
				}
				
				Class[] c = { Folder.class, int.class };
				messageConstructor = messageClass.getConstructor(c);
			} catch (Exception ex) {
				if (session.getDebug())
					session.getDebugOut().println("DEBUG: failed to load POP3 message class: " + ex);
			}
		}
	}
	
	protected synchronized boolean protocolConnect(String host, int portNum, String user, String passwd) throws MessagingException {
		
		// check for non-null values of host, password, user
		if (host == null || passwd == null || user == null)
			return false;
			
		// if port is not specified, set it to value of mail.pop3.port
		// property if it exists, otherwise default to 110
		if (portNum == -1) {
			String portstring = session.getProperty("mail." + name + ".port");
			if (portstring != null)
				portNum = Integer.parseInt(portstring);
		}
		
		if (portNum == -1)
			portNum = defaultPort;
		
		this.host = host;
		this.portNum = portNum;
		this.user = user;
		this.passwd = passwd;
		try {
			port = getPort(null);
		} catch (EOFException eex) {
			throw new AuthenticationFailedException(eex.getMessage());
		} catch (IOException ioex) {
			throw new MessagingException("Connect failed", ioex);
		}
		
		return true;
	}
	
	/**
	 * Check whether this store is connected. Override superclass method, to actually ping our server connection.
	 * <p>
	 */
	/*
	 * Note that we maintain somewhat of an illusion of being connected even if we're not really connected. This is because a Folder can use the connection and close it when it's done. If we then ask whether the Store's connected we want the answer to be true, as long as we can reconnect at that point. This means that we need to be able to reconnect the Store on demand.
	 */
	public synchronized boolean isConnected() {
		if (!super.isConnected()) {
			// if we haven't been connected at all, don't bother with
			// the NOOP.
			return false;
		}
		
		synchronized (this) {
			try {
				if (this.port == null) {
					this.port = getPort(null);
				} else {
					this.port.noop();
				}
				return true;
			} catch (IOException ioex) {
				// no longer connected, close it down
				try {
					super.close(); // notifies listeners
					return false;
				} catch (MessagingException mex) {
					return false;
//				} finally {
//					return false;
				}
			}
		}
	}
	
	synchronized Protocol getPort(POP3Folder owner) throws IOException {
		Protocol p;
		
		// if we already have a port, remember who's using it
		if (port != null && portOwner == null) {
			portOwner = owner;
			return port;
		}
		
		// need a new port, create it and try to login
		p = new Protocol(host, portNum, session.getDebug(), session.getDebugOut(), session.getProperties(), "mail." + name, isSSL);
		
		String msg = null;
		if ((msg = p.login(user, passwd)) != null) {
			try {
				p.quit();
				throw new EOFException(msg);
			} catch (IOException ioex) {
				throw new EOFException(msg);
//			} finally {
//				throw new EOFException(msg);
			}
		}
		
		/*
		 * If a Folder closes the port, and then a Folder is opened, the Store won't have a port. In that case, the getPort call will come from Folder.open, but we need to keep track of the port in the Store so that a later call to Folder.isOpen, which calls Store.isConnected, will use the same port.
		 */
		if (port == null && owner != null) {
			port = p;
			portOwner = owner;
		}
		if (portOwner == null)
			portOwner = owner;
		return p;
	}
	
	synchronized void closePort(POP3Folder owner) {
		if (portOwner == owner) {
			port = null;
			portOwner = null;
		}
	}
	
	public synchronized void close() throws MessagingException {
		try {
			if (port != null)
				port.quit();
		} catch (IOException ioex) {
		} finally {
			port = null;
			
			// to set the state and send the closed connection event
			super.close();
		}
	}
	
	public Folder getDefaultFolder() throws MessagingException {
		checkConnected();
		return new DefaultFolder(this);
	}
	
	/**
	 * Only the name "INBOX" is supported.
	 */
	public Folder getFolder(String name) throws MessagingException {
		checkConnected();
		return new POP3Folder(this, name);
	}
	
	public Folder getFolder(URLName url) throws MessagingException {
		checkConnected();
		return new POP3Folder(this, url.getFile());
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		
		if (port != null) // don't force a connection attempt
			close();
	}
	
	private void checkConnected() throws MessagingException {
		if (!super.isConnected())
			throw new MessagingException("Not connected");
	}
}
