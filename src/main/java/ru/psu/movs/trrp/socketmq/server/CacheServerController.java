package ru.psu.movs.trrp.socketmq.server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.SerializationUtils;
import ru.psu.movs.trrp.socketmq.AppConfig;
import ru.psu.movs.trrp.socketmq.Row;
import ru.psu.movs.trrp.socketmq.api.MQRequest;
import ru.psu.movs.trrp.socketmq.api.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

public class CacheServerController implements Runnable {
    private final CacheServerService service;
    private Channel mqChannel;
    private final AppConfig appConfig;

    public CacheServerController(CacheServerService service) {
        appConfig = AppConfig.load();
        this.service = service;

        AppConfig.MessageQueueServer messageQueueServer = appConfig.messageQueueServer;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(messageQueueServer.host);
        factory.setPort(messageQueueServer.port);
        factory.setUsername(messageQueueServer.username);
        factory.setPassword(messageQueueServer.password);
        try {
            mqChannel = factory.newConnection().createChannel();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            mqChannel = null;
        }
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(
                appConfig.cacheServer.port, 50, InetAddress.getByName(appConfig.cacheServer.host))) {
            System.out.printf("[CacheServerController] Running at %s:%d%n", appConfig.cacheServer.host, appConfig.cacheServer.port);
            ExecutorService pool = Executors.newCachedThreadPool();
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    pool.execute(() -> handleConnection(socket));
                }
            } finally {
                pool.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {
        Request request;
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            request = (Request) ois.readObject();

            //service.читаем из ненорм
            ArrayList<Row> rows = service.GetData();
            System.out.println("------------------");
            System.out.println("Rows are read");

            //отправляем
            switch (request.getRequestType()) {
                case Socket:
                    System.out.println("Using socket...");
                    try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                        oos.write(rows.size());
                        oos.flush();

                        for (Row row : rows) {
                            System.out.println("Sending row: " + row.d_name);
                            oos.writeObject(row);
                            oos.flush();
                        }

                        System.out.println("Rows are sent");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case MQ:
                    try {
                        System.out.println("Using mq...");

                        String QUEUE_NAME = "jopa";
                        mqChannel.queueDeclare(QUEUE_NAME, false, false, false, null);
                        String message = "Hello World!";
                        mqChannel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                        System.out.println(" [x] Sent '" + message + "'");



//                mqChannel.exchangeDeclare("", "fanout", false, true, false, null);
//                mqChannel.queueBind("", "", "");

//                        String queueName = ((MQRequest) request).queueName;
//                        System.out.println(queueName);
//
//                        String exchangeName = "emm123";
//                        mqChannel.exchangeDeclare(exchangeName, "fanout", false, true, false, null);
//                        mqChannel.queueBind(queueName, exchangeName, "");
//
////                        mqChannel.queueDeclare(queueName, false, false, false, null);
//
//                        for (Row row : rows) {
//                            System.out.println("Sending row: " + row.d_name);
//                            mqChannel.basicPublish(exchangeName, queueName, null, SerializationUtils.serialize(row));
//                        }
//                        System.out.println("Rows are sent");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
//        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
//            Request objFromStream = (Request) ois.readObject();
//            switch (objFromStream.getRequestType()) {
//                case ListResources:
//                    Set<String> availableResources = service.listResources();
//                    try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
//                        oos.writeObject(new ListResourcesResponse(availableResources));
//                        oos.flush();
//                    }
//                    break;
//                case Subscribe:
//                    SubscribeRequest subscribeRequest = (SubscribeRequest) objFromStream;
//                    service.subscribe(subscribeRequest.resourceUrl, subscribeRequest.queueName);
//                    break;
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }
}
