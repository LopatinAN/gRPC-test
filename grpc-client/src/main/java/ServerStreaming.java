import grpc.service.Request;
import grpc.service.Response;
import grpc.service.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ServerStreaming {

    private static ManagedChannel channel;
    private static final String DOMAIN = "localhost";

    private static final int PORT = 50051;

    private static final Random random = new Random();

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        System.out.println("Select message to server:");
        String message = scan.nextLine();
        scan.close();

        channel = ManagedChannelBuilder.forAddress(DOMAIN, PORT)
                .usePlaintext()
                .build();

        ServiceGrpc.ServiceBlockingStub blockingStub = ServiceGrpc.newBlockingStub(channel);
        ServiceGrpc.ServiceStub asyncStub = ServiceGrpc.newStub(channel);

        Request request = Request.newBuilder()
                .setId(random.nextInt())
                .setMessage(message)
                .build();

        Iterator<Response> responseIterator;

        try {
            responseIterator = blockingStub.serverSideStreaming(request);

            while (responseIterator.hasNext()) {
                Response response = responseIterator.next();
                System.out.println("Got response:\n" + response);
            }
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
