package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.core.Apathy;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String-munges 2.4-style janky config into 2.5-style forge toml so it at least doesn't reset *all* your settings.
 * i wrote this class in a haze, it might work. this is Not Robust. doesn't attempt to port over comments either
 * the expectation is that you will feed this file straight into the forge config loader and it will stomp on it
 */
public class LegacyToTomlUpgrader {
	public static void doIt() {
		Path dir = FMLPaths.CONFIGDIR.get();
		if(Files.exists(dir.resolve("apathy-boss.toml"))) return; //already done
		
		try {
			reallyDoIt(dir.resolve(Apathy.MODID).resolve("general.cfg"), dir.resolve("apathy-general.toml"));
			reallyDoIt(dir.resolve(Apathy.MODID).resolve("mobs.cfg"), dir.resolve("apathy-mobs.toml"));
			reallyDoIt(dir.resolve(Apathy.MODID).resolve("boss.cfg"), dir.resolve("apathy-boss.toml"));
			
			Path oldMobsJson = dir.resolve(Apathy.MODID).resolve("mobs.json");
			if(Files.exists(oldMobsJson)) {
				Apathy.instance.log.warn("MOVING MOBS.JSON FROM {} TO {}", oldMobsJson, Apathy.instance.mobsJsonPath());
				Files.copy(oldMobsJson, Apathy.instance.mobsJsonPath());
				Files.delete(oldMobsJson);
			}
		} catch (Exception e) {
			Apathy.instance.log.error("Problem upgrading old config: " + e.getMessage(), e);
		}
	}
	
	private static final Pattern headerPattern = Pattern.compile("^## (.*) ##$");
	private static final Pattern entryPattern = Pattern.compile("^([^#:]*):\\s*(.*)$");
	
	private static void reallyDoIt(Path oldPath, Path newPath) throws IOException {
		if(Files.notExists(oldPath)) return;
		if(Files.exists(newPath)) return;
		Apathy.instance.log.warn("UPGRADING OLD-FORMAT CONFIG AT {} TO TOML-FORMAT CONFIG AT {}", oldPath, newPath);
		
		Matcher m;
		List<String> newFile = new ArrayList<>(); newFile.add("[Uncategorized]");
		for(String oldLine : Files.readAllLines(oldPath, StandardCharsets.UTF_8)) {
			m = headerPattern.matcher(oldLine);
			if(m.matches()) {
				newFile.add("[\"" + m.group(1) + "\"]");
				continue;
			}
			m = entryPattern.matcher(oldLine);
			if(m.matches()) {
				String value = m.group(2).trim();
				boolean unquotedInToml = "true".equals(value) || "false".equals(value);
				try {
					Long.parseLong(value);
					unquotedInToml = true;
				} catch (Exception ignored) {}
				newFile.add(m.group(1).trim() + "=" + (unquotedInToml ? value : "\"" + value + "\""));
			}
		}
		
		if(newPath.getParent() != null) Files.createDirectories(newPath.getParent());
		Files.write(newPath, newFile, StandardCharsets.UTF_8);
		Files.delete(oldPath);
	}
}
