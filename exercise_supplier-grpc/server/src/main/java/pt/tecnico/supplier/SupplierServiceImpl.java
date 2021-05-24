package pt.tecnico.supplier;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import com.google.protobuf.ByteString;
import com.google.type.Money;

import io.grpc.stub.StreamObserver;
import pt.tecnico.supplier.domain.Supplier;
import pt.tecnico.supplier.grpc.Product;
import pt.tecnico.supplier.grpc.ProductsRequest;
import pt.tecnico.supplier.grpc.ProductsResponse;
import pt.tecnico.supplier.grpc.SupplierGrpc;
import pt.tecnico.supplier.grpc.SignedResponse;
import pt.tecnico.supplier.grpc.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import java.security.Key;
import java.security.MessageDigest;

import static pt.tecnico.supplier.SupplierServer.symKey;

public class SupplierServiceImpl extends SupplierGrpc.SupplierImplBase {

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

	/** Domain object. */
	final private Supplier supplier = Supplier.getInstance();

	/** Constructor */
	public SupplierServiceImpl() {
		debug("Loading demo data...");
		supplier.demoData();
	}

	/** Helper method to convert domain product to message product. */
	private Product buildProductFromProduct(pt.tecnico.supplier.domain.Product p) {
		Product.Builder productBuilder = Product.newBuilder();
		productBuilder.setIdentifier(p.getId());
		productBuilder.setDescription(p.getDescription());
		productBuilder.setQuantity(p.getQuantity());

		Money.Builder moneyBuilder = Money.newBuilder();
		moneyBuilder.setCurrencyCode("EUR").setUnits(p.getPrice());
		productBuilder.setPrice(moneyBuilder.build());

		return productBuilder.build();
	}

	@Override
	public void listProducts(ProductsRequest request, StreamObserver<SignedResponse> responseObserver) {
		debug("listProducts called");

		debug("Received request:");
		debug(request.toString());
		debug("in binary hexadecimals:");
		byte[] requestBinary = request.toByteArray();
		debug(String.format("%d bytes%n", requestBinary.length));

		// build response
		SignedResponse.Builder signedResponse = SignedResponse.newBuilder();

		ProductsResponse.Builder responseBuilder = ProductsResponse.newBuilder();
		responseBuilder.setSupplierIdentifier(supplier.getId());
		for (String pid : supplier.getProductsIDs()) {
			pt.tecnico.supplier.domain.Product p = supplier.getProduct(pid);
			Product product = buildProductFromProduct(p);
			responseBuilder.addProduct(product);
		}
		ProductsResponse productsResponse = responseBuilder.build();


		// TODO
		try {
			signedResponse.setSignature(
				Signature.newBuilder()
					.setValue(ByteString.copyFrom(makeDigest(productsResponse.toByteArray(), symKey)))
					.setSignerId("boy serveeeer")
					.build()
			);
		} catch(Exception e ) {
			e.printStackTrace();
		}

		signedResponse.setResponse(changedResponse);
		SignedResponse response = signedResponse.build();

		debug("Response to send:");
		debug(response.toString());
		debug("in binary hexadecimals:");
		byte[] responseBinary = response.toByteArray();
		debug(printHexBinary(responseBinary));
		debug(String.format("%d bytes%n", responseBinary.length));

		// send single response back
		responseObserver.onNext(response);
		// complete call
		responseObserver.onCompleted();
	}

	private static byte[] makeDigest(byte[] plainBytes, Key key) throws Exception {
		// make MAC


		System.out.println("Signing...");
		byte[] cipherDigest = digestAndCipher(plainBytes, key);
		System.out.println("CipherDigest:");
		System.out.println(printHexBinary(cipherDigest));

		return cipherDigest;
	}

	/** auxiliary method to calculate digest from text and cipher it */
	private static byte[] digestAndCipher(byte[] bytes, Key key) throws Exception {

		// get a message digest object using the specified algorithm
		MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);

		// calculate the digest and print it out
		messageDigest.update(bytes);
		byte[] digest = messageDigest.digest();
		System.out.println("Digest:");
		System.out.println(printHexBinary(digest));

		// get an AES cipher object
		Cipher cipher = Cipher.getInstance(SYM_CIPHER);

		// encrypt the plain text using the key
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherDigest = cipher.doFinal(digest);

		return cipherDigest;
	}
}
