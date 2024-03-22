import grpc.service.Request;
import grpc.service.Response;
import grpc.service.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class UnaryRpc {

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

        Response response;

        try {
            response = blockingStub.unary(request);

            System.out.println("Got response:\n" + response);

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }

    }
}
