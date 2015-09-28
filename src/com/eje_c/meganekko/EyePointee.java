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

/**
 * Super class of GVRMeshEyePointee.
 * 
 * An GVREyePointee is something that is being pointed at by a picking ray.
 * GVREyePointees are held by {@linkplain EyePointeeHolder eye pointee
 * holders}. Eye pointee holders are attached to {@link SceneObject}s so they
 * can be returned by the {@link Picker}.
 * 
 * <p>
 * This bizarre little class only contains a constructor and was intended as a
 * sort of interface. The only actual {@link EyePointee} is
 * {@link MeshEyePointee}.
 */

public class EyePointee extends HybridObject {
    EyePointee(VrContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }
}
