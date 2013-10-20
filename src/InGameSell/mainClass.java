package InGameSell;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class mainClass extends JavaPlugin implements Listener
{

	FileConfiguration cfg;
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
		getLogger().info("Enabled!");
	}
	
	
	public void onDisable()
	{
		getLogger().info("Disabled!");
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
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getTypeId() == click && p.getItemInHand() != null) {
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
				PreparedStatement oldcash = conn.prepareStatement("SELECT * FROM iConomy WHERE username = ?");
				oldcash.setString(1, name);
				ResultSet balance1 = oldcash.executeQuery();
					while(balance1.next()) {
						int balance = balance1.getInt("balance");
						int newbalance = balance + cash;
						PreparedStatement newcash = conn.prepareStatement("UPDATE iConomy SET balance = ? WHERE username = ?");
						newcash.setInt(1, newbalance);
						newcash.setString(2, name);
							if(newcash.executeUpdate() != 0) {
								p.setItemInHand(null);
								e.getPlayer().sendMessage("Вы продали "+col+" блок(ов) с ID "+id+":"+subid+" на общую сумму в "+cash+" доллар(ов).\nТеперь Ваш игровой счет составляет "+newbalance+" доллар(ов).");
							}
							else {
								e.getPlayer().sendMessage("Произошла ошибка");
							}
					}
			}
			
			conn.close();
		}
	}
	
}
