package de.oliver.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.oliver.Npc;
import de.oliver.NpcPlugin;
import de.oliver.events.PacketReceivedEvent;
import de.oliver.utils.ReflectionUtils;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PacketReceivedListener implements Listener {

    @EventHandler
    public void onPacketReceived(PacketReceivedEvent event){

        if(event.getPacket() instanceof ServerboundInteractPacket interactPacket){
            String hand = "";

            if(interactPacket.getActionType() != ServerboundInteractPacket.ActionType.ATTACK){
                hand = ReflectionUtils.getValue(ReflectionUtils.getValue(interactPacket, "b"), "a").toString(); // ServerboundInteractPacket.InteractionAction.hand
            }

            int entityId = interactPacket.getEntityId();
            ServerboundInteractPacket.ActionType action = interactPacket.getActionType();
            boolean isSneaking = interactPacket.isUsingSecondaryAction();

            if(action == ServerboundInteractPacket.ActionType.ATTACK || action == ServerboundInteractPacket.ActionType.INTERACT && hand.equalsIgnoreCase("MAIN_HAND")){
                Npc npc = NpcPlugin.getInstance().getNpcManager().getNpc(entityId);
                if(npc == null){
                    return;
                }

                npc.getOnClick().accept(event.getPlayer());
                if(npc.getServerCommand() != null && npc.getServerCommand().length() > 0){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), npc.getServerCommand().replace("{player}", event.getPlayer().getName()));
                }

                if(npc.getPlayerCommand() != null && npc.getPlayerCommand().length() > 0){

                    if(npc.getPlayerCommand().toLowerCase().startsWith("server")){
                        String[] args = npc.getPlayerCommand().split(" ");
                        if(args.length < 2){
                            return;
                        }
                        String server = args[1];

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(server);
                        event.getPlayer().sendPluginMessage(NpcPlugin.getInstance(), "BungeeCord", out.toByteArray());
                        return;
                    }

                    event.getPlayer().performCommand(npc.getPlayerCommand());
                }
            }
        }

    }

}
