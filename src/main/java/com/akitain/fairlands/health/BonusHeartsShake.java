package com.akitain.fairlands.health;

// Client-side "no" feedback: the bonus hearts jitter for a moment when a draught is refused.
public final class BonusHeartsShake {
    public static final long DURATION_MS = 450L;
    private static long shakingUntil;

    private BonusHeartsShake() {
    }

    public static void trigger() {
        shakingUntil = System.currentTimeMillis() + DURATION_MS;
    }

    public static boolean isShaking() {
        return System.currentTimeMillis() < shakingUntil;
    }

    /** Horizontal jitter in pixels, alternating fast enough to read as a refusal. */
    public static int offset() {
        if (!isShaking()) {
            return 0;
        }

        long remaining = shakingUntil - System.currentTimeMillis();
        return (remaining / 60L) % 2L == 0L ? -1 : 1;
    }
}
