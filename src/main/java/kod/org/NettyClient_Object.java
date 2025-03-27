package kod.org;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NettyClient_Object {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static int personCounter;

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ClientHandler());
                        }
                    });

            Channel channel = bootstrap.connect(HOST, PORT).sync().channel();
            System.out.println("Клиент подключен к серверу " + HOST + ":" + PORT);



            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = reader.readLine();
                if (input == null || "exit".equalsIgnoreCase(input)) {
                    break;
                }
                Person person=new Person(personCounter,input);
                channel.writeAndFlush(person);
                personCounter++;
            }

        } finally {
            group.shutdownGracefully();
        }
    }

    private static class ClientHandler extends SimpleChannelInboundHandler<Person> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Person msg) {
            System.out.printf("Ответ от сервера:\n " +
                                "получен объект с именем: %s\n " +
                                "идентификатором: %d\n",msg.getName(),msg.getId());
            personCounter++;

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
