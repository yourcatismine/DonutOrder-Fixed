/*
 * Decompiled with CFR 0.152.
 */
package me.clanify.donutOrder.data;

public enum AlphaSort {
    A_Z,
    Z_A;


    public AlphaSort toggle() {
        return this == A_Z ? Z_A : A_Z;
    }
}

