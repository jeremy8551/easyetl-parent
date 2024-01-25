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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import icu.etl.util.Ensure;

/**
 * 脚本引擎管理器，用于支持 JDK1.5
 *
 * @author Mike Grogan
 * @author A. Sundararajan
 * @since 1.6
 */
public class ScriptEngineManager {

    private boolean debug = false;

    private HashSet<ScriptEngineFactory> factorys;

    private HashMap<String, ScriptEngineFactory> nameAssociations;

    private HashMap<String, ScriptEngineFactory> extensionAssociations;

    private HashMap<String, ScriptEngineFactory> mimeTypeAssociations;

    private javax.script.Bindings globalScope;

    public ScriptEngineManager() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        this.init(cl);
    }

    public ScriptEngineManager(ClassLoader loader) {
        this.init(loader);
    }

    private void init(final ClassLoader loader) {
        this.globalScope = new SimpleBindings();
        this.factorys = new HashSet<ScriptEngineFactory>();
        this.nameAssociations = new HashMap<String, ScriptEngineFactory>();
        this.extensionAssociations = new HashMap<String, ScriptEngineFactory>();
        this.mimeTypeAssociations = new HashMap<String, ScriptEngineFactory>();
        this.initEngines(loader);
    }

    private ServiceLoader<ScriptEngineFactory> getServiceLoader(final ClassLoader loader) {
        if (loader != null) {
            return ServiceLoader.load(ScriptEngineFactory.class, loader);
        } else {
            return ServiceLoader.loadInstalled(ScriptEngineFactory.class);
        }
    }

    private void initEngines(final ClassLoader loader) {
        Iterator<ScriptEngineFactory> it = null;
        try {
            ServiceLoader<ScriptEngineFactory> sl = AccessController.doPrivileged(new PrivilegedAction<ServiceLoader<ScriptEngineFactory>>() {

                public ServiceLoader<ScriptEngineFactory> run() {
                    return getServiceLoader(loader);
                }
            });

            it = sl.iterator();
        } catch (ServiceConfigurationError e) {
            System.err.println("Can't find ScriptEngineFactory providers: " + e.getMessage());
            if (debug) {
                e.printStackTrace();
            }
            return;
        }

        try {
            while (it.hasNext()) {
                try {
                    ScriptEngineFactory factory = it.next();
                    this.factorys.add(factory);
                } catch (ServiceConfigurationError e) {
                    System.err.println("ScriptEngineManager providers.next(): " + e.getMessage());
                    if (debug) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
        } catch (ServiceConfigurationError e) {
            System.err.println("ScriptEngineManager providers.hasNext(): " + e.getMessage());
            if (debug) {
                e.printStackTrace();
            }
            return;
        }
    }

    public void setDebug(boolean v) {
        debug = v;
    }

    public void setBindings(javax.script.Bindings obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Global scope cannot be null.");
        } else {
            this.globalScope = obj;
        }
    }

    public Bindings getBindings() {
        return this.globalScope;
    }

    public void put(String key, Object value) {
        this.globalScope.put(key, value);
    }

    public Object get(String key) {
        return this.globalScope.get(key);
    }

    public javax.script.ScriptEngine getEngineByName(String shortName) {
        Ensure.notNull(shortName);

        // look for registered name first
        if (this.nameAssociations.containsKey(shortName)) {
            ScriptEngineFactory factory = this.nameAssociations.get(shortName);
            try {
                javax.script.ScriptEngine engine = factory.getScriptEngine();
                engine.setBindings(this.getBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception exp) {
                if (debug) {
                    exp.printStackTrace();
                }
            }
        }

        for (ScriptEngineFactory factory : this.factorys) {
            List<String> names = null;
            try {
                names = factory.getNames();
            } catch (Exception exp) {
                if (debug) {
                    exp.printStackTrace();
                }
            }

            if (names != null) {
                for (String name : names) {
                    if (shortName.equals(name)) {
                        try {
                            javax.script.ScriptEngine engine = factory.getScriptEngine();
                            engine.setBindings(this.getBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
                            return engine;
                        } catch (Exception exp) {
                            if (debug) {
                                exp.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public javax.script.ScriptEngine getEngineByExtension(String extension) {
        Ensure.notNull(extension);

        // look for registered extension first
        if (this.extensionAssociations.containsKey(extension)) {
            ScriptEngineFactory factory = this.extensionAssociations.get(extension);
            try {
                javax.script.ScriptEngine engine = factory.getScriptEngine();
                engine.setBindings(this.getBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception e) {
                if (debug) {
                    e.printStackTrace();
                }
            }
        }

        for (ScriptEngineFactory factory : this.factorys) {
            List<String> exts = null;
            try {
                exts = factory.getExtensions();
            } catch (Exception e) {
                if (debug) {
                    e.printStackTrace();
                }
            }

            if (exts == null) {
                continue;
            }

            for (String ext : exts) {
                if (extension.equals(ext)) {
                    try {
                        javax.script.ScriptEngine engine = factory.getScriptEngine();
                        engine.setBindings(this.getBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
                        return engine;
                    } catch (Exception e) {
                        if (debug) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

    public javax.script.ScriptEngine getEngineByMimeType(String mimeType) {
        Ensure.notNull(mimeType);

        // look for registered types first
        if (this.mimeTypeAssociations.containsKey(mimeType)) {
            ScriptEngineFactory factory = this.mimeTypeAssociations.get(mimeType);
            try {
                javax.script.ScriptEngine engine = factory.getScriptEngine();
                engine.setBindings(this.getBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception e) {
                if (debug) {
                    e.printStackTrace();
                }
            }
        }

        for (ScriptEngineFactory factory : this.factorys) {
            List<String> types = null;
            try {
                types = factory.getMimeTypes();
            } catch (Exception e) {
                if (debug) {
                    e.printStackTrace();
                }
            }

            if (types == null) {
                continue;
            }

            for (String type : types) {
                if (mimeType.equals(type)) {
                    try {
                        ScriptEngine engine = factory.getScriptEngine();
                        engine.setBindings(this.getBindings(), ScriptContext.GLOBAL_SCOPE);
                        return engine;
                    } catch (Exception e) {
                        if (debug) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<ScriptEngineFactory> getEngineFactories() {
        List<ScriptEngineFactory> list = new ArrayList<ScriptEngineFactory>(this.factorys.size());
        for (ScriptEngineFactory factory : this.factorys) {
            list.add(factory);
        }
        return Collections.unmodifiableList(list);
    }

    public void registerEngineName(String name, ScriptEngineFactory factory) {
        Ensure.notNull(name);
        Ensure.notNull(factory);
        this.nameAssociations.put(name, factory);
    }

    public void registerEngineMimeType(String type, ScriptEngineFactory factory) {
        Ensure.notNull(type);
        Ensure.notNull(factory);
        this.mimeTypeAssociations.put(type, factory);
    }

    public void registerEngineExtension(String extension, ScriptEngineFactory factory) {
        Ensure.notNull(extension);
        Ensure.notNull(factory);
        this.extensionAssociations.put(extension, factory);
    }

}
