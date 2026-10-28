# Magnetization
This project was built with [ForgeGradle-1.2 example project by anatawa12](https://github.com/anatawa12/ForgeGradle-example#how-to-replace-forgegradle-12-with-anatawa12s-fork). First you will need to setup the environment for that project. Once you have done that, you can use the files from this repository to replace the ones that came with the ForgeGradle-1.2 example project.

# Setting up an environment for terminal
All the files and configurations for this are already in place, but tested only for Linux. Make sure you have a JDK for Java 8 installed, not just JRE. For Linux Mint, the package name is `openjdk-8-jdk`.
By default, Java 8 is installed to `/usr/lib/jvm/java-8-openjdk-amd64`. This might be different for your distro or you might have installed Java 8 to somewhere else. In this case, open `gradle.properties` and change `org.gradle.java.home` to point to your Java 8 installation folder.

To build the mod, run:
```
./gradlew build
```

To test if after you've built, run:
```
./gradlew runClient
```

Once built, the mod binaries are placed into `build/libs/` - in case I forget to update the binary in a possible future code update.
