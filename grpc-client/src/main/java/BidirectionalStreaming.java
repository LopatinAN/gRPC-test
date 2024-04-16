import grpc.service.Request;
import grpc.service.Response;
import grpc.service.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BidirectionalStreaming {

    private static final Logger logger = Logger.getLogger(ClientSideStreaming.class.getName());
    private static ManagedChannel channel;

    private static final String DOMAIN = "localhost";

    private static final int PORT = 50051;

    public static void main(String[] args) throws InterruptedException {

        Scanner scan = new Scanner(System.in);
        System.out.println("Enter message to server:");
        String message = scan.nextLine();
        scan.close();

        channel = ManagedChannelBuilder.forAddress(DOMAIN, PORT)
                .usePlaintext()
                .build();

        ServiceGrpc.ServiceStub asyncStub = ServiceGrpc.newStub(channel);

        StreamObserver<Response> responseStreamObserver = new StreamObserver<Response>() {
            @Override
            public void onNext(Response response) {
                System.out.println("Received response:\n" + response);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                System.out.println("Finished stream");
            }
        };

        StreamObserver<Request> requestStreamObserver = asyncStub.bidirectionalStreaming(responseStreamObserver);

        logger.info("Send requests...");

        for (int i = 1; i < 3; ++i) {
            requestStreamObserver.onNext(Request.newBuilder()
                    .setRqId(RandomStringUtils.random(10, true, true))
                    .setTimestamp(System.currentTimeMillis())
                    .setMessage("Message number - " + i + ": " + message)
                    .build());
        }

        Thread.sleep(1500);
        requestStreamObserver.onCompleted();
        Thread.sleep(500);

        close();
    }

    private static void close() {
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to close channel", e);
        }
    }
}
