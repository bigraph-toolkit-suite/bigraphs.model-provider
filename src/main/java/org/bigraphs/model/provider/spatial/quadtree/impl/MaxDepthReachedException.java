package org.bigraphs.model.provider.spatial.quadtree.impl;

public class MaxDepthReachedException extends RuntimeException {
    public MaxDepthReachedException(String message) {
        super(message);
    }
}