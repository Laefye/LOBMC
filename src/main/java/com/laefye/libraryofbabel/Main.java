package com.laefye.libraryofbabel;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setBrandName("Library of Babel");
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        instanceContainer.getWorldBorder().setDiameter(Double.POSITIVE_INFINITY);
        instanceContainer.setGenerator(new Library());
        instanceContainer.setTimeRate(0);
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new WordPos(0, 0, 0, 0, 0).getPos());
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().setGameMode(GameMode.CREATIVE);
            event.getPlayer().setFlying(true);
            event.getPlayer().addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 1, Integer.MAX_VALUE));
        });

        globalEventHandler.addListener(PlayerSkinInitEvent.class, event -> {
            PlayerSkin skin = PlayerSkin.fromUsername(event.getPlayer().getUsername());
            event.setSkin(skin);
        });

        var tp = new Command("tp");
        tp.addSyntax((sender, context) -> {
            var d = context.get(ArgumentType.String("word"));
            var word = WordPos.of(new BigInteger(d, Character.MAX_RADIX));
            if (sender instanceof Player player)
            {
                player.teleport(word.getPos());
                player.sendMessage("Bit size:" + (word.getSeed().bitCount()));
                player.sendMessage("Book number:" + (word.book + 1));
                player.sendMessage("Page number:" + (word.page + 1));
            }
        }, ArgumentType.String("word"));
        var speed = new Command("speed");
        speed.addSyntax((sender, context) -> {
            var sp = context.get(ArgumentType.Float("speed"));
            if (sender instanceof Player player)
            {
                player.setFlyingSpeed(sp);
            }
        }, ArgumentType.Float("speed"));

        MinecraftServer.getCommandManager().register(tp);
        MinecraftServer.getCommandManager().register(speed);

        globalEventHandler.addListener(PlayerBlockInteractEvent.class, event -> {
            event.setCancelled(true);
            event.setBlockingItemUse(true);
            if (!event.getBlock().compare(Block.BOOKSHELF)) {
                return;
            }
            var beginPos = WordPos.of(Pos.fromPoint(event.getBlockPosition()), 0, 0);
            var endPos = WordPos.of(Pos.fromPoint(event.getBlockPosition()), WordPos.BOOK_MASK.intValue(), WordPos.PAGE_MASK.intValue());
            var inventory = new Inventory(InventoryType.CHEST_4_ROW,
                    beginPos.getSeed().toString(Character.MAX_RADIX) + "-" +
                            endPos.getSeed().toString(Character.MAX_RADIX)
            );
            for (int i = 0; i < 32; i++) {
                inventory.setItemStack(i, ItemStack.builder(Material.BOOK)
                        .displayName(Component.text(i + 1))
                        .build());
            }
            inventory.addInventoryCondition((player, slot, clickType, inventoryConditionResult) -> {
                inventoryConditionResult.setCancel(true);
                if (slot < WordPos.BOOK_MASK.intValue()+1)
                {
                    player.closeInventory();
                    var book = Book.builder();
                    for (int i = 0; i < WordPos.PAGE_MASK.intValue()+1; i++) {
                        book.addPage(Component.text(WordPos.of(Pos.fromPoint(event.getBlockPosition()), slot, i).getSeed().toString(Character.MAX_RADIX)));
                    }
                    player.openBook(book);
                }
            });
            event.getPlayer().openInventory(inventory);
        });

        globalEventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            event.setCancelled(true);
        });

        minecraftServer.start("0.0.0.0", 25565);
    }
}
