package com.lukashornych.mathmare;

import lombok.*;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Convenient way to handle projection and view matrices in game.
 *
 * @author Lukáš Hornych
 */
@ToString
@EqualsAndHashCode
public class Camera {

    protected final GameManager gameManager;

    @Getter @Setter protected Vector3f position;
    @Getter protected float azimuth;
    @Getter protected float zenith;

    protected Matrix4f projection;
    protected Matrix4f view;

    public Camera(@NonNull GameManager gameManager, @NonNull Vector3f position, float azimuth, float zenith) {
        this.gameManager = gameManager;

        this.position = position;
        this.azimuth = azimuth;
        this.zenith = zenith;

        this.projection = new Matrix4f();
        this.view = new Matrix4f();
    }

    /**
     * Builds projection matrix from current camera state
     *
     * @return projection matrix
     */
    public Matrix4f getProjection() {
        projection.identity();

        float aspectRatio = gameManager.getWindow().getWidth() / (float) gameManager.getWindow().getHeight();
        projection.perspective(1f, aspectRatio, 0.1f, 100f);

        return projection;
    }

    /**
     * Builds view matrix from current camera state
     *
     * @return view matrix
     */
    public Matrix4f getView() {
        final Vector3f xAxis = new Vector3f(
                Math.cos(azimuth),
                0f,
                -Math.sin(azimuth)
        );
        final Vector3f yAxis = new Vector3f(
                Math.sin(azimuth) * Math.sin(zenith),
                Math.cos(zenith),
                Math.cos(azimuth) * Math.sin(zenith)
        );
        final Vector3f zAxis = new Vector3f(
                Math.sin(azimuth) * Math.cos(zenith),
                -Math.sin(zenith),
                Math.cos(zenith) * Math.cos(azimuth)
        );

        view = new Matrix4f(
                new Vector4f(xAxis, -(new Vector3f(position).dot(xAxis))),
                new Vector4f(yAxis, -(new Vector3f(position).dot(yAxis))),
                new Vector4f(zAxis, -(new Vector3f(position).dot(zAxis))),
                new Vector4f(0f, 0f, 0f, 1f)
        );
        view.transpose();

        return view;
    }

    /**
     * Increases current azimuth by the step
     *
     * @param step how much azimuth to add
     */
    public void addAzimuth(float step) {
        azimuth += step;
        azimuth = azimuth % (float) (Math.PI * 2);
    }

    /**
     * Increases current zenith by the step
     *
     * @param step how much zenith to add
     */
    public void addZenith(float step) {
        zenith += step;
        zenith = Math.clamp((float) (-Math.PI / 2f), (float) (Math.PI / 2f), zenith);
    }
}
