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

package com.eje_c.meganekko.texture;

import com.eje_c.meganekko.HybridObject;
import com.eje_c.meganekko.TextureParameters;
import com.eje_c.meganekko.VrContext;

/**
 * Wrapper for a GL texture.
 */
public class Texture extends HybridObject {
    protected Texture(VrContext vrContext, long ptr) {
        super(vrContext, ptr);
    }

    protected Texture(VrContext vrContext) {
        super(vrContext);
    }

    /**
     * Get the ID generated by {@code glGenTextures()}.
     *
     * @return The GL ID of the texture.
     */
    public int getId() {
        return getId(getNative());
    }

    /**
     * Update the texture parameters {@link TextureParameters} after the
     * texture has been created.
     */
    public void updateTextureParameters(TextureParameters textureParameters) {
        updateTextureParameters(getNative(), textureParameters.getCurrentValuesArray());
    }

    private static native int getId(long texture);

    private static native void updateTextureParameters(long texture, int[] textureParametersValues);
}