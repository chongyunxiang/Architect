/*******************************************************************************
 *     Architect - A free 2D/3D home and interior designer
 *     Copyright (C) 2021, 2022  Daniel Höh
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;

import de.dh.cad.architect.model.assets.AssetRefPath;
import de.dh.cad.architect.model.objects.Anchor;
import de.dh.cad.architect.model.objects.BaseAnchoredObject;
import de.dh.cad.architect.model.objects.BaseObject;
import de.dh.cad.architect.model.objects.BaseSolidObject;
import de.dh.cad.architect.model.objects.SurfaceConfiguration;
import de.dh.cad.architect.ui.Strings;
import de.dh.cad.architect.ui.controller.UiController;
import de.dh.cad.architect.ui.properties.ConstantUiProperty;
import de.dh.cad.architect.ui.properties.UiProperty;
import de.dh.cad.architect.ui.properties.UiProperty.PropertyType;
import de.dh.cad.architect.ui.view.IObjectReconciler;

public abstract class BaseObjectUIRepresentation extends AbstractObjectUIRepresentation {
    public static final String KEY_PROPERTY_TYPE = "type";
    public static final String KEY_PROPERTY_ID = "id";
    public static final String KEY_PROPERTY_NAME = "name";

    public BaseObjectUIRepresentation(IObjectReconciler reconciler) {
        super(reconciler);
    }

    @Override
    public ObjectProperties getProperties(BaseObject bo, UiController uiController) {
        OrderedMap<String, Collection<UiProperty<?>>> result = new LinkedMap<>();
        addProperties(result, bo, uiController);
        return new ObjectProperties(result);
    }

    // To be overridden and called as super method
    protected void addProperties(Map<String, Collection<UiProperty<?>>> result, BaseObject bo, UiController uiController) {
        Collection<UiProperty<?>> commonProperties = result.computeIfAbsent("Allgemein", cat -> new ArrayList<>());
        commonProperties.addAll(Arrays.<UiProperty<?>>asList(
                new ConstantUiProperty<>(bo, KEY_PROPERTY_TYPE, "Typ", PropertyType.String, bo.getClass().getSimpleName()),
                new ConstantUiProperty<>(bo, KEY_PROPERTY_ID, "Id", PropertyType.String, bo.getId()),
                new UiProperty<String>(bo, KEY_PROPERTY_NAME, "Name", PropertyType.String, true) {
                    @Override
                    public String getValue() {
                        return bo.getName();
                    }

                    @Override
                    public void setValue(Object value) {
                        bo.setName((String) value);
                        uiController.notifyObjectsChanged(bo);
                    }
                }
        ));
        if (bo instanceof BaseSolidObject bso) {
            Collection<UiProperty<?>> surfaeProperties = result.computeIfAbsent("Oberflächen", cat -> new ArrayList<>());
            for (SurfaceConfiguration sc : bso.getSurfaceConfigurations()) {
                AssetRefPath materialAssignment = sc.getMaterialAssignment();
                surfaeProperties.add(new ConstantUiProperty<>(bo, sc.getSurfaceTypeId(), sc.getSurfaceTypeId(), PropertyType.String, materialAssignment == null ? "<Standard>" : materialAssignment.toPathString()));
            }
        }
    }

    public static String getShortName(BaseObject modelObject) {
        if (modelObject instanceof Anchor anchor) {
            String result = anchor.getAnchorType();
            if (StringUtils.isEmpty(result)) {
                return anchor.getId();
            }
            return result;
        }
        return getObjName(modelObject);
    }

    public static String getObjectTypeName(BaseObject modelObject, Cardinality cardinality) {
        return getObjectTypeName(modelObject.getClass(), cardinality);
    }

    public static String getObjectTypeName(Class<? extends BaseObject> cls, Cardinality cardinality) {
        AbstractObjectUIRepresentation uiRepresentation = ObjectTypesRegistry.getUIRepresentation(cls);
        if (uiRepresentation == null) {
            return cls.getSimpleName();
        }
        return uiRepresentation.getTypeName(cardinality);
    }

    public static String getObjName(BaseObject modelObject) {
        String name = modelObject.getName();
        String typeName = getObjectTypeName(modelObject, Cardinality.Singular);
        if (modelObject instanceof Anchor anchor) {
            BaseAnchoredObject owner = anchor.getAnchorOwner();
            String result = getObjName(owner);
            String anchorInfo = anchor.getAnchorType();
            if (StringUtils.isEmpty(anchorInfo)) {
                anchorInfo = MessageFormat.format(Strings.ANCHOR_NAME_INFO_ID, anchor.getId());
            } else {
                anchorInfo = MessageFormat.format(Strings.ANCHOR_NAME_INFO_TYPE, anchorInfo);
            }
            return MessageFormat.format(Strings.ANCHOR_NAME, result, anchorInfo);
        } else if (StringUtils.isEmpty(name)) {
            return typeName + " " + modelObject.getId();
        } else if (!name.startsWith(typeName)) {
            return typeName + " '" + name + "'";
        } else {
            return name;
        }
    }
}