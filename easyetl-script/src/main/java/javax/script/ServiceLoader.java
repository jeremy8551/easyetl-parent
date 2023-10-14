/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javax.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 脚本引擎服务加载器
 *
 * @author Mark Reinhold
 * @since 1.6
 */
public final class ServiceLoader<S> implements Iterable<S> {

    private static final String PREFIX = "META-INF/services/";

    private final Class<S> service;

    private final ClassLoader loader;

    private final AccessControlContext acc;

    private LinkedHashMap<String, S> providers;

    private LazyIterator lookupIterator;

    private ServiceLoader(Class<S> svc, ClassLoader cl) {
        if (svc == null) {
            throw new NullPointerException();
        }

        this.providers = new LinkedHashMap<String, S>();
        this.service = svc;
        this.loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
        this.acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        this.reload();
    }

    public void reload() {
        this.providers.clear();
        this.lookupIterator = new LazyIterator(this.service, this.loader);
    }

    private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, URL u, int line, String msg) throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }

    private int parseLine(Class<?> service, URL u, BufferedReader br, int lc, List<String> names) throws IOException, ServiceConfigurationError {
        String line = br.readLine();
        if (line == null) {
            return -1;
        }

        int ci = line.indexOf('#');
        if (ci >= 0) {
            line = line.substring(0, ci);
        }

        line = line.trim();
        int length = line.length();
        if (length != 0) {
            if ((line.indexOf(' ') >= 0) || (line.indexOf('\t') >= 0)) {
                fail(service, u, lc, "Illegal configuration-file syntax");
            }

            int cp = line.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                fail(service, u, lc, "Illegal provider-class name: " + line);
            }

            for (int i = Character.charCount(cp); i < length; i += Character.charCount(cp)) {
                cp = line.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                    fail(service, u, lc, "Illegal provider-class name: " + line);
                }
            }

            if (!this.providers.containsKey(line) && !names.contains(line)) {
                names.add(line);
            }
        }
        return lc + 1;
    }

    private Iterator<String> parse(Class<?> service, URL url) throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader br = null;

        ArrayList<String> names = new ArrayList<String>();
        try {
            in = url.openStream();
            br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int line = 1;
            while ((line = this.parseLine(service, url, br, line, names)) >= 0) {
            }
        } catch (IOException x) {
            fail(service, "Error reading configuration file", x);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }

                if (in != null) {
                    in.close();
                }
            } catch (IOException y) {
                fail(service, "Error closing configuration file", y);
            }
        }

        return names.iterator();
    }

    private class LazyIterator implements Iterator<S> {
        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        private boolean hasNextService() {
            if (this.nextName != null) {
                return true;
            }
            if (this.configs == null) {
                try {
                    String fullName = PREFIX + this.service.getName();
                    if (this.loader == null) {
                        this.configs = ClassLoader.getSystemResources(fullName);
                    } else {
                        this.configs = this.loader.getResources(fullName);
                    }
                } catch (IOException x) {
                    fail(this.service, "Error locating configuration files", x);
                }
            }

            while ((this.pending == null) || !this.pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                this.pending = parse(this.service, this.configs.nextElement());
            }

            this.nextName = this.pending.next();
            return true;
        }

        private S nextService() {
            if (!hasNextService()) {
                throw new NoSuchElementException();
            }

            String cn = this.nextName;
            this.nextName = null;
            Class<?> c = null;
            try {
                c = Class.forName(cn, false, this.loader);
            } catch (ClassNotFoundException x) {
                fail(this.service, "Provider " + cn + " not found");
            }

            if (!this.service.isAssignableFrom(c)) {
                fail(this.service, "Provider " + cn + " not a subtype");
            }

            try {
                S p = this.service.cast(c.newInstance());
                providers.put(cn, p);
                return p;
            } catch (Throwable x) {
                fail(this.service, "Provider " + cn + " could not be instantiated", x);
            }
            throw new Error(); // This cannot happen
        }

        public boolean hasNext() {
            if (acc == null) {
                return hasNextService();
            } else {
                PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        return hasNextService();
                    }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        public S next() {
            if (acc == null) {
                return nextService();
            } else {
                PrivilegedAction<S> action = new PrivilegedAction<S>() {
                    public S run() {
                        return nextService();
                    }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public Iterator<S> iterator() {
        return new Iterator<S>() {

            Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();

            public boolean hasNext() {
                if (this.knownProviders.hasNext()) {
                    return true;
                }
                return lookupIterator.hasNext();
            }

            public S next() {
                if (knownProviders.hasNext()) {
                    return knownProviders.next().getValue();
                }
                return lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        return new ServiceLoader<S>(service, loader);
    }

    public static <S> ServiceLoader<S> load(Class<S> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return ServiceLoader.load(service, cl);
    }

    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoader prev = null;
        while (cl != null) {
            prev = cl;
            cl = cl.getParent();
        }
        return ServiceLoader.load(service, prev);
    }

    public String toString() {
        return "java.util.ServiceLoader[" + service.getName() + "]";
    }

}
