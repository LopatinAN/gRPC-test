import grpc.service.Attribute;
import grpc.service.Request;
import grpc.service.Response;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcService extends grpc.service.ServiceGrpc.ServiceImplBase {
    private static final Logger logger = Logger.getLogger(GrpcService.class.getName());
    private static long count = 0;

    private static int requestCount = 0;

    private static StringBuilder messageAppender = new StringBuilder();

    private static StringBuilder rqIdAppender = new StringBuilder();

    @Override
    public void unary(Request request, StreamObserver<Response> responseObserver) {

        logger.info("The received request:\n" + request);

        Response response = Response.newBuilder()
                .setRsId(request.getRqId())
                .setTimestamp(System.currentTimeMillis())
                .setDetails("Response from server")
                .addAttributes(Attribute.newBuilder()
                        .setTextId("Request message")
                        .setText(request.getMessage())
                        .build())
                .setCount(count)
                .build();

        count++;

        logger.info("Send response:\n" + response);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void serverSideStreaming(Request request, StreamObserver<Response> responseObserver) {

        logger.info("The received request:\n" + request);

        int numberOfMessages = 3;

        for (int i = 0; i < numberOfMessages; ++i) {
            Response response = Response.newBuilder()
                    .setRsId(request.getRqId())
                    .setTimestamp(System.currentTimeMillis())
                    .setDetails("Response from server Streaming")
                    .addAttributes(Attribute.newBuilder()
                            .setTextId("Request message")
                            .setText(request.getMessage())
                            .build())
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

            @Override
            public void onNext(Request request) {

                logger.info("The received request:\n" + request);
                count++;
                requestCount++;

                messageAppender
                        .append(request.getMessage())
                        .append("; ");

                rqIdAppender
                        .append(request.getRqId())
                        .append("; ");
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, "Error", throwable);
            }

            @Override
            public void onCompleted() {

                messageAppender.setLength(Math.max(messageAppender.length() - 2, 0));

                rqIdAppender.setLength(Math.max(rqIdAppender.length() -2, 0));

                Response response = Response.newBuilder()
                        .setRsId(RandomStringUtils.random(10, true, true))
                        .setTimestamp(System.currentTimeMillis())
                        .setCount(count)
                        .setDetails("Client Side Streaming, received " + requestCount + " requests from client")
                        .addAttributes(Attribute.newBuilder()
                                .setTextId("Request message")
                                .setText(messageAppender.toString())
                                .build())
                        .addAttributes(Attribute.newBuilder()
                                .setTextId("Request count")
                                .setOrdinal(requestCount)
                                .build())
                        .addAttributes(Attribute.newBuilder()
                                .setTextId("rqId")
                                .setText(rqIdAppender.toString())
                                .build())
                        .build();

                logger.info("Send response:\n" + response);

                messageAppender = new StringBuilder();
                rqIdAppender = new StringBuilder();
                requestCount = 0;

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

                logger.info("The received request:\n" + request);

                requestCount++;

                int numberOfMessages = 2;

                for (int i = 0; i < numberOfMessages; ++i) {

                    count++;

                    Response response = Response.newBuilder()
                            .setRsId(request.getRqId())
                            .setTimestamp(System.currentTimeMillis())
                            .setDetails("Bidirectional Streaming, received " + requestCount + " requests from client")
                            .setCount(count)
                            .addAttributes(Attribute.newBuilder()
                                    .setTextId("Request message")
                                    .setText(request.getMessage())
                                    .build())
                            .addAttributes(Attribute.newBuilder()
                                    .setTextId("Request count")
                                    .setOrdinal(requestCount)
                                    .build())
                            .build();

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
                requestCount = 0;

                responseObserver.onCompleted();
            }
        };
    }
}
