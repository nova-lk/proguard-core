/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package proguard.dexfile.ir.stmt;

import proguard.dexfile.ir.LabelAndLocalMapper;

/**
 * Represent a RETURN_VOID statement
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 * @see Stmt.ST#RETURN_VOID
 */
public class ReturnVoidStmt extends Stmt.E0Stmt {

  public ReturnVoidStmt() {
    super(ST.RETURN_VOID);
  }

  @Override
  public Stmt clone(LabelAndLocalMapper mapper) {
    return new ReturnVoidStmt();
  }

  @Override
  public String toString() {
    return "return";
  }
}
