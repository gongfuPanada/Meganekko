/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eje_c.meganekko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import com.eje_c.meganekko.Material.GVRShaderType;
import com.eje_c.meganekko.RenderData.GVRRenderMaskBit;

/**
 * One of the key GVRF classes: a scene object.
 * 
 * Every scene object has a {@linkplain #getTransform() location}, and can have
 * {@linkplain #children() children}. An invisible scene object can be used to
 * move a set of scene as a unit, preserving their relative geometry. Invisible
 * scene objects don't need any {@linkplain SceneObject#getRenderData()
 * render data.}
 * 
 * <p>
 * Visible scene objects must have render data
 * {@linkplain SceneObject#attachRenderData(RenderData) attached.} Each
 * {@link RenderData} has a {@link Mesh GL mesh} that defines its
 * geometry, and a {@link Material} that defines its surface.
 */
public class SceneObject extends HybridObject {

	private Transform mTransform;
	private RenderData mRenderData;
	private EyePointeeHolder mEyePointeeHolder;
	private SceneObject mParent;
	private final List<SceneObject> mChildren = new ArrayList<SceneObject>();
	private float mOpacity = 1.0f;
	private boolean mVisible = true;

	/**
	 * Constructs an empty scene object with a default {@link Transform
	 * transform}.
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}
	 */
	public SceneObject(GLContext gvrContext) {
		this(gvrContext, NativeSceneObject.ctor());
	}
	
	protected SceneObject(GLContext gvrContext, long nativePointer) {
	    super(gvrContext, nativePointer);
        attachTransform(new Transform(gvrContext));
	}

	/**
	 * Constructs a scene object with an arbitrarily complex mesh.
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}
	 * @param mesh
	 *            a {@link Mesh} - usually generated by one of the
	 *            {@link GLContext#loadMesh(AndroidResource)} methods, or
	 *            {@link GLContext#createQuad(float, float)}
	 */
	public SceneObject(GLContext gvrContext, Mesh mesh) {
		this(gvrContext);
		RenderData renderData = new RenderData(gvrContext);
		attachRenderData(renderData);
		renderData.setMesh(mesh);
	}

	/**
	 * Constructs a rectangular scene object, whose geometry is completely
	 * specified by the width and height.
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}
	 * @param width
	 *            the scene object's width
	 * @param height
	 *            the scene object's height
	 */
	public SceneObject(GLContext gvrContext, float width, float height) {
		this(gvrContext, gvrContext.createQuad(width, height));
	}

	/**
	 * The base texture constructor: Constructs a scene object with
	 * {@linkplain Mesh an arbitrarily complex geometry} that uses a specific
	 * shader to display a {@linkplain Texture texture.}
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}
	 * @param mesh
	 *            a {@link Mesh} - usually generated by one of the
	 *            {@link GLContext#loadMesh(AndroidResource)} methods, or
	 *            {@link GLContext#createQuad(float, float)}
	 * @param texture
	 *            a {@link Texture}
	 * @param shaderId
	 *            a specific shader Id - see {@link GVRShaderType} and
	 *            {@link MaterialShaderManager}
	 * 
	 */
	public SceneObject(GLContext gvrContext, Mesh mesh,
			Texture texture, MaterialShaderId shaderId) {
		this(gvrContext, mesh);

		Material material = new Material(gvrContext, shaderId);
		material.setMainTexture(texture);
		getRenderData().setMaterial(material);
	}

	private static final MaterialShaderId STANDARD_SHADER = GVRShaderType.Texture.ID;

	/**
	 * Constructs a scene object with {@linkplain Mesh an arbitrarily complex
	 * geometry} that uses the standard {@linkplain Texture 'texture shader'} to
	 * display a {@linkplain Texture texture.}
	 * 
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}
	 * @param mesh
	 *            a {@link Mesh} - usually generated by one of the
	 *            {@link GLContext#loadMesh(AndroidResource)} methods, or
	 *            {@link GLContext#createQuad(float, float)}
	 * @param texture
	 *            a {@link Texture}
	 */
	public SceneObject(GLContext gvrContext, Mesh mesh,
			Texture texture) {
		this(gvrContext, mesh, texture, STANDARD_SHADER);
	}

	/**
	 * Very high-level constructor that asynchronously loads the mesh and
	 * texture.
	 * 
	 * Note that because of <a href="package-summary.html#async">asynchronous
	 * request consolidation</a> you generally don't have to do anything special
	 * to create several objects that share the same mesh or texture: if you
	 * create all the objects in {@link GVRScript#onInit(GLContext) onInit(),}
	 * the meshes and textures will generally <em>not</em> have loaded before
	 * your {@code onInit()} method finishes. Thus, the loading code will see
	 * that, say, {@code sceneObject2} and {@code sceneObject3} are using the
	 * same mesh as {@code sceneObject1}, and will only load the mesh once.
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}.
	 * @param futureMesh
	 *            mesh of the object.
	 * @param futureTexture
	 *            texture of the object.
	 * 
	 * @since 1.6.8
	 */
	public SceneObject(GLContext gvrContext, Future<Mesh> futureMesh,
			Future<Texture> futureTexture) {
		this(gvrContext);

		// Create the render data
		RenderData renderData = new RenderData(gvrContext);

		// Set the mesh
		renderData.setMesh(futureMesh);

		// Set the texture
		Material material = new Material(gvrContext);
		material.setMainTexture(futureTexture);
		renderData.setMaterial(material);

		// Attach the render data
		attachRenderData(renderData);
	}

	/**
	 * Very high-level constructor that asynchronously loads the mesh and
	 * texture.
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}.
	 * @param mesh
	 *            Basically, a stream containing a mesh file.
	 * @param texture
	 *            Basically, a stream containing a texture file. This can be
	 *            either a compressed texture or a regular Android bitmap file.
	 * 
	 * @since 1.6.7
	 */
	public SceneObject(GLContext gvrContext, AndroidResource mesh,
			AndroidResource texture) {
		this(gvrContext, gvrContext.loadFutureMesh(mesh), gvrContext
				.loadFutureTexture(texture));
	}

	/**
	 * Create a standard, rectangular texture object, using a non-default shader
	 * to apply complex visual affects.
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}
	 * @param width
	 *            the rectangle's width
	 * @param height
	 *            the rectangle's height
	 * @param texture
	 *            a {@link Texture}
	 * @param shaderId
	 *            a specific shader Id
	 */
	public SceneObject(GLContext gvrContext, float width, float height,
			Texture texture, MaterialShaderId shaderId) {
		this(gvrContext, gvrContext.createQuad(width, height), texture,
				shaderId);
	}

	/**
	 * Constructs a 2D, rectangular scene object that uses the standard
	 * {@linkplain Texture 'texture shader'} to display a {@linkplain Texture
	 * texture.}
	 * 
	 * @param gvrContext
	 *            current {@link GLContext}
	 * @param width
	 *            the rectangle's width
	 * @param height
	 *            the rectangle's height
	 * @param texture
	 *            a {@link Texture}
	 */
	public SceneObject(GLContext gvrContext, float width, float height,
			Texture texture) {
		this(gvrContext, width, height, texture, STANDARD_SHADER);
	}

	/**
	 * Get the (optional) name of the object.
	 * 
	 * @return The name of the object. If no name has been assigned, the
	 *         returned string will be empty.
	 */
	public String getName() {
		return NativeSceneObject.getName(getNative());
	}

	/**
	 * Set the (optional) name of the object.
	 * 
	 * Scene object names are not needed: they are only for the application's
	 * convenience.
	 * 
	 * @param name
	 *            Name of the object.
	 */
	public void setName(String name) {
		NativeSceneObject.setName(getNative(), name);
	}

	/**
	 * Replace the current {@link Transform transform}
	 * 
	 * @param transform
	 *            New transform.
	 */
	void attachTransform(Transform transform) {
		mTransform = transform;
		NativeSceneObject.attachTransform(getNative(), transform.getNative());
	}

	/**
	 * Remove the object's {@link Transform transform}. After this call, the
	 * object will have no transformations associated with it.
	 */
	void detachTransform() {
		mTransform = null;
		NativeSceneObject.detachTransform(getNative());
	}

	/**
	 * Get the {@link Transform}.
	 * 
	 * A {@link Transform} encapsulates a 4x4 matrix that specifies how to
	 * render the {@linkplain Mesh GL mesh:} transform methods let you move,
	 * rotate, and scale your scene object.
	 * 
	 * @return The current {@link Transform transform}. If no transform is
	 *         currently attached to the object, returns {@code null}.
	 */
	public Transform getTransform() {
		return mTransform;
	}

	/**
	 * Attach {@linkplain RenderData rendering data} to the object.
	 * 
	 * If other rendering data is currently attached, it is replaced with the
	 * new data. {@link RenderData} contains the GL mesh, the texture, the
	 * shader id, and various shader constants.
	 * 
	 * @param renderData
	 *            New rendering data.
	 */
	public void attachRenderData(RenderData renderData) {
		mRenderData = renderData;
		renderData.setOwnerObject(this);
		NativeSceneObject.attachRenderData(getNative(), renderData.getNative());
	}

	/**
	 * Detach the object's current {@linkplain RenderData rendering data}.
	 * 
	 * An object with no {@link RenderData} is not visible.
	 */
	public void detachRenderData() {
		if (mRenderData != null) {
			mRenderData.setOwnerObject(null);
		}
		mRenderData = null;
		NativeSceneObject.detachRenderData(getNative());
	}

	/**
	 * Get the current {@link RenderData}.
	 * 
	 * @return The current {@link RenderData rendering data}. If no rendering
	 *         data is currently attached to the object, returns {@code null}.
	 */
	public RenderData getRenderData() {
		return mRenderData;
	}

	/**
	 * Attach a new {@link EyePointeeHolder} to the object.
	 * 
	 * If another {@link EyePointeeHolder} is currently attached, it is
	 * replaced with the new one.
	 * 
	 * @param eyePointeeHolder
	 *            New {@link EyePointeeHolder}.
	 */
	public void attachEyePointeeHolder(EyePointeeHolder eyePointeeHolder) {
		mEyePointeeHolder = eyePointeeHolder;
		eyePointeeHolder.setOwnerObject(this);
		NativeSceneObject.attachEyePointeeHolder(getNative(),
				eyePointeeHolder.getNative());
	}

	/**
	 * Attach a default {@link EyePointeeHolder} to the object.
	 * 
	 * The default holder contains a single {@link MeshEyePointee}, which
	 * refers to the bounding box of the {@linkplain Mesh mesh} in this scene
	 * object's {@linkplain RenderData render data}. If you need more control
	 * (multiple meshes, perhaps, or using the actual mesh instead of a bounding
	 * box) use the {@linkplain #attachEyePointeeHolder(EyePointeeHolder)
	 * explicit overload.} If another {@link EyePointeeHolder} is currently
	 * attached, it is replaced with the new one.
	 * 
	 * @return {@code true} if and only this scene object has render data
	 *         <em>and</em> you have called either
	 *         {@link RenderData#setMesh(Mesh)} or
	 *         {@link RenderData#setMesh(Future)}; {@code false}, otherwise.
	 */
	public boolean attachEyePointeeHolder() {
		RenderData renderData = getRenderData();
		if (renderData == null) {
			return false;
		}

		Future<EyePointee> eyePointee = renderData.getMeshEyePointee();
		if (eyePointee == null) {
			return false;
		}

		EyePointeeHolder eyePointeeHolder = new EyePointeeHolder(
				getGVRContext());
		eyePointeeHolder.addPointee(eyePointee);
		attachEyePointeeHolder(eyePointeeHolder);
		return true;
	}

	/**
	 * Detach the object's current {@link EyePointeeHolder}.
	 */
	public void detachEyePointeeHolder() {
		// see GVRPicker.findObjects
		Picker.sFindObjectsLock.lock();
		try {
			if (mEyePointeeHolder != null) {
				mEyePointeeHolder.setOwnerObject(null);
			}
			mEyePointeeHolder = null;
			NativeSceneObject.detachEyePointeeHolder(getNative());
		} finally {
			Picker.sFindObjectsLock.unlock();
		}
	}

	/**
	 * Get the attached {@link EyePointeeHolder}
	 * 
	 * @return The {@link EyePointeeHolder} attached to the object. If no
	 *         {@link EyePointeeHolder} is currently attached, returns
	 *         {@code null}.
	 */
	public EyePointeeHolder getEyePointeeHolder() {
		return mEyePointeeHolder;
	}

	/**
	 * Simple, high-level API to enable or disable eye picking for this scene
	 * object.
	 * 
	 * The {@linkplain #attachEyePointeeHolder(EyePointeeHolder) low-level
	 * API} gives you a lot of control over eye picking, but it does involve an
	 * awful lot of details. Since most apps are just going to use the
	 * {@linkplain #attachEyePointeeHolder() simple API} anyhow, this method
	 * (and {@link #getPickingEnabled()}) provides a simple boolean property.
	 * 
	 * @param enabled
	 *            Should eye picking 'see' this scene object?
	 * 
	 * @since 2.0.2
	 */
	public void setPickingEnabled(boolean enabled) {
		if (enabled != getPickingEnabled()) {
			if (enabled) {
				attachEyePointeeHolder();
			} else {
				detachEyePointeeHolder();
			}
		}
	}

	/**
	 * Is eye picking enabled for this scene object?
	 * 
	 * @return Whether eye picking can 'see' this scene object?
	 * 
	 * @since 2.0.2
	 */
	public boolean getPickingEnabled() {
		return mEyePointeeHolder != null;
	}

	/**
	 * Get the {@linkplain SceneObject parent object.}
	 * 
	 * If the object has been {@link #addChildObject(SceneObject) added as a
	 * child} to another {@link SceneObject}, returns that object. Otherwise,
	 * returns {@code null}.
	 * 
	 * @return The parent {@link SceneObject} or {@code null}.
	 */
	public SceneObject getParent() {
		return mParent;
	}

	/**
	 * Add {@code child} as a child of this object.
	 * 
	 * @param child
	 *            {@link SceneObject Object} to add as a child of this
	 *            object.
	 */
	public void addChildObject(SceneObject child) {
		mChildren.add(child);
		child.mParent = this;
		NativeSceneObject.addChildObject(getNative(), child.getNative());
	}

	/**
	 * Remove {@code child} as a child of this object.
	 * 
	 * @param child
	 *            {@link SceneObject Object} to remove as a child of this
	 *            object.
	 */
	public void removeChildObject(SceneObject child) {
		mChildren.remove(child);
		child.mParent = null;
		NativeSceneObject.removeChildObject(getNative(), child.getNative());
	}

	/**
	 * Check if {@code otherObject} is colliding with this object.
	 * 
	 * @param otherObject
	 *            {@link SceneObject Object} to check for collision with this
	 *            object.
	 * @return {@code true) if objects collide, {@code false} otherwise
	 */
	public boolean isColliding(SceneObject otherObject) {
		return NativeSceneObject.isColliding(getNative(),
				otherObject.getNative());
	}

	/**
	 * Sets the range of distances from the camera where this object will be
	 * shown.
	 *
	 * @param minRange
	 *            The closest distance to the camera in which this object
	 *            should be shown. This should be a positive number between 0
	 *            and Float.MAX_VALUE.
	 * @param maxRange
	 *            The farthest distance to the camera in which this object
	 *            should be shown. This should be a positive number between 0
	 *            and Float.MAX_VALUE.
	 */
	public void setLODRange(float minRange, float maxRange) {
		if (minRange < 0 || maxRange < 0) {
			throw new IllegalArgumentException(
					"minRange and maxRange must be between 0 and Float.MAX_VALUE");
		}
		if (minRange > maxRange) {
			throw new IllegalArgumentException(
					"minRange should not be greater than maxRange");
		}
		NativeSceneObject.setLODRange(getNative(), minRange, maxRange);
	}

	/**
	 * Get the minimum distance from the camera in which to show this object.
	 * 
	 * @return the minimum distance from the camera in which to show this
	 *         object. Default value is 0.
	 */
	public float getLODMinRange() {
		return NativeSceneObject.getLODMinRange(getNative());
	}

	/**
	 * Get the maximum distance from the camera in which to show this object.
	 * 
	 * @return the maximum distance from the camera in which to show this
	 *         object. Default value is Float.MAX_VALUE.
	 */
	public float getLODMaxRange() {
		return NativeSceneObject.getLODMaxRange(getNative());
	}

	/**
	 * Get the number of child objects.
	 * 
	 * @return Number of {@link SceneObject objects} added as children of
	 *         this object.
	 */
	public int getChildrenCount() {
		return mChildren.size();
	}

	/**
	 * Get the child object at {@code index}.
	 * 
	 * @param index
	 *            Position of the child to get.
	 * @return {@link SceneObject Child object}.
	 * 
	 * @throws {@link
	 *             java.lang.IndexOutOfBoundsException} if there is no child at
	 *             that position.
	 */
	public SceneObject getChildByIndex(int index) {
		return mChildren.get(index);
	}

	/**
	 * As an alternative to calling {@link #getChildrenCount()} then repeatedly
	 * calling {@link #getChildByIndex(int)}, you can
	 * 
	 * <pre>
	 * for (GVRSceneObject child : parent.children()) {
	 * }
	 * </pre>
	 * 
	 * @return An {@link Iterable}, so you can use Java's enhanced for loop.
	 *         This {@code Iterable} gives you an {@link Iterator} that does not
	 *         support {@link Iterator#remove()}.
	 *         <p>
	 *         At some point, this might actually return a
	 *         {@code List<GVRSceneObject>}, but that would require either
	 *         creating an immutable copy or writing a lot of code to support
	 *         methods like {@link List#addAll(java.util.Collection)} and
	 *         {@link List#clear()} - for now, we just create a very
	 *         light-weight class that only supports iteration.
	 */
	public Iterable<SceneObject> children() {
		return new Children(this);
	}

	/**
	 * Get all the children, in a single list.
	 * 
	 * @return An un-modifiable list of this object's children.
	 * 
	 * @since 2.0.0
	 */
	public List<SceneObject> getChildren() {
		return Collections.unmodifiableList(mChildren);
	}

	/** The internal list - do not make any changes! */
	List<SceneObject> rawGetChildren() {
		return mChildren;
	}

	private static class Children implements Iterable<SceneObject>,
			Iterator<SceneObject> {

		private final SceneObject object;
		private int index;

		private Children(SceneObject object) {
			this.object = object;
			this.index = 0;
		}

		@Override
		public Iterator<SceneObject> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return index < object.getChildrenCount();
		}

		@Override
		public SceneObject next() {
			return object.getChildByIndex(index++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Add {@code childComponent} as a child of this object (owner object of the
	 * component is added as child). Adding a component will increase the
	 * {@link getChildrenCount() getChildrenCount()} for this scene object.
	 * 
	 * @param childComponent
	 *            {@link Component Component} to add as a child of this
	 *            object.
	 */
	public void addChildObject(Component childComponent) {
		if (childComponent.getOwnerObject() != null) {
			addChildObject(childComponent.getOwnerObject());
		}
	}

	/**
	 * Remove {@code childComponent} as a child of this object (owner object of
	 * the component is removed as child). Removing a component will decrease
	 * the {@link getChildrenCount() getChildrenCount()} for this scene object.
	 * 
	 * @param childComponent
	 *            {@link Component Component} to remove as a child of this
	 *            object.
	 */
	public void removeChildObject(Component childComponent) {
		if (childComponent.getOwnerObject() != null) {
			removeChildObject(childComponent.getOwnerObject());
		}
	}

	/**
	 * Set visibility of this object. This affects also all children of this
	 * object.
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.mVisible = visible;
		updateVisibility();
	}

	/**
	 * Get visibility set by {@link setVisible() setVisible()}.
	 * 
	 * @return Visibility of this object.
	 */
	public boolean isVisible() {
		return mVisible;
	}

	/**
	 * Get if object is shown to scene.
	 * 
	 * @return Visibility of this object. Even if isVisible() == true, it can
	 *         return false if this parent.isVisible() == false.
	 */
	public boolean isShown() {

		if (!mVisible)
			return mVisible;

		if (getParent() == null)
			return mVisible;

		return mVisible && getParent().isShown();
	}

	private void updateVisibility() {
		RenderData renderData = getRenderData();
		if (renderData != null) {
			boolean visible = isShown();
			renderData.setRenderMask(visible ? GVRRenderMaskBit.Left | GVRRenderMaskBit.Right : 0);
		}

		for (SceneObject child : children()) {
			child.updateVisibility();
		}
	}

	/**
	 * Set opacity of this object. This affects also all children of this
	 * object.
	 * 
	 * @param opacity
	 */
	public void setOpacity(float opacity) {
		this.mOpacity = opacity;
		updateOpacity();
	}

	/**
	 * Get opacity set by {@link setOpacity() setOpacity()}.
	 * 
	 * @return opacity
	 */
	public float getOpacity() {
		return mOpacity;
	}

	private float getInternalOpacity() {
		float parentOpacity = getParent() != null ? getParent().getInternalOpacity() : 1.0f;
		return mOpacity * parentOpacity;
	}

	private void updateOpacity() {
		RenderData renderData = getRenderData();
		if (renderData != null) {
			Material material = renderData.getMaterial();
			if (material != null) {
				float opacity = getInternalOpacity();
				material.setOpacity(opacity);
			}
		}

		for (SceneObject child : children()) {
			child.updateOpacity();
		}
	}
}

class NativeSceneObject {
	static native long ctor();

	static native String getName(long sceneObject);

	static native void setName(long sceneObject, String name);

	static native void attachTransform(long sceneObject, long transform);

	static native void detachTransform(long sceneObject);

	static native void attachRenderData(long sceneObject, long renderData);

	static native void detachRenderData(long sceneObject);

	static native void attachEyePointeeHolder(long sceneObject,
			long eyePointeeHolder);

	static native void detachEyePointeeHolder(long sceneObject);

	static native long setParent(long sceneObject, long parent);

	static native void addChildObject(long sceneObject, long child);

	static native void removeChildObject(long sceneObject, long child);

	static native boolean isColliding(long sceneObject, long otherObject);

	static native void setLODRange(long sceneObject, float minRange, float maxRange);

	static native float getLODMinRange(long sceneObject);

	static native float getLODMaxRange(long sceneObject);
}
