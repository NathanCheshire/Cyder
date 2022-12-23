package main.java.cyder.files;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import main.java.cyder.enums.Extension;
import main.java.cyder.exceptions.FatalException;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.strings.StringUtil;
import main.java.cyder.utils.ArrayUtil;
import main.java.cyder.utils.OsUtil;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Static utilities having to do with files, their names, properties, and attributes.
 */
public final class FileUtil {
    /**
     * The metadata signature for a png file.
     */
    public static final ImmutableList<Integer> PNG_SIGNATURE
            = ImmutableList.of(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);

    /**
     * The metadata signature for a jpg file.
     */
    public static final ImmutableList<Integer> JPG_SIGNATURE = ImmutableList.of(0xFF, 0xD8, 0xFF);

    /**
     * The metadata signature for a wav file (RIFF).
     */
    public static final ImmutableList<Integer> WAV_SIGNATURE = ImmutableList.of(0x52, 0x49, 0x46, 0x46);

    /**
     * The metadata signature for an mp3 file.
     */
    public static final ImmutableList<Integer> MP3_SIGNATURE = ImmutableList.of(0x49, 0x44, 0x33);

    /**
     * The audio formats Cyder supports.
     */
    public static final ImmutableList<String> SUPPORTED_AUDIO_EXTENSIONS
            = ImmutableList.of(Extension.WAV.getExtension(), Extension.MP3.getExtension());

    /**
     * The image formats Cyder supports.
     */
    public static final ImmutableList<String> SUPPORTED_IMAGE_EXTENSIONS = ImmutableList.of(
            Extension.PNG.getExtension(), Extension.JPG.getExtension(), Extension.JPEG.getExtension());

    /**
     * Supported font types that are loaded upon Cyder's start.
     */
    public static final ImmutableList<String> SUPPORTED_FONT_EXTENSIONS
            = ImmutableList.of(Extension.TTF.getExtension());

    /**
     * The signature for true-type font file formats.
     */
    public static final ImmutableList<Integer> TTF_SIGNATURE = ImmutableList.of(0x00, 0x01, 0x00, 0x00, 0x00);

    /**
     * The regex string for extracting the filename from the extension.
     */
    private static final String filenameRegex = "\\.([^.]+)$";

    /**
     * Suppress default constructor.
     */
    private FileUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns whether the provided file is a supported image file by validating
     * the file extension and the file byte signature.
     *
     * @param file the file to determine if it is a supported image type
     * @return whether the provided file is a supported image file
     */
    public static boolean isSupportedImageExtension(File file) {
        checkNotNull(file);

        return StringUtil.in(getExtension(file.getName()), true, SUPPORTED_IMAGE_EXTENSIONS)
                && (fileMatchesSignature(file, PNG_SIGNATURE) || fileMatchesSignature(file, JPG_SIGNATURE));
    }

    /**
     * Returns whether the provided file is a supported audio file by validating
     * the file extension and the file byte signature.
     *
     * @param file the filename to determine if it is a supported audio type
     * @return whether the provided file is a supported audio file
     */
    public static boolean isSupportedAudioExtension(File file) {
        checkNotNull(file);

        return StringUtil.in(getExtension(file.getName()), true, SUPPORTED_AUDIO_EXTENSIONS)
                && (fileMatchesSignature(file, WAV_SIGNATURE) || fileMatchesSignature(file, MP3_SIGNATURE));
    }

    /**
     * Returns whether the provided file is a supported font file.
     *
     * @param file the file to validate
     * @return whether the provided file is a supported font file
     */
    public static boolean isSupportedFontExtension(File file) {
        checkNotNull(file);

        return StringUtil.in(getExtension(file.getName()), true, SUPPORTED_FONT_EXTENSIONS)
                && fileMatchesSignature(file, TTF_SIGNATURE);
    }

    /**
     * Returns whether the given file matches the provided signature.
     * Example: passing a png image and an integer array of "89 50 4E 47 0D 0A 1A 0A"
     * should return true
     *
     * @param file              the file to validate
     * @param expectedSignature the expected file signature bytes
     * @return whether the given file matches the provided signature
     */
    public static boolean fileMatchesSignature(File file, ImmutableList<Integer> expectedSignature) {
        checkNotNull(file);
        checkArgument(file.exists());
        checkNotNull(expectedSignature);
        checkArgument(!expectedSignature.isEmpty());

        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            int[] headerBytes = new int[expectedSignature.size()];

            for (int i = 0 ; i < expectedSignature.size() ; i++) {
                headerBytes[i] = inputStream.read();

                if (headerBytes[i] != expectedSignature.get(i)) {
                    return false;
                }
            }
        } catch (IOException ex) {
            ExceptionHandler.handle(ex);
            return false;
        }

        return true;
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     *
     * @param file the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()} )} to get the full filename + extension)
     * @return the file name requested
     */
    public static String getFilename(String file) {
        checkNotNull(file);
        checkArgument(!file.isEmpty());

        return file.replaceAll(filenameRegex, "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     *
     * @param file the name of the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(String file) {
        checkNotNull(file);
        checkArgument(!file.isEmpty());

        return file.replace(getFilename(file), "");
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     *
     * @param file the name of the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()})} to get the full filename + extension)
     * @return the file name requested
     */
    public static String getFilename(File file) {
        checkNotNull(file);

        return file.getName().replaceAll(filenameRegex, "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     *
     * @param file the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(File file) {
        checkNotNull(file);

        return file.getName().replace(getFilename(file), "");
    }

    /**
     * Uses a regex to get the file extension of the provided file.
     *
     * @param file the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtensionWithoutPeriod(File file) {
        checkNotNull(file);
        checkArgument(!file.getName().isEmpty());

        String filename = getFilename(file);
        String withoutFilename = file.getName().replace(filename, "");
        if (withoutFilename.length() > 1) {
            return withoutFilename.substring(1);
        }

        return "";
    }

    /**
     * Returns whether the provided file ends in the expected extension.
     *
     * @param file              the file to validate the extension again
     * @param expectedExtension the expected extension such as ".json"
     * @return whether the provided file ends in the expected extension
     */
    public static boolean validateExtension(File file, String expectedExtension) {
        checkNotNull(file);
        checkNotNull(expectedExtension);
        checkArgument(!expectedExtension.isEmpty());

        return getExtension(file).equalsIgnoreCase(expectedExtension);
    }

    /**
     * Returns whether the provided file ends in one of the expected extensions.
     *
     * @param file               the file to validate the extension again
     * @param expectedExtensions the expected extensions such as ".json", ".mp3", ".png", etc.
     * @return whether the provided file ends in one of the expected extension
     */
    public static boolean validateExtension(File file, String expectedExtension, String... expectedExtensions) {
        checkNotNull(file);
        checkNotNull(expectedExtension);
        checkArgument(!expectedExtension.isEmpty());
        checkNotNull(expectedExtensions);
        checkArgument(expectedExtensions.length > 0);

        ImmutableList<String> extensions = new ImmutableList.Builder<String>()
                .add(expectedExtension)
                .addAll(ArrayUtil.toList(expectedExtensions)).build();

        return StringUtil.in(getExtension(file), false, extensions);
    }

    /**
     * Returns whether the provided file ends in one of the expected extensions.
     *
     * @param file               the file to validate the extension again
     * @param expectedExtensions the expected extensions such as ".json", ".mp3", ".png", etc
     * @return whether the provided file ends in one of the expected extension
     */
    public static boolean validateExtension(File file, Collection<String> expectedExtensions) {
        checkNotNull(file);
        checkNotNull(expectedExtensions);
        checkArgument(expectedExtensions.size() > 0);

        return StringUtil.in(getExtension(file), false, expectedExtensions);
    }

    /**
     * Returns whether the file's name without the extension matches the expected name.
     *
     * @param file         the file
     * @param expectedName the expected name
     * @return whether the file's name without the extension matches the expected name
     */
    public static boolean validateFileName(File file, String expectedName) {
        checkNotNull(file);
        checkNotNull(expectedName);
        checkArgument(!expectedName.isEmpty());

        return getFilename(file).equals(expectedName);
    }

    /**
     * Returns whether the contents of the two files are equal.
     *
     * @param fileOne the first file
     * @param fileTwo the second file
     * @return whether the contents of the two file are equal
     */
    public static boolean fileContentsEqual(File fileOne, File fileTwo) {
        checkNotNull(fileOne);
        checkNotNull(fileTwo);

        if (!fileOne.exists() || !fileTwo.exists()) return false;

        try {
            return com.google.common.io.Files.equal(fileOne, fileTwo);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    /**
     * Zips the provided file/folder and deletes the original if successful and requested.
     *
     * @param source      the file/dir to zip
     * @param destination the destination of the zip archive
     */
    public static void zip(String source, String destination) {
        checkNotNull(source);
        checkNotNull(destination);
        checkArgument(!source.isEmpty());
        checkArgument(!destination.isEmpty());

        String usedFileName;

        try {
            if (new File(destination).exists()) {
                int incrementer = 1;
                usedFileName = destination.replace(Extension.ZIP.getExtension(), "")
                        + CyderStrings.underscore + incrementer + Extension.ZIP.getExtension();

                while (new File(usedFileName).exists()) {
                    incrementer++;
                    usedFileName = destination.replace(Extension.ZIP.getExtension(), "")
                            + CyderStrings.underscore + incrementer + Extension.ZIP.getExtension();
                }
            } else {
                usedFileName = destination;
            }

            Path zipFile = Files.createFile(Paths.get(usedFileName));
            Path sourceDirPath = Paths.get(source);

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))
                 ; Stream<Path> paths = Files.walk(sourceDirPath)) {
                paths.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                    try {
                        zipOutputStream.putNextEntry(zipEntry);
                        Files.copy(path, zipOutputStream);
                        zipOutputStream.closeEntry();
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                });
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The buffer sized used for zip file extraction.
     */
    public static final int ZIP_BUFFER_SIZE = 1024;

    /**
     * Unzips the provided zip directory to the provided directory.
     *
     * @param sourceZip         the source zip file
     * @param destinationFolder the folder to save the contents of the zip to
     * @return whether the unzipping process was successful
     */
    @CanIgnoreReturnValue /* some callers don't care */
    public static boolean unzip(File sourceZip, File destinationFolder) {
        checkNotNull(sourceZip);
        checkNotNull(destinationFolder);
        checkArgument(sourceZip.exists());
        checkArgument(destinationFolder.exists());

        byte[] buffer = new byte[ZIP_BUFFER_SIZE];

        try {
            FileInputStream fis = new FileInputStream(sourceZip);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry zipEntry = zis.getNextEntry();

            // for all zip entries
            while (zipEntry != null) {
                File zippedFile = OsUtil.buildFile(destinationFolder.getAbsolutePath(), zipEntry.getName());

                // ensure parents of zip entry exist
                File zipEntryParent = new File(zippedFile.getParent());
                boolean made = zipEntryParent.mkdirs();

                if (!made) {
                    throw new IOException("Failed to create parent zip");
                }

                FileOutputStream fos = new FileOutputStream(zippedFile);

                int len;

                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                // clean up
                closeIfNotNull(fos);
                zis.closeEntry();

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
            closeIfNotNull(zis);
            closeIfNotNull(fis);
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            return false;
        }

        return true;
    }

    /**
     * Closes the provided object which implements {@link Closeable}.
     *
     * @param closable the object to close and free
     */
    @SuppressWarnings("UnusedAssignment") /* Freeing up resource */
    public static void closeIfNotNull(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
                closable = null;
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    /**
     * Returns a list of all files contained within the startDir and subdirectories
     * that have the specified extension.
     *
     * @param startDir  the starting directory
     * @param extension the specified extension. Ex. ".java" (Pass null to ignore file extensions)
     * @return an ArrayList of all files with the given extension found within the startDir and
     * subdirectories
     */
    public static ImmutableList<File> getFiles(File startDir, String extension) {
        checkNotNull(startDir);
        checkArgument(startDir.exists());
        checkNotNull(extension);
        checkArgument(!extension.isEmpty());

        // init return set
        ArrayList<File> ret = new ArrayList<>();

        // should be directory but test anyway
        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files == null) return ImmutableList.copyOf(ret);

            Arrays.stream(files).forEach(file -> ret.addAll(getFiles(file, extension)));
        } else if (getExtension(startDir).equals(extension)) {
            ret.add(startDir);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Determines a unique name for the provided file so that it may be placed
     * in the provided directory without collisions.
     * Note that this returns the complete filename and not a created {@link File} object.
     *
     * @param file      the file to find a unique name for
     * @param directory the directory to place the file in
     * @return a unique name for the file. Note this may or may not equal
     * the original file name
     */
    public static String constructUniqueName(File file, File directory) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        File[] files = directory.listFiles();
        if (files == null || ArrayUtil.isEmpty(files)) return file.getName();

        ArrayList<String> filenames = new ArrayList<>(files.length);
        Arrays.stream(files).forEach(neighboringFile -> filenames.add(neighboringFile.getName()));

        String filenameAndExtension = file.getName();
        String[] filenameAndExtensionArr = filenameAndExtension.split("\\.");
        String name = filenameAndExtensionArr[0];
        String extension = filenameAndExtensionArr[1];

        String ret = filenameAndExtension;

        int number = 1;
        while (StringUtil.in(ret, true, filenames)) {
            ret = name + CyderStrings.underscore + number + "." + extension;
            number++;
        }

        return ret;
    }

    /**
     * Returns an immutable list of files found within the provided directory and all sub-directories.
     *
     * @param topLevelDirectory the top level directory to search for files in
     * @return an immutable list of files found within the provided directory and all sub-directories
     */
    public static ImmutableList<File> getFiles(File topLevelDirectory) {
        return getFiles(topLevelDirectory, true);
    }

    /*
     * Returns an immutable list of files found within the provided directory.
     *
     * @param topLevelDirectory the top level directory to search for files in
     * @param recursive whether to find files recursively starting from the provided directory
     * @return an immutable list of files found within the provided directory
     */
    public static ImmutableList<File> getFiles(File topLevelDirectory, boolean recursive) {
        return getFiles(topLevelDirectory, recursive, "");
    }

    /**
     * Returns an immutable list of files found within the provided directory.
     *
     * @param topLevelDirectory the top level directory to search for files in
     * @param recursive         whether to find files recursively starting from the provided directory
     * @param extensionRegex    the regex to match extensions for such as "(txt|jpg)"
     * @return an immutable list of files found within the provided directory
     */
    public static ImmutableList<File> getFiles(File topLevelDirectory, boolean recursive, String extensionRegex) {
        Preconditions.checkNotNull(topLevelDirectory);
        Preconditions.checkArgument(topLevelDirectory.exists());
        Preconditions.checkArgument(topLevelDirectory.isDirectory());
        Preconditions.checkNotNull(extensionRegex);

        File[] topLevelFiles = topLevelDirectory.listFiles();
        if (topLevelFiles == null || ArrayUtil.isEmpty(topLevelFiles)) return ImmutableList.of();

        ArrayList<File> ret = new ArrayList<>();

        for (File file : topLevelFiles) {
            if (file.isFile()) {
                String extension = FileUtil.getExtension(file).substring(1);

                if (extensionRegex.isEmpty() || extension.matches(extensionRegex)) {
                    ret.add(file);
                }
            } else if (recursive && file.isDirectory()) {
                ret.addAll(getFiles(file, true, extensionRegex));
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns a list of folders found within the provided directory.
     *
     * @param topLevelDirectory the directory to find folders within
     * @return a list of folders found within the provided directory
     */
    public static ImmutableList<File> getFolders(File topLevelDirectory) {
        return getFolders(topLevelDirectory, true);
    }

    /**
     * Returns a list of folders found within the provided directory.
     *
     * @param topLevelDirectory the directory to find folders within
     * @param recursive         whether to recurse from the top level directory
     * @return a list of folders found within the provided directory
     */
    public static ImmutableList<File> getFolders(File topLevelDirectory, boolean recursive) {
        Preconditions.checkNotNull(topLevelDirectory);
        Preconditions.checkArgument(topLevelDirectory.exists());
        Preconditions.checkArgument(topLevelDirectory.isDirectory());

        LinkedList<File> ret = new LinkedList<>();

        File[] topLevelFiles = topLevelDirectory.listFiles();

        if (topLevelFiles != null && topLevelFiles.length > 0) {
            Arrays.stream(topLevelFiles).forEach(file -> {
                if (file.isDirectory()) {
                    ret.add(file);

                    if (recursive) {
                        ret.addAll(getFolders(file, true));
                    }
                }
            });
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Reads the contents of the provided file and returns the collective contents in a singular string.
     *
     * @param file the file whose contents to read
     * @return the contents of the file
     * @throws IOException if reading the file fails
     */
    public static String readFileContents(File file) throws IOException {
        return Files.readString(Path.of(file.getAbsolutePath()));
    }

    /**
     * Returns a hex string for the provided binary file.
     *
     * @param file the binary file of pure binary contents
     * @return the String of hex data from the file
     */
    public static String getHexString(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.BIN.getExtension()));

        try {
            BufferedReader fis = new BufferedReader(new FileReader(file));
            String[] stringBytes = fis.readLine().split("(?<=\\G........)");
            StringBuilder sb = new StringBuilder();

            for (String stringByte : stringBytes) {
                sb.append(Integer.toString(Integer.parseInt(stringByte, 2), 16));
            }

            fis.close();
            return sb.toString();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new FatalException("Could not read binary file");
    }

    /**
     * Returns a binary string for the provided binary file.
     *
     * @param file the binary file of pure binary contents
     * @return the String of binary data from the file
     */
    public static String getBinaryString(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.BIN.getExtension()));

        try {
            BufferedReader fis = new BufferedReader(new FileReader(file));
            String stringBytes = fis.readLine();
            fis.close();
            return stringBytes;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new FatalException("Could not read binary file");
    }

    /**
     * Opens the provided resource.
     *
     * @param resource                  the resource to open
     * @param useCyderHandlerIfPossible whether to attempt to open the resource using a Cyder handler if possible
     */
    public static void openResource(String resource, boolean useCyderHandlerIfPossible) {
        File referenceFile = new File(resource);
        boolean referenceFileExists = referenceFile.exists();

        if (referenceFileExists && useCyderHandlerIfPossible) {
            for (CyderFileHandler handler : CyderFileHandler.values()) {
                if (handler.shouldUseForFile(referenceFile)) {
                    handler.open(referenceFile);
                    return;
                }
            }
        }

        openResourceUsingNativeProgram(resource);
    }

    /**
     * Opens the provided resource using the native {@link Desktop}.
     * This could be a file, directory, url, link, etc.
     *
     * @param resource the resource to open
     */
    public static void openResourceUsingNativeProgram(String resource) {
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(!resource.isEmpty());

        try {
            File filePointer = new File(resource);
            if (filePointer.exists()) {
                Desktop.getDesktop().open(filePointer);
                Logger.log(LogTag.SYSTEM_IO, "Opening file: " + filePointer.getAbsolutePath());
            } else {
                Desktop.getDesktop().browse(new URI(resource));
                Logger.log(LogTag.LINK, resource);
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Returns a list of lines from the provided file.
     *
     * @param file a file
     * @return a list of lines from the provided file
     */
    public static ImmutableList<String> getFileLines(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());

        ArrayList<String> ret = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ret.add(line);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Writes the provided lines to the provided file with newlines after each written line.
     *
     * @param file   the file to write the lines too
     * @param lines  the lines to write
     * @param append whether to append the lines or overwrite the existing content, if any, with the new content
     */
    public static void writeLinesToFile(File file, List<String> lines, boolean append) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkNotNull(lines);
        Preconditions.checkArgument(!lines.isEmpty());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
