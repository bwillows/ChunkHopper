package bwillows.chunkhopper.economy;

import org.bukkit.OfflinePlayer;

public interface EconomyProvider {

    boolean hasBalance(OfflinePlayer player, double amount);

    boolean withdraw(OfflinePlayer player, double amount);

    boolean deposit(OfflinePlayer player, double amount);

    double getBalance(OfflinePlayer player);
}
