import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    public static String FILE_TEMP_NAME = "temp.txt";

    public static void main(String[] args) {

        Logger logger = new Logger();

        createFile("./Games", true, null, logger);
        createFile("./Games/src", true, null, logger);
        createFile("./Games/res", true, null, logger);
        createFile("./Games/savegames", true, null, logger);
        createFile("./Games/temp", true, null, logger);
        createFile("./Games/temp", false, FILE_TEMP_NAME, logger);

        GameProgress gameProgress1 = new GameProgress(100, 5, 10, 3);
        GameProgress gameProgress2 = new GameProgress(30, 7, 17, 10);
        GameProgress gameProgress3 = new GameProgress(75, 9, 25, 21);

        saveGame("./Games/savegames/", gameProgress1, logger);
        saveGame("./Games/savegames/", gameProgress2, logger);
        saveGame("./Games/savegames/", gameProgress3, logger);

        zipFiles("./Games/savegames", logger);
        deleteFiles("./Games/savegames", logger);
        openZip("./Games/savegames/zip_gameProgresses.zip", "./Games/savegames/", logger);
        openProgress("./Games/savegames/");

        String loggerResult = logger.getRecords();
        System.out.println(loggerResult);


        try (FileOutputStream fos = new FileOutputStream("./Games/temp/" + FILE_TEMP_NAME)) {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] bytes = loggerResult.getBytes(StandardCharsets.UTF_8);

            bos.write(bytes, 0, bytes.length);
            bos.flush();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }


    }


    public static void createFile(String path, boolean isDir, String fileName, Logger logger) {
        File file = null;

        if (isDir) {
            file = new File(path);
            boolean success = file.mkdirs();
            if (success) {
                logger.recordAction("Папка успешно создана по адресу \"" + path + "\"");
            } else {
                logger.recordAction("Папка с путём \"" + path + "\" не создана");
            }

        } else {
            file = new File(path, fileName);
            try {
                boolean success = file.createNewFile();
                if (success) {
                    logger.recordAction("Файл с именем  \"" + fileName + "\" успешно создан по адресу \"" + path + "\"");
                } else {
                    logger.recordAction("Файл с именем  \"" + fileName + "\" не создан");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void saveGame(String directoryPath, GameProgress gameProgress, Logger logger) {
        String date = Instant.now().toString();
        String fileName = "gameProgress" + "_" + date + ".dat";
        try (FileOutputStream fos = new FileOutputStream(directoryPath + fileName)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(gameProgress);
            logger.recordAction("Файл игрового прогресса с именем  \"" + fileName + "\" сохранён");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            logger.recordAction("Файл игрового прогресса с именем  \"" + fileName + "\" НЕ сохранён");
        }

    }

    public static void zipFiles(String directoryPath, Logger logger) {
        File dir = new File(directoryPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            String archiveName = "zip_gameProgresses.zip";
            if (files != null) {
                try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(directoryPath + "/" + archiveName))) {
                    logger.recordAction("Создан архив " + archiveName);


                    for (File file : files) {
                        String fileName = file.getName();

                        logger.recordAction("Архивирование файла: " + fileName);

                        try (FileInputStream fis = new FileInputStream(directoryPath + "/" + fileName)) {
                            ZipEntry entry = new ZipEntry(fileName);
                            zout.putNextEntry(entry);
                            byte[] buffer = new byte[fis.available()];
                            fis.read(buffer);
                            zout.write(buffer);
                            zout.closeEntry();
                            logger.recordAction("Файл " + fileName + " в архифе " + archiveName);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                            logger.recordAction("Файл " + fileName + " не попал в архиф " + archiveName);
                        }
                    }

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            } else {
                logger.recordAction("Каталог пуст: " + directoryPath);
            }
        } else {
            logger.recordAction("Каталог не существует или не является директорией: " + directoryPath);
        }
    }

    public static void deleteFiles(String directoryPath, Logger logger) {
        File dir = new File(directoryPath);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();

            for (File file : files) {
                String fileFullName = file.getName();
                String[] fileName = fileFullName.split("\\.");
                if (!fileName[fileName.length - 1].equals("zip")) {
                    file.delete();
                    logger.recordAction("Файл: " + fileFullName + " удалён");
                }
            }
        }

    }

    public static void openZip(String directoryPathZip, String directoryPathFromZip, Logger logger) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(directoryPathZip))) {
            ZipEntry entry;
            String name;

            while ((entry = zis.getNextEntry()) != null) {
                name = entry.getName();
                FileOutputStream fos = new FileOutputStream(directoryPathFromZip + "/" + name);
                for (int c = zis.read(); c != -1; c = zis.read()) {
                    fos.write(c);
                }

                fos.flush();
                zis.closeEntry();
                fos.close();

                logger.recordAction("Файл: " + name + " извлечён из архива");
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void openProgress(String directoryPath) {
        File dir = new File(directoryPath);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();

            if (files != null) {

                for (File file : files) {
                    GameProgress gameProgress = null;
                    String fileFullName = file.getName();
                    String[] fileName = fileFullName.split("\\.");
                    if (fileName[fileName.length - 1].equals("dat")) {
                        try (FileInputStream fis = new FileInputStream(directoryPath + "/" + fileFullName)) {
                            ObjectInputStream ois = new ObjectInputStream(fis);
                            gameProgress = (GameProgress) ois.readObject();
                            System.out.println(gameProgress);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }

                    }
                }
            }
        }
    }
}