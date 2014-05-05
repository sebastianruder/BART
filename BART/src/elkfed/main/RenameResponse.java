/*
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.mmax.MMAX2FilenameFilter;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.io.File;

/**
 *
 * @author yannick.versley
 */
public class RenameResponse {

    public static void main(String[] args) {
        File dataDir = ConfigProperties.getInstance().getTestData();
        String resp1, resp2;
        if (args.length > 1) {
            resp1 = args[0];
            resp2 = args[1];
        } else {
            resp1 = "response";
            resp2 = args[0];
        }
        for (File mmaxFile : dataDir.listFiles(MMAX2FilenameFilter.FILTER_INSTANCE)) {
            String fname = mmaxFile.getName();
            if (fname.endsWith(".mmax")) {
                fname = fname.substring(0, fname.length() - 5);
            }
            MiniDiscourse doc = MiniDiscourse.load(mmaxFile.getParentFile(),
                    fname);
            MarkableLevel response1 = doc.getMarkableLevelByName(resp1);
            MarkableLevel response2 = doc.getMarkableLevelByName(resp2);
            response2.deleteAllMarkables();
            response2.copyMarkables(response1);
            response2.saveMarkables();
        }
    }
}
