package org.readutf.orchestrator.server.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.readutf.orchestrator.shared.packet.PacketManager
import org.readutf.orchestrator.shared.packet.serializer.impl.FastJsonPacketSerializer

class NetworkManager {
    init {
        val bootstrap = createBootstrap()

        val future = bootstrap.bind(8080).sync()

        println("Orchestrator started")

        future.channel().closeFuture().sync()
    }

    private lateinit var packetManager: PacketManager

    fun createPacketManager(channelPipeline: ChannelPipeline): PacketManager =
        PacketManager.netty(FastJsonPacketSerializer(), channelPipeline)

    private fun createBootstrap(): ServerBootstrap {
        val bootstrap = ServerBootstrap()
        bootstrap
            .group(
                NioEventLoopGroup(),
                NioEventLoopGroup(),
            ).channel(NioServerSocketChannel::class.java)
            .childHandler(
                object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(socketChannel: SocketChannel?) {
                        val pipeline = socketChannel!!.pipeline()
                        packetManager = createPacketManager(pipeline)
                    }
                },
            )
        return bootstrap
    }
}
