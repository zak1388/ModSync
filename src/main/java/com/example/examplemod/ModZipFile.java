package com.example.examplemod;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;


import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.IModFile;

public class ModZipFile {

	private static Logger LOGGER = LogUtils.getLogger();
	private static File modZipFile;
	private static boolean generating = false;
	
	public static void generateModZipFile() throws IOException {
		LOGGER.debug("Generating mod zip file...");

		File incompleteModZipFile = new File("mods.zip");
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(incompleteModZipFile)));
		
		ModList modList = ModList.get();
		for (int i = 0; i < modList.size(); i++) {
			IModFile modFile = modList.getModFiles().get(i).getFile();
			
			LOGGER.debug("Packing mod: " + modFile.getFileName());

			try {
				if (!modFile.getFilePath().toFile().canRead() || modFile.getFilePath().toFile().isDirectory()) {
					throw new RuntimeException("Cannot read mod file");
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to pack mod: " + modFile.getFileName());
				continue;
			}
			
			ZipEntry entry = new ZipEntry(modFile.getFileName());
			zos.putNextEntry(entry);
			
			FileInputStream modFileInputStream = new FileInputStream(modFile.getFilePath().toFile());
			int lastByte = modFileInputStream.read();
			while (lastByte != -1) {
				zos.write(lastByte);
				lastByte = modFileInputStream.read();
			}
			
			zos.closeEntry();
			LOGGER.debug("Finished packing mod: " + modFile.getFileName());
		}
		
		zos.close();
		
		modZipFile = incompleteModZipFile;
		LOGGER.debug("Finished generating mod zip file");
		generating = false;
	}
	
	public static File getModZipFile() {
		if (modZipFile == null && !generating) {
			generating = true;
			new Thread(() -> {
				try {
					generateModZipFile();
				} catch (IOException e) {
					LOGGER.error("Failed to generate mod zip file because " + e.getMessage() + 
							Stream.of(e.getStackTrace())
							.map(StackTraceElement::toString)
							.map((string) -> "\n\t at " + string)
							.collect(Collectors.joining())
					);
				}
			}).start();
		}
		
		return modZipFile;
	}
	
}
