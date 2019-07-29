package me.sirrus86.s86powers.localization;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;

public class LocaleLoader {
	
	private final YamlConfiguration defYaml;
	private final File locDir;
	private static File locFile;
	protected static YamlConfiguration LOCALIZATION_YAML;
	
	private final String header = "To create your own localization file, make a copy of this file, name the copied file\n"
			+ "something with no spaces, then replace the text to the right of the colon with what\n"
			+ "should be read. To ensure a given line is readable by the plugin, leave any single quotes\n"
			+ "as-is. If any given line is deleted or unreadable, the plugin will use the default enUS\n"
			+ "line when needed.\n"
			+ "\n"
			+ "Words following an ampersand '&' are replaced by the plugin with the below data:\n"
			+ "&class - Gets the name of the specified class\n"
			+ "&cooldown - Renders the number as a long string, e.g. one minute five seconds\n"
			+ "&file - Gets the name of the specified file\n"
			+ "&group - Gets the name of the specified group\n"
			+ "&int - Gets a number supplied by the plugin\n"
			+ "&player - Gets the name of the specified player\n"
			+ "&power - Gets the name of the specified power\n"
			+ "&string - Is replaced by an expected string\n"
			+ "&syntax - Gets the help syntax for a specified command\n"
			+ "&type - Gets the name of the specified power type\n"
			+ "&value - Gets the expected value and outputs as string\n"
			+ "\n"
			+ "When changing a message, be sure to keep the above words in the message.\n"
			+ "Removing a special word from a message leaves it with no way to render unique info.\n"
			+ "Adding special words that weren't originally in the message has no effect.\n"
			+ "\n"
			+ "Once the new file is created, run the following command in-game or from the console:\n"
			+ "/p config set plugin.localization newfile\n"
			+ "Replacing newfile with the file you created, minus the .yml extension.\n"
			+ "You can alternatively edit the config.yml file directly while the server is inactive.\n"
			+ "\n"
			+ "Note: Editing this file (enUS.yml) is pointless as it is overwritten every time the\n"
			+ "server restarts.";
	
	private static final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	
	public LocaleLoader() throws IOException {
		locDir = new File(plugin.getDataFolder(), "localization");
		if (!locDir.exists()) {
			locDir.mkdirs();
		}
		locFile = new File(locDir, ConfigOption.Plugin.LOCALIZATION + ".yml");
		LOCALIZATION_YAML = YamlConfiguration.loadConfiguration(locFile);
		File defLocFile = new File(locDir, "enUS.yml");
		if (!defLocFile.exists()) {
			defLocFile.createNewFile();
		}
		defYaml = YamlConfiguration.loadConfiguration(defLocFile);
		defYaml.options().header(header);
		for (LocaleString string : LocaleString.values()) {
			defYaml.set(string.getPath(), string.getDefaultText());
		}
		defYaml.save(defLocFile);
		LOCALIZATION_YAML.setDefaults(defYaml);
	}
	
}
