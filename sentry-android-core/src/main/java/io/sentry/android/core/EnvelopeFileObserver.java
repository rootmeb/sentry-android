package io.sentry.android.core;

import android.os.FileObserver;
import io.sentry.core.IEnvelopeSender;
import io.sentry.core.ILogger;
import io.sentry.core.SentryLevel;
import io.sentry.core.util.Objects;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class EnvelopeFileObserver extends FileObserver {

  private final String rootPath;
  private final IEnvelopeSender envelopeSender;
  private @NotNull final ILogger logger;

  // The preferred overload (Taking File instead of String) is only available from API 29
  @SuppressWarnings("deprecation")
  EnvelopeFileObserver(String path, IEnvelopeSender envelopeSender, @NotNull ILogger logger) {
    super(path);
    this.rootPath = Objects.requireNonNull(path, "File path is required.");
    this.envelopeSender = Objects.requireNonNull(envelopeSender, "Envelope sender is required.");
    this.logger = Objects.requireNonNull(logger, "Logger is required.");
  }

  @Override
  public void onEvent(int eventType, @Nullable String relativePath) {
    if (relativePath == null || eventType != FileObserver.CLOSE_WRITE) {
      return;
    }

    logger.log(
        SentryLevel.DEBUG,
        "onEvent fired for EnvelopeFileObserver with event type %d on path: %s for file %s.",
        eventType,
        this.rootPath,
        relativePath);

    // TODO: Only some event types should be pass through?

    envelopeSender.processEnvelopeFile(this.rootPath + File.separator + relativePath);
  }
}
