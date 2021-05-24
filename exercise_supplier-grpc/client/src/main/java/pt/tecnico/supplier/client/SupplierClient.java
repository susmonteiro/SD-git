package pt.tecnico.supplier.client;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.supplier.grpc.ProductsRequest;
import pt.tecnico.supplier.grpc.ProductsResponse;
import pt.tecnico.supplier.grpc.SupplierGrpc;

import pt.tecnico.supplier.grpc.SignedResponse;
import pt.tecnico.supplier.grpc.Signature;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.protobuf.ByteString;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.InputStream;
import java.io.FileInputStream;

public class SupplierClient {

	/** Digest algorithm. */
	private static final String DIGEST_ALGO = "SHA-256";

	/**
	 * Symmetric cipher: combination of algorithm, block processing, and padding.
	 */
	private static final String SYM_CIPHER = "AES/ECB/PKCS5Padding";

	/**
	 * Set flag to true to print debug messages. The flag can be set using the
	 * -Ddebug command line option.
	 */
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

	/** Helper method to print debug messages. */
	private static void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

	public static Key symKey = null;

	public static void main(String[] args) throws Exception {
		System.out.println(SupplierClient.class.getSimpleName() + " starting ...");

		// Receive and print arguments.
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s host port%n", SupplierClient.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String target = host + ":" + port;

		symKey = readKey(args[2]);

		// Channel is the abstraction to connect to a service end-point.
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		// Create a blocking stub for making synchronous remote calls.
		SupplierGrpc.SupplierBlockingStub stub = SupplierGrpc.newBlockingStub(channel);

		// Prepare request.
		ProductsRequest request = ProductsRequest.newBuilder().build();
		System.out.println("Request to send:");
		System.out.println(request.toString());
		debug("in binary hexadecimals:");
		byte[] requestBinary = request.toByteArray();
		debug(printHexBinary(requestBinary));
		debug(String.format("%d bytes%n", requestBinary.length));

		// Make the call using the stub.
		System.out.println("Remote call...");
		SignedResponse response = stub.listProducts(request);

		// Print response.
		System.out.println("Received response:");
		System.out.println(response.toString());
		debug("in binary hexadecimals:");
		byte[] responseBinary = response.toByteArray();
		debug(printHexBinary(responseBinary));
		debug(String.format("%d bytes%n", responseBinary.length));


		// check Auth
		byte[] decipheredDigest = decipherDigest(response.getSignature().getValue().toByteArray(), symKey);
		byte[] calculatedDigest = makeDigest(response.getResponse().toByteArray());
		
		if (Arrays.equals(decipheredDigest, calculatedDigest))
			System.out.println("Signature is valid! Message accepted! :)");
		else
			System.out.println("Signature is invalid! Message rejected! :(");

		// A Channel should be shutdown before stopping the process.
		channel.shutdownNow();
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

	/** auxiliary method to calculate digest from text and cipher it */
	private static byte[] makeDigest(byte[] bytes) throws Exception {

		// get a message digest object using the specified algorithm
		MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);

		// calculate the digest and print it out
		messageDigest.update(bytes);
		byte[] digest = messageDigest.digest();
		System.out.println("Digest:");
		System.out.println(printHexBinary(digest));

		return digest;
	}

	private static byte[] decipherDigest(byte[] bytes, Key key) throws Exception {
		// get an AES cipher object
		Cipher cipher = Cipher.getInstance(SYM_CIPHER);

		// encrypt the plain text using the key
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherDigest = cipher.doFinal(bytes);

		return cipherDigest;
	}
}
