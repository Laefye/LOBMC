package com.laefye.libraryofbabel;

import net.minestom.server.coordinate.Pos;

import java.math.BigInteger;

public class WordPos {
    public int aX;
    public int aY;
    public int aZ;
    public int book;
    public int page;

    public WordPos(int aX, int aY, int aZ, int book, int page) {
        this.aX = aX;
        this.aY = aY;
        this.aZ = aZ;
        this.book = book;
        this.page = page;
    }

    public static WordPos of(Pos pos, int book, int page) {
        var aX = pos.blockX() / 5 + 5_000_000;
        var aY = pos.blockY() / 5 + 12;
        var aZ = pos.blockZ() / 5 + 5_000_000;
        if (aX < 0 || aY < 0 || aZ < 0) {
            return null;
        }
        return new WordPos(aX, aY, aZ, book, page);
    }

    public static int X_BITS = 23;
    public static int Y_BITS = 6;
    public static int Z_BITS = 23;
    public static int BOOK_BITS = 5;
    public static int PAGE_BITS = 6;

    private static BigInteger mask(int bit) {
        var r = 1;
        for (int i = 0; i < bit; i++) {
            r *= 2;
        }
        return BigInteger.valueOf(r - 1);
    }

    public static BigInteger X_MASK = mask(X_BITS);
    public static BigInteger Y_MASK = mask(Y_BITS);
    public static BigInteger Z_MASK = mask(Z_BITS);
    public static BigInteger BOOK_MASK = mask(BOOK_BITS);
    public static BigInteger PAGE_MASK = mask(PAGE_BITS);

    public BigInteger getSeed() {
        return BigInteger.ZERO
                .add(BigInteger.valueOf(aZ).and(Z_MASK))
                .shiftLeft(Y_BITS)
                .add(BigInteger.valueOf(aY).and(Y_MASK))
                .shiftLeft(X_BITS)
                .add(BigInteger.valueOf(aX).and(X_MASK))
                .shiftLeft(BOOK_BITS)
                .add(BigInteger.valueOf(book).and(BOOK_MASK))
                .shiftLeft(PAGE_BITS)
                .add(BigInteger.valueOf(page).and(PAGE_MASK));
    }

    public static WordPos of(BigInteger seed) {
        var page = seed.and(PAGE_MASK).intValue();
        var book = seed.shiftRight(PAGE_BITS).and(BOOK_MASK).intValue();
        var aX = seed.shiftRight(PAGE_BITS + BOOK_BITS).and(X_MASK).intValue();
        var aY = seed.shiftRight(PAGE_BITS + BOOK_BITS + X_BITS).and(Y_MASK).intValue();
        var aZ = seed.shiftRight(PAGE_BITS + BOOK_BITS + X_BITS + Y_BITS).and(Z_MASK).intValue();
        return new WordPos(aX, aY, aZ, book, page);

    }

    public static boolean isValid(Pos pos) {
        if (pos.blockX() % 5 != 0) {
            return false;
        }
        if (pos.blockY() % 5 != 0) {
            return false;
        }
        if (pos.blockZ() % 5 != 0) {
            return false;
        }
        var aX = pos.blockX() / 5 + 5_000_000;
        var aY = pos.blockY() / 5 + 12;
        var aZ = pos.blockZ() / 5 + 5_000_000;
        if (aX < 0 || aY < 0 || aZ < 0) {
            return false;
        }
        return aX < 8_388_608 && aY < 64 && aZ < 8_388_608;
    }

    public Pos getPos() {
        var x = (aX - 5_000_000) * 5;
        var y = (aY - 12) * 5;
        var z = (aZ - 5_000_000) * 5;
        return new Pos(x, y, z);
    }
}
