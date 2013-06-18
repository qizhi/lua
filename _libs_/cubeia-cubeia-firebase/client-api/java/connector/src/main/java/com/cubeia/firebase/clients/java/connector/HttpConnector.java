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
package com.cubeia.firebase.clients.java.connector;

import static com.cubeia.firebase.clients.java.connector.HttpConstants.HANDSHAKE_HTTP_HEADER;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxJsonSerializer;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;

@Deprecated
public class HttpConnector extends ConnectorBase {
	
	private final StyxJsonSerializer serializer = new StyxJsonSerializer(new ProtocolObjectFactory());
	private final Logger log = Logger.getLogger(getClass());
	
	private HttpClient client;
	
	private final String host;
	private final int port;
	private final String path;
	
	private HttpExchange exchange;
	
	public HttpConnector(String host, int port, String path, boolean useHandshake, int handshakeSignature) {
		super(useHandshake, handshakeSignature);
		this.host = host;
		this.port = port;
		this.path = path;
	}

	@Override
	public void send(ProtocolObject packet) {
		exchange.offer(packet);
	}

	@Override
	public void connect() throws IOException, GeneralSecurityException {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", port, PlainSocketFactory.getSocketFactory()));
		ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
		client = new DefaultHttpClient(cm);
		// StringWriter w = new StringWriter();
		// new Exception().printStackTrace(new PrintWriter(w));
		exchange = new HttpExchange(); // w.toString());
		exchange.start();
	}

	@Override
	public void disconnect() {
		client.getConnectionManager().shutdown();
		exchange.halt();
	}

	@Override
	public boolean isConnected() {
		return exchange.flag.get();
	}

	
	// --- PRIVATE CLASSES --- //
	
	private class HttpExchange extends Thread {
		
		private final AtomicBoolean flag = new AtomicBoolean(true);
		// private BlockingQueue<ProtocolObject> outgoing = new LinkedBlockingQueue<ProtocolObject>();
		private AtomicReference<String> jsessionCookie = new AtomicReference<String>();
		
		private AtomicReference<HttpUriRequest> current = new AtomicReference<HttpUriRequest>();
		
		private CountDownLatch latch = new CountDownLatch(1);
		
		public void halt() {
			// System.out.println("HALT " + Thread.currentThread().getId());
			flag.set(false);
			abortCurrent();
		}
		
		public void offer(ProtocolObject o) {
			try {
				latch.await();
			} catch (InterruptedException e) { }
			// System.out.println("OFFER");
			offerOutgoing(o);
			//outgoing.add(o);
			//abortCurrent();
		}
		
		@Override
		public void run() {
			// System.out.println("START " + Thread.currentThread().getId() + " " + trace);
			while(flag.get()) {
				// System.out.println("ROUND");
				// cancelCurrent();
				// offerOutgoing();
				pollIncoming();
			}
		}
		
		
		// --- PRIVATE METHODS --- //
		
		private void abortCurrent() {
			HttpUriRequest get = current.get();
			if(get != null) {
				// System.out.println("ABORT " + Thread.currentThread().getId());
				get.abort();
			}
		}

		private void pollIncoming() {
			// System.out.println("POLL " + Thread.currentThread().getId());
			HttpGet get = new HttpGet(createUri());
			checkSetCookie(get);
			try {
				current.set(get);
				// System.out.println("EXEC START");
				HttpResponse resp = client.execute(get);
				// System.out.println("EXEC + " + resp.getStatusLine().getStatusCode());
				checkForCookie(resp);
				HttpEntity entity = resp.getEntity();
				String json = EntityUtils.toString(entity); // TODO read stream instead...
				// System.out.println("JSON: " + json);
				if(json != null && json.length() > 0) {
					if(json.trim().startsWith("{")) {
						doFinalDispatch(serializer.fromJson(json));
					} else {
						for (ProtocolObject o : serializer.fromJsonList(json)) {		
							doFinalDispatch(o);
						}
					}
				} else {
					consume(resp);
				}
				current.set(null);
			} catch(InterruptedIOException e) {
				// This is an abort, just ignore...
			} catch (Exception e) {
				log.error("Failed get request", e);
			} finally {
				latch.countDown();
			}
		}

		private void checkSetCookie(HttpRequestBase get) {
			if(jsessionCookie.get() != null) {
				get.setHeader("Cookie", jsessionCookie.get());
			} else if(useHandshake) { // set only if we don't have a valid session
				get.setHeader(HANDSHAKE_HTTP_HEADER, String.valueOf(handshakeSignature));
			}
		}

		private void offerOutgoing(ProtocolObject o) {
			List<ProtocolObject> list = Collections.singletonList(o); //new LinkedList<ProtocolObject>();
			// outgoing.drainTo(list);
			if(list.size() > 0) {
				String json = serializer.toJsonList(list);
				System.out.println("POST: " + json);
				// TODO Set more headers
				HttpPost post = new HttpPost(createUri());
				checkSetCookie(post);
				// TODO Set charset etc
				try {
					post.setEntity(new StringEntity(json));
					HttpResponse resp = client.execute(post);
					checkForCookie(resp);
					consume(resp);
				} catch(Exception e) {
					log.error("Failed post", e);
				}
			}
		}

		private void consume(HttpResponse resp) throws IllegalStateException, IOException {
			EntityUtils.consume(resp.getEntity());
		}

		private void checkForCookie(HttpResponse resp) {
			Header h = resp.getFirstHeader("Set-Cookie");
			if(h != null && h.getValue().startsWith("JSESSIONID")) {
				this.jsessionCookie.set(h.getValue());
			}
		}

		private String createUri() {
			return "http://" + host + ":" + port + path;
		}

		/*private ContentExchange createExchange() {
			ContentExchange e = new ContentExchange(true) {
				
				@Override
				protected void onResponseHeader(Buffer name, Buffer value) throws IOException {
					super.onResponseHeader(name, value);
					String head = name.toString("UTF-8");
					String val = value.toString("UTF-8");
					if(head.equals("Set-Cookie") && val.startsWith("JSESSIONID")) {
						jsessionCookie.set(val);
					}
				}
				
				@Override
				protected void onResponseComplete() throws IOException {
					super.onResponseComplete();
					System.out.println("COMPLETE");
					// TODO Handle errors
					int status = getResponseStatus();
					String json = getResponseContent();
					if(json != null && json.length() > 0) {
						if(status == 200) {
							List<ProtocolObject> list = serializer.fromJsonList(json);
							for (ProtocolObject o : list) {
								System.out.println("DISPATCH: " + o);
								doFinalDispatch(o);
							}
						} else {
							System.out.println("KACK! " + json);
						}
					}
					//if(getMethod().equals("GET")) {
						synchronized(this) {
							this.notifyAll();
						}
					//}
				}
			};
			e.setAddress(new Address(host, port));
			e.setRequestURI(path);
			if(jsessionCookie.get() != null) {
				e.addRequestHeader("Cookie", jsessionCookie.get());
			}
			return e;
		}*/
		
		private void doFinalDispatch(final ProtocolObject packet) {
			dispatcher.submit(new Runnable() {
			
				public void run() {
					for (PacketListener v : listeners) {
						v.packetRecieved(packet);
					}
				}
			});
		}
		
		/*private void cancelCurrent() {
			synchronized(this) {
				if(currentExchange != null) {
					System.out.println("CANCELLING");
					currentExchange.cancel();
					currentExchange = null;
				}
			}
		}*/
	}
}
