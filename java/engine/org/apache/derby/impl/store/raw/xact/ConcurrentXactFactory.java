/*

   Derby - Class org.apache.derby.impl.store.raw.xact.ConcurrentXactFactory

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derby.impl.store.raw.xact;

/**
 * A {@code TransactionFactory} similar to {@code XactFactory}, only that it
 * provides overrides that give access to classes in the {@code
 * java.util.concurrent} package, if supported by the runtime environment.
 * This class is used instead of {@code XactFactory} on Java 1.5 and higher.
 */
public class ConcurrentXactFactory extends XactFactory {
    @Override
    TransactionMapFactory createMapFactory() {
        return new ConcurrentTransactionMapFactory();
    }
}
