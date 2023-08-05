/*******************************************************************************
 *     Architect - A free 2D/3D home and interior designer
 *     Copyright (C) 2021 - 2023  Daniel Höh
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>
 *******************************************************************************/
package de.dh.cad.architect.ui.objects;

import de.dh.utils.Vector2D;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class SurfaceData {
    protected final MeshView mMeshview;
    protected final String mSurfaceTypeId;
    protected Vector2D mSurfaceSize = null;

    public SurfaceData(String surfaceTypeId) {
        this(surfaceTypeId, new MeshView());
    }

    public SurfaceData(String surfaceTypeId, MeshView meshView) {
        mSurfaceTypeId = surfaceTypeId;
        mMeshview = meshView;
        if (mMeshview.getMaterial() == null) {
            mMeshview.setMaterial(new PhongMaterial());
        }
    }

    public String getSurfaceTypeId() {
        return mSurfaceTypeId;
    }

    public MeshView getMeshView() {
        return mMeshview;
    }

    public PhongMaterial getMaterial() {
        return (PhongMaterial) mMeshview.getMaterial();
    }

    public Vector2D getSurfaceSize() {
        return mSurfaceSize;
    }

    public void setSurfaceSize(Vector2D value) {
        mSurfaceSize = value;
    }
}