/*
 * Copyright 2018-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rabbitmq.classic.samples.acknowledgement.mandatory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Return;
import com.rabbitmq.client.ReturnCallback;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 生产者：设置消息 mandatory = true
 *
 * @author Sven Augustus
 */
public class MandatoryProducer {

  public static final String AMQP_URL = "amqp://mq:mq123@127.0.0.1:5672";

  private static final String QUEUE_NAME = "samples.queue.test";
  private static final String EXCHANGE_NAME = "samples.exchange.test";
  // 如果 direct 模式交换器， ROUTING_KEY 与 BINDING_KEY 必须完全匹配
  private static final String BINDING_KEY = "samples.test.key";
  private static final String ROUTING_KEY = "not.matched.key";

  public static void main(String[] args)
      throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUri(AMQP_URL);
    Connection conn = factory.newConnection();
    Channel channel = conn.createChannel();

    // 创建了一个持久化的、非自动删除的、绑定类型为 direct 的交换器
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, false,
        new HashMap<>(8));
    // 队列被声明为持久化的 非排他的 非自动删除的，而且也被分配另一个确定的己知的名称(由客户端分配而非 RabbitMQ 自动生成)。
    channel.queueDeclare(QUEUE_NAME, true, false, false, null);

    // 使用路由键将队列和交换器绑定起来
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, BINDING_KEY);

    // TODO 生产者通过调用channel的 confirmSelect 方法将channel设置为 confirm 模式
    channel.confirmSelect();

    byte[] messageBodyBytes = ("Hello World! " + LocalDateTime.now()).getBytes();
    // 发送消息
    // FIXME 设置消息 mandatory = true, 如果交换器无法根据自身的类型和路由键找到一个符合条件的队列， RabbitMQ 返回消息内容
    channel.addReturnListener(new ReturnCallback() {
      @Override
      public void handle(Return returnMessage) {
        // Decode message
        String content = new String(returnMessage.getBody());
        System.out
            .printf("Return Messages, replyText= %s, exchange= %s, routingKey= %s , body= %s %n",
                returnMessage.getReplyText(), returnMessage.getExchange(),
                returnMessage.getRoutingKey(), content);
        // FIXME 注意这里需要添加处理消息重发的场景
      }
    });
    // FIXME 设置消息 mandatory = true （因为如果为 false, 交换器无法根据自身的类型和路由键找到一个符合条件的队列，但是消息处理无异常，那么 RabbitMQ 返回的还是 OKay的）。
    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
        // 消息持久化
        .deliveryMode(2).build();
    channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, true, properties, messageBodyBytes);

    boolean sendOk = false;
    try {
      // TODO 普通confirm模式：每发送一条消息后，调用waitForConfirms()方法，等待服务器端confirm。实际上是一种串行confirm了。
      //  其返回的条件是客户端收到了相应的 Basic.Ack/.Nack 或者被中断。
      sendOk = channel.waitForConfirms();
    } catch (InterruptedException e) {
      sendOk = false;
    } finally {
      if (sendOk) {
        System.out.println("Send OK. msg=" + new String(messageBodyBytes));
      } else {
        System.err.println("Send Failed.");
        // FIXME 注意这里需要添加处理消息重发的场景
      }
    }

    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    channel.close();
    conn.close();
  }

}