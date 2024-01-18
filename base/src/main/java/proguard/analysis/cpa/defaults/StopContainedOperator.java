/*
 * ProGuardCORE -- library to process Java bytecode.
 *
 * Copyright (c) 2002-2022 Guardsquare NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package proguard.analysis.cpa.defaults;

import java.util.Collection;
import proguard.analysis.cpa.interfaces.AbstractState;
import proguard.analysis.cpa.interfaces.Precision;
import proguard.analysis.cpa.interfaces.StopOperator;

/**
 * This {@link StopOperator} returns true if the reached set contains the input {@link
 * AbstractState}.
 *
 * @author Dmitry Ivanov
 */
public final class StopContainedOperator implements StopOperator {

  // implementations for StopOperator

  @Override
  public boolean stop(
      AbstractState abstractState,
      Collection<? extends AbstractState> reachedAbstractStates,
      Precision precision) {
    return reachedAbstractStates.contains(abstractState);
  }
}
