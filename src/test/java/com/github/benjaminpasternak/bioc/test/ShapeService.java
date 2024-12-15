package com.github.benjaminpasternak.bioc.test;

import com.github.benjaminpasternak.bioc.annotations.Inject;

public class ShapeService {

    private Shape circle;
    public Rectangle rectangle;

    @Inject
    public ShapeService(Shape circle, Rectangle rectangle) {
        this.circle = circle;
        this.rectangle = rectangle;
    }

}
