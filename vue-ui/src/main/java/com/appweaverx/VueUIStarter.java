package com.appweaverx;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;

@Component
public class VueUIStarter {

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationEvent(ApplicationStartedEvent event) {
        final Class<?> mainApplicationClass = event.getSpringApplication().getMainApplicationClass();
        if(mainApplicationClass == null)
            throw new IllegalStateException("Could not find main application class");

        final Path applicationModuleDirectory = getModuleDirectoryOfClass(mainApplicationClass);
        final Path vueUIModuleDirectory = getModuleDirectoryOfClass(VueUIStarter.class);
        final VueCompiler vueCompiler = new VueCompiler(applicationModuleDirectory, vueUIModuleDirectory);

        if(!vueCompiler.hasVueSrcDirectory()) {
            if(CmdLineHelper.getUserInputAsBoolean("Vue source folder not found. Dou you want to create it?", false)) {
                vueCompiler.createInitialVueProjectStructure();
                vueCompiler.install();
                vueCompiler.compile();
            }
            System.exit(0);
        } else if (!vueCompiler.hasCompiledSources()) {
            if(CmdLineHelper.getUserInputAsBoolean("No compiled vue sources found. Dou you want to create it?", false)) {
                vueCompiler.install();
                vueCompiler.compile();
            }
            System.exit(0);
        }

    }


    private Path getModuleDirectoryOfClass(Class<?> mainClass) {
        try {
            CodeSource codeSource = mainClass.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                throw new IllegalStateException("Could not find main application class code source");
            }

            File moduleDir = getModuleDirectoryFileOfCodeSource(codeSource);
            return moduleDir.toPath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to detect module name", e);
        }
    }

    private File getModuleDirectoryFileOfCodeSource(CodeSource codeSource) throws URISyntaxException {
        URL location = codeSource.getLocation();
        File file = new File(location.toURI());
        File moduleDir;
        if (file.isDirectory()) {
            moduleDir = file.getParentFile().getParentFile();
        } else {
            moduleDir = file.getParentFile();
        }
        return moduleDir;
    }

}
