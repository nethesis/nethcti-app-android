package org.linphone.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileManager {
    private String ENCODING = "UTF-8";

    public static void delete(File f) {
        if(!f.exists()) return;
        if (f.isDirectory())
            for (File c : f.listFiles()) delete(c);
        f.delete();
    }

    public void writeToFile(String pJSON, String path, String pFileName){
        File folder = new File(path);
        File outputFile = new File(folder, pFileName);
        outputFile.getParentFile().mkdirs();

        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new FileWriter(outputFile);
            outputStreamWriter.write(pJSON);
            outputStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String readJSONFromFile(File f) {
        String ret = "";
        try {
            FileInputStream input = new FileInputStream(f);
            if (input != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                input.close();
                ret = stringBuilder.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            //} finally {
            //IOUtils.closeQuietly(writer);
            //IOUtils.closeQuietly(input);
        }

        return ret;
    }

}
