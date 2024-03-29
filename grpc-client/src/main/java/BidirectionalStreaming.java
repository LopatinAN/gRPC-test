import grpc.service.Request;
import grpc.service.Response;
import grpc.service.ServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

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
        System.out.println("Select message to server:");
        String message = scan.nextLine();
        scan.close();

        channel = ManagedChannelBuilder.forAddress(DOMAIN, PORT)
                .usePlaintext()
                .build();

        ServiceGrpc.ServiceStub asyncStub = ServiceGrpc.newStub(channel);

        StreamObserver<Response> responseStreamObserver = new StreamObserver<Response>() {
            @Override
            public void onNext(Response response) {
                System.out.println("Got response:\n" + response);
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

        for (int i = 0; i < 2; ++i) {
            requestStreamObserver.onNext(Request.newBuilder()
                    .setId(i +1)
                    .setMessage(message + ". Message number: " + i)
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
