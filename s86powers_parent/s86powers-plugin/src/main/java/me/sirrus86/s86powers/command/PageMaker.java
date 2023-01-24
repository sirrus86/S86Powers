package me.sirrus86.s86powers.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import me.sirrus86.s86powers.localization.LocaleString;

public class PageMaker {

	private final String header;
	private ChatPage output;
	
	public PageMaker(final String header, String text, final int page) {
//		text.split("\n");
		output = ChatPaginator.paginate(text, page, Integer.MAX_VALUE, 7);
		if (page > output.getTotalPages()) {
			output = ChatPaginator.paginate(text, output.getTotalPages(), Integer.MAX_VALUE, 7);
		}
		else if (page <= 0) {
			output = ChatPaginator.paginate(text, 1, Integer.MAX_VALUE, 7);
		}
		this.header = header + ChatColor.RESET + (output.getTotalPages() > 1 ? (" | " + LocaleString.PAGE_OF.build(output.getPageNumber(), Integer.toString(output.getTotalPages()))) : "");
	}
	
	public String getHeader() {
		return header;
	}
	
	public String[] getOutput() {
		return output.getLines();
	}
	
	public void send(CommandSender sender) {
		sender.sendMessage(getHeader());
		sender.sendMessage(getOutput());
	}
	
}
