package com.zak.modsync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.FileInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.Arrays;


import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.IModFile;

public class ModZipFile {

	private static Logger LOGGER = LogUtils.getLogger();
	private static File modZipFile;
	private static boolean generating = false;
	
	public static void generateModZipFile() throws IOException {
		LOGGER.debug("Generating mod zip file...");

		File incompleteModZipFile = new File("mods.zip");
		ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(incompleteModZipFile)));
		
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
			zipOutputStream.putNextEntry(entry);
			
			FileInputStream modFileInputStream = new FileInputStream(modFile.getFilePath().toFile());
			
			int bytesRead = 0;
            final int bufferSize = 4096;
			do {
				byte[] block = new byte[bufferSize];
				bytesRead = modFileInputStream.read(block);
				zipOutputStream.write(block, 0, bytesRead);
			} while (bytesRead == bufferSize);
			
            modFileInputStream.close();
			zipOutputStream.closeEntry();
			LOGGER.debug("Finished packing mod: " + modFile.getFileName());
		}
		
		zipOutputStream.close();
		
		modZipFile = incompleteModZipFile;
		LOGGER.debug("Finished generating mod zip file");
		generateHash();
		generating = false;
	}

	public static void generateHash() {
		LOGGER.debug("Generating hash for mods.zip...");
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

			BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(modZipFile));

			int bytesRead = 0;
			final int bufferSize = 4096;
			do {
				byte[] block = new byte[bufferSize];
				bytesRead = fileInputStream.read(block);
				messageDigest.update(block, 0, bytesRead);
			} while (bytesRead == bufferSize);
			byte[] hash = messageDigest.digest();
			fileInputStream.close();

			BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(new File("mods.zip.sha256")));
			fileOutputStream.write(hash);
			fileOutputStream.close();
		} catch (IOException exception) {
			LOGGER.error("IOException", exception);
		} catch (NoSuchAlgorithmException exception) {
			LOGGER.error("could not use the sha256 hashing algorithm", exception);
		}
		LOGGER.debug("Generated hash for mods.zip");
	}

	public static boolean verifyExistingModZipFile() {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

			File modsZipFile = new File("mods.zip");
			BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(modsZipFile));
			
			int bytesRead = 0;
			final int bufferSize = 4096;
			do {
				byte[] block = new byte[bufferSize];
				bytesRead = fileStream.read(block);
				messageDigest.update(block, 0, bytesRead);
			} while (bytesRead == bufferSize);
			byte[] generatedHash = messageDigest.digest();
			fileStream.close();

			fileStream = new BufferedInputStream(new FileInputStream(new File("mods.zip.sha256")));
			byte[] storedHash = fileStream.readAllBytes();
			fileStream.close();

			return Arrays.equals(storedHash, generatedHash);
		} catch (IOException exception) {
			LOGGER.error("Could not get hash from file 'mods.zip.sha256'", exception);
		} catch (NoSuchAlgorithmException exception) {
			LOGGER.error("Could not use the sha256 hashing algorithm", exception);
		}

		return false;
	}
	
	public static File getModZipFile() {
		if (modZipFile == null && !generating) {
			if (verifyExistingModZipFile()) {
				LOGGER.debug("Using already generated mods.zip file ^-^");
				modZipFile = new File("mods.zip");
			} else {
				generating = true;
				new Thread(() -> {
					try {
						generateModZipFile();
					} catch (IOException e) {
						LOGGER.error("Failed to generate mod zip file", e);
					}
				}).start();
			}
		}
		
		return modZipFile;
	}
	
}
