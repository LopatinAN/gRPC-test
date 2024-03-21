import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GrpcServer {

    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

    private Server server;

    private void start(int port) throws IOException {

        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new GrpcService())
                .build()
                .start();

        logger.info("Server started, listening on " + port);
    }

    private void stop() throws InterruptedException {
        if (server != null)
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null)
            server.awaitTermination();
        logger.info("Server stopped");
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = 50051;

        final GrpcServer server = new GrpcServer();
        server.start(port);
        server.blockUntilShutdown();
    }

}
