import grpc.service.Request;
import grpc.service.Response;
import grpc.service.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class ServerStreaming {

    private static final String DOMAIN = "localhost";

    private static final int PORT = 50051;

    public static void main(String[] args) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(DOMAIN, PORT)
                .usePlaintext()
                .build();

        ServiceGrpc.ServiceBlockingStub blockingStub = ServiceGrpc.newBlockingStub(channel);
        ServiceGrpc.ServiceStub asyncStub = ServiceGrpc.newStub(channel);

        Request request = Request.newBuilder()
                .setId(1L)
                .setMessage("Hello")
                .build();

        Iterator<Response> responseIterator;

        try {
            responseIterator = blockingStub.serverSideStreaming(request);

            while (responseIterator.hasNext()) {
                Response response = responseIterator.next();
                System.out.println("Got response:\n" + response);
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
