package com.github.sbaudoin.sonar.plugins.yaml;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {
    private Utils() {
    }


    public static InputFile getInputFile(String relativePath) throws IOException {
        return TestInputFileBuilder.create("", relativePath)
                .setContents(new String(Files.readAllBytes(Paths.get(relativePath))))
                .build();
    }
}
