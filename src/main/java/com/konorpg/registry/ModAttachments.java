package com.konorpg.registry;

import com.konorpg.KonoRPG;
import com.konorpg.data.PlayerRpgData;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, KonoRPG.MOD_ID);

    public static final Supplier<AttachmentType<PlayerRpgData>> RPG = ATTACHMENTS.register("rpg",
            () -> AttachmentType.serializable(PlayerRpgData::new).copyOnDeath().build());

    private ModAttachments() {}
}
