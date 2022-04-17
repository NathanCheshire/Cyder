package cyder.utilities;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Static utilities having to do with files, their names, properties, and attributes.
 */
public class FileUtil {
    /**
     * The image formats Cyder supports.
     */
    public static final String[] SUPPORTED_IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg"};

    /**
     * The metadata signature for a png file.
     */
    public static final int[] PNG_SIGNATURE = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    /**
     * The metadata signature for a jpg file.
     */
    public static final int[] JPG_SIGNATURE = {0xFF, 0xD8, 0xFF};

    /**
     * The audio formats Cyder supports.
     */
    public static final String[] SUPPORTED_AUDIO_EXTENSIONS = {".wav", ".mp3"};

    /**
     * The metadata signature for a wav file (RIFF).
     */
    public static final int[] WAV_SIGNATURE = {0x52, 0x49, 0x46, 0x46};

    /**
     * The metadata signature for an mp3 file.
     */
    public static final int[] MP3_SIGNATURE = {0x49, 0x44, 0x33};

    /**
     * Suppress default constructor.
     */
    private FileUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Returns whether the provided file is a supported image file by validating
     * the file extension and the file byte signature.
     *
     * @param f the file to determine if it is a supported image type
     * @return whether the provided file is a supported image file
     */
    public static boolean isSupportedImageExtension(File f) {
        checkNotNull(f);

        return StringUtil.in(getExtension(f.getName()), true, SUPPORTED_IMAGE_EXTENSIONS)
                && (matchesSignature(f, PNG_SIGNATURE) || matchesSignature(f, JPG_SIGNATURE));
    }

    /**
     * Returns whether the provided file is a supported audio file by validating
     * the file extension and the file byte signature.
     *
     * @param f the file to determine if it is a supported audio type
     * @return whether the provided file is a supported audio file
     */
    public static boolean isSupportedAudioExtension(File f) {
        checkNotNull(f);

        return StringUtil.in(getExtension(f.getName()), true, SUPPORTED_AUDIO_EXTENSIONS)
                && (matchesSignature(f, WAV_SIGNATURE) || matchesSignature(f, MP3_SIGNATURE));
    }

    /**
     * Returns whether the given file matches the provided signature.
     * Example: passing a png image and an integer array of "89 50 4E 47 0D 0A 1A 0A"
     *          should return true
     *
     * @param file the file to validate
     * @param expectedSignature the expected file signature bytes
     * @return whether the given file matches the provided signature
     */
    public static boolean matchesSignature(File file, int[] expectedSignature) {
        if (file == null || expectedSignature == null || expectedSignature.length == 0)
            return false;
        if (!file.exists())
            return false;

        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            int[] headerBytes = new int[expectedSignature.length];

            for (int i = 0; i < expectedSignature.length; i++) {
                headerBytes[i] = inputStream.read();

                if (headerBytes[i] != expectedSignature[i]) {
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
        return file.replaceAll("\\.([^.]+)$", "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     *
     * @param file the name of the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(String file) {
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
        return file.getName().replaceAll("\\.([^.]+)$", "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     *
     * @param file the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(File file) {
        return file.getName().replace(getFilename(file), "");
    }

    /**
     * Returns whether the provided file ends in the expected extension.
     *
     * @param file the file to validate the extension again
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
     * @param file the file to validate the extension again
     * @param expectedExtensions the expected extensions such as ".json", ".mp3", ".png", etc.
     * @return whether the provided file ends in one of the expected extension
     */
    public static boolean validateExtension(File file, String... expectedExtensions) {
        checkNotNull(file);
        checkNotNull(expectedExtensions);
        checkArgument(expectedExtensions.length > 0);

        return StringUtil.in(getExtension(file), false, expectedExtensions);
    }

    /**
     * Returns whether the file's name without the extension matches the expected name.
     *
     * @param file the file
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
     * Supported font types that are loaded upon Cyder's start.
     */
    public static final String[] SUPPORTED_FONT_EXTENSIONS = {".ttf"};

    /**
     * The metadata signature for a ttf file.
     */
    public static final int[] TTF_SIGNATURE = {0x00, 0x01, 0x00, 0x00, 0x00, 0x10, 0x01, 0x00, 0x00, 0x04, 0x00, 0x00};

    /**
     * Returns whether the contents of the two files are equal.
     *
     * @param fileOne the first file
     * @param fileTwo the second file
     * @return whether the contents of the two file are equal
     */
    public static boolean fileContentsEqual(File fileOne, File fileTwo) {
        if (fileOne == null || fileTwo == null)
            return false;

        if (!fileOne.exists() || !fileTwo.exists())
            return false;

       boolean ret;

        try {
            ret = com.google.common.io.Files.equal(fileOne, fileTwo);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            ret = false;
        }

        return ret;
    }

    /**
     * Zips the provided file/folder and deletes the original if successful and requested.
     *
     * @param source the file/dir to zip
     * @param destination the destination of the zip archive
     */
    public static void zip(String source, String destination)  {
        checkNotNull(source);
        checkNotNull(destination);

        String usedFileName;

        try {

            if (new File(destination).exists()) {
                int incrementer = 1;
                usedFileName = destination.replace(".zip","") + "_" + incrementer + ".zip";

                while (new File(usedFileName).exists()) {
                    incrementer++;
                    usedFileName = destination.replace(".zip","") + "_" + incrementer + ".zip";
                }
            } else {
                usedFileName = destination;
            }

            Path zipFile = Files.createFile(Paths.get(usedFileName));
            Path sourceDirPath = Paths.get(source);

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile));
                 Stream<Path> paths = Files.walk(sourceDirPath)) {
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
    public static final int BUFFER_SIZE = 1024;

    /**
     * Unzips the provided zip directory to the provided directory.
     *
     * @param sourceZip the source zip file
     * @param destinationFolder the folder to save the contents of the zip to
     * @return whether the unzipping process was successful
     */
    @CanIgnoreReturnValue /* some callers don't care */
    public static boolean unzip(File sourceZip, File destinationFolder) {
        checkNotNull(sourceZip);
        checkNotNull(destinationFolder);
        checkArgument(sourceZip.exists());
        checkArgument(destinationFolder.exists());

        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            FileInputStream fis = new FileInputStream(sourceZip);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry zentry = zis.getNextEntry();

            // for all zip entries
            while (zentry != null) {
                File zippedFile = OSUtil.buildFile(destinationFolder.getAbsolutePath(), zentry.getName());

                // ensure parents of zentry exist
                File zentryParent = new File(zippedFile.getParent());
                zentryParent.mkdirs();

                FileOutputStream fos = new FileOutputStream(zippedFile);

                int len;

                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                // clean up
                fos.close();
                zis.closeEntry();

                zentry = zis.getNextEntry();
            }

            // clean up
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            return false;
        }

        return true;
    }
}
