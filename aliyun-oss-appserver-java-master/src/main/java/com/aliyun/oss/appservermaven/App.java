package com.aliyun.oss.appservermaven;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class App {
	public static void main(String[] args) throws Exception {
		// 开启Server，端口号为8080
		Server server = new Server();
		int port = 8080; // 默认端口是：8080
		if (args.length > 0) {
			port = Integer.parseInt(args[0]); // 通过命令行参数指定监听 端口。
		}

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		System.out.println("App server is running on port : " + port);

		server.setConnectors(new Connector[] { connector });
		// 设置handler根路径。
		ServletContextHandler webApiContext = new ServletContextHandler();
		webApiContext.setContextPath("/");
		webApiContext.addServlet(new ServletHolder(CallbackServer.class), "/*");
		webApiContext.setSessionHandler(new SessionHandler());

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { webApiContext, new DefaultHandler() });
		// 增加handlers并启动server。
		server.setHandler(handlers);
		server.start();
		server.join();
	}
}
