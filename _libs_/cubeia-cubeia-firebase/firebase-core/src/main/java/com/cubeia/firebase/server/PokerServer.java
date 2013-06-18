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
package com.cubeia.firebase.server;

import java.io.File;
import java.security.SecureRandom;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.server.instance.ServerInstance;
import com.game.server.bootstrap.BootstrapNode;
import com.game.server.bootstrap.InitFolders;
import com.game.server.bootstrap.SharedClassLoader;

/**
 * Simple test class for running a single server instance with all
 * possible nodes using default ids within a pre-setup environment, 
 * such as eclipse.
 * 
 * One argument pointing with the server id is accepted.
 * 
 * @author lars.nilsson
 */
public class PokerServer {

    /**
     * @param args
     */
    public static void main(String[] args) { 
    	try {
    		ceckLog4JConfig(args);
	    	System.setProperty("eclipse", "true");
	    	ServerInstance inst = new ServerInstance();
	    	inst.addTrustedSarLocation(new File(new File(Constants.FIREBASE_HOME), "dist/"));
	    	inst.init(checkId(args), checkInitFolder(args), new SharedClassLoader(PokerServer.class.getClassLoader()), new BootstrapNode[] { new BootstrapNode("singleton", null) });
	    	inst.start();
	    	Hook h = new Hook(inst);
	    	// openGuiControl(h);
	    	Runtime.getRuntime().addShutdownHook(h);
    	} catch (SystemException e) {
			e.printStackTrace();
		}
    }
    
	private static void ceckLog4JConfig(String[] args) {
		String path = "conf/";
		File test = new File(path, "log4j.xml");
		if(test.exists()) {
			DOMConfigurator.configureAndWatch(test.getAbsolutePath(), 5000);
		} else {
			test = new File(path, "log4j.properties");
			if(test.exists()) {
				PropertyConfigurator.configureAndWatch(test.getAbsolutePath(), 5000);
			}
		}
	}
    
	private static InitFolders checkInitFolder(String[] args) {
		return new InitFolders(new File("conf/"), new File("game/"), new File("lib/"), new File("work/"));
	}

	/*private static void openGuiControl(final Hook h) {
		JFrame frame = new JFrame("Server Stopper");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(100, 80);
		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());
		JButton button = new JButton("Stop");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				h.run();
			}
		});
		content.add(button);
		frame.setVisible(true);
		frame.requestFocus();
	}*/

	private static String checkId(String[] args) {
		if(args.length > 0) return args[0];
		return String.valueOf(new SecureRandom().nextInt());
	}
	
	private static class Hook extends Thread {
		
		private boolean isRun;
		private ServerInstance inst;
		
		private Hook(ServerInstance inst) {
			this.inst = inst;
		}
		
		@Override
		public synchronized void run() {
			if(!isRun) {
				isRun = true;
				inst.stop();
    			inst.destroy();
			}
		}
	}
}

