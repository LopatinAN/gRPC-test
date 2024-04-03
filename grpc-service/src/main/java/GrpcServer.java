import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.netty.shaded.io.netty.internal.tcnative.SSLContext;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.InputMismatchException;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

import static io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth.REQUIRE;

public class GrpcServer {

    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

    private static final String JKS_STORAGE_TYPE = "JKS";

    private static final String KEYSTORE_PASSWORD = "serverpass";
    private static final String TRUSTSTORE_PASSWORD = "trustpass";

    private static final int TLS_PORT = 1414;
    private static final int MTLS_PORT = 1415;
    private static final int PLAINTEXT_PORT = 50051;

    private Server server;

    public static void main(String[] args) throws Exception {

        int connectionType = 0;
        Scanner scan = new Scanner(System.in);
        System.out.println("Choose connection type:");
        System.out.println(
                "To set Plaintext send: 1\n" +
                "To set TLS send: 2\n" +
                "To set mTLS send: 3"
        );

        if (scan.hasNextInt())
            connectionType = scan.nextInt();

        scan.close();

        final GrpcServer server = new GrpcServer();

        switch (connectionType) {
            case 1:
                server.startPlaintext(PLAINTEXT_PORT);
                break;
            case 2:
                server.startWithTLS(TLS_PORT, false);
                break;
            case 3:
                server.startWithTLS(MTLS_PORT, true);
                break;
            default:
                System.out.println("Send a number from 1 to 3");
        }

        server.blockUntilShutdown();
    }

    private void startPlaintext(int port) {

        try {
            server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                    .addService(new GrpcService())
                    .build()
                    .start();

            logger.info("Server started, listening on port: " + port);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Can't start gRPC server", e);
        }

    }

    private void startWithTLS(int port, boolean mTLS) {

        try {
            server = NettyServerBuilder.forPort(port)
                    .sslContext(buildSslContext(mTLS))
                    .addService(new GrpcService())
                    .build()
                    .start();

            logger.info("Server started, listening on port: " + port);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Can't start gRPC server", e);
        }

    }

    private void stop() throws InterruptedException {
        if (server != null)
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null)
            server.awaitTermination();
    }

    private static SslContext buildSslContext(boolean mTLS) {

        logger.info("Building gRPC SSL context");

        try {

            if (!mTLS) {
                return GrpcSslContexts.configure(
                                SslContextBuilder.forServer(getKeyManagerFactory()),
                                SslProvider.OPENSSL)
                        .build();
            } else {
                return GrpcSslContexts.configure(
                                SslContextBuilder.forServer(getKeyManagerFactory())
                                        .trustManager(getTrustManagerFactory())
                                        .clientAuth(REQUIRE),
                                SslProvider.OPENSSL)
                        .build();
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to build SslContext", e);
        }
    }

    private static KeyManagerFactory getKeyManagerFactory() throws Exception {

        String path = Optional.ofNullable(GrpcServer.class.getResource("/server.jks")).map(java.net.URL::getPath).orElse("/server.jks");
        File serverJks = new File(path);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(JKS_STORAGE_TYPE);
        keyStore.load(Files.newInputStream(serverJks.toPath()), KEYSTORE_PASSWORD.toCharArray());
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        return keyManagerFactory;
    }

    private static TrustManagerFactory getTrustManagerFactory() throws Exception{

        String path = Optional.ofNullable(GrpcServer.class.getResource("/serverTrustStore.jks")).map(java.net.URL::getPath).orElse("/serverTrustStore.jks");
        File serverJks = new File(path);
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        final KeyStore keyStore = KeyStore.getInstance(JKS_STORAGE_TYPE);
        keyStore.load(Files.newInputStream(serverJks.toPath()), TRUSTSTORE_PASSWORD.toCharArray());
        trustManagerFactory.init(keyStore);

        return trustManagerFactory;
    }

}
