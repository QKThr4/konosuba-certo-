package com.konorpg.event;

import com.konorpg.KonoRPG;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.network.Net;
import com.konorpg.quest.QuestManager;
import com.konorpg.rpg.RpgManager;
import com.konorpg.world.CityBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KonoRPG.MOD_ID)
public final class ModCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("konorpg")
                .then(Commands.literal("city").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    CityBuilder.ensureCity(p.server);
                    CityBuilder.teleportToPlaza(p);
                    return 1;
                }))
                .then(Commands.literal("hub").executes(c -> {
                    Net.openHub(c.getSource().getPlayerOrException(), "status");
                    return 1;
                }))
                .then(Commands.literal("intro").requires(s -> s.hasPermission(2)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    RpgManager.data(p).introDone = false;
                    Net.openScreen(p, "intro", new net.minecraft.nbt.CompoundTag());
                    return 1;
                }))
                .then(Commands.literal("xp").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1_000_000)).executes(c -> {
                            RpgManager.grantXp(c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "amount"));
                            return 1;
                        })))
                .then(Commands.literal("money").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(-1_000_000, 1_000_000)).executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            RpgManager.addMoney(p, IntegerArgumentType.getInteger(c, "amount"));
                            RpgManager.sync(p);
                            return 1;
                        })))
                .then(Commands.literal("quest").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word()).executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            boolean ok = QuestManager.accept(p, StringArgumentType.getString(c, "id"), true);
                            c.getSource().sendSuccess(() -> Component.literal(ok ? "Quest iniciada." : "Quest inválida ou já ativa."), false);
                            return ok ? 1 : 0;
                        })))
                .then(Commands.literal("reset").requires(s -> s.hasPermission(2)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    PlayerRpgData d = RpgManager.data(p);
                    d.deserializeNBT(p.registryAccess(), new PlayerRpgData().serializeNBT(p.registryAccess()));
                    RpgManager.refreshAttributes(p);
                    RpgManager.sync(p);
                    c.getSource().sendSuccess(() -> Component.literal("Dados RPG resetados."), false);
                    return 1;
                })));
    }

    private ModCommands() {}
}
