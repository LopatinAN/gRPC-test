import grpc.service.Request;
import grpc.service.Response;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcService extends grpc.service.ServiceGrpc.ServiceImplBase {
    private static final Logger logger = Logger.getLogger(GrpcService.class.getName());

    @Override
    public void unary(Request request, StreamObserver<Response> responseObserver) {

        Response response = Response.newBuilder()
                .setRsId(1L)
                .setDetails("Что-то там")
                .setCount(123)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void serverSideStreaming(Request request, StreamObserver<Response> responseObserver) {

        int count = 3;

        for (int i = 0; i < count; ++i) {
            Response response = Response.newBuilder()
                    .setRsId((long)i)
                    .setDetails("response")
                    .setCount(count)
                    .build();

            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Request> clientSideStreaming(StreamObserver<Response> responseObserver) {
        return new StreamObserver<Request>() {

            int requestCount;
            @Override
            public void onNext(Request request) {
                requestCount++;
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, "Error", throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Response.newBuilder()
                        .setRsId(ThreadLocalRandom.current().nextInt())
                        .setCount(requestCount)
                        .setDetails("Some details").build());

                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Request> bidirectionalStreaming(StreamObserver<Response> responseObserver) {
        return new StreamObserver<Request>() {
            @Override
            public void onNext(Request request) {
                int count = 3;

                for (int i = 0; i < count; ++i) {
                    Response response = Response.newBuilder()
                            .setRsId((long)i)
                            .setDetails("response")
                            .setCount(count)
                            .build();

                    responseObserver.onNext(response);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, "Error", throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
