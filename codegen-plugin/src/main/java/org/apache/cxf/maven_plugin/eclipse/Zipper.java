/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.maven_plugin.eclipse;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Zip;

public final class Zipper extends Zip {
    
    private Zipper() {
        setProject(new Project());
        getProject().init();
        this.setTaskType("zip");
        this.setTaskName("zip");
        this.setOwningTarget(new Target());        
    }

    public static void zip(File baseDir, File destFile) {
        Zipper zipper = new Zipper();
        zipper.setCompress(true);
        zipper.setDestFile(destFile);
        zipper.setBasedir(baseDir);
        zipper.execute();
    }
}
