

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.nio.file.attribute.BasicFileAttributes;

public class Main {

    static String destPathRede = "";
    static String  ROOTDESTPATH = "Z:\\Supervisora\\RTA";
    static File ROOTDESTPATHILE = new File(ROOTDESTPATH);
    static String ROOTORIGINPATH = "D:\\Documentos\\Users\\Eduardo\\Documentos\\ANTT\\OneDrive - ANTT- Agencia Nacional de Transportes Terrestres\\CRO\\Relatórios RTA";
    static File ORIGINPATHFILE = new File(ROOTORIGINPATH);

    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner scanner = getScanner();

        File[] originFiles = ORIGINPATHFILE.listFiles();
        int i = 0;
        System.out.println("escolha:");
        for (File file : originFiles) {
            i++;
            System.out.printf("Digite %d. for %s \n", i, file.getName());

        }
        String option = scanner.nextLine();
        int optionInt = Integer.parseInt(option);

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
        String creationMonth = creationDate.substring(5).trim();

        System.out.println("year " + creationYear);
        System.out.println("motn " + creationMonth);

        Path destParentPath = Paths.get(ROOTDESTPATHILE + File.separator + parent + File.separator + creationYear + File.separator + creationDate);
        File destParentPathFile = destParentPath.toFile();
        if (!destParentPathFile.exists()) {
            destParentPathFile.mkdirs();
        }

        if (chosenFile.isDirectory()) {
            startMovingFiles(chosenFile, destParentPathFile);
        } else {
            Files.move(chosenFile.toPath(), Paths.get(destParentPathFile.getAbsoluteFile() + File.separator + chosenFile.getName()));

        }

        System.out.println(optionStr);

    }

    public static void oldFile() throws IOException {


        System.out.println("Cole o endereço da pasta completo one estão os arquivos fotos zipados");
        String zipFile = new Scanner(System.in).nextLine();
        zipFile = zipFile.replace("\\", File.separator);
        String destDirectory = zipFile;

        System.out.println("Cole o endereço da pasta da rede onde serão colados movidos os novos arquivos de fotos zipados");
        destPathRede = new Scanner(System.in).nextLine();

        if(!new File(destPathRede).exists()){
            new File(destPathRede).mkdirs();
        }

        System.out.println(zipFile);

        unzipAllPath(zipFile, destDirectory);

        zipAllDirectory(destDirectory);


    }

    public static void startMovingFiles(File originFile, File destFinalPath) throws IOException {
        File[] chosenFiles = originFile.listFiles();

        for (File file: chosenFiles){
            if(!file.isDirectory()){
                Files.move(file.toPath(), Paths.get(destFinalPath + File.separator + originFile.getName() + File.separator + file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } else {
                File[] subFiles = file.listFiles();
                for (File subFile : subFiles) {
                    if (subFile.getName().contains(".zip")) {
                        unzip(subFile.getAbsolutePath(), subFile.toPath().getParent().toString());

                    } else {
                        System.out.println("esse zip tem que consertar para depois colar la");
                    }
                }
                System.out.println("nome file  --> " + file.getName());
                File compactado = zipAllDirectory1(file);
                for (File f: compactado.listFiles()){
                    Path finalFile = Paths.get(destFinalPath + File.separator + originFile.getName() + File.separator + file.getName() + File.separator + f.getName());

                    System.out.println("f --> " + f);
                    System.out.println("destfinal --> " + finalFile);
                    Files.move(f.toPath(), finalFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }

        }

    }

    public static void unzipAllPath(String zipFilePath, String destDirectory) throws IOException {
        File originPath = new File(zipFilePath);

        File[] listFiles = originPath.listFiles();

        for (File file: listFiles){
            if(!file.isDirectory()) {
                unzip(file.getAbsolutePath(), destDirectory);
            }
        }

    }

    public static File zipAllDirectory1(File originDirectory) throws IOException {


        File[] listFiles = originDirectory.listFiles();

        File diretorio = null;

        if(listFiles != null) {

            for (File file : listFiles) {
                if (file.isDirectory()) {

                    String sourceFile = file.getAbsolutePath();
                    String destFileName = originDirectory + File.separator + "compactado" + File.separator + file.getName() + ".zip";

                    diretorio = new File(originDirectory + File.separator + "compactado");
                    if (!diretorio.exists()) {
                        diretorio.mkdirs();
                    }


                    FileOutputStream fos = new FileOutputStream(destFileName);
                    ZipOutputStream zipOut = new ZipOutputStream(fos);
                    File fileToZip = new File(sourceFile);

                    System.out.println("gravando o arquivo: " + fileToZip.getName() + " em --> " + destFileName);

                    zipFile(fileToZip, fileToZip.getName(), zipOut);
                    zipOut.close();
                    fos.close();
                }
            }

            return diretorio;
        }
        else {
            System.out.println("vazio");
            return null;
        }

    }



    public static void zipAllDirectory(String originDirectory) throws IOException {

        File sourcePath = new File(originDirectory);

        File[] listFiles = sourcePath.listFiles();

        File diretorio = null;

        for (File file: listFiles){
            if (file.isDirectory()) {

                String sourceFile = file.getAbsolutePath();
                String destFileName =  originDirectory + File.separator + "compactado" + File.separator + file.getName() + ".zip";

                diretorio = new File(originDirectory + File.separator + "compactado");
                if(!diretorio.exists()){
                    diretorio.mkdirs();
                }


                FileOutputStream fos = new FileOutputStream(destFileName);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                File fileToZip = new File(sourceFile);

                System.out.println("gravando o arquivo: " + fileToZip.getName() + " em --> " + destFileName);

                zipFile(fileToZip, fileToZip.getName(), zipOut);
                zipOut.close();
                fos.close();
            }
        }


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

            System.out.println(fileName);


            if (!fileName.contains(".jpg")) {
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
                continue;
            }
            File f = new File(fileName);
            if (f.isDirectory()){
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


        javaxt.io.Image image = new javaxt.io.Image(zipIn.readAllBytes());
        image.rotate();
        try{
            if (image.getWidth() > 500){
                image.setWidth(500);
            }
        } catch (NullPointerException e){
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
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
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

        System.out.println(Charset.availableCharsets());
        return new Scanner(new InputStreamReader(System.in, charsetUTF));
    }

    private static String getCreationDate(File file){

        BasicFileAttributes attrs;

        try {
            attrs = Files.readAttributes(Path.of(String.valueOf(file)), BasicFileAttributes.class);
            FileTime time = attrs.creationTime();

            String pattern = "yyyy-MM";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            String formatted = simpleDateFormat.format( new Date( time.toMillis() - 86400000) );

            return formatted;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}


