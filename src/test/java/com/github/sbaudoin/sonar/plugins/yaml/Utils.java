/**
 * Copyright (c) 2018, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.sonar.plugins.yaml;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {
    public static final String MODULE_KEY = "moduleKey";


    private Utils() {
    }


    public static InputFile getInputFile(String relativePath) throws IOException {
        return TestInputFileBuilder.create(MODULE_KEY, relativePath)
                .setContents(new String(Files.readAllBytes(Paths.get(relativePath))))
                .build();
    }

    public static SensorContextTester getSensorContext() {
        return SensorContextTester.create(Paths.get("src", "test", "files"));
    }
}
