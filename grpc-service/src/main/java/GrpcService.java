import grpc.service.Request;
import grpc.service.Response;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcService extends grpc.service.ServiceGrpc.ServiceImplBase {
    private static final Logger logger = Logger.getLogger(GrpcService.class.getName());
    private static int count = 1;

    @Override
    public void unary(Request request, StreamObserver<Response> responseObserver) {

        logger.info("Get request:\n" + request);

        Response response = Response.newBuilder()
                .setRsId(System.currentTimeMillis())
                .setDetails("Something " + request.getMessage())
                .setCount(count)
                .build();

        count++;

        logger.info("Send response:\n" + response);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void serverSideStreaming(Request request, StreamObserver<Response> responseObserver) {

        logger.info("Get request:\n" + request);

        int numberOfMessages = 3;

        for (int i = 0; i < numberOfMessages; ++i) {
            Response response = Response.newBuilder()
                    .setRsId(System.currentTimeMillis())
                    .setDetails("Response from server Streaming")
                    .setCount(count)
                    .build();

            count++;

            logger.info("Send response:\n" + response);

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

                logger.info("Get request:\n" + request);

                requestCount++;
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, "Error", throwable);
            }

            @Override
            public void onCompleted() {

                Response response = Response.newBuilder()
                        .setRsId(System.currentTimeMillis())
                        .setCount(requestCount)
                        .setDetails("Some details")
                        .build();

                logger.info("Send response:\n" + response);

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Request> bidirectionalStreaming(StreamObserver<Response> responseObserver) {
        return new StreamObserver<Request>() {
            @Override
            public void onNext(Request request) {

                logger.info("Get request:\n" + request);

                int numberOfMessages = 2;

                for (int i = 0; i < numberOfMessages; ++i) {
                    Response response = Response.newBuilder()
                            .setRsId(System.currentTimeMillis())
                            .setDetails("BidirectionalStreaming response")
                            .setCount(count)
                            .build();

                    count++;

                    logger.info("Send response:\n" + response);

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
