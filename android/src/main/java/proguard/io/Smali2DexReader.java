package proguard.io;

import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Smali2DexReader implements  DataEntryReader{

    private final DataEntryReader dataEntryReader;

    public Smali2DexReader(DataEntryReader dataEntryReader) {

        this.dataEntryReader = dataEntryReader;

    }

    @Override
    public void read(DataEntry dataEntry) throws IOException {

        SmaliOptions options = new SmaliOptions();
        File dexFile = File.createTempFile("classes", ".dex");
        options.outputDexFile = dexFile.getAbsolutePath();

//        InputStream inputStream = dataEntry.getInputStream();
//        File smaliFile = File.createTempFile("temp", ".smali");
//        FileOutputStream fos = new FileOutputStream(smaliFile);
//        boolean smaliSuccess = Smali.assemble(options, smaliFile.getAbsolutePath());

//        Smali.assemble(options, dataEntry.getInputStream());

        FileDataEntry fileDataEntry = new FileDataEntry(dexFile);
        dataEntryReader.read(fileDataEntry);
        dexFile.deleteOnExit();

    }
}