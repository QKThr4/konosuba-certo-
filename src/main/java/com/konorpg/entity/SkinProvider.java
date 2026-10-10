package com.konorpg.entity;

/** Implemented by humanoid entities that render with the player model and a named skin texture. */
public interface SkinProvider {
    String skinName();

    float modelScale();
}
