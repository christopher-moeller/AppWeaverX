package com.appweaverx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class VueCompiler
{

    private final Path applicationModulePath;
    private final Path vueUIModulePath;

    public VueCompiler(Path applicationModulePath, Path vueUIModulePath) {
        this.applicationModulePath = applicationModulePath;
        this.vueUIModulePath = vueUIModulePath;
    }

    public boolean hasVueSrcDirectory() {
        return applicationModulePath.resolve("vue").toFile().exists();
    }

    public void createInitialVueProjectStructure() {
        applicationModulePath.resolve("vue").toFile().mkdirs();

        final Path vueSrcPath = vueUIModulePath.resolve("vue");
        final Path vueTargetPath = applicationModulePath.resolve("vue");
        try (var files = Files.walk(vueSrcPath)) {
            files.forEach(source -> {
                try {
                    Path target = vueTargetPath.resolve(vueSrcPath.relativize(source));
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy: " + source, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void compile() {

        try {
            buildVueApp();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            copyVueBuildToStatic();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void install() {
        try {
            System.out.println("🔧 Running NPM install ...");

            // Vue project folder
            File vueFolder = new File(applicationModulePath.resolve("vue").toAbsolutePath().toString());

            // Run "npm run build"
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(vueFolder);
            builder.command("npm", "install");
            builder.inheritIO(); // show logs in console

            Process process = builder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Vue install failed with exit code: " + exitCode);
            }

            System.out.println("✅ Vue build completed!");
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildVueApp() throws IOException, InterruptedException {
        System.out.println("🔧 Running Vue build...");

        // Vue project folder
        File vueFolder = new File(applicationModulePath.resolve("vue").toAbsolutePath().toString());

        // Run "npm run build"
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(vueFolder);
        builder.command("npm", "run", "build");
        builder.inheritIO(); // show logs in console

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Vue build failed with exit code: " + exitCode);
        }

        System.out.println("✅ Vue build completed!");
    }

    private void copyVueBuildToStatic() throws IOException {
        System.out.println("📂 Copying Vue build to static folder...");

        // Vue dist folder
        Path vueDistPath = Paths.get(applicationModulePath.resolve("vue").toAbsolutePath().toString(), "dist");

        // Spring Boot static folder
        Path staticPath = getStaticDirectory();

        // Thymeleaf templates folder
        Path templatePath = getTemplatesDirectory();

        // Clear static folder before copying
        clearFolder(staticPath);
        clearFolder(templatePath);

        // Copy files from dist to static
        try (var files = Files.walk(vueDistPath)) {
            files.forEach(source -> {
                try {
                    Path target = staticPath.resolve(vueDistPath.relativize(source));
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        if( source.getFileName().toString().equals("index.html")) {
                            Path templateTarget = templatePath.resolve(vueDistPath.relativize(source));
                            Files.createDirectories(templateTarget.getParent());
                            Files.copy(source, templateTarget, StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy: " + source, e);
                }
            });
        }

        System.out.println("✅ Vue build copied to static!");
    }

    private static void clearFolder(Path folderPath) throws IOException {
        if (Files.exists(folderPath)) {
            try (var files = Files.walk(folderPath)) {
                files.sorted(Comparator.reverseOrder()) // delete children before parents
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to delete: " + path, e);
                            }
                        });
            }
        }
    }

    public boolean hasCompiledSources() {
        return folderIsNotEmpty(getStaticDirectory()) && folderIsNotEmpty(getTemplatesDirectory());
    }

    private boolean folderIsNotEmpty(Path folderPath) {
        if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
            try (var files = Files.list(folderPath)) {
                return files.findAny().isPresent();
            } catch (IOException e) {
                throw new RuntimeException("Failed to check folder: " + folderPath, e);
            }
        }
        return false;
    }

    private Path getStaticDirectory() {
        return Paths.get(applicationModulePath.toAbsolutePath().toString(), "src", "main", "resources", "static");
    }

    private Path getTemplatesDirectory() {
        return Paths.get(applicationModulePath.toAbsolutePath().toString(), "src", "main", "resources", "templates");
    }
}
