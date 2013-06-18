/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.game.server.bootstrap;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Server main class. This class starts a JMX server instance, attaches 
 * a shutdown hook and waits for more instructions via JMX. You can start one
 * or more nodes immediately by supplying the "-n" switch followed by a comma
 * separated list of node identifiers in the following format:
 * 
 * <pre>
 * 		game:<id>
 * 		client:<id>
 *      mtt:<id>
 * 		master:<id>
 * 		singleton
 * </pre>
 * 
 * Obviously, the "singleton" argument will be for a single server, starting one
 * instance of each node in memory.
 *
 * The following default directories are used:
 * 
 * <pre>
 * 		"conf/" - where it will look for configuration files
 * 		"lib/" - where it will read libraries (jar files), requires "common" and "internal" subdirectories
 *      "server/" - server home (deployment etc)
 * </pre>
 * 
 * The following startup parameters are accepted:
 *  
 * <pre>
 * 		--conf | -c         - directory for configuration files (optional)
 * 		--lib | -l          - directory for libraries (optional)
 *		--node | -n			- immediately init a named nodes, comma separated list(optional)
 *      --game | -g			- directory for the game deployemtns (optional)
 *      --work | -w			- work directory (optional)
 *      --id | -i			- server id (optional, will be randomnized)
 * <pre>
 * 
 * This following example, starts a server with a master node:
 * 
 * <pre>
 * 		...Server -i server1 -n master:mas1
 * </pre>
 * 
 * @author lars.nilsson
 */

public class Server {

    private static final String DEF_CONF_DIR = "conf/";
    private static final String DEF_WORK_DIR = "work/";
    private static final String DEF_GAME_DIR = "game/";
    private static final String DEF_LIB_DIR = "lib/";
    
    private static final String CLASS_NAME = "com.cubeia.firebase.server.instance.ServerInstance";
    
    private static final Class<?>[] INIT_METHOD_PARAMS = { String.class, InitFolders.class, SharedClassLoader.class,(new BootstrapNode[0]).getClass() };
    
    private static final String INIT_METHOD_NAME = "init";
    private static final String DESTROY_METHOD_NAME = "destroy";
    private static final String START_METHOD_NAME = "start";
    private static final String STOP_METHOD_NAME = "stop";
    
	private static final long LOG_WATCH_INTERVAL = 5000;

	public static void main(String[] arr) {
        if(isHelp(arr)) {
            printHelp();
        } else { 
            try {
            	Server serv = new Server(arr, new PrintWriter(System.out));
            	serv.init();
            	serv.start();
            } catch(Throwable e) {
                // Ugly catch all here...
                System.err.println(" *** Failed to start server: ");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
	
	
	// --- INSTANCE MEMBERS --- //

	private final String[] args;
	//private final PrintWriter sysOut;
	
	private File confDir, libDir, gameDir, workDir;
	private BootstrapNode[] nodes;
	private ServerClassLoader systemLoader;
	private SharedClassLoader sharedLoader;
	private Object server;
	private Class<?> serverClass;
	private String id;
	
	private boolean started;
	private boolean initiated;
	
    public Server(String[] arr, PrintWriter sysOut) {
    	started = false;
    	//this.sysOut = sysOut;
    	this.args = arr;
	}

    public synchronized void init() throws Exception {
    	if(!initiated) {
    		checkInitId();
	    	checkInitDirectories();
	    	ceckLog4JConfig();
	    	setupBootstrapNodes();
	    	createClassLoaders();
	    	serverClass = getServerClass();
	        server = instantiateServer(serverClass);
	        addShutdownHook(server, serverClass);
	    	boolean b = getImmediateStart();
	    	initiated = true;
	    	if(b) start();
    	}
    }

	private void ceckLog4JConfig() {
		File test = new File(confDir, "log4j.xml");
		if(test.exists()) {
			DOMConfigurator.configureAndWatch(test.getAbsolutePath(), LOG_WATCH_INTERVAL);
		} else {
			test = new File(confDir, "log4j.properties");
			if(test.exists()) {
				PropertyConfigurator.configureAndWatch(test.getAbsolutePath(), LOG_WATCH_INTERVAL);
			}
		}
	}

	public synchronized void start() throws Exception {
		if(!started) {
	        initServer();
	        startServer();
	        started = true;
		}
    }
    
    public synchronized void stop() throws Exception {
    	if(started) {
	    	stopServer();
	    	started = false;
    	}
    }
    
    public synchronized void destroy() throws Exception {
    	if(initiated) {
    		destroyServer();
	    	initiated = false;
    	}
    }
    
	
    // --- PRIVATE METHODS --- //
    
	private void setupBootstrapNodes() {
		nodes = getStartupNodes();
	}
    
	private void checkInitId() {
		String tmp = getArgumentString(args, new String[] { "-i", "--id"});
		if(tmp == null) {
			tmp = newRandomId();
			System.out.println(" *** WARN: No server id found, will use random id: " + tmp);
		}
		this.id = tmp;
	}
	
	private String newRandomId() {
		return String.valueOf(new SecureRandom().nextInt());
	}

	private void checkInitDirectories() {
		confDir = getConfDir();
		gameDir = getGameDir();
		workDir = getWorkDir();
		libDir = getLibDir();
	}
    
	private void createClassLoaders() throws Exception {
		createSharedClassLoader();
		createSystemClassLoader();
	}

	private void createSharedClassLoader() {
		sharedLoader = new SharedClassLoader(getClass().getClassLoader());
	}

	private void createSystemClassLoader() throws Exception {
		systemLoader = getClassLoader();
	}
    
    private BootstrapNode[] getStartupNodes() {
    	if(args.length == 0) return null;
		String tmp = getArgumentString(args, new String[] { "-n", "--node"});
		String[] strings = (tmp == null ? new String[0] : tmp.split(","));
		List<BootstrapNode> list = new LinkedList<BootstrapNode>();
		for (String string : strings) {
			int i = string.indexOf(':');
			if(i == -1) {
				if(!isNodeType(string)) throw new IllegalArgumentException("argument '" + string + "' is not recognized as a node type");
				list.add(new BootstrapNode(string, null));
			} else {
				String t = string.substring(0, i);
				if(!isNodeType(t)) throw new IllegalArgumentException("argument '" + t + "' is not recognized as a node type");
				String id = string.substring(i + 1);
				list.add(new BootstrapNode(t, id));
			}
		}
		return list.toArray(new BootstrapNode[list.size()]);
	}

	/*private static String getNodeType(String[] arr) {
		if(arr.length == 0) return null;
		String tmp = getArgumentString(arr, new String[] { "-n", "--node"});
		if(tmp != null && isNodeType(tmp)) return tmp.substring(0, tmp.indexOf(':'));
		else return null;
    }

	private static String getNodeId(String[] arr) {
		if(arr.length == 0) return null;
		String tmp = getArgumentString(arr, new String[] { "-n", "--node"});
		if(tmp != null && isNodeType(tmp)) return tmp.substring(tmp.indexOf(':') + 1);
		else return null;
    }*/
	
	private boolean isNodeType(String tmp) {
		return tmp.equals("mtt") || tmp.equals("jms") || tmp.equals("manager") || tmp.equals("master") || tmp.equals("client") || tmp.equals("game") || tmp.equals("singleton");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object instantiateServer(Class serverClass) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
       Constructor c = serverClass.getConstructor(new Class[0]);
       return c.newInstance(new Object[0]);
	}

    private Class<?> getServerClass() throws ClassNotFoundException {
    	// Class one = systemLoader.loadClass("com.game.server.Startable");
        Class<?> two = systemLoader.loadClass(CLASS_NAME);

        /*ClassLoader sys = getClass().getClassLoader();
        
        System.out.println(one.getClassLoader() == two.getClassLoader());
        System.out.println(sys == one.getClassLoader());
        System.out.println(sys == two.getClassLoader());*/
        
        return two;
    }
    
    private ServerClassLoader getClassLoader() throws MalformedURLException {
        if(shouldUseRootLoader()) {
        	throw new UnsupportedOperationException();
        	/*Thread th = Thread.currentThread();
        	if(th.getContextClassLoader() != null) return th.getContextClassLoader();
        	else return Server.class.getClassLoader();*/
        }
        else {
        	URL[] libUrls = getLibUrls();
        	ServerClassLoader loader = new ServerClassLoader(libUrls, sharedLoader);
        	Thread.currentThread().setContextClassLoader(loader);
            return loader;
        }
    }

	private URL[] getLibUrls() throws MalformedURLException {
		List<URL> list = new LinkedList<URL>();
		addToList(toURLArray(new File(libDir, "internal")), list);
		if(useGeneratedLibs()) addToList(toURLArray(new File(libDir, "generated")), list);
		addToList(new URL[] { confDir.toURI().toURL() }, list);
		return list.toArray(new URL[list.size()]);
	}
	
    private boolean useGeneratedLibs() {
		for(String s : args) {
			if(s.equals("--gen") || s.equals("-g")) return true;
		}
		return false;
	}

    private void addToList(URL[] urls, List<URL> list) {
    	for (URL url : urls) {
			list.add(url);
		}
	}

	private URL[] toURLArray(File libDir) throws MalformedURLException {
        List<URL> list = new LinkedList<URL>();
        addFilesAsURLs(libDir, list);
        URL[] arr = new URL[list.size()];
        list.toArray(arr);
        return arr;
    }

    private void addFilesAsURLs(File dir, List<URL> list) throws MalformedURLException {
        File[] arr = dir.listFiles();
        if(arr == null) return; // SANITY CHECK
        for (File file : arr) {
            if(file.isFile()) {
                list.add(file.toURI().toURL());
            }
        }
    }

    private File getLibDir() { 
    	String[] names = new String[] { "-l", "--lib" };
        File f = getArgumentDirectory(args, names, DEF_LIB_DIR, false);
        if(f == null) throw new IllegalStateException(" *** Failed to find library directory '" + getArgumentString(args, names) + "'");
        if(!(new File(f, "internal").exists())) throw new IllegalStateException(" *** The library directory '" + getArgumentString(args, names) + "' must contain a subfolder named \"internal\"!");
        return f;
    }
    
    private boolean getImmediateStart() {
    	/*
    	 * The start flag has been deprecated.
    	 */
    	warnDeprecatedStartFlag();
		return true;
	}

	private void warnDeprecatedStartFlag() {
		for(String s : args) {
			if(s.equals("--start") || s.equals("-s")) {
				System.out.println(" *** INFO: The start flag (\"-s\" or \"--start\") has been deprecated and is no longer used.");
			}
		}
	}

    private boolean shouldUseRootLoader() {
		for(String s : args) {
			if(s.equals("--noload") || s.equals("-nl")) return true;
		}
		return false;
	}
    
    private File getConfDir() {
    	String[] names = new String[] { "-c", "--conf" };
        File f = getArgumentDirectory(args, names, DEF_CONF_DIR, false);
        if(f == null) throw new IllegalStateException(" *** Failed to find configuration directory '" + getArgumentString(args, names) + "'");
        return f;
    }
    
    private File getGameDir() {
    	String[] names = new String[] { "-g", "--game" };
        File f = getArgumentDirectory(args, names, DEF_GAME_DIR, false);
        if(f == null) throw new IllegalStateException(" *** Failed to find game deployment directory '" + getArgumentString(args, names) + "'");
        return f;
    }
    
    private File getWorkDir() {
    	String[] names = new String[] { "-w", "--work" };
        File f = getArgumentDirectory(args, names, DEF_WORK_DIR, true);
        if(f == null) throw new IllegalStateException(" *** Failed to find or create work directory '" + getArgumentString(args, names) + "'");
        return f;
    }
    
    /*private File getServerDir() {
        File f = getArgumentDirectory(args, new String[] { "-sh", "--server-home" }, DEF_SERVER_DIR);
        if(f == null) throw new IllegalStateException(" *** Failed to find server home directory!");
        return f;
    }*/

    private File getArgumentDirectory(String[] arr, String[] names, String def, boolean mayCreate) {
        String dir = getArgumentString(arr, names);
        return (dir == null ? resolveDir(def, mayCreate) : resolveDir(dir, mayCreate));
    }

    private File resolveDir(String dir, boolean mayCreate) {
        File f = new File(dir);
        if(f.isAbsolute()) return checkDirExists(f, mayCreate);
        else {
            File root = new File(System.getProperty("user.dir"));
            f = new File(root, dir);
            return checkDirExists(f, mayCreate);
        }
    }

    private File checkDirExists(File f, boolean mayCreate) {
        if(!f.exists() && (!mayCreate || !f.mkdir())) return null;
        if(f.isFile()) return null;
        return f;
    }

    private String getArgumentString(String[] args, String[] names) {
        for (int i = 0; i < args.length; i++) {
            for (String name : names) {
                if(name.equals(args[i])) {
                    if(i + 1 < args.length) return args[i + 1];
                    else return null;
                }
            }
        }
        return null;
    }

    private static void printHelp() {
        System.out.println("invocation:");
        System.out.println("");
        System.out.println("\tjava com.game.server.Server [commands]");
        System.out.println("");
        System.out.println("commands:");
        System.out.println("");
        printCommand("-h | --help", "display help", 3);
        printCommand("-c <dir>  | --conf <dir>", "specify config directory", 2);
        printCommand("-g <dir>  | --game <dir>", "specify game directory", 2);
        printCommand("-d <dir>  | --data <dir>", "specify data directory", 2);
        printCommand("-n <node> | --node <nodes>", "specify node to start, comma separated list (<nodetype>:<nodeid>)", 2);
        printCommand("-l <dir>  | --lib <dir>", "specify library directory", 2);
        //printCommand("-sh <dir> | --server-home <dir>", "specify server home directory", 1);
        //printCommand("-nl | --noload ", "ingore class loading", 3);
        System.out.println("");
        System.out.println("node types: client|game|master|singleton");
        System.out.println("");
    }

    private static void printCommand(String c, String desc, int tabs) {
        String t = "";
        for(int i = 0; i < tabs; i++) {
            t += "\t";
        }
        System.out.println("\t" + c + t + desc);
    }

    private static boolean isHelp(String[] arr) {
        String s = (arr.length > 0 ? arr[0] : "");
        return s.equals("-h") || s.equals("--help") || s.equals("?");
    }

    private void addShutdownHook(final Object server, Class<?> serverClass) throws SecurityException, NoSuchMethodException {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    stop();
                    destroy();
                } catch (Exception e) {
                    System.err.println(" *** Failed to destroy server instance: ");
                    e.printStackTrace();
                } 
            }
        }, "shutdown hook"));
    }

    private void startServer() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = getStartMethod(serverClass);
        method.invoke(server, (Object[])null);
    }
    
    private void stopServer() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = getStopMethod(serverClass);
        method.invoke(server, (Object[])null);
    }
    
    private void destroyServer() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = getDestroyMethod(serverClass);
        method.invoke(server, (Object[])null);
    }
    
    private void initServer() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = getInitMethod(serverClass);
        InitFolders folders = new InitFolders(confDir, gameDir, libDir, workDir);
        method.invoke(server, new Object[] { id, folders, sharedLoader, nodes });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Method getInitMethod(Class serverClass) throws SecurityException, NoSuchMethodException {
        return serverClass.getMethod(INIT_METHOD_NAME, INIT_METHOD_PARAMS);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Method getStartMethod(Class serverClass) throws SecurityException, NoSuchMethodException {
        return serverClass.getMethod(START_METHOD_NAME, (Class[])null);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Method getStopMethod(Class serverClass) throws SecurityException, NoSuchMethodException {
        return serverClass.getMethod(STOP_METHOD_NAME, (Class[])null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Method getDestroyMethod(Class serverClass) throws SecurityException, NoSuchMethodException {
        return serverClass.getMethod(DESTROY_METHOD_NAME, (Class[])null);
    }
}
