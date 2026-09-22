package uk.iwaservice.sbwarmory.entity;

/**
 * Marker for our own guided-missile entities (SRAW, cruise/beast missile) that happen to extend
 * SuperbWarfare's {@code GunGrenadeEntity} for convenience but are not actually grenades. Anything
 * that should be immune to grenade-only systems (e.g. {@link ActiveDefenseSystemEntity}) implements
 * this instead of being named individually in an exclusion list that's easy to forget to update.
 */
public interface GuidedMissile {
}
