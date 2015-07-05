package com.techshroom.thxmumfrey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import net.eq2online.macros.scripting.IErrorLogger;
import net.eq2online.macros.scripting.LoadedModuleInfo;
import net.eq2online.macros.scripting.ModuleLoader;
import net.eq2online.macros.scripting.api.IMacroEventProvider;
import net.eq2online.macros.scripting.api.IMacrosAPIModule;
import net.eq2online.macros.scripting.api.IScriptAction;
import net.eq2online.macros.scripting.api.IScriptedIterator;
import net.eq2online.macros.scripting.api.IVariableProvider;
import net.eq2online.macros.scripting.parser.ScriptCore;
import net.minecraft.client.Minecraft;
import autovalue.shaded.com.google.common.common.base.Throwables;

import com.google.common.io.ByteStreams;
import com.mumfrey.liteloader.InitCompleteListener;
import com.mumfrey.liteloader.core.LiteLoader;

public class LiteModLoadModule implements InitCompleteListener {

    @Override
    public String getName() {
        return "LoadCommandingText";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void init(File configPath) {
        packJAR();
        // applyModules();
    }

    private void packJAR() {
        URL src =
                getClass().getProtectionDomain().getCodeSource().getLocation();
        Path codeSource;
        try {
            // com.techshroom.thxmumfrey.LiteModLoadModule == 4 parents
            codeSource =
                    Paths.get(src.toURI()).getParent().getParent().getParent()
                            .getParent().toAbsolutePath();
        } catch (URISyntaxException e1) {
            return;
        }
        try (JarOutputStream out =
                new JarOutputStream(
                        new FileOutputStream(
                                "./liteconfig/common/macros/modules/module_comtext.jar"))) {
            try (Stream<Path> paths = Files.walk(codeSource)) {
                paths.forEachOrdered(x -> {
                    ZipEntry entry =
                            new JarEntry(codeSource.relativize(x).toString()
                                    .replace(File.separatorChar, '/'));
                    try {
                        out.putNextEntry(entry);
                        if (Files.isRegularFile(x)) {
                            try (InputStream sourceData =
                                    Files.newInputStream(x)) {
                                ByteStreams.copy(sourceData, out);
                            }
                        }
                        out.closeEntry();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            try {
                Files.deleteIfExists(codeSource);
            } catch (IOException e1) {
            }
        }
    }

    @Override
    public void upgradeSettings(String version, File configPath,
            File oldConfigPath) {
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame,
            boolean clock) {
    }

    @Override
    public void onInitCompleted(Minecraft minecraft, LiteLoader loader) {
    }

    private void applyModules() {
        System.err.println("Applying modules...");
        /*
         * we assume dev here, because if this isn't dev why are you running
         * this code ffs
         */
        URL src =
                getClass().getProtectionDomain().getCodeSource().getLocation();
        Path codeSource;
        try {
            // com.techshroom.thxmumfrey.LiteModLoadModule == 4 parents
            codeSource =
                    Paths.get(src.toURI()).getParent().getParent().getParent()
                            .getParent();
        } catch (URISyntaxException e1) {
            return;
        }
        initLoader(codeSource);
        try {
            LoadedModuleInfo lmi =
                    new LoadedModuleInfo(codeSource.toAbsolutePath().toFile());
            ClassLoader classLoader = ScriptCore.class.getClassLoader();
            Files.walkFileTree(codeSource, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                    file = file.toAbsolutePath();
                    if (file.toString().endsWith(".class")) {
                        doLoadMyClass(lmi,
                                      classLoader,
                                      file.toFile(),
                                      file.getFileName().toString()
                                              .replace(".class", ""),
                                      codeSource.relativize(file).toString()
                                              .replace(".class", "")
                                              .replace(File.separatorChar, '.'),
                                      LiteModLoadModule.this.logger);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            lmi.printStatus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doLoadMyClass(LoadedModuleInfo lmi, ClassLoader classLoader,
            File module, String className, String fullClassName,
            IErrorLogger logger) {
        IMacrosAPIModule newModule = null;
        boolean ignoreNull = false;

        if (className.startsWith("ScriptAction")) {
            newModule =
                    lmi.addAction(addModule(classLoader,
                                            IScriptAction.class,
                                            "action",
                                            fullClassName,
                                            logger));
        } else if (className.startsWith("VariableProvider")) {
            newModule =
                    lmi.addProvider(this.addModule(classLoader,
                                                   IVariableProvider.class,
                                                   "variable provider",
                                                   fullClassName,
                                                   logger));
        } else if (className.startsWith("ScriptedIterator")) {
            newModule =
                    lmi.addIterator(this.addModule(classLoader,
                                                   IScriptedIterator.class,
                                                   "iterator",
                                                   fullClassName,
                                                   logger));
            ignoreNull = true;
        } else if (className.startsWith("EventProvider")) {
            newModule =
                    lmi.addEventProvider(this
                            .addModule(classLoader,
                                       IMacroEventProvider.class,
                                       "event provider",
                                       fullClassName,
                                       logger));
        } else {
            return;
        }

        if (newModule == null && !ignoreNull) {
            if (logger != null)
                logger.logError("API: Error initialising " + module.getName());
            return;
        }
    }

    private volatile IErrorLogger logger;
    private volatile ModuleLoader loader;
    private volatile Method addModule;

    private void initLoader(Path location) {
        this.loader = new ModuleLoader(location.toFile());
    }

    private Method getAddModule() {
        if (this.addModule == null) {
            try {
                Class<?> macroModCore =
                        Class.forName("net.eq2online.macros.core.MacroModCore");
                macroModCore.getDeclaredMethod("getInstance").invoke(null);
                this.logger =
                        (IErrorLogger) macroModCore
                                .getDeclaredMethod("getMacroManager")
                                .invoke(null);
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException
                    | SecurityException | ClassNotFoundException e1) {
                e1.printStackTrace();
                this.logger = System.err::println;
            }
            this.loader = new ModuleLoader(new File("."));
            try {
                this.addModule =
                        ModuleLoader.class
                                .getDeclaredMethod("addModule", new Class<?>[] {
                                        ClassLoader.class, Class.class,
                                        String.class, String.class,
                                        IErrorLogger.class });
                this.addModule.setAccessible(true);
            } catch (NoSuchMethodException | SecurityException e) {
                Throwables.propagate(e);
            }
        }
        return this.addModule;
    }

    private <T extends IMacrosAPIModule> T addModule(ClassLoader classLoader,
            Class<T> moduleClassType, String moduleType, String className,
            IErrorLogger logger) {
        try {
            return moduleClassType.cast(getAddModule().invoke(this.loader,
                                                              classLoader,
                                                              moduleClassType,
                                                              moduleType,
                                                              className,
                                                              logger));
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException all) {
            Throwable e = all;
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
            }
            throw Throwables.propagate(e);
        }
    }
}
