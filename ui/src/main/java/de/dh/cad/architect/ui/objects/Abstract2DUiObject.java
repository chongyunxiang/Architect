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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.dh.cad.architect.ui.controller.UiController;
import de.dh.cad.architect.ui.view.construction.Abstract2DView;
import de.dh.cad.architect.ui.view.construction.ConstructionView;
import de.dh.cad.architect.ui.view.construction.DragControl2D;
import de.dh.cad.architect.ui.view.construction.UiPlanPosition;
import de.dh.utils.fx.MouseHandlerContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Scale;

/**
 * UI representation of any UI object like model objects and ancillary objects.
 */
public abstract class Abstract2DUiObject extends Group {
    public interface IInitDragHandler {
        void onStartDragEvent(UiPlanPosition startDragPoint);
    }

    public interface IDragHandler {
        void onDragEvent(UiPlanPosition startPoint, UiPlanPosition currentDragPoint);
    }

    protected class UnscaledNode {
        protected final Node mNode;
        protected final Scale mNodeScaleCorrection;

        public UnscaledNode(Node node, Scale nodeScaleCorrection) {
            mNode = node;
            mNodeScaleCorrection = nodeScaleCorrection;
        }

        public Node getNode() {
            return mNode;
        }

        public Scale getNodeScaleCorrection() {
            return mNodeScaleCorrection;
        }
    }

    protected final Collection<UnscaledNode> mUnscaledNodes = new ArrayList<>();
    protected final Abstract2DView mParentView;

    protected final BooleanProperty mMouseOverProperty = new SimpleBooleanProperty(this, "isMouseOver", false);

    protected final EventHandler<MouseEvent> MOUSE_ENTERED_MOUSE_OVER_LISTENER = mouseEvent -> {
        mMouseOverProperty.set(true);
    };

    protected final EventHandler<MouseEvent> MOUSE_EXITED_MOUSE_OVER_LISTENER = mouseEvent -> {
        mMouseOverProperty.set(false);
    };

    protected Abstract2DUiObject(Abstract2DView parentView) {
        mParentView = parentView;

        addEventHandler(MouseEvent.MOUSE_ENTERED, MOUSE_ENTERED_MOUSE_OVER_LISTENER);
        addEventHandler(MouseEvent.MOUSE_EXITED, MOUSE_EXITED_MOUSE_OVER_LISTENER);
    }

    /**
     * Called after this view object was removed from the plan.
     * Can remove event handlers etc.
     */
    public void dispose() {
        // To be overridden

        removeEventHandler(MouseEvent.MOUSE_EXITED, MOUSE_EXITED_MOUSE_OVER_LISTENER);
        removeEventHandler(MouseEvent.MOUSE_ENTERED, MOUSE_ENTERED_MOUSE_OVER_LISTENER);
    }

    public UiController getUiController() {
        return mParentView.getUiController();
    }

    public Abstract2DView getParentView() {
        return mParentView;
    }

    public BooleanProperty mouseOverProperty() {
        return mMouseOverProperty;
    }

    public boolean isMouseOver() {
        return mMouseOverProperty.get();
    }

    protected double getScaleCompensation() {
        return ((ConstructionView) mParentView).getScaleCompensation();
    }

    /**
     * Updates the scale compensation for unscaled nodes.
     * This method can be overridden to be used as hook to update after a scale process.
     */
    public void updateScale(double scaleCompensation) {
        for (UnscaledNode un : mUnscaledNodes) {
            updateScale(un, scaleCompensation);
        }
    }

    protected void updateScale(UnscaledNode un, double scaleCompensation) {
        Scale nodeScaleCorrection = un.getNodeScaleCorrection();
        nodeScaleCorrection.setX(scaleCompensation);
        nodeScaleCorrection.setY(scaleCompensation);
    }

    /**
     * Adds the given node as a part of this model object representation where the graphical representation
     * will be scaled by zoom operations. This is used for almost all shapes representing the actual 2D object.
     */
    protected void addScaled(Node node) {
        node.setUserData(this);
        getChildren().add(node);
    }

    /**
     * Adds the given node as a part of this model object representation where the graphical representation
     * won't be scaled by zoom operations; a compensating {@link Scale} will be added to the node's
     * {@link #getTransforms() transforms} when it's added. That {@link Scale} object is returned by this method.
     * The system will automatically set a compensating scale to that returend scale object which makes the size of
     * the added node remain the same in all zoom levels. To stick the node to a fixed position relative to this (scaled) owner
     * representation, the node's center/pivot point should be configured in the node's relative coordinate system, which
     * will hold it automatically at the same (scaled) plan position.
     * This is typically used for shapes which display ancillary information like text and anchor circles.
     *
     * There are two common use cases for positioning of unscaled objects:
     * <ul>
     * <li>The center of the unscaled node is relative to the (scaled) plan object and also modified by scale operations
     * (E.g. in the modification images in the corners of support objects)</li>
     * <li>The node's size should remain the same, like a well end label or a dimensioning length string.</li>
     * </ul>
     *
     * Note that adding a node via this method will add an additional update step to the compensating {@link Scale} object
     * in each zoom operation. For typical non-trivial use cases, recalculation/repositionion of child elements on scale update processes
     * should better be done by overriding {@link #updateScale(double)} where the child's scale compensation can be updated.
     */
    protected Scale addUnscaled(Node node) {
        node.setUserData(this);
        getChildren().add(node);
        Scale nodeScaleCorrection = new Scale();
        node.getTransforms().add(nodeScaleCorrection);
        UnscaledNode un = new UnscaledNode(node, nodeScaleCorrection);
        mUnscaledNodes.add(un);
        updateScale(un, getScaleCompensation());
        return un.getNodeScaleCorrection();
    }

    protected void remove(Node node) {
        node.setUserData(null);
        // TODO: Improve this
        for (Iterator<UnscaledNode> i = mUnscaledNodes.iterator(); i.hasNext();) {
            UnscaledNode unscaledNode = i.next();
            if (unscaledNode.getNode().equals(node)) {
                i.remove();
                break;
            }
        }
        getChildren().remove(node);
    }

    /**
     * Utility method to configure the dragging feature of a modification control.
     */
    protected MouseHandlerContext createDragHandler(IInitDragHandler onStartDragHandler, IDragHandler onDragHandler, IDragHandler onEndDragHandler, Cursor mouseOverCursor, Cursor dragCursor) {
        DragControl2D dragControl = new DragControl2D();

        return new MouseHandlerContext(
            // Mouse pressed
            mouseEvent -> {
                if (mouseEvent.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                UiPlanPosition uiPosition = mParentView.getPlanPositionFromScene(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                if (onStartDragHandler != null) {
                    onStartDragHandler.onStartDragEvent(uiPosition);
                }
                dragControl.setPosition(uiPosition);
                getScene().setCursor(dragCursor);
                mouseEvent.consume();
            },
            // Mouse released
            mouseEvent -> {
                if (mouseEvent.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                getScene().setCursor(Cursor.DEFAULT);
                if (onEndDragHandler != null) {
                    UiPlanPosition currentPosition = mParentView.getPlanPositionFromScene(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    UiPlanPosition startPosition = dragControl.getPosition();
                    onEndDragHandler.onDragEvent(startPosition, currentPosition);
                    mouseEvent.consume();
                }
            },
            // Mouse moved
            null,
            // Mouse dragged
            mouseEvent -> {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    return;
                }
                if (onDragHandler != null) {
                    UiPlanPosition startPosition = dragControl.getPosition();
                    UiPlanPosition currentPosition = mParentView.getPlanPositionFromScene(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    onDragHandler.onDragEvent(startPosition, currentPosition);
                    mouseEvent.consume();
                }
            },
            // Mouse entered
            mouseEvent -> {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    getScene().setCursor(mouseOverCursor);
                }
                // Don't consume event, could be needed by another handler
            },
            // Mouse exited
            mouseEvent -> {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    getScene().setCursor(Cursor.DEFAULT);
                }
                // Don't consume event, could be needed by another handler
            });
    }
}
