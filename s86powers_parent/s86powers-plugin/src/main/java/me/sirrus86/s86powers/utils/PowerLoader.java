package me.sirrus86.s86powers.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.tools.version.MCServer;
import me.sirrus86.s86powers.tools.version.MCVersion;

public class PowerLoader {

	private static final String POWER_PREFIX = "me.sirrus86.s86powers.powers.internal.";
	private static final String[] TYPE_PREFIX = new String[] { "defense.", "offense.", "passive.", "utility.", "" };
	
	public PowerLoader(File file) {
		load(file);
	}
	
	private void load(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					String o1name = o1.getName();
					int o1dot = o1name.lastIndexOf('.');
					String o2name = o2.getName();
					int o2dot = o2name.lastIndexOf('.');
					if ((o1dot == -1) == (o2dot == -1)) {
						o1name = o1name.substring(o1dot + 1);
						o2name = o2name.substring(o2dot + 1);
						return o1name.compareTo(o2name);
					}
					else if (o1dot == -1) {
						return -1;
					}
					else {
						return 1;
					}
				}
			});
			try {
				ClassLoader loader = new PowerClassLoader(file.toURI().toURL());
				for (final File item : files) {
					load(item, loader);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		else if (IOHelper.isJar(file)) {
			try {
				ClassLoader loader = new PowerClassLoader(IOHelper.getJarUrl(file));
				load(new JarFile(file), loader);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void load(File file, ClassLoader loader) throws IOException {
		if (IOHelper.isJar(file)) {
			load(new JarFile(file), new PowerClassLoader(IOHelper.getJarUrl(file)));
		}
		else {
			if (loader == null) {
				loader = new PowerClassLoader(file.getParentFile().toURI().toURL());
			}
			load(file, loader, "");
		}
	}
	
	private void load(File file, ClassLoader loader, String prefix) {
		if (file.isDirectory()) {
			if (!file.getName().startsWith(".")) {
				for (final File f : file.listFiles()) {
					load(f, loader, prefix + file.getName() + ".");
				}
			}
		}
		else {
			String name = prefix + file.getName();
			final String ext = ".class";
			if (name.endsWith(ext)
					&& !name.startsWith(".")
					&& !name.contains("!")
					&& !name.contains("$")) {
				name = name.substring(0, name.length() - ext.length());
				load(loader, name, file.getAbsolutePath());
			}
		}
	}
	
	private void load(JarFile jar, ClassLoader loader) {
		final Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			final JarEntry e = entries.nextElement();
			final String name = e.getName().replace('/', '.');
			final String ext = ".class";
			if (name.endsWith(ext)
					&& !name.contains("$")) {
				load(loader, name.substring(0, name.length() - ext.length()), jar.getName());
			}
		}
	}
	
	private void load(ClassLoader loader, String name, String path) {
		Class<?> clazz = null;
        try {
    		clazz = loader.loadClass(name);
    		if (clazz == null) {
    			clazz = resolveClass(loader, name);
    		}
    		if (clazz != null) {
    			if (clazz.isAnnotationPresent(PowerManifest.class)) {
    				final PowerManifest manifest = clazz.getAnnotation(PowerManifest.class);
    				if (manifest.type() == null) {
    					if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
    						S86Powers.log(Level.WARNING, LocaleString.INVALID_POWER_TYPE.build(name));
    					}
    				}
    				else if (manifest.name() == "") {
    					if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
    						S86Powers.log(Level.WARNING, LocaleString.INVALID_POWER_NAME.build(name));
    					}
    				}
    				else if (manifest.version().ordinal() > MCVersion.CURRENT_VERSION.ordinal()) {
    					if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
    						S86Powers.log(Level.WARNING, LocaleString.INVALID_SERVER_VERSION.build(name, manifest.version()));
    					}
    				}
    				else if (manifest.server().ordinal() > MCServer.CURRENT_SERVER.ordinal()) {
    					if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
    						S86Powers.log(Level.WARNING, LocaleString.INVALID_SERVER_SOFTWARE.build(name, manifest.server()));
    					}
    				}
    				else if (manifest.incomplete()
    						&& !ConfigOption.Powers.LOAD_INCOMPLETE_POWERS) {
    					if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
    						S86Powers.log(Level.WARNING, LocaleString.INCOMPLETE_POWER.build(name));
    					}
    				}
    				else if (manifest.usesPackets()
    						&& S86Powers.getProtocolLib() == null
    						&& !ConfigOption.Powers.BYPASS_PROTOCOLLIB_REQUIREMENT) {
    					if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
    						S86Powers.log(Level.WARNING, LocaleString.POWER_REQUIRES_PROTOCOLLIB.build(name));
    					}
    				}
    				else {
    					load(clazz);
    				}
    			}
    			else if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
    				S86Powers.log(Level.WARNING, LocaleString.INVALID_POWER_MANIFEST.build(name));
    			}
    		}
        } catch (Throwable e) {
			e.printStackTrace();
            return;
        }
	}
	
	private void load(Class<?> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (clazz.getSuperclass().equals(Power.class)) {
			if (!S86Powers.getConfigManager().isBlocked(clazz.getSimpleName())) {
				Power power = clazz.asSubclass(Power.class).getDeclaredConstructor().newInstance();
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					S86Powers.log(Level.INFO, LocaleString.POWER_LOAD_SUCCESS.build(power));
				}
				power.setEnabled(true);
				S86Powers.getConfigManager().addPower(power);
			}
			else {
				S86Powers.log(Level.WARNING, LocaleString.POWER_LOAD_BLOCKED.build(clazz));
			}
		}
	}
	
	private Class<?> resolveClass(ClassLoader loader, String name) throws ClassNotFoundException {
		Class<?> clazz = null;
		for (int i = 0; i < TYPE_PREFIX.length; i ++) {
			clazz = loader.loadClass(POWER_PREFIX + TYPE_PREFIX[i] + name);
			if (clazz != null) {
				break;
			}
		}
		return clazz;
	}
	
}
