package com.tmri.obd.file_rw.util;

import android.os.Environment;
import android.util.Log;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ly on 2019/7/31 13:39
 * <p>
 * Copyright is owned by chengdu haicheng technology
 * co., LTD. The code is only for learning and sharing.
 * It is forbidden to make profits by spreading the code.
 */
public class FileUtils {

    public static void writefile(String s) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().toString(), "/laiyu");
            File f = new File(file, "hello.txt");
            if (!file.exists()) {
                file.mkdir();
            }
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter  fw = new FileWriter(f, true);
            fw.write(s + "\n");
            fw.close();

        } catch (IOException e) {
            Log.d("lylog", "ssssssss");
        }

    }
    static  int count = 0;
    public static  void writeXLSfile(String args){
        try{
            File file = new File(Environment.getExternalStorageDirectory().toString(), "h.xls");
            if (!file.exists()) {
                file.createNewFile();
            }
            Workbook book = Workbook.getWorkbook(file);
            Sheet sheet = book.getSheet(0);

            int length = sheet.getRows();
            WritableWorkbook wbook = Workbook.createWorkbook(file,book);
            WritableSheet sh = wbook.getSheet(0);

            Label label = new Label(0, length, args);
            sh.addCell(label);

            wbook.write();
            wbook.close();
        }catch (IOException e){
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }

    }
}
