import grpc.service.Request;
import grpc.service.Response;
import grpc.service.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UnaryRpc {
    private static final Logger logger = Logger.getLogger(UnaryRpcWithTLS.class.getName());

    private static ManagedChannel channel;
    private static final String DOMAIN = "localhost";

    private static final int PORT = 50051;

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        System.out.println("Enter message to server:");
        String message = scan.nextLine();
        scan.close();

        channel = ManagedChannelBuilder.forAddress(DOMAIN, PORT)
                .usePlaintext()
                .build();

        ServiceGrpc.ServiceBlockingStub blockingStub = ServiceGrpc.newBlockingStub(channel);
        ServiceGrpc.ServiceStub asyncStub = ServiceGrpc.newStub(channel);

        Request request = Request.newBuilder()
                .setRqId(RandomStringUtils.random(10, true, true))
                .setTimestamp(System.currentTimeMillis())
                .setMessage(message)
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


}
