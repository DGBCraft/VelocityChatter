package online.dgbcraft.velocity.chatter.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;
import online.dgbcraft.velocity.chatter.VelocityChatter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Sanluli36li
 */
public class L10nUtil {
    private static final String JAR = "jar";
    private static final String L10N = "l10n";

    public static Path getL10nPath() {
        Path l10nPath;
        URL knownResource = VelocityChatter.class.getClassLoader().getResource("l10n/messages.properties");
        if (knownResource == null) {
            throw new IllegalStateException("messages.properties does not exist, don't know where we are");
        }
        if (JAR.equals(knownResource.getProtocol())) {
            String jarPathRaw = knownResource.toString().split("!")[0];
            URI path = URI.create(jarPathRaw + "!/");
            try {
                FileSystem fileSystem = FileSystems.newFileSystem(path, Map.of("create", "true"));
                l10nPath = fileSystem.getPath(L10N);
                if (!Files.exists(l10nPath)) {
                    throw new IllegalStateException("l10n does not exist, don't know where we are");
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            try {
                URL url = VelocityChatter.class.getClassLoader().getResource(L10N);
                if (url == null) {
                    throw new IllegalStateException("l10n does not exist, don't know where we are");
                }
                URI uri = url.toURI();
                l10nPath = Paths.get(uri);
            } catch (URISyntaxException e2) {
                throw new IllegalStateException(e2);
            }
        }
        return l10nPath;
    }

    public static void registerTranslations() {
        TranslationStore.StringBased<MessageFormat> store = TranslationStore.messageFormat(Key.key("vchatter", "translations"));
        store.defaultLocale(Locale.US);

        Path l10nPath = getL10nPath();

        try (final Stream<Path> files = Files.walk(l10nPath)) {
            files.filter(Files::isRegularFile).forEach(file -> {
                final String filename = com.google.common.io.Files
                    .getNameWithoutExtension(file.getFileName().toString());
                final String localeName = filename.replace("messages_", "")
                    .replace("messages", "")
                    .replace('_', '-');
                final Locale locale = localeName.isBlank()
                    ? Locale.US
                    : Locale.forLanguageTag(localeName);

                store.registerAll(locale, file, false);
            });
        } catch (IOException e) {
            // logger.error("Encountered an I/O error whilst loading translations", e);
        }
        GlobalTranslator.translator().addSource(store);
    }
}