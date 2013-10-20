package InGameSell;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	private mainClass plugin;
	public Commands(mainClass plugin) {
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String [] args) {
		Player player = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("addsell") || cmd.getName().equalsIgnoreCase("editsell")) {
		int id = Integer.parseInt(args[0]);
		int subid = Integer.parseInt(args[1]);
		int price = Integer.parseInt(args[2]);
		if(cmd.getName().equalsIgnoreCase("addsell") && id != 0 && price != 0) {
			try {
				String mysql = plugin.cfg.getString("mysql");
				String username = plugin.cfg.getString("username");
				String password = plugin.cfg.getString("password");
				Connection conn = DriverManager.getConnection(mysql, username, password);
				PreparedStatement addsell = conn.prepareStatement("INSERT IGNORE INTO blocks (id,subid,price) VALUES (?,?,?)");
				addsell.setInt(1, id);
				addsell.setInt(2, subid);
				addsell.setInt(3, price);
				if(addsell.executeUpdate() != 0) {
				player.sendMessage("������ ���� ������� �������� � ��!");
				}
				else {
					player.sendMessage("������ ID ��� ������� � ���� ��� �������� ���� ������� �������!");
				}
				conn.close();
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("editsell") && id != 0 && price != 0) {
			try {
				String mysql = plugin.cfg.getString("mysql");
				String username = plugin.cfg.getString("username");
				String password = plugin.cfg.getString("password");
				Connection conn = DriverManager.getConnection(mysql, username, password);
				PreparedStatement editsell = conn.prepareStatement("UPDATE blocks SET price = ? WHERE id = ? AND subid = ?");
				editsell.setInt(1, price);
				editsell.setInt(2, id);
				editsell.setInt(3, subid);
				if(editsell.executeUpdate() != 0) {
				player.sendMessage("������ � �� ���� ������� ��������!");
				}
				else {
					player.sendMessage("��������� ID � Subid �� ���� ������� � ��. �������������� ��������� /addsell!");
				}
				conn.close();
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		}
		if(cmd.getName().equalsIgnoreCase("helpsell")) {
			player.sendMessage("����� �������� �������� � �� ����������� ������� \"/addsell [id] [subid] [����]\"\n����� �������� �������� � �� ����������� ������� \"/editsell [id] [subid] [����� ����]\"");
			return true;
		}
		return false;
	}
}