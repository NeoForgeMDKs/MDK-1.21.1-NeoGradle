package com.rz.mswsm;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_CHUNK_DIAGNOSTICS;

    static {
        BUILDER.push("diagnostics");

        ENABLE_CHUNK_DIAGNOSTICS = BUILDER
                .comment(
                        "Enables advanced chunk shutdown diagnostics.",
                        "Intended for troubleshooting world save/unload hangs.",
                        "WARNING: Enabling this captures stack traces and may produce large logs and additional runtime overhead.",
                        "Leave disabled during normal gameplay. A game restart is recommended after changing this option."
                )
                .define("enableChunkDiagnostics", false);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static boolean chunkDiagnosticsEnabled() {
        return ENABLE_CHUNK_DIAGNOSTICS.get();
    }
}
