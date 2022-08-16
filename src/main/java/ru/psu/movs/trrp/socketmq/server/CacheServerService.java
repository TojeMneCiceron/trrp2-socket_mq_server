package ru.psu.movs.trrp.socketmq.server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import ru.psu.movs.trrp.socketmq.AppConfig;
import ru.psu.movs.trrp.socketmq.Row;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.*;

public class CacheServerService {
//    private final Channel mqChannel;

    public CacheServerService() throws IOException, TimeoutException {
        AppConfig appConfig = AppConfig.load();
//        AppConfig.MessageQueueServer messageQueueServer = appConfig.messageQueueServer;
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(messageQueueServer.host);
//        factory.setPort(messageQueueServer.port);
//        factory.setUsername(messageQueueServer.username);
//        factory.setPassword(messageQueueServer.password);
//        mqChannel = factory.newConnection().createChannel();
    }

    public ArrayList<Row> GetData()
    {
        ArrayList<Row> rows = new ArrayList<Row>();

        try
        {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Пользователь\\Desktop\\bd.sqlite");

            Statement statmt = conn.createStatement();
            String query = "SELECT d_name, description, p_name, o_name, phone, s_name, age FROM nenorm;";

            ResultSet resSet = statmt.executeQuery(query);

            while (resSet.next())
            {
                String[] s = new String[7];
                for (int i = 1; i <= 7; i++)
                    s[i - 1] = resSet.getString(i);
                rows.add(new Row(s));
            }
            resSet.close();
            conn.close();
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }

        return rows;
    }

//    public void subscribe(String resourceUrl, String queueName) throws IOException {
//        String exchangeName = Utils.buildExchangeName(resourceUrl);
//        mqChannel.exchangeDeclare(exchangeName, "fanout", false, true, false, null);
//        mqChannel.queueBind(queueName, exchangeName, "");
//        System.out.printf("[%s] new subscriber: %s%n", resourceUrl, queueName);
//    }

//    private void scheduleRssCacheUpdateAgentsExecution() {
//        Runnable loadResources = () -> trackedResources.forEach(resource ->
//                rssCacheAgentsThreadPool.submit(new CacheUpdateAgent(resource, mqChannel))
//        );
//        scheduledExecutor.scheduleAtFixedRate(loadResources, 10, 20, TimeUnit.SECONDS);
//    }
}
