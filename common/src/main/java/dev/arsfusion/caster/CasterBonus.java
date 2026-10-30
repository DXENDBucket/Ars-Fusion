package dev.arsfusion.caster;

/** Loader-neutral item modifier. Native operation ordinals are addition, multiplied base, multiplied total. */
public record CasterBonus(String attribute,String identity,double amount,int operation) {}
