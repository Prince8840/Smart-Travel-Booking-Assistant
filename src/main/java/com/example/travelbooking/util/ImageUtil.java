package com.example.travelbooking.util;

import javax.swing.*;
import java.awt.*;

public class ImageUtil {
    private static final String BASE = "cities/";
    private static final String[] EXTS = {"jpg","jpeg","png","gif","bmp"};

    public static ImageIcon loadFirstFromCityFolder(String city, int w, int h) {
        java.util.List<java.net.URL> urls = listCityResourceUrls(city);
        if (!urls.isEmpty()) return scale(urls.get(0), w, h);
        return null;
    }

    public static java.util.List<ImageIcon> loadAllFromCityFolder(String city, int w, int h) {
        java.util.List<ImageIcon> list = new java.util.ArrayList<>();
        for (java.net.URL url : listCityResourceUrls(city)) {
            ImageIcon ic = scale(url, w, h);
            if (ic != null) list.add(ic);
        }
        return list;
    }

    private static java.util.List<java.net.URL> listCityResourceUrls(String city) {
        java.util.List<java.net.URL> urls = new java.util.ArrayList<>();
        String prefix = BASE + city + "/";
        ClassLoader cl = ImageUtil.class.getClassLoader();
        try {
            // If running from classes folder (development), read filesystem
            java.net.URL dirUrl = cl.getResource(prefix);
            if (dirUrl != null && dirUrl.getProtocol().equals("file")) {
                java.nio.file.Path p = java.nio.file.Paths.get(dirUrl.toURI());
                try (java.util.stream.Stream<java.nio.file.Path> s = java.nio.file.Files.list(p)) {
                    s.filter(fp -> {
                        String n = fp.getFileName().toString().toLowerCase();
                        for (String e : EXTS) if (n.endsWith("."+e)) return true;
                        return false;
                    }).sorted().forEach(fp -> {
                        try { urls.add(fp.toUri().toURL()); } catch (Exception ignored) {}
                    });
                }
                return urls;
            }
            // If packaged in a jar, enumerate entries from the jar
            java.net.URL codeSrc = ImageUtil.class.getProtectionDomain().getCodeSource().getLocation();
            java.nio.file.Path jarPath = java.nio.file.Paths.get(codeSrc.toURI());
            if (java.nio.file.Files.isRegularFile(jarPath) && jarPath.toString().endsWith(".jar")) {
                try (java.util.jar.JarFile jf = new java.util.jar.JarFile(jarPath.toFile())) {
                    java.util.Enumeration<java.util.jar.JarEntry> en = jf.entries();
                    while (en.hasMoreElements()) {
                        java.util.jar.JarEntry je = en.nextElement();
                        String name = je.getName();
                        if (name.startsWith(prefix) && !name.endsWith("/")) {
                            String low = name.toLowerCase();
                            boolean ok = false;
                            for (String e : EXTS) if (low.endsWith("."+e)) { ok = true; break; }
                            if (ok) {
                                urls.add(new java.net.URL("jar:" + jarPath.toUri().toURL() + "!/" + name));
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return urls;
    }

    private static ImageIcon scale(java.net.URL url, int w, int h) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(url);
            if (img == null) return null;
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ignored) { return null; }
    }
}
