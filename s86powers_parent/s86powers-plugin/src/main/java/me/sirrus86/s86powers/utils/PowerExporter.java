package me.sirrus86.s86powers.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;

public class PowerExporter {

	private static final String POWER_PREFIX = "me/sirrus86/s86powers/powers/internal/";
	private final S86Powers plugin;
	
	public PowerExporter(S86Powers plugin, File file) {
		this.plugin = plugin;
		try {
			export(new JarFile(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void export(JarFile file) {
		Enumeration<JarEntry> entries = file.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.startsWith(POWER_PREFIX)
					&& !name.contains("$")
					&& name.endsWith(".class")
					&& (name.contains("/defense/")
							|| name.contains("/offense/")
							|| name.contains("/passive/")
							|| name.contains("/utility/")
							|| ConfigOption.Powers.LOAD_INCOMPLETE_POWERS)) {
				File classFile = new File(plugin.getPowerDirectory(), name.substring(name.lastIndexOf("/")));
				if (!classFile.exists()
						|| classFile.length() == 0L
						|| classFile.lastModified() < entry.getTime()) {
					try {
						classFile.createNewFile();
						byte[] buffer = new byte[1024];
						int bytesRead;
						InputStream is = file.getInputStream(entry);
						FileOutputStream fos = new FileOutputStream(classFile);
						while ((bytesRead = is.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
						is.close();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
}
