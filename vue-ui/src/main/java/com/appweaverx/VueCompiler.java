package com.appweaverx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Optional;

public class VueCompiler
{
    public static void main(String[] args) throws IOException, InterruptedException {

        final Path vueUIModulePath = findVueUIModulePath()
                .orElseThrow(() -> new RuntimeException("Could not find vue-ui module path"));

        buildVueApp(vueUIModulePath);
        copyVueBuildToStatic(vueUIModulePath);
    }

    private static Optional<Path> findVueUIModulePath() {
        final Class<?> rootClass = VueCompiler.class;
        final URL resource = rootClass.getResource(rootClass.getSimpleName() + ".class");
        if(resource == null) {
            return Optional.empty();
        }

        Path path = Path.of(resource.getPath());
        while(path != null) {
            File file = path.toFile();
            if(file.getName().equals("vue-ui")) {
                return Optional.of(path);
            }
            path = path.getParent();
        }

        return Optional.empty();
    }

    private static void buildVueApp(Path vueUIModulePath) throws IOException, InterruptedException {
        System.out.println("🔧 Running Vue build...");

        // Vue project folder
        File vueFolder = new File(vueUIModulePath.resolve("vue").toAbsolutePath().toString());

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

    private static void copyVueBuildToStatic(Path vueUIModulePath) throws IOException {
        System.out.println("📂 Copying Vue build to static folder...");

        // Vue dist folder
        Path vueDistPath = Paths.get(vueUIModulePath.resolve("vue").toAbsolutePath().toString(), "dist");

        // Spring Boot static folder
        Path staticPath = Paths.get(vueUIModulePath.toAbsolutePath().toString(), "src", "main", "resources", "static");

        // Thymeleaf templates folder
        Path templatePath = Paths.get(vueUIModulePath.toAbsolutePath().toString(), "src", "main", "resources", "templates");

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
}
