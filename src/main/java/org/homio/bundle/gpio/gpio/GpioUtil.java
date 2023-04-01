package org.homio.bundle.gpio.gpio;

import com.pi4j.context.Context;
import com.pi4j.platform.Platform;
import com.pi4j.platform.Platforms;
import com.pi4j.provider.Providers;
import com.pi4j.registry.Registry;
import com.pi4j.util.Console;
import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.homio.bundle.api.util.LogOutputStream;

public class GpioUtil {

    public static void printInfo(Context pi4j, Logger log) {
        var console = new Console();
        var printStream = new PrintStream(new LogOutputStream(log, Level.INFO));
        printLoadedPlatforms(pi4j, console, printStream);
        printDefaultPlatform(pi4j, console, printStream);
        printProviders(pi4j, console, printStream);
        printRegistry(pi4j, console, printStream);
    }

    private static void printProviders(Context pi4j, Console console, PrintStream printStream) {
        Providers providers = pi4j.providers();
        console.box("Pi4J PROVIDERS");
        providers.describe().print(printStream);
        printStream.flush();
        console.println();
    }

    private static void printRegistry(Context pi4j, Console console, PrintStream printStream) {
        Registry registry = pi4j.registry();
        console.box("Pi4J REGISTRY");
        registry.describe().print(printStream);
        printStream.flush();
        console.println();
    }

    private static void printDefaultPlatform(Context pi4j, Console console, PrintStream printStream) {
        Platform platform = pi4j.platform();
        console.box("Pi4J DEFAULT PLATFORM");
        platform.describe().print(printStream);
        printStream.flush();
        console.println();
    }

    private static void printLoadedPlatforms(Context pi4j, Console console, PrintStream printStream) {
        Platforms platforms = pi4j.platforms();
        console.box("Pi4J PLATFORMS");
        platforms.describe().print(printStream);
        printStream.flush();
        console.println();
    }
}
