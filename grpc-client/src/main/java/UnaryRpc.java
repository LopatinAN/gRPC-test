import grpc.service.Request;
import grpc.service.Response;
import grpc.service.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class UnaryRpc {


    private static final String DOMAIN = "localhost";

    private static final int PORT = 50051;

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(DOMAIN, PORT)
                .usePlaintext()
                .build();

        ServiceGrpc.ServiceBlockingStub blockingStub = ServiceGrpc.newBlockingStub(channel);

        Request request = Request.newBuilder()
                .setId(1L)
                .setMessage("Hello")
                .build();

        Response response = blockingStub.unary(request);

        System.out.println("Got response:\n" + response);

        channel.shutdownNow();
    }
}
