/*
 * Copyright 2015 Norbert Potocki (norbert.potocki@nort.pl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cfg4j.sample;

import org.cfg4j.source.context.propertiesprovider.PropertiesProvider;
import org.cfg4j.source.context.propertiesprovider.PropertiesProviderSelector;
import org.cfg4j.source.context.propertiesprovider.PropertyBasedPropertiesProvider;
import org.cfg4j.source.context.propertiesprovider.YamlBasedPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Controller
@EnableAutoConfiguration
@ComponentScan
public class MainController implements CommandLineRunner {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  @Value("${configPath}")
  private String configPath;

  public void run(String... args) throws Exception {

    List<Path> configFiles = new LinkedList<>();

    Path start = Paths.get(configPath);
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (file.toString().endsWith(".yaml") || file.toString().endsWith("properties")) {
          LOG.info("found config file: " + file.toAbsolutePath().toString());
          configFiles.add(file);
        }

        return FileVisitResult.CONTINUE;
      }
    });

    PropertiesProviderSelector propertiesProviderSelector = new PropertiesProviderSelector(
        new PropertyBasedPropertiesProvider(), new YamlBasedPropertiesProvider()
    );

    Properties properties = new Properties();

    for (Path path : configFiles) {
      try (InputStream input = new FileInputStream(path.toFile())) {

        PropertiesProvider provider = propertiesProviderSelector.getProvider(path.getFileName().toString());
        properties.putAll(provider.getProperties(input));

      } catch (IOException e) {
        throw new IllegalStateException("Unable to load properties from file: " + path, e);
      }
    }

    System.out.println(properties);
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(MainController.class, args);
  }

}
