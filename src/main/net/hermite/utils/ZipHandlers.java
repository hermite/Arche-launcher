package net.hermite.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHandlers {

    private static ArrayList<String> listFiles;

    public static void SaveFilesOnZip(String Directory, String SaveFile) {


        final int BUFFER = 2048;

        File node = new File(Directory);
        listFiles = new ArrayList();
        listeFichiers(node);

        try {
            byte buffer[] = new byte[BUFFER];

            FileOutputStream files = new FileOutputStream(SaveFile);
            ZipOutputStream zipos = new ZipOutputStream(files);

            for (String file : listFiles) {
                ZipEntry zipentry = new ZipEntry(file);
                zipos.putNextEntry(zipentry);
                FileInputStream in =
                        new FileInputStream(Directory + File.separator + file);

                int lec;
                while ((lec = in.read(buffer)) > 0) {
                    zipos.write(buffer, 0, lec);
                }

                in.close();
                System.out.println("Fichier " + file + " ajouté");
            }

            zipos.closeEntry();
            zipos.close();

            System.out.println("Fichier compressé créé!");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static ArrayList<String> listeFichiers(File node){

        //ajouter les fichiers
        if(node.isFile()){
            String file = node.getAbsoluteFile().toString();
            System.out.println("node Path =" + node.getPath());
            String Path = file.substring(node.getParent().length()+1, file.length());
            listFiles.add(Path);
        }

        if(node.isDirectory()){
            String[] subFile = node.list();
            for(String filename : subFile){
                listeFichiers(new File(node, filename));
            }
        }
        return listFiles;
    }

    public static void unzip(File zipfile, File folder) throws FileNotFoundException, IOException{

        // création de la ZipInputStream qui va servir à lire les données du fichier zip
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(
                        new FileInputStream(zipfile.getCanonicalFile())));

        // extractions des entrées du fichiers zip (i.e. le contenu du zip)
        ZipEntry ze = null;
        try {
            while((ze = zis.getNextEntry()) != null){
                // Pour chaque entrée, on crée un fichier
                // dans le répertoire de sortie "folder"
                File f = new File(folder.getCanonicalPath(), ze.getName());

                // Si l'entrée est un répertoire,
                // on le crée dans le répertoire de sortie
                // et on passe à l'entrée suivante (continue)
                if (ze.isDirectory()) {
                    f.mkdirs();
                    continue;
                }

                // L'entrée est un fichier, on crée une OutputStream
                // pour écrire le contenu du nouveau fichier
                f.getParentFile().mkdirs();
                OutputStream fos = new BufferedOutputStream(
                        new FileOutputStream(f));

                // On écrit le contenu du nouveau fichier
                // qu'on lit à partir de la ZipInputStream
                // au moyen d'un buffer (byte[])
                try {
                    try {
                        final byte[] buf = new byte[8192];
                        int bytesRead;
                        while (-1 != (bytesRead = zis.read(buf)))
                            fos.write(buf, 0, bytesRead);
                    }
                    finally {
                        fos.close();
                    }
                }
                catch (final IOException ioe) {
                    // en cas d'erreur on efface le fichier
                    f.delete();
                    throw ioe;
                }
            }
        }
        finally {
            // fermeture de la ZipInputStream
            zis.close();
        }
    }
}
