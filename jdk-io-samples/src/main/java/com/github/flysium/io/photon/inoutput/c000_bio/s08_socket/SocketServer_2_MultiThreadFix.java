/*
 * Copyright 2018-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.flysium.io.photon.inoutput.c000_bio.s08_socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * web服务器模拟---多线程+线程池 响应方式
 *
 * @author Sven Augustus
 * @version 2017年1月12日
 */
public class SocketServer_2_MultiThreadFix {

	private static final int PORT = 9090;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		java.net.ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORT);

			/**
			 * 方式1：利用原有封装api
			 */
			java.util.concurrent.ExecutorService executorService = Executors.newFixedThreadPool(5);
			/**
			 * 方式2：自定义ThreadPoolExecutor
			 */
			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 15, 3,
					TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(5),
					new ThreadPoolExecutor.CallerRunsPolicy());

			while (true) {
				final Socket client = serverSocket.accept();

				// executorService.submit(new ClientHandler(client));

				threadPoolExecutor.execute(new ClientHandler(client));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class ClientHandler implements Runnable {

		private Socket client;

		public ClientHandler(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			// System.out.println("ServerSocket服务器接收到一个终端" + client.getInetAddress() +
			// "信号...");
			BufferedReader br = null;
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				br = new BufferedReader(new InputStreamReader(client.getInputStream()));

				// String msg = br.readLine();
				StringBuffer stringBuffer = new StringBuffer();
				CharBuffer charBuffer = CharBuffer.allocate(1024);
				while (br.read(charBuffer) > 0) {
					charBuffer.flip();
					stringBuffer.append(charBuffer.toString());
				}
				String msg = stringBuffer.toString();
				System.out.println("收到" + client.getInetAddress() + "发送的: " + msg);

				try {
					client.shutdownInput();
				} catch (Exception e) {
					e.printStackTrace();
				}
				/**
				 * 模拟业务逻辑处理时间耗时
				 */
				Thread.sleep(1000);

				bw.write("您好，已收到您发的消息[" + msg + "]");
				bw.flush();
				try {
					client.shutdownOutput();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
