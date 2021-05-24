package pt.tecnico.supplier;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;


import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.InputStream;
import java.io.FileInputStream;

public class SupplierServer {

	public static Key symKey = null;

	public static void main(String[] args) throws Exception {
		System.out.println(SupplierServer.class.getSimpleName() + " starting...");

		// Receive and print arguments.
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", Server.class.getName());
			return;
		}

		final int port = Integer.parseInt(args[0]);
		final BindableService impl = new SupplierServiceImpl();

		symKey = readKey(args[1]);

		Server server = null;
		try {
			// Create a new server to listen on port.
			server = ServerBuilder.forPort(port).addService(impl).build();
			server.start();
			// Server threads are running in the background.
			System.out.println("Server started");
			System.out.println();
			// Wait until server is terminated.
			System.out.println("Press enter to shutdown");
			System.in.read();
			server.shutdown();
		} finally {
			if (server != null)
				server.shutdown();
		}
	}

	public static Key readKey(String keyPath) throws Exception {
		System.out.println("Reading key from " + keyPath + " ...");
		
		//InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		InputStream fis = new FileInputStream(keyPath);
		byte[] encoded = new byte[fis.available()];
		fis.read(encoded);
		fis.close();

		System.out.println("Key of len " + encoded.length + " :");
		System.out.println(printHexBinary(encoded));
		SecretKeySpec keySpec = new SecretKeySpec(encoded, "AES");

		return keySpec;
	}

}
