package dev.arsfusion.bow;

/** Per-projectile opt-in; implemented on Projectile, not on unrelated entities. */
public interface SpellCarryingProjectile {
    BowPayload arsFusion$payload();
    void arsFusion$payload(BowPayload payload);
}
