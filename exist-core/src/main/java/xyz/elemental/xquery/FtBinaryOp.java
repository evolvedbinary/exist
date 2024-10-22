/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

public abstract class FtBinaryOp extends FtSelection {
    private final FtSelection left;
    private final FtSelection right;

    public FtBinaryOp(FtSelection left, FtSelection right) {
        this.left = left;
        this.right = right;
    }

    public FtSelection getLeft() {
        return left;
    }

    public FtSelection getRight() {
        return right;
    }
}
