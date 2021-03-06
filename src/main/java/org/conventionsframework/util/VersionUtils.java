/*
 * Copyright 2011-2014 Conventions Framework.
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
package org.conventionsframework.util;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rmpestano
 */
public class VersionUtils implements Serializable {


    @Produces
    @Named(value = "conventionsVersion")
    public String getCoreVersion() {
        try {
            return java.util.ResourceBundle.getBundle("conventions").getString("conventions.version");
        } catch (Exception ex) {
            Logger.getLogger(VersionUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


}
