import grpc.service.Request;
import grpc.service.Response;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcService extends grpc.service.ServiceGrpc.ServiceImplBase {
    private static final Logger logger = Logger.getLogger(GrpcService.class.getName());
    private static int count = 1;

    private static final StringBuilder rqIdAppender = new StringBuilder();

    @Override
    public void unary(Request request, StreamObserver<Response> responseObserver) {

        logger.info("Get request:\n" + request);

        Response response = Response.newBuilder()
                .setRsId(request.getRqId())
                .setTimestamp(System.currentTimeMillis())
                .setDetails("Received request with message: " + request.getMessage())
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
                    .setRsId(request.getRqId())
                    .setTimestamp(System.currentTimeMillis())
                    .setDetails("Response from server Streaming on request: " + request.getMessage())
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
                count++;
                requestCount++;

                rqIdAppender
                        .append(request.getRqId())
                        .append(", ");
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, "Error", throwable);
            }

            @Override
            public void onCompleted() {

                Response response = Response.newBuilder()
                        .setRsId(RandomStringUtils.random(10, true, true))
                        .setTimestamp(System.currentTimeMillis())
                        .setCount(count)
                        .setDetails("Received " + requestCount + " requests from client with rqId: " + rqIdAppender.toString())
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
                            .setRsId(request.getRqId())
                            .setTimestamp(System.currentTimeMillis())
                            .setDetails("BidirectionalStreaming response on request\n" + request.getMessage())
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
