

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FileUtils;

public class Main {

    static String destPathRede = "";
    static String ROOTDESTPATH = "Z:\\Supervisora\\RTA";
    //    static String ROOTDESTPATH = "D:\\Documentos\\Users\\Eduardo\\Downloads\\teste";
    static File ROOTDESTPATHILE = new File(ROOTDESTPATH);
    static String ROOTORIGINPATH = "D:\\Documentos\\Users\\Eduardo\\Documentos\\ANTT\\OneDrive - ANTT- Agencia Nacional de Transportes Terrestres\\CRO\\Relatórios RTA";
    static File ORIGINPATHFILE = new File(ROOTORIGINPATH);

    public static void main(String[] args) throws IOException {

        Scanner scanner = getScanner();

        File[] originFiles = ORIGINPATHFILE.listFiles();
        int i = 0;
        System.out.println("escolha:");
        for (File file : originFiles) {
            i++;
            System.out.printf("Digite %d. for %s \n", i, file.getName());

        }
        String option = scanner.nextLine();

        int optionInt = getNumberParsed(option);

        while (optionInt < 1 || optionInt > originFiles.length) {
            i = 0;
            System.out.println("escolha:");
            for (File file : originFiles) {
                i++;
                System.out.printf("Digite %d. for %s \n", i, file.getName());

            }
            option = scanner.nextLine();

            optionInt = getNumberParsed(option);
        }


        File chosenFile = originFiles[optionInt - 1];

        String optionStr = chosenFile.getName();

        String parent = "";


        if (optionStr.toLowerCase().contains("diário")) {
            parent = "Relatórios diários";
        } else if (optionStr.toLowerCase().contains("acompanhamento")) {
            parent = "Relatórios Semanais";
        } else if (optionStr.toLowerCase().contains("de obra")) {
            parent = "Relatórios de Obras";
        } else {
            parent = "Relatórios Diversos";
        }


        String creationDate = getCreationDate(chosenFile);
        String creationYear = creationDate.substring(0, 4).trim();


        Path destParentPath = Paths.get(ROOTDESTPATHILE + File.separator + parent + File.separator + creationYear + File.separator + creationDate);
        if (!destParentPath.toFile().exists()) {
            destParentPath.toFile().mkdirs();
        }
        if (!chosenFile.isDirectory()) {
            FileUtils.copyFile(chosenFile, new File(destParentPath + File.separator + chosenFile.getName()));
        } else {
            startMovingFiles(chosenFile, destParentPath.toFile());
        }

        System.out.println(optionStr);

    }

    public static int getNumberParsed(String option) {
        int optionInt = 0;

        try {
            optionInt = Integer.parseInt(option);
            return optionInt;

        } catch (NumberFormatException e) {
            System.out.println("Opção Incorreta, Tente Novamente.....");
            return -1;
        }
    }


    public static void startMovingFiles(File originFile, File destFinalPath) throws IOException {
        File[] chosenFilesIndirectory = originFile.listFiles();

        for (File file : chosenFilesIndirectory) {
            if (!file.isDirectory()) {
                if (!file.exists()) {
                    file.mkdirs();
                }

                System.out.println("Transferindo Documentos no Diretorio Root");
                System.out.printf("%s --> %s\n", file.toPath(), destFinalPath + File.separator + originFile.getName() + File.separator + file.getName());

                File destFinalFile = new File(destFinalPath + File.separator + originFile.getName() + File.separator + file.getName());
                // concorrencia
                CopyFiles copyfiles = new CopyFiles("teste", file, destFinalFile);
                copyfiles.start();
//                FileUtils.moveFile(file, new File(destFinalPath + File.separator + originFile.getName() + File.separator + file.getName()));
            } else {
                // dentro da pasta anexo
                File[] subFiles = file.listFiles();


               // colocar em ordem decrescente por tamanho do arquivo

                for (int i = 0; i < subFiles.length; i++) {
                    for (int j = i + 1; j < subFiles.length; j++) {
                        File tmp = subFiles[i];
                        if (subFiles[i].length() < subFiles[j].length()) {
                            tmp = subFiles[i];
                            subFiles[i] = subFiles[j];
                            subFiles[j] = tmp;
                        }
                    }
                }


                for (File subFile : subFiles) {

                    if (subFile.getName().contains(".zip")) {
                        unzip(subFile.getAbsolutePath(), subFile.toPath().getParent().toString());

                        int length = subFile.getName().length();

                        File file1 = new File(subFile.toPath().getParent().toString() + File.separator + subFile.getName().substring(0, length - 4));

                        File destFinalFile = new File(destFinalPath + File.separator + originFile.getName() + File.separator + file.getName() + File.separator + subFile.getName());

                        File destFile = zipAllDirectory(file1, subFile.toPath().getParent());

                        // concurrency

                        CopyFiles copyfiles = new CopyFiles("copiando diretorio",
                                destFile, destFinalFile);
                        copyfiles.start();


                    } else {
                        System.out.println("esse zip tem que consertar para depois colar la");
                    }
                }
            }
        }
    }

    public static File zipAllDirectory(File unzipedDirectroy, Path fileToSave) throws IOException {

        File diretorio = null;

        String destFileName = "";

        if (unzipedDirectroy.isDirectory()) {

            File test = new File(unzipedDirectroy.getParent());
            if (!test.exists()) {
                test.mkdirs();
            }
            ;


            diretorio = new File(unzipedDirectroy.getParent() + File.separator + "compactado");
            if (!diretorio.exists()) {
                diretorio.mkdirs();
            }

            destFileName = unzipedDirectroy.getParent() + File.separator + "compactado" + File.separator + unzipedDirectroy.getName() + ".zip";

            FileOutputStream fos = new FileOutputStream(destFileName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);


            System.out.println("gravando o arquivo: " + unzipedDirectroy.toPath() + " em --> " + destFileName);

            zipFile(unzipedDirectroy, unzipedDirectroy.getName(), zipOut);
            zipOut.close();
            fos.close();
        }
        return new File(destFileName);
    }


    private static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }


        FileInputStream nfis = new FileInputStream(zipFilePath);
        ZipInputStream zipIn = new ZipInputStream(nfis, Charset.forName("IBM850"));
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null) {

            String fileName = entry.getName();

            String filePath = destDirectory + File.separator + fileName;

            System.out.println("descompactando: " + fileName);


            if (!fileName.contains(".jpg")) {
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
                continue;
            }
            File f = new File(fileName);
            if (f.isDirectory()) {
                System.out.println(fileName + " --> entrou no directory");
                continue;
            }

            extractFile(zipIn, filePath);

            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        File parentFile = new File(filePath).getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        System.out.println("Reduzindo o tamanho das imagens usando javaxt: " + Paths.get(filePath).getFileName());

        javaxt.io.Image image = new javaxt.io.Image(zipIn.readAllBytes());
        image.rotate();
        try {
            if (image.getWidth() > 500) {
                image.setWidth(500);
            }
        } catch (NullPointerException e) {
            System.out.println("null point. no ideia");
        }

        ByteArrayInputStream iba = new ByteArrayInputStream(image.getByteArray());

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[3000];
        int len = 0;
        while ((len = iba.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, len);
        }
        bos.close();
    }


    // zip file

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                System.out.println("zipando --> " + childFile);
                zipFile(childFile, fileName + File.separator + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];

        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    private static Scanner getScanner() {

        Charset charsetUTF = StandardCharsets.UTF_8;
        if (Charset.defaultCharset().displayName().toLowerCase().contains("win")) {
            Charset charsetIBM = Charset.forName("IBM850");

            return new Scanner(new InputStreamReader(System.in, charsetIBM));
        }

        return new Scanner(new InputStreamReader(System.in, charsetUTF));
    }

    private static String getCreationDate(File file) {

        BasicFileAttributes attrs;

        try {
            attrs = Files.readAttributes(Path.of(String.valueOf(file)), BasicFileAttributes.class);
            FileTime time = attrs.creationTime();

            String pattern = "yyyy-MM";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            String formatted = simpleDateFormat.format(new Date(time.toMillis() - 86400000));

            return formatted;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


