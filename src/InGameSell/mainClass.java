package InGameSell;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;


public class mainClass extends JavaPlugin implements Listener
{
	FileConfiguration cfg;
	
	public static Economy econ = null;
	
	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(this, this);
		
	        File file = new File(getDataFolder() + "/config.yml");
	        cfg = getConfig();
	        if(!file.exists())
	        {
	            cfg.addDefault("mysql", "jdbc:mysql://localhost/dbname");
	            cfg.addDefault("username", "username");
	            cfg.addDefault("password", "password");
	            cfg.addDefault("click", 6);
	            cfg.addDefault("subid", 4);
	            cfg.options().copyDefaults(true);
	            saveConfig();
	        }
	        String mysql = cfg.getString("mysql");
			String username = cfg.getString("username");
			String password = cfg.getString("password");
			Connection conn;
			try {
				conn = DriverManager.getConnection(mysql, username, password);
				PreparedStatement newtbl = conn.prepareStatement("CREATE TABLE IF NOT EXISTS blocks"
						+ "("
						+ "id INT NOT NULL,"
						+ "subid INT NOT NULL,"
						+ "price INT NOT NULL"
						+ ");"
						);
				newtbl.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		getCommand("addsell").setExecutor(new Commands(this));
		getCommand("editsell").setExecutor(new Commands(this));
		getCommand("helpsell").setExecutor(new Commands(this));
		
		if (!setupEconomy() ) {
            getLogger().info("Не удалось подключиться к экономике!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		getLogger().info("Enabled!");
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("Disabled!");
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	@EventHandler
	@SuppressWarnings("deprecation")
	public void onPlayerInteract (PlayerInteractEvent e) throws SQLException {
		String mysql = cfg.getString("mysql");
		String username = cfg.getString("username");
		String password = cfg.getString("password");
		Connection conn = DriverManager.getConnection(mysql, username, password);
		Player p = e.getPlayer();
		int click = cfg.getInt("click");
		int clicksubid = cfg.getInt("subid");
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getTypeId() == click && e.getClickedBlock().getData() == clicksubid && p.getItemInHand() != null) {
			e.setCancelled(true);
			int id = p.getItemInHand().getTypeId();
			int subid = p.getItemInHand().getDurability();
			int col = p.getItemInHand().getAmount();
			PreparedStatement selprice = conn.prepareStatement("SELECT * FROM blocks WHERE id = ? AND subid = ?");
			selprice.setInt(1, id);
			selprice.setInt(2, subid);
			ResultSet result1 = selprice.executeQuery();
			while (result1.next()) {
				int result = result1.getInt("price");
				int cash = result * col;
				String name = e.getPlayer().getName();
				EconomyResponse r = mainClass.econ.depositPlayer(name, cash);
							if(r.transactionSuccess()) {
								p.setItemInHand(null);
								e.getPlayer().sendMessage("Вы продали "+col+" блок(ов) с ID "+id+":"+subid+" на общую сумму в "+cash+" доллар(ов).\nТеперь Ваш игровой счет составляет "+econ.format(r.balance)+".");
							}
							else {
								e.getPlayer().sendMessage("Произошла ошибка");
							}
			}
			
			conn.close();
		}
	}
	
}
