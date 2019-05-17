import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * 处理服务端 channel
 * @author zhiwj
 * @date 2019/5/5
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {
    //1.DiscardServerHandler 继承自 ChannelInboundHandlerAdapter，
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
        ByteBuf in = (ByteBuf) msg;

        try {
            while (in.isReadable()) {
                byte b = in.readByte();
                System.out.print((char) b);
                System.out.flush();
            }
        }finally {
//            in.release();
            ReferenceCountUtil.release(msg);
        }


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
