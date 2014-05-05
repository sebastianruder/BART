/*
 *   Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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
package elkfed.mmax.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import static elkfed.mmax.minidisc.MiniDiscourse.guessEncoding;

/** this importer is meant to read the same format that
 * elkfed.mmax.minidisc.Exporter writes, i.e.
 * "word" tags with tokens in the "f" attribute,
 * and everything else on a markable layer corresponding to
 * its tag name.
 * @author yannick
 */
public class GenericImporter extends Importer {

    File input;

    private static String sourcename(File src) {
        String src_name=src.getName();
        if (src_name.endsWith(".xml")) {
            src_name=src_name.substring(0,src_name.length()-4);
        }
        return src_name;
    }
    public GenericImporter(File src, File dir) {
        super(dir, sourcename(src));
        input = src;
    }

    public void do_create() throws XmlPullParserException, FileNotFoundException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new FileInputStream(input), guessEncoding(input));
        int eventType = xpp.getEventType();
        do {
            if (eventType == xpp.START_TAG) {
                String tagname = xpp.getName();
                //System.out.println("Open Tag:" + xpp.getName());
                if ("word".equals(tagname)) {
                    //System.out.println("word:"+xpp.nextText());
                    add_token(xpp.getAttributeValue("f", null));
                } else {
                    Tag t = push_tag(tagname);
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        String att = xpp.getAttributeName(i);
                        String val = xpp.getAttributeValue(i);
                        t.attrs.put(att, val);
                    }
                }
            }
            else if (eventType == xpp.END_TAG) {
                pop_tag(xpp.getName());
            }
            eventType = xpp.nextToken();

        } while (eventType != xpp.END_DOCUMENT);
        create();
    }

    public static void main(String[] args) {
        try {
            GenericImporter imp = new GenericImporter(new File(args[0]), new File(args[1]));
            imp.do_create();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
