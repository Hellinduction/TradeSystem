package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.TradeInitializedPacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.spigot.trade.Trade;
import me.nahu.scheduler.wrapper.runnable.WrappedRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TradeInitializedPacketHandler implements PacketHandler<TradeInitializedPacket> {
    private static final long MAX_WAITING_TIME = 1000;

    @Override
    public void process(@NotNull TradeInitializedPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        if (markAsReceived(packet)) return;

        long start = System.currentTimeMillis();

        new WrappedRunnable() {
            @Override
            public void run() {
                if (markAsReceived(packet)) {
                    super.cancel();
                    return;
                }

                if (System.currentTimeMillis() - start > MAX_WAITING_TIME) {
                    super.cancel();
                    TradeSystem.getInstance().getLogger().severe("The trade initialization packet from " + packet.getPlayer() + " was not received by the server!");
                }
            }
        }.runTaskTimer(TradeSystem.getInstance().getScheduler(), 1L, 1L);
    }

    private boolean markAsReceived(@NotNull TradeInitializedPacket packet) {
        Trade trade = TradeSystem.handler().getTrade(packet.getPlayer());
        if (trade != null) {
            if (trade instanceof ProxyTrade) ((ProxyTrade) trade).receiveFirstPacket();
            return true;
        }

        return false;
    }
}
