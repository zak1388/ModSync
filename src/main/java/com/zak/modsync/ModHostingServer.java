package com.zak.modsync;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import com.sun.net.httpserver.*;

import net.minecraftforge.fml.ModList;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

public class ModHostingServer {

	private final static Logger LOGGER = LogUtils.getLogger();
	private HttpServer server;
	
	public ModHostingServer() {
	}
	
	protected static void modListServerHandler(HttpExchange t) throws IOException {
		StringBuilder response = new StringBuilder();
		ModList modList = ModList.get();
		
		response.append("{");
		
		// build mod list
		response.append("\"mods\": [");
		modList.getMods().forEach(mod -> response.append('"' + mod.getDisplayName() + '"' + ", "));
		response.delete(response.length() - 2, response.length() - 1);
		response.append("]");
		
		response.append("}");
				
		t.getResponseHeaders().set("Content-Type", "application/json;");
		t.sendResponseHeaders(200, response.length());

		OutputStream bodyStream = t.getResponseBody();
		bodyStream.write(response.toString().getBytes());
		bodyStream.close();
	}
	
	protected static void modZipHandler(HttpExchange t) {
		BufferedInputStream modZipFileInputStream = null;
		try {
			LOGGER.debug("mod zip handler says hi!");
			if (ModZipFile.getModZipFile() == null) {
				String response = "Generating zip file...";
				
				t.getResponseHeaders().set("Content-type", "text/plain");
				t.sendResponseHeaders(202, response.length());
				
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
				
				return;
			}
			
			LOGGER.debug("sending mods.zip");
			modZipFileInputStream = new BufferedInputStream(new FileInputStream(ModZipFile.getModZipFile()));
			
			t.getResponseHeaders().set("Content-Type", "application/zip");
			t.sendResponseHeaders(200, ModZipFile.getModZipFile().length());
			
			OutputStream bodyStream = t.getResponseBody();
			
			int bytesRead = 0;
            final int bufferSize = 4096;
			do {
				byte[] block = new byte[bufferSize];
				bytesRead = modZipFileInputStream.read(block);
				bodyStream.write(block, 0, bytesRead);
			} while (bytesRead == bufferSize);
			
			bodyStream.close();
			LOGGER.debug("sent mods.zip successfully");
		} catch (IOException exception) {
			LOGGER.error("Failed to send mods zip file due to an IOException: " + exception.getMessage() + " caused by " + exception.getCause());
		} finally {
			try {
				modZipFileInputStream.close();
			} catch (IOException e) {
				LOGGER.error("Failed to close mod zip file input stream");
			}
		}
	}
	
	public void run() {
		server = null;
		
		try {
			LOGGER.debug("Trying to create http server");
			int port = 23682;
			server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/mods", ModHostingServer::modListServerHandler);
			server.createContext("/mods.zip", ModHostingServer::modZipHandler);
			server.setExecutor(null);
			server.start();
						
			LOGGER.debug("Running http server");
		} catch (IOException e) {
			LOGGER.error("httpserver mess didnt work");
			LOGGER.error(e.getMessage());
			LOGGER.error(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()).toString());
		}
	}
	
	public void stop() {
		server.stop(0);
	}
}
