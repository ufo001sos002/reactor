/*
 * Copyright (c) 2011-2015 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.io.net;

import reactor.Environment;
import reactor.core.support.Assert;
import reactor.fn.Function;
import reactor.io.buffer.Buffer;
import reactor.io.net.http.HttpClient;
import reactor.io.net.http.HttpServer;
import reactor.io.net.impl.netty.http.NettyHttpClient;
import reactor.io.net.impl.netty.http.NettyHttpServer;
import reactor.io.net.impl.netty.tcp.NettyTcpClient;
import reactor.io.net.impl.netty.tcp.NettyTcpServer;
import reactor.io.net.impl.netty.udp.NettyDatagramServer;
import reactor.io.net.impl.zmq.tcp.ZeroMQTcpClient;
import reactor.io.net.impl.zmq.tcp.ZeroMQTcpServer;
import reactor.io.net.tcp.TcpClient;
import reactor.io.net.tcp.TcpServer;
import reactor.io.net.udp.DatagramServer;
import reactor.rx.Streams;

/**
 * A Streams add-on to work with network facilities from reactor-net, e.g.:
 * <p>
 * <pre>
 * {@code
 * //echo server
 * NetStreams.tcpServer(1234).pipeline( connection ->
 *   connection
 * )
 *
 * NetStreams.tcpClient(1234).connect( output ->
 *   output
 *   .sendAndReceive(Buffer.wrap("hello"))
 *   .onSuccess(log::info)
 * )
 *
 * NetStreams.tcpServer(spec -> spec.listen(1234)).pipeline( intput, output -> {
 *      input.consume(log::info);
 *     Streams.period(1l).subscribe(output);
 * })
 *
 * NetStreams.tcpClient(spec -> spec.codec(kryoCodec)).connect( output, input -> {
 *   input.consume(log::info);
 *   output.send("hello");
 * })
 *
 * }
 * </pre>
 *
 * @author Stephane Maldini
 */
public class NetStreams extends Streams {

	public static final int    DEFAULT_PORT         = 12012;
	public static final String DEFAULT_BIND_ADDRESS = "127.0.0.1";
	public static final Class<? extends TcpServer>      DEFAULT_TCP_SERVER_TYPE;
	public static final Class<? extends TcpClient>      DEFAULT_TCP_CLIENT_TYPE;
	public static final Class<? extends HttpServer>     DEFAULT_HTTP_SERVER_TYPE;
	public static final Class<? extends HttpClient>     DEFAULT_HTTP_CLIENT_TYPE;
	public static final Class<? extends DatagramServer> DEFAULT_UDP_SERVER_TYPE;

	private NetStreams() {
	}

	// TCP

	/**
	 * @return
	 */
	public static TcpServer<Buffer, Buffer> tcpServer() {
		return tcpServer(DEFAULT_BIND_ADDRESS);
	}

	/**
	 * @param port
	 * @return
	 */
	public static TcpServer<Buffer, Buffer> tcpServer(int port) {
		return tcpServer(DEFAULT_BIND_ADDRESS, port);
	}

	/**
	 * @param bindAddress
	 * @return
	 */
	public static TcpServer<Buffer, Buffer> tcpServer(String bindAddress) {
		return tcpServer(bindAddress, DEFAULT_PORT);
	}

	/**
	 * @param bindAddress
	 * @param port
	 * @return
	 */
	public static TcpServer<Buffer, Buffer> tcpServer(final String bindAddress, final int port) {
		return tcpServer(new Function<Spec.TcpServer<Buffer, Buffer>, Spec.TcpServer<Buffer, Buffer>>() {
			@Override
			public Spec.TcpServer<Buffer, Buffer> apply(Spec.TcpServer<Buffer, Buffer> serverSpec) {
				if(Environment.alive()){
					serverSpec.env(Environment.get());
				}
				return serverSpec.listen(bindAddress, port);
			}
		});
	}

	/**
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> TcpServer<IN, OUT> tcpServer(
			Function<? super Spec.TcpServer<IN, OUT>, ? extends Spec.TcpServer<IN, OUT>> configuringFunction
	) {
		return tcpServer(DEFAULT_TCP_SERVER_TYPE, configuringFunction);
	}

	/**
	 * @param serverFactory
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> TcpServer<IN, OUT> tcpServer(
			Class<? extends TcpServer> serverFactory,
			Function<? super Spec.TcpServer<IN, OUT>, ? extends Spec.TcpServer<IN, OUT>> configuringFunction
	) {
		return configuringFunction.apply(new Spec.TcpServer<IN, OUT>(serverFactory)).get();
	}


	/**
	 * @return
	 */
	public static TcpClient<Buffer, Buffer> tcpClient() {
		return tcpClient(DEFAULT_BIND_ADDRESS);
	}

	/**
	 * @param bindAddress
	 * @return
	 */
	public static TcpClient<Buffer, Buffer> tcpClient(String bindAddress) {
		return tcpClient(bindAddress, DEFAULT_PORT);
	}

	/**
	 * @param port
	 * @return
	 */
	public static TcpClient<Buffer, Buffer> tcpClient(int port) {
		return tcpClient(DEFAULT_BIND_ADDRESS, port);
	}

	/**
	 * @param bindAddress
	 * @param port
	 * @return
	 */
	public static TcpClient<Buffer, Buffer> tcpClient(final String bindAddress, final int port) {
		return tcpClient(new Function<Spec.TcpClient<Buffer, Buffer>, Spec.TcpClient<Buffer, Buffer>>() {
			@Override
			public Spec.TcpClient<Buffer, Buffer> apply(Spec.TcpClient<Buffer, Buffer> clientSpec) {
				if(Environment.alive()){
					clientSpec.env(Environment.get());
				}
				return clientSpec.connect(bindAddress, port);
			}
		});
	}

	/**
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> TcpClient<IN, OUT> tcpClient(
			Function<? super Spec.TcpClient<IN, OUT>, ? extends Spec.TcpClient<IN, OUT>> configuringFunction
	) {
		return tcpClient(DEFAULT_TCP_CLIENT_TYPE, configuringFunction);
	}

	/**
	 * @param clientFactory
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> TcpClient<IN, OUT> tcpClient(
			Class<? extends TcpClient> clientFactory,
			Function<? super Spec.TcpClient<IN, OUT>, ? extends Spec.TcpClient<IN, OUT>> configuringFunction
	) {
		return configuringFunction.apply(new Spec.TcpClient<IN, OUT>(clientFactory)).get();
	}

	// HTTP

	/**
	 * @return
	 */
	public static HttpServer<Buffer, Buffer> httpServer() {
		return httpServer(DEFAULT_BIND_ADDRESS);
	}

	/**
	 * @param bindAddress
	 * @return
	 */
	public static <IN, OUT> HttpServer<IN, OUT> httpServer(String bindAddress) {
		return httpServer(bindAddress, DEFAULT_PORT);
	}

	/**
	 * @param port
	 * @return
	 */
	public static <IN, OUT> HttpServer<IN, OUT> httpServer(int port) {
		return httpServer(DEFAULT_BIND_ADDRESS, port);
	}

	/**
	 * @param bindAddress
	 * @param port
	 * @return
	 */
	public static <IN, OUT> HttpServer<IN, OUT> httpServer(final String bindAddress, final int port) {
		return httpServer(new Function<Spec.HttpServer<IN, OUT>, Spec.HttpServer<IN, OUT>>() {
			@Override
			public Spec.HttpServer<IN, OUT> apply(Spec.HttpServer<IN, OUT> serverSpec) {
				if(Environment.alive()){
					serverSpec.env(Environment.get());
				}
				return serverSpec.listen(bindAddress, port);
			}
		});
	}

	/**
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> HttpServer<IN, OUT> httpServer(
			Function<? super Spec.HttpServer<IN, OUT>, ? extends Spec.HttpServer<IN, OUT>> configuringFunction
	) {
		return httpServer(DEFAULT_HTTP_SERVER_TYPE, configuringFunction);
	}

	/**
	 * @param serverFactory
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> HttpServer<IN, OUT> httpServer(
			Class<? extends HttpServer> serverFactory,
			Function<? super Spec.HttpServer<IN, OUT>, ? extends Spec.HttpServer<IN, OUT>> configuringFunction
	) {
		return configuringFunction.apply(new Spec.HttpServer<IN, OUT>(serverFactory)).get();
	}


	/**
	 * @return
	 */
	public static HttpClient<Buffer, Buffer> httpClient() {
		return httpClient(DEFAULT_BIND_ADDRESS);
	}

	/**
	 * @param bindAddress
	 * @return
	 */
	public static  HttpClient<Buffer, Buffer> httpClient(String bindAddress) {
		return httpClient(bindAddress, DEFAULT_PORT);
	}

	/**
	 * @param port
	 * @return
	 */
	public static HttpClient<Buffer, Buffer> httpClient(int port) {
		return httpClient(DEFAULT_BIND_ADDRESS, port);
	}

	/**
	 * @param bindAddress
	 * @param port
	 * @return
	 */
	public static HttpClient<Buffer, Buffer> httpClient(final String bindAddress, final int port) {
		return httpClient(new Function<Spec.HttpClient<Buffer, Buffer>, Spec.HttpClient<Buffer, Buffer>>() {
			@Override
			public Spec.HttpClient<Buffer, Buffer> apply(Spec.HttpClient<Buffer, Buffer> clientSpec) {
				if(Environment.alive()){
					clientSpec.env(Environment.get());
				}
				return clientSpec.connect(bindAddress, port);
			}
		});
	}

	/**
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> HttpClient<IN, OUT> httpClient(
			Function<? super Spec.HttpClient<IN, OUT>, ? extends Spec.HttpClient<IN, OUT>> configuringFunction
	) {
		return httpClient(DEFAULT_HTTP_CLIENT_TYPE, configuringFunction);
	}

	/**
	 * @param clientFactory
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> HttpClient<IN, OUT> httpClient(
			Class<? extends HttpClient> clientFactory,
			Function<? super Spec.HttpClient<IN, OUT>, ? extends Spec.HttpClient<IN, OUT>> configuringFunction
	) {
		return configuringFunction.apply(new Spec.HttpClient<IN, OUT>(clientFactory)).get();
	}

	// UDP

	/**
	 * @return
	 */
	public static DatagramServer<Buffer, Buffer> udpServer() {
		return udpServer(DEFAULT_BIND_ADDRESS);
	}

	/**
	 * @param bindAddress
	 * @return
	 */
	public static DatagramServer<Buffer, Buffer> udpServer(String bindAddress) {
		return udpServer(bindAddress, DEFAULT_PORT);
	}

	/**
	 * @param port
	 * @return
	 */
	public static DatagramServer<Buffer, Buffer> udpServer(int port) {
		return udpServer(DEFAULT_BIND_ADDRESS, port);
	}

	/**
	 * @param bindAddress
	 * @param port
	 * @return
	 */
	public static DatagramServer<Buffer, Buffer> udpServer(final String bindAddress, final int port) {
		return udpServer(new Function<Spec.DatagramServer<Buffer, Buffer>, Spec.DatagramServer<Buffer, Buffer>>() {
			@Override
			public Spec.DatagramServer<Buffer, Buffer> apply(Spec.DatagramServer<Buffer, Buffer> serverSpec) {
				if(Environment.alive()){
					serverSpec.env(Environment.get());
				}
				return serverSpec.listen(bindAddress, port);
			}
		});
	}

	/**
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> DatagramServer<IN, OUT> udpServer(
			Function<? super Spec.DatagramServer<IN, OUT>, ? extends Spec.DatagramServer<IN, OUT>> configuringFunction
	) {
		return udpServer(DEFAULT_UDP_SERVER_TYPE, configuringFunction);
	}

	/**
	 * @param serverFactory
	 * @param configuringFunction
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> DatagramServer<IN, OUT> udpServer(
			Class<? extends DatagramServer> serverFactory,
			Function<? super Spec.DatagramServer<IN, OUT>, ? extends Spec.DatagramServer<IN, OUT>> configuringFunction
	) {
		return configuringFunction.apply(new Spec.DatagramServer<IN, OUT>(serverFactory)).get();
	}


	/**
	 * Utils to read the ChannelStream underlying channel
	 */

	@SuppressWarnings("unchecked")
	public static <E, IN, OUT> E delegate(ChannelStream<IN, OUT> channelStream) {
		return (E)delegate(channelStream, Object.class);
	}

	@SuppressWarnings("unchecked")
	public static <E, IN, OUT> E delegate(ChannelStream<IN, OUT> channelStream, Class<E> clazz) {
		Assert.isTrue(
				clazz.isAssignableFrom(channelStream.delegate().getClass()),
				"Underlying channel is not of the given type: " + clazz.getName()
		);

		return (E) channelStream.delegate();
	}

	/**
	 * INTERNAL CLASSPATH INIT
	 */

	static {
		boolean hasNetty = false;
		try {
			Class.forName("io.netty.channel.Channel");
			hasNetty = true;
		} catch (ClassNotFoundException cnfe) {
			//IGNORE
		}
		if (hasNetty) {
			DEFAULT_TCP_SERVER_TYPE = NettyTcpServer.class;
			DEFAULT_TCP_CLIENT_TYPE = NettyTcpClient.class;
			DEFAULT_UDP_SERVER_TYPE = NettyDatagramServer.class;
			DEFAULT_HTTP_SERVER_TYPE = NettyHttpServer.class;
			DEFAULT_HTTP_CLIENT_TYPE = NettyHttpClient.class;
		} else {
			boolean hasZMQ = false;

			DEFAULT_UDP_SERVER_TYPE = null;
			DEFAULT_HTTP_SERVER_TYPE = null;
			DEFAULT_HTTP_CLIENT_TYPE = null;

			try {
				Class.forName("org.zeromq.ZMQ");
				hasZMQ = true;
			} catch (ClassNotFoundException cnfe) {
				//IGNORE
			}


			if (hasZMQ) {
				DEFAULT_TCP_SERVER_TYPE = ZeroMQTcpServer.class;
				DEFAULT_TCP_CLIENT_TYPE = ZeroMQTcpClient.class;
			} else {
				DEFAULT_TCP_SERVER_TYPE = null;
				DEFAULT_TCP_CLIENT_TYPE = null;
			}
		}

	}

	/**
	 * @return a Specification to configure and supply a Reconnect handler
	 */
	static public Spec.IncrementalBackoffReconnect backoffReconnect(){
		return new Spec.IncrementalBackoffReconnect();
	}
}
