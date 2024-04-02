import grpc.service.Request;
import grpc.service.Response;
import grpc.service.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.lang3.RandomStringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UnaryRpcWithMTLS {

    private static final Logger logger = Logger.getLogger(UnaryRpcWithTLS.class.getName());
    private static ManagedChannel channel;

    private static final String DOMAIN = "localhost";

    private static final int MTLS_PORT = 1415;

    private static final String JKS_STORAGE_TYPE = "JKS";

    private static final String TRUSTSTORE_PASSWORD = "trustpass";
    private static final String KEYSTORE_PASSWORD = "clientpass";

    private static final ApplicationProtocolConfig alpn = new ApplicationProtocolConfig(
            ApplicationProtocolConfig.Protocol.ALPN,
            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
            ApplicationProtocolNames.HTTP_2);


    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        System.out.println("Enter message to server:");
        String message = scan.nextLine();
        scan.close();

        channel = NettyChannelBuilder.forAddress(DOMAIN, MTLS_PORT)
                .negotiationType(NegotiationType.TLS)
                .sslContext(buildSslContext())
                .build();

        ServiceGrpc.ServiceBlockingStub blockingStub = ServiceGrpc.newBlockingStub(channel);

        Request request = Request.newBuilder()
                .setRqId(RandomStringUtils.random(10, true, true))
                .setTimestamp(System.currentTimeMillis())
                .setMessage("mTLS message " + message)
                .build();

        Response response;

        try {
            logger.info("Send request...");
            response = blockingStub.unary(request);

            System.out.println("Received response:\n" + response);
        } finally {
            close();
        }
    }

    private static void close() {
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to close channel", e);
        }
    }

    private static SslContext buildSslContext() {

        logger.info("Building gRPC SSL context");

        try {
            return SslContextBuilder.forClient()
                    .applicationProtocolConfig(alpn)
                    .trustManager(getTrustManagerFactory())
                    .keyManager(getKeyManagerFactory())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Unable to build SslContext", e);
        }
    }

    private static KeyManagerFactory getKeyManagerFactory() throws Exception {

        String path = Optional.ofNullable(GrpcServer.class.getResource("/client.jks")).map(java.net.URL::getPath).orElse("/client.jks");
        File serverJks = new File(path);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(JKS_STORAGE_TYPE);
        keyStore.load(Files.newInputStream(serverJks.toPath()), KEYSTORE_PASSWORD.toCharArray());
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        return keyManagerFactory;
    }

    private static TrustManagerFactory getTrustManagerFactory() throws Exception{

        String path = Optional.ofNullable(GrpcServer.class.getResource("/clientTrustStore.jks")).map(java.net.URL::getPath).orElse("/clientTrustStore.jks");
        File serverJks = new File(path);
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        final KeyStore keyStore = KeyStore.getInstance(JKS_STORAGE_TYPE);
        keyStore.load(Files.newInputStream(serverJks.toPath()), TRUSTSTORE_PASSWORD.toCharArray());
        trustManagerFactory.init(keyStore);

        return trustManagerFactory;
    }
}
