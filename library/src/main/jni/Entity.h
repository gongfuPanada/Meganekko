/*
 * Copyright 2016 eje inc.
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
#ifndef ENTITY_H
#define ENTITY_H

#include "HybridObject.h"
#include "SurfaceRender.h"

using namespace OVR;

namespace mgn {
class Entity : public HybridObject {

public:
  Entity();
  virtual ~Entity();

  void SetWorldModelMatrix(const Matrix4f &m);
  const Matrix4f &GetWorldModelMatrix();
  ovrSurfaceDef *GetOrCreateSurfaceDef();
  ovrSurfaceDef *GetSurfaceDef();

private:
  Matrix4f modelMatrix;
  ovrSurfaceDef *surfaceDef;
};
}

#endif