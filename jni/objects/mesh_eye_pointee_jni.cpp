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


/***************************************************************************
 * JNI
 ***************************************************************************/

#include "mesh_eye_pointee.h"

#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
JNIEXPORT jlong JNICALL
Java_com_eje_1c_meganekko_NativeMeshEyePointee_ctor(JNIEnv * env,
        jobject obj, jlong jmesh);

JNIEXPORT void JNICALL
Java_com_eje_1c_meganekko_NativeMeshEyePointee_setMesh(JNIEnv * env,
        jobject obj, jlong jmesh_eye_pointee, jlong jmesh);
}

JNIEXPORT jlong JNICALL
Java_com_eje_1c_meganekko_NativeMeshEyePointee_ctor(JNIEnv * env,
        jobject obj, jlong jmesh) {
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    return reinterpret_cast<jlong>(new MeshEyePointee(mesh));
}

JNIEXPORT void JNICALL
Java_com_eje_1c_meganekko_NativeMeshEyePointee_setMesh(JNIEnv * env,
        jobject obj, jlong jmesh_eye_pointee, jlong jmesh) {
    MeshEyePointee* mesh_eye_pointee =
            reinterpret_cast<MeshEyePointee*>(jmesh_eye_pointee);
    Mesh* mesh = reinterpret_cast<Mesh*>(jmesh);
    mesh_eye_pointee->set_mesh(mesh);
}

}
