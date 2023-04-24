package org.jesuitasrioja;

import java.io.*;
import java.net.SocketException;
import java.util.Scanner;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * @author Iker Fernández
 * @ PSP
 * Realiza un programa propio sobre FTP. Por ejemplo: “Crea un programa cliente
 * que acceda a un repositorio FTP y realice diferentes acciones”.
 * <p>
 * POSIBLES FUNCIONES FTP
 * LISTAR Ficheros y Directorios (diferenciar)-> PRACT1 EJ3, PRACT1 EJ6
 * CREAR Ficheros y Directorios-> PRACT1 EJ2
 * COPIAR Ficheros y Directorios-> PRACT1 EJ2
 * BORRAR Ficheros y Directorios (recursividad)
 * SUBIR Ficheros
 * DESCARGAR Ficheros y Directorios
 * ARCHIVOS COMPRIMIDOS: -> PRACT1 EJ7
 * LISTAR Ficheros y Directorios
 * COMPRIMIR Ficheros y Directorios
 * DESCOMPRIMIR Ficheros y Directorios
 */
public class MainMenu {

    public static void main(String[] args) throws IOException {
        // Datos conexión y variables del menú
        FTPClient cliente = new FTPClient();
        String servFTP = "192.168.1.233";
        System.out.println("Nos conectamos a: " + servFTP);
        String usuario = "";
        String clave = "";
        boolean salir = false;
        Scanner input = new Scanner(System.in);
        String option = "";
        String nombre = "";
        String nuevo = "";
        try {
            // Login
            cliente.connect(servFTP);
            cliente.enterLocalPassiveMode();
            System.out.print(cliente.getReplyString());

            boolean condition = cliente.login(usuario, clave);
            System.out.print(cliente.getReplyString());
            if (condition) {
                System.out.println("Login correcto...");
                cliente.enterLocalPassiveMode();
            } else {
                System.out.println("Login Incorrecto");
                cliente.disconnect();
                System.exit(1);
            }

            // Menú: Opciones FTP
            while (!salir) {
                System.out.println("\nDirectorio actual: " + cliente.printWorkingDirectory());
                System.out.print(
                        "\nLista de opciones FTP:\n\t1.  Listar archivos y carpetas en el directorio actual.\n\t2.  " +
                                "Cambiar directorio.\n\t3.  Crear archivo o directorio.\n\t4.  Borrar archivo o " +
                                "directorio.\n\t5.  Renombrar archivo o directorio.\n\t6.  " +
                                "Subir archivo o directorio.\n\t7" +
                                ".  Descargar archivo o directorio.\n\t8.  Salir.\n\n " +
                                "Opción: ");
                option = input.nextLine();

                // Listar directorio actual
                if (option.equals("1")) {

                    System.out.println("\nDirectorio actual: " + cliente.printWorkingDirectory());

                    FTPFile[] files = cliente.listFiles();
                    System.out.println("Ficheros en el directorio actual: " + files.length);

                    String tipos[] = {"Fichero", "Directorio", "Enlace simb."};

                    for (int i = 0; i < files.length; i++) {
                        System.out.println("\t" + files[i].getName() + "\t==>\t" + tipos[files[i].getType()]);
                    }

                    // Cambiar de directorio
                } else if (option.equals("2")) {
                    System.out.println("Cambiar a directorio: ");
                    option = input.nextLine();
                    cliente.changeWorkingDirectory(option);

                    /* Confirmación cambio de directorio
                    if (!cliente.printWorkingDirectory().equals( "/" + directorio) && !cliente.printWorkingDirectory().equals(directorio)){
                        System.out.println("Directorio " + directorio + " no existe. No se ha producido ningún cambio.");
                        directorio = "";
                    }*/

                    // Crear nuevo archivo o directorio
                } else if (option.equals("3")) {
                    System.out.print("Elije entre archivo o directorio:\n\t1) Archivo\n\t2) Directorio\n Opcion: ");
                    option = input.nextLine();

                    // Crear archivo
                    if (option.equals("1")) {
                        System.out.println("Introduce el nombre y extensión del nuevo archivo: ");
                        option = input.nextLine();
                        createFile(option);
                        FileInputStream inputStream = new FileInputStream(option);
                        cliente.storeFile(option, inputStream);
                        inputStream.close();
                        deleteFile(new File(option));

                        // Crear directorio
                    } else if (option.equals("2")) {
                        System.out.println("Introduce un nombre para el directorio: ");
                        option = input.nextLine();
                        condition = cliente.makeDirectory(option);
                        if (condition) {
                            System.out.println("Directorio creado correctamente: " + option);
                        } else {
                            System.out.println("Error al crear el directorio. La carpeta ya existe.");
                        }
                        // Salir de opción 3
                    } else {
                        System.out.println("ERROR: Ninguna opción seleccionada.");
                    }

                    // Borrar archivo o directorio
                } else if (option.equals("4")) {
                    System.out.println("Introduce el directorio a eliminar: ");
                    option = input.nextLine();
                    String parent = cliente.printWorkingDirectory();
                    removeDirectory(cliente, parent, option );

                    // Renombrar archivo o directorio
                } else if (option.equals("5")) {
                    System.out.println("Introduce el archivo o directorio a renombrar: ");
                    nombre = input.nextLine();
                    System.out.println("Introduce el nuevo nombre: ");
                    nuevo = input.nextLine();
                    boolean success = cliente.rename(nombre, nuevo);
                    if (success) {
                        System.out.println("Renombrado correctamente.");
                    } else {
                        System.out.println("ERROR: No se ha podido cambiar el nombre.");
                    }

                    // Subir archivos o directorios
                } else if (option.equals("6")) {
                    System.out.println("Introduce la ruta del archivo o directorio que quieres subir: ");
                    nombre = input.nextLine();
                    System.out.println("Introduce la ruta del archivo o directorio donde quieres subirlo en el " +
                            "servidor: ");
                    nuevo = input.nextLine();
                    uploadDirectory(cliente, nombre, nuevo, "");

                    /* Subir archivo
                    System.out.println("Introduce la ruta del archivo que quieres subir: ");
                    nombre = input.nextLine();
                    File firstLocalFile = new File(nombre);
                    System.out.println("Introduce la ruta del archivo donde quieres subirlo en el servidor: ");
                    nuevo = input.nextLine();
                    String firstRemoteFile = nuevo;
                    InputStream inputStream = new FileInputStream(firstLocalFile);

                    System.out.println("Subiendo archivo...");
                    boolean done = cliente.storeFile(firstRemoteFile, inputStream);
                    inputStream.close();
                    if (done) {
                        System.out.println("Archivo subido correctamente");
                    }
*/
                    // Descargar archivos o directorios
                } else if (option.equals("7")) {
                    System.out.print("Elije entre archivo o directorio a descargar:\n\t1) Archivo\n\t2) Directorio\n " +
                            "Opcion: ");
                    option = input.nextLine();

                        // Descargar archivo
                    if (option.equals("1")) {
                        System.out.println("Introduce la ruta del archivo que quieres descargar: ");
                        nombre = input.nextLine();
                        System.out.println("Introduce la ruta del archivo donde descargarlo: ");
                        nuevo = input.nextLine();
                        downloadSingleFile(cliente, nombre, nuevo);

                        if (condition) {
                            System.out.println("Archivo descargado correctamente.");
                        } else {
                            System.out.println("Error al descargar el archivo.");
                        }

                        // Descargar directorio
                    } else if (option.equals("2")) {
                        System.out.println("Introduce la ruta del directorio que quieres descargar: ");
                        nombre = input.nextLine();
                        System.out.println("Introduce la ruta del directorio donde descargarlo: ");
                        nuevo = input.nextLine();
                        downloadDirectory(cliente, nombre, "", nuevo);

                        if (condition) {
                            System.out.println("Directorio descargado correctamente.");
                        } else {
                            System.out.println("Error al descargar el directorio.");
                        }
                        // Salir de opción 3
                    } else {
                        System.out.println("ERROR: Ninguna opción seleccionada.");
                    }

                    // Salir del programa
                } else if (option.equals("8")) {

                    boolean logout = cliente.logout();
                    System.out.print(cliente.getReplyString());
                    if (logout)
                        System.out.println("Logout del servidor FTP...");
                    else
                        System.out.println("Error al hacer Logout...");

                    cliente.disconnect();
                    System.out.println("Saliendo del programa...");
                    salir = true;

                    // Opción no disponible
                } else {
                    System.out.print("La opción " + option
                            + " no se ha encontrado. Por favor, introduzca un número del 1 al 8.");
                }
            }

        } catch (SocketException se) {
            System.out.println("ERROR: Tiempo de conexión");
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    public static void createFile(String name) {
        try {
            File myObj = new File(name);
            if (myObj.createNewFile()) {
                System.out.println("Archivo creado: " + myObj.getName());
            } else {
                System.out.println("El archivo ya existe");
            }
        } catch (IOException e) {
            System.out.println("Ha ocurrido un error");
        }
    }

    public static void deleteFile(File name) {
        name.delete();
        /*
        if (!name.exists()) {
            System.out.println("El archivo no existe.");
        } else {
            name.delete();
            System.out.println("El archivo fue eliminado.");
        }*/

    }

    public static void removeDirectory(FTPClient ftpClient, String parentDir,
                                       String currentDir) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = ftpClient.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = parentDir + "/" + currentDir + "/"
                        + currentFileName;
                if (currentDir.equals("")) {
                    filePath = parentDir + "/" + currentFileName;
                }

                if (aFile.isDirectory()) {
                    // remove the sub directory
                    removeDirectory(ftpClient, dirToList, currentFileName);
                } else {
                    // delete the file
                    boolean deleted = ftpClient.deleteFile(filePath);
                    if (deleted) {
                        System.out.println("ELIMINADO el archivo: " + filePath);
                    } else {
                        System.out.println("ERROR: No se ha podido eliminar el archivo: "
                                + filePath);
                    }
                }
            }

            // finally, remove the directory itself
            boolean removed = ftpClient.removeDirectory(dirToList);
            if (removed) {
                System.out.println("ELIMINADO el directorio: " + dirToList);
            } else {
                System.out.println("ERROR: No se ha podido eliminar el directorio: " + dirToList);
            }
        }
    }
    public static void uploadDirectory(FTPClient ftpClient,
                                       String remoteDirPath, String localParentDir, String remoteParentDir)
            throws IOException {

        System.out.println("LISTANDO directorio: " + localParentDir);

        File localDir = new File(localParentDir);
        File[] subFiles = localDir.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            for (File item : subFiles) {
                String remoteFilePath = remoteDirPath + "/" + remoteParentDir
                        + "/" + item.getName();
                if (remoteParentDir.equals("")) {
                    remoteFilePath = remoteDirPath + "/" + item.getName();
                }


                if (item.isFile()) {
                    // upload the file
                    String localFilePath = item.getAbsolutePath();
                    System.out.println("Subiendo archivo: " + localFilePath);
                    boolean uploaded = uploadSingleFile(ftpClient,
                            localFilePath, remoteFilePath);
                    if (uploaded) {
                        System.out.println("Archivo subido a: "
                                + remoteFilePath);
                    } else {
                        System.out.println("ERROR: No se ha podido subir el archivo: "
                                + localFilePath);
                    }
                } else {
                    // create directory on the server
                    boolean created = ftpClient.makeDirectory(remoteFilePath);
                    if (created) {
                        System.out.println("CREADO el directorio: "
                                + remoteFilePath);
                    } else {
                        System.out.println("ERROR: No se ha podido crear el directorio: "
                                + remoteFilePath);
                    }

                    // upload the sub directory
                    String parent = remoteParentDir + "/" + item.getName();
                    if (remoteParentDir.equals("")) {
                        parent = item.getName();
                    }

                    localParentDir = item.getAbsolutePath();
                    uploadDirectory(ftpClient, remoteDirPath, localParentDir,
                            parent);
                }
            }
        }
    }

    public static boolean uploadSingleFile(FTPClient ftpClient,
                                           String localFilePath, String remoteFilePath) throws IOException {
        File localFile = new File(localFilePath);

        InputStream inputStream = new FileInputStream(localFile);
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.storeFile(remoteFilePath, inputStream);
        } finally {
            inputStream.close();
        }
    }

    public static boolean downloadSingleFile(FTPClient ftpClient,
                                             String remoteFilePath, String savePath) throws IOException {
        File downloadFile = new File(savePath);

        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }

        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(downloadFile));
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
    public static void downloadDirectory(FTPClient ftpClient, String parentDir,
                                         String currentDir, String saveDir) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = ftpClient.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = parentDir + "/" + currentDir + "/"
                        + currentFileName;
                if (currentDir.equals("")) {
                    filePath = parentDir + "/" + currentFileName;
                }

                String newDirPath = saveDir + parentDir + File.separator
                        + currentDir + File.separator + currentFileName;
                if (currentDir.equals("")) {
                    newDirPath = saveDir + parentDir + File.separator
                            + currentFileName;
                }

                if (aFile.isDirectory()) {
                    // create the directory in saveDir
                    File newDir = new File(newDirPath);
                    boolean created = newDir.mkdirs();
                    if (created) {
                        System.out.println("CREADO el directorio: " + newDirPath);
                    } else {
                        System.out.println("ERROR: No se ha podido crear el directorio: " + newDirPath);
                    }

                    // download the sub directory
                    downloadDirectory(ftpClient, dirToList, currentFileName,
                            saveDir);
                } else {
                    // download the file
                    boolean success = downloadSingleFile(ftpClient, filePath,
                            newDirPath);
                    if (success) {
                        System.out.println("DESCARGADO el archivo: " + filePath);
                    } else {
                        System.out.println("ERROR: No se ha podido descargar el archivo: "
                                + filePath);
                    }
                }
            }
        }
    }
}
