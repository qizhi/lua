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
package com.cubeia.firebase.server.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.api.util.StringList;
import com.cubeia.util.IoUtil;

/**
 * This helper class takes a jgroups config and returns the jgroups 
 * xml configuration file as a string, ready to write to disc or include
 * elsewhere.
 * 
 * @author Lars J. Nilsson
 */
public class JGroupsConfigurator {
	
	private final static String UDP_STACK = "udpJGroupsConfig.xml";
	private final static String TCP_NIO_STACK = "tcpNioJGroupsConfig.xml";
	private final static String TCP_STACK = "tcpJGroupsConfig.xml";
	
	//private final static String UDP_24_STACK = "udp2.4JGroupsConfig.xml";
	//private final static String TCP_NIO_24_STACK = "tcpNio2.4JGroupsConfig.xml";
	//private final static String TCP_24_STACK = "tcp2.4JGroupsConfig.xml";
	
	
	/**
	 * This method create a JGroups config XML file and returns it as a string. The
	 * given config must contain an mcast address, otherwise an illegal argument 
	 * exception will be thrown.
	 * 
	 * <p>The bind address may be null, or may have -1 as a port number, indicating
	 * a random port.</p>
	 * 
	 * <p>Will use default UDP jgroups stack.</p>
	 * 
	 * @param config The configuration, must not be null
	 * @param bindAddres Optional bind address, may be null
	 * @param bindInterface Optional bind interface (eg 'eth1'), or null
	 * @return The XML config as a string, never null
	 * @throws IOException
	 */
	public static final String toXMLCharacters(JGroupsConfig config, SocketAddress bindAddress, String bindInterface) throws IOException {
		return toXMLCharacters(config, bindAddress, bindInterface, (config == null ? TransportType.UDP : config.getTpType()));
	}
	


	
	// --- PRIVATE METHODS --- //
	
	private static String toXMLCharacters(JGroupsConfig config, SocketAddress bindAddress, String bindInterface, TransportType tp) throws IOException {
		Arguments.notNull(tp, "transport type");
		String path = getTpConfigPath(tp);
		return toXMLCharacters(config, bindAddress, bindInterface, path);
	}

	private static String getTpConfigPath(TransportType tp) {
		if(tp.equals(TransportType.UDP)) {
			return UDP_STACK;
		} else if(tp.equals(TransportType.TCP_NIO)) {
			return TCP_NIO_STACK;
		} else {
			return TCP_STACK;
		}
	}
	
	/**
	 * This method create a JGroups config XML file and returns it as a string. The
	 * given config must contain an mcast address, otherwise an illegal argument 
	 * exception will be thrown.
	 * 
	 * <p>The bind address may be null, or may have -1 as a port number, indicating
	 * a random port.
	 * 
	 * @param config The configuration, must not be null
	 * @param bindAddres Optional bind address, may be null
	 * @param bindInterface The bind interface name, or null
	 * @param stack, the name of the stack file
	 * @return The XML config as a string, never null
	 * @throws IOException
	 */
	private static final String toXMLCharacters(JGroupsConfig config, SocketAddress bindAddress, String bindInterface, String stack) throws IOException {
		Arguments.notNull(config, "config");
		verifyMCastAddress(config);
		String s = readConfig(stack);
		s = doDefaultReplace(s, config);
		s = doCompressReplace(s, config);
		s = doStateTransferReplace(s, config);
		s = doMainPoolReplace(s, config);
		s = doOobPoolReplace(s, config);
		s = doFdSockReplace(s, config);
		s = doFdReplace(s, config);
		s = doBindReplace(s, bindInterface, bindAddress, config);
		s = doTcpReplace(s, config);
		s = doFcReplcace(s, config);
		return s;
	}
	
	private static String doFcReplcace(String s, JGroupsConfig config) {
		return s.replace("${fc-max-credits}", String.valueOf(config.getFcMaxCredits()));
	}

	private static String doTcpReplace(String s, JGroupsConfig config) {
		// done in binding: s = s.replace("${start-port}", String.valueOf(config.getTcpStartPort()));
		s = s.replace("${tcp-reader-threads}", String.valueOf(config.getTcpReaderThreads()));
		s = s.replace("${tcp-writer-threads}", String.valueOf(config.getTcpWriterThreads()));
		s = s.replace("${tcp-processor-threads}", String.valueOf(config.getTcpProcessorThreads()));
		s = s.replace("${tcp-processor-min-threads}", String.valueOf(config.getTcpProcessorMinThreads()));
		s = s.replace("${tcp-processor-max-threads}", String.valueOf(config.getTcpProcessorMaxThreads()));
		s = s.replace("${tcp-processor-queue-size}", String.valueOf(config.getTcpProcessorQueueSize()));
		if(config.getUseTcpUnicast()) {
			 s = s.replace("${unicast}", "<UNICAST loopback=\" " + String.valueOf(config.getLoopback()) + "\" timeout=\"" + toString(config.getTcpUnicastTimeouts()) + "\" eager_lock_release=\"true\" />"); // immediate_ack=\"true\" />");
		} else {
			s = s.replace("${unicast}", "");
		}
		return s;
	}

	private static String toString(StringList list) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			b.append(list.get(i));
			if(i + 1 < list.size()) {
				b.append(",");
			}
		}
		return b.toString();
	}

	private static String doBindReplace(String s, String bindInt, SocketAddress bindAd, JGroupsConfig config) {
		s = doBindAddrReplace(s, bindAd, config);
		return doBindInterfaceReplace(s, bindInt);
	}

	private static String doBindInterfaceReplace(String s, String bindInt) {
		return (bindInt == null ? s.replace("${bind-interface}", "") : s.replace("${bind-interface}", "bind_interface=\"" + bindInt + "\""));
	}

	private static String doBindAddrReplace(String s, SocketAddress ad,
			JGroupsConfig config) {
		if(ad == null) {
			if(!isTcp(config)) {
				s = s.replace("${bind-address}", "");
			} else {
				s = s.replace("${bind-address}", "start_port=\"" + config.getTcpStartPort() + "\" end_port=\"" + config.getTcpEndPort() + "\"");
			}
			s = s.replace("${mping-bind-address}", "");
			return s;
		} else if(ad.getPort() == -1) {
			if(!isTcp(config)) {
				// udp+no port
				s = s.replace("${bind-address}", "bind_addr=\"" + ad.getHost().getHostAddress() + "\"");
			} else {
				// tcp+startport+endport
				s = s.replace("${bind-address}", "bind_addr=\"" + ad.getHost().getHostAddress() + "\" start_port=\"" + config.getTcpStartPort() + "\" end_port=\"" + config.getTcpEndPort() + "\"");
			}
			s = s.replace("${mping-bind-address}", "bind_addr=\"" + ad.getHost().getHostAddress() + "\"");
			return s;
		} else {
			if(!isTcp(config)) {
				// udp+port
				s = s.replace("${bind-address}", "bind_addr=\"" + ad.getHost().getHostAddress() + "\" bind_port=\"" + ad.getPort() + "\"");
			} else {
				//tcp+port
				s = s.replace("${bind-address}", "bind_addr=\"" + ad.getHost().getHostAddress() + "\" start_port=\"" + ad.getPort() + "\"");	
			}
			s = s.replace("${mping-bind-address}", "bind_addr=\"" + ad.getHost().getHostAddress() + "\"");
			return s;
		}
	}

	private static boolean isTcp(JGroupsConfig config) {
		return !config.getTpType().equals(TransportType.UDP);
	}

	private static String doCompressReplace(String s, JGroupsConfig config) {
		if(!config.getUseCompress()) {
			return s.replace("${compress}", "");
		} else {
			StringBuilder b = new StringBuilder("<COMPRESS min_size=\"");
			b.append(config.getCompressMinSize()).append("\" compression_level=\"");
			b.append(config.getCompressLevel()).append("\" />"); // down_thread=\"false\" up_thread=\"false\" />");
			return s.replace("${compress}", b.toString());
		}
	}
	
	private static String doFdReplace(String s, JGroupsConfig conf) {
		if(!conf.getUseFd()) {
			return s.replace("${fd}", "");
		} else {
			StringBuilder b = new StringBuilder("<FD timeout=\"");
			b.append(conf.getFdTimeout()).append("\" max_tries=\"");
			b.append(conf.getFdRetries()).append("\" shun=\"");
			b.append(conf.getFdShun()).append("\" />"); //down_thread=\"false\" up_thread=\"false\" />");
			return s.replace("${fd}", b.toString());
		}
	}

	private static String doFdSockReplace(String s, JGroupsConfig conf) {
		if(!conf.getUseFdSock()) {
			return s.replace("${fd-sock}", "");
		} else {
			return s.replace("${fd-sock}", "<FD_SOCK />"); //down_thread=\"false\" up_thread=\"false\" />");
		}
	}
	
	private static String doStateTransferReplace(String s, JGroupsConfig conf) {
		if(!conf.getUseStateTransfer()) {
			return s.replace("${state-transfer}", "");
		} else {
			return s.replace("${state-transfer}", "<pbcast.STATE_TRANSFER />"); //down_thread=\"false\" up_thread=\"false\" />");
		}
	}

	private static void verifyMCastAddress(JGroupsConfig config) {
		if(config.getMcastAddress() == null) {
			throw new IllegalArgumentException("Missing mcast address in config.");
		}
	}

	private static String doMainPoolReplace(String s, JGroupsConfig config) {
		return doThreadPoolReplace(s, "main", config.getMainPoolProperties(), config.getMainRejectionPolicy());
	}
	
	private static String doOobPoolReplace(String s, JGroupsConfig config) {
		return doThreadPoolReplace(s, "oob", config.getOobPoolProperties(), config.getOobRejectionPolicy());
	}
	
	private static String doThreadPoolReplace(String s, String pref, ThreadPoolProperties props, RejectionPolicy policy) {
		s = s.replace("${" + pref + "-core-size}", String.valueOf(props.getCoreSize()));
		s = s.replace("${" + pref + "-max-size}", String.valueOf(props.getMaxSize()));
		s = s.replace("${" + pref + "-timeout}", String.valueOf(props.getTimeout()));
		s = s.replace("${" + pref + "-use-queue}", String.valueOf(props.isQueueingEnable()));
		s = s.replace("${" + pref + "-queue-size}", String.valueOf(props.getQueueSize()));
		s = s.replace("${" + pref + "-rejection-policy}", policy.getName());
		return s;
	}

	private static String doDefaultReplace(String s, JGroupsConfig config) {
		s = s.replace("${mping-timeout}", String.valueOf(config.getMpingTimeout()));
		s = s.replace("${use-concurrent-stack}", String.valueOf(config.getUseConcurrentStack()));
		s = s.replace("${loopback}", String.valueOf(config.getLoopback()));
		s = s.replace("${print-gms-address}", String.valueOf(config.getPrintGmsAddress()));
		s = s.replace("${discard-nakacks}", String.valueOf(config.getDiscardDeliveredNakAcks()));
		s = s.replace("${frag-size}", String.valueOf(config.getFragSize()));
		s = s.replace("${max-bundle-size}", String.valueOf(config.getMaxBundleSize()));
		s = s.replace("${use-bundling}", String.valueOf(config.getUseBundling()));
		// s = s.replace("${use-mcast}", String.valueOf(config.getUseMcast()));
		s = s.replace("${stable-delay}", String.valueOf(config.getStableDelay()));
		s = s.replace("${stable-max-bytes}", String.valueOf(config.getStableMaxBytes()));
		s = s.replace("${stable-avarage-gossip}", String.valueOf(config.getStableAvarageGossip()));
		SocketAddress ad = config.getMcastAddress();
		s = s.replace("${mcast-address}", ad.getHost().getHostAddress());
		s = s.replace("${mcast-port}", String.valueOf(ad.getPort()));
		return s;
	}

	private static String readConfig(String stack) throws IOException {
		Package pack = JGroupsConfigurator.class.getPackage();
		String name = pack.getName().replace('.', '/') + "/" + stack;
		InputStream in = JGroupsConfigurator.class.getClassLoader().getResourceAsStream(name);
		if(in == null) throw new FileNotFoundException("Default config '" + name + " not found in class path.");
		try {
			return IoUtil.readAsString(in);
		} finally {
			IoUtil.safeClose(in);
		}
	}
	
}
