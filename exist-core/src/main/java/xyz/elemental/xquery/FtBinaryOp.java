/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

public abstract class FtBinaryOp extends FTMatch {
    private final FTMatch left;
    private final FTMatch right;

    public FtBinaryOp(FTMatch left, FTMatch right) {
        this.left = left;
        this.right = right;
    }

    public FTMatch getLeft() {
        return left;
    }

    public FTMatch getRight() {
        return right;
    }
}
