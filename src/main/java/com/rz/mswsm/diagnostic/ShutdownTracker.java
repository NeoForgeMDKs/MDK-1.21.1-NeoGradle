package com.rz.mswsm.diagnostic;

public final class ShutdownTracker
{
    private static volatile boolean shuttingDown = false;

    private ShutdownTracker()
    {
    }

    public static void beginShutdown()
    {
        shuttingDown = true;
    }

    public static boolean isShuttingDown()
    {
        return shuttingDown;
    }

    public static void reset()
    {
        shuttingDown = false;
    }
}