package bwillows.chunkhopper.commands;

import bwillows.chunkhopper.ChunkHopper;
import bwillows.chunkhopper.ChunkHopperConfig;
import bwillows.chunkhopper.Utils;
import bwillows.chunkhopper.common.ItemType;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ChunkHopperCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chunkhopper.*")) {
            String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("no-permission");
            if (message != null) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                sender.sendMessage(message);
            }
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "/chunkhopper help");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                {
                    sender.sendMessage(ChatColor.RED + "/chunkhopper reload");
                    sender.sendMessage(ChatColor.RED + "/chunkhopper give [player] [amount]");
                    sender.sendMessage(ChatColor.RED + "/chunkhopper setworth [amount]");
                    sender.sendMessage(ChatColor.RED + "/chunkhopper worth");
                    sender.sendMessage(ChatColor.RED + "/chunkhopper version");
                }
            return true;
            case "ver":
            case "version":
                {
                    if(ChunkHopper.version != null) {
                        sender.sendMessage(ChunkHopper.version);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Version information is not available.");
                    }
                }
            return true;
            case "give":
                {
                    if(args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "/chunkhopper give [player] [amount]");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                    int amount = 1;
                    if(args.length == 3) {
                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
                            return true;
                        }
                    }
                    if(amount < 1) {
                        sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
                        return true;
                    }

                    // Prevent console from recieving unnecessary messages
                    if(!(sender instanceof ConsoleCommandSender)) {
                        if(sender instanceof Player) {
                            if(!((Player) sender).equals(target)) {
                                String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("on-give");
                                if (message != null && !message.trim().isEmpty()) {
                                    message = ChatColor.translateAlternateColorCodes('&', message);
                                    sender.sendMessage(message.replace("%player%", target.getName()));
                                }
                            }
                        }
                    }

                    // Prevents players from being spammed with messages when recieveing multiple items
                    boolean recieve_inventory_message = false;
                    boolean recieve_drop_on_ground_message = false;

                    // Use for loop to create different IDs for each item
                    for(int i = 0; i < amount; i++) {
                        if(!Utils.isInventoryFull(target)) {
                            target.getInventory().addItem(ChunkHopper.instance.manager.getChunkHopperItem());
                            if(!recieve_inventory_message) {
                                String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("on-recieve");
                                if (message != null && !message.trim().isEmpty()) {
                                    message = ChatColor.translateAlternateColorCodes('&', message);
                                    target.sendMessage(message);
                                }
                            }
                            recieve_inventory_message = true;
                        } else {
                            if(ChunkHopper.instance.chunkHopperConfig.config.settings.dropGiveOnGroundInventoryFull) {
                                target.getWorld().dropItemNaturally(target.getLocation(), ChunkHopper.instance.manager.getChunkHopperItem());
                                if(!recieve_drop_on_ground_message) {
                                    String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("on-recieve-inventory-full");
                                    if (message != null && !message.trim().isEmpty()) {
                                        message = ChatColor.translateAlternateColorCodes('&', message);
                                        target.sendMessage(message);
                                    }
                                }
                                recieve_drop_on_ground_message = true;
                            }
                        }
                    }
                }
            return true;
            case "reload":
                {
                    ChunkHopper.instance.reload();
                    String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("reload");
                    if (message != null) {
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        sender.sendMessage(message);
                    }
                }
            return true;
            case "setworth":
                {
                    if(args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "/chunkhopper setworth [amount]");
                        return true;
                    }
                    double amount;
                    try {
                        amount = Double.parseDouble(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
                        return true;
                    }
                    if(amount < 0) {
                        sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
                        return true;
                    }

                    if(!ChunkHopper.instance.economy.isHooked()) {
                        sender.sendMessage(ChatColor.RED + "Economy is not hooked. Please check your configuration.");
                        return true;
                    }

                    ItemStack itemInHand;

                    if(ChunkHopper.IS_1_9) {
                        itemInHand = ((Player) sender).getInventory().getItemInMainHand();
                    } else {
                        itemInHand = ((Player) sender).getItemInHand();
                    }

                    if(itemInHand == null || itemInHand.getType() == Material.AIR) {
                        sender.sendMessage(ChatColor.RED + "Invalid item in hand.");
                        return true;
                    }

                    ItemType itemType = new ItemType(itemInHand.getType(), itemInHand.getDurability());

                    ChunkHopper.instance.worth.getLocalProvider().setWorth(itemType, amount);

                    String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("set-worth");
                    if(message != null && !message.trim().isEmpty()) {
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        sender.sendMessage(message);
                    }
                }
                return true;
            case "worth":
                {
                    ItemStack itemInHand;

                    if(ChunkHopper.IS_1_9) {
                        itemInHand = ((Player) sender).getInventory().getItemInMainHand();
                    } else {
                        itemInHand = ((Player) sender).getItemInHand();
                    }

                    if(itemInHand == null || itemInHand.getType() == Material.AIR) {
                        sender.sendMessage(ChatColor.RED + "Invalid item in hand.");
                        return true;
                    }

                    ItemType itemType = new ItemType(itemInHand.getType(), itemInHand.getDurability());
                    double worth = ChunkHopper.instance.worth.getWorth(itemType);
                    String message = ChunkHopper.instance.chunkHopperConfig.langYml.getString("worth");
                    if(message != null && !message.trim().isEmpty()) {
                        message = ChatColor.translateAlternateColorCodes('&', message);
                        message = message.replace("%worth%", ChunkHopper.numberFormat.format(worth));
                        message = message.replace("%item%", itemType.material.name());
                        sender.sendMessage(message);
                    }
                }
            return true;
            default :
                {
                    sender.sendMessage(ChatColor.RED + "Unknown subcommand. Try \"/chunkhopper help\"?");
                }
            return true;
        }
    }
}
