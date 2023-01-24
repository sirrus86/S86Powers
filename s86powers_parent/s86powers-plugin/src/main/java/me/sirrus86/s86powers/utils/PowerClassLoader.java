package me.sirrus86.s86powers.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class PowerClassLoader extends ClassLoader {

	private final URL base;
	
	public PowerClassLoader(final URL url) {
		base = url;
	}
	
	@Override
	public Class<?> loadClass(final String name, final boolean resolve) {
		Class<?> clazz = findLoadedClass(name);
		if (clazz == null) {
			try {
				clazz = Class.forName(name);
				return clazz;
			} catch (final Throwable ignored) {
			}
		}
		if (clazz == null) {
			try {
				final InputStream in = getResourceAsStream(name.replace('.', '/') + ".class");
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final byte[] buffer = new byte[IOHelper.BYTE_BUFFER];
				int len;
				while (in != null && (len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				final byte[] bytes = out.toByteArray();
				try {
					clazz = defineClass(name, bytes, 0, bytes.length);
				}
				catch (Throwable ignored) {
				}
				if (clazz != null
						&& resolve) {
					resolveClass(clazz);
				}
				out.flush();
				out.close();
			} catch (final Exception e) {
				if (clazz == null) {
					try {
						clazz = findSystemClass(name);
					} catch (ClassNotFoundException ignored) {
					}
				}
				if (clazz == null) {
					try {
						super.loadClass(name, resolve);
					} catch (ClassNotFoundException ignored) {}
				}
			}
		}
		return clazz;
	}
	
	@Override
	public URL getResource(final String name) {
		try {
			return new URL(base, name);
		} catch (final MalformedURLException e) {
			return null;
		}
	}
	
	@Override
	public InputStream getResourceAsStream(final String name) {
		try {
			return new URL(base, name).openStream();
		} catch (final IOException e) {
			return null;
		}
	}
	
}
