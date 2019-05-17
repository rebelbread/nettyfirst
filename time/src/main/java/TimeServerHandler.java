import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 处理服务端 channel
 * @author zhiwj
 * @date 2019/5/5
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    //1.TimeServerHandler 继承自 ChannelInboundHandlerAdapter，
    // 这个类实现了 ChannelInboundHandler接口，
    // ChannelInboundHandler 提供了许多事件处理的接口方法，
    // 然后你可以覆盖这些方法。
    // 现在仅仅只需要继承 ChannelInboundHandlerAdapter 类而不是你自己去实现接口方法。

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //2.这里我们覆盖了 chanelRead() 事件处理方法。
        // 每当从客户端收到新的数据时，这个方法会在收到消息时被调用，
        // 这个例子中，收到的消息的类型是 ByteBuf

        //3.为了实现 DISCARD 协议，处理器不得不忽略所有接受到的消息。
        // ByteBuf 是一个引用计数对象，这个对象必须显示地调用 release() 方法来释放。
        // 请记住处理器的职责是释放所有传递到处理器的引用计数对象。
        // 通常，channelRead() 方法的实现就像下面的这段代码：
//        ((ByteBuf) msg).release();
        System.out.println(ctx.channel().remoteAddress()+"->Server :"+ msg.toString());
        ChannelFuture future = ctx.writeAndFlush("read:" + msg.toString());
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush("channelReadComplete");
        super.channelReadComplete(ctx);
    }

    //1.channelActive() 方法将会在连接被建立并且准备进行通信时被调用。
    // 因此让我们在这个方法里完成一个代表当前时间的32位整数消息的构建工作。
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {


        // 2.为了发送一个新的消息，我们需要分配一个包含这个消息的新的缓冲。
        // 因为我们需要写入一个32位的整数，因此我们需要一个至少有4个字节的 ByteBuf。
        // 通过 ChannelHandlerContext.alloc() 得到一个当前的ByteBufAllocator，
        // 然后分配一个新的缓冲。
//        ByteBuf time = ctx.alloc().buffer(4);
//        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
        // 3.和往常一样我们需要编写一个构建好的消息。但是等一等，flip 在哪？难道我们使用 NIO 发送消息时不是调用 java.nio.ByteBuffer.flip() 吗？ByteBuf 之所以没有这个方法因为有两个指针，一个对应读操作一个对应写操作。当你向 ByteBuf 里写入数据的时候写指针的索引就会增加，同时读指针的索引没有变化。读指针索引和写指针索引分别代表了消息的开始和结束。
        //比较起来，NIO 缓冲并没有提供一种简洁的方式来计算出消息内容的开始和结尾，除非你调用 flip 方法。当你忘记调用 flip 方法而引起没有数据或者错误数据被发送时，你会陷入困境。这样的一个错误不会发生在 Netty 上，因为我们对于不同的操作类型有不同的指针。你会发现这样的使用方法会让你过程变得更加的容易，因为你已经习惯一种没有使用 flip 的方式。
        //
        //另外一个点需要注意的是 ChannelHandlerContext.write() (和 writeAndFlush() )方法会返回一个 ChannelFuture 对象，一个 ChannelFuture 代表了一个还没有发生的 I/O 操作。这意味着任何一个请求操作都不会马上被执行，因为在 Netty 里所有的操作都是异步的。举个例子下面的代码中在消息被发送之前可能会先关闭连接。
//        ChannelFuture f = ctx.writeAndFlush(time);
//        f.addListener(future -> {
//            assert f == future;
//            ctx.close();
//        });
        ChannelFuture f = ctx.writeAndFlush(new UnixTime());
        f.addListener(ChannelFutureListener.CLOSE);


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 4.exceptionCaught() 事件处理方法是当出现 Throwable 对象才会被调用，
        // 即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
        // 在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉。
        // 然而这个方法的处理方式会在遇到不同异常的情况下有不同的实现，
        // 比如你可能想在关闭连接之前发送一个错误码的响应消息。
        super.exceptionCaught(ctx, cause);
    }

}
