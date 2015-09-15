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

package com.eje_c.meganekko.asynchronous;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.eje_c.meganekko.AndroidResource;
import com.eje_c.meganekko.GLContext;
import com.eje_c.meganekko.CubemapTexture;
import com.eje_c.meganekko.HybridObject;
import com.eje_c.meganekko.Texture;
import com.eje_c.meganekko.AndroidResource.CancelableCallback;
import com.eje_c.meganekko.asynchronous.Throttler.AsyncLoader;
import com.eje_c.meganekko.asynchronous.Throttler.AsyncLoaderFactory;
import com.eje_c.meganekko.asynchronous.Throttler.GlConverter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Async resource loading: cube map textures.
 * 
 * Since ZipInputStream does not support mark() and reset(), we directly use
 * BitmapFactory .decodeStream() in loadResource().
 * 
 * @since 1.6.9
 */
abstract class AsyncCubemapTexture {

    /*
     * The API
     */

    static void loadTexture(GLContext gvrContext,
            CancelableCallback<Texture> callback,
            AndroidResource resource, int priority, Map<String, Integer> map) {
        faceIndexMap = map;
        Throttler.registerCallback(gvrContext, TEXTURE_CLASS, callback,
                resource, priority);
    }

    /*
     * Static constants
     */

    // private static final String TAG = Log.tag(AsyncCubemapTexture.class);

    private static final Class<? extends HybridObject> TEXTURE_CLASS = CubemapTexture.class;

    /*
     * Asynchronous loader
     */

    private static class AsyncLoadCubemapTextureResource extends
            AsyncLoader<CubemapTexture, Bitmap[]> {

        private static final GlConverter<CubemapTexture, Bitmap[]> sConverter = new GlConverter<CubemapTexture, Bitmap[]>() {

            @Override
            public CubemapTexture convert(GLContext gvrContext,
                    Bitmap[] bitmapArray) {
                return new CubemapTexture(gvrContext, bitmapArray);
            }
        };

        protected AsyncLoadCubemapTextureResource(GLContext gvrContext,
                AndroidResource request,
                CancelableCallback<HybridObject> callback, int priority) {
            super(gvrContext, sConverter, request, callback);
        }

        @Override
        protected Bitmap[] loadResource() {
            Bitmap[] bitmapArray = new Bitmap[6];
            ZipInputStream zipInputStream = new ZipInputStream(
                    resource.getStream());

            try {
                ZipEntry zipEntry = null;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String imageName = zipEntry.getName();
                    Integer imageIndex = faceIndexMap.get(imageName);
                    if (imageIndex == null) {
                        throw new IllegalArgumentException("Name of image ("
                                + imageName + ") is not set!");
                    }
                    bitmapArray[imageIndex] = BitmapFactory
                            .decodeStream(zipInputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            resource.closeStream();
            return bitmapArray;
        }
    }

    static {
        Throttler.registerDatatype(TEXTURE_CLASS,
                new AsyncLoaderFactory<CubemapTexture, Bitmap[]>() {

                    @Override
                    AsyncLoadCubemapTextureResource threadProc(
                            GLContext gvrContext, AndroidResource request,
                            CancelableCallback<HybridObject> callback,
                            int priority) {
                        return new AsyncLoadCubemapTextureResource(gvrContext,
                                request, callback, priority);
                    }
                });
    }

    private static Map<String, Integer> faceIndexMap;
}
